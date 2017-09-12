package com.alivc.videochat.demo.logic;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.alibaba.sdk.client.WebSocketConnectOptions;
import com.alibaba.sdk.mns.MnsControlBody;
import com.alivc.videochat.publisher.AlivcPublisherPerformanceInfo;
import com.alivc.videochat.publisher.MediaError;
import com.alivc.videochat.AlivcPlayerPerformanceInfo;
import com.alivc.videochat.AlivcVideoChatHost;
import com.alivc.videochat.IVideoChatHost;
import com.alivc.videochat.demo.base.AsyncCallback;
import com.alivc.videochat.demo.base.ContextBase;
import com.alivc.videochat.demo.base.ILifecycleListener;
import com.alivc.videochat.demo.bi.InviteServiceBI;
import com.alivc.videochat.demo.bi.LiveServiceBI;
import com.alivc.videochat.demo.bi.ServiceBI;
import com.alivc.videochat.demo.bi.ServiceBIFactory;
import com.alivc.videochat.demo.exception.ChatSessionException;
import com.alivc.videochat.demo.http.form.FeedbackForm;
import com.alivc.videochat.demo.http.form.InviteForm;
import com.alivc.videochat.demo.http.model.InviteFeedbackResult;
import com.alivc.videochat.demo.http.model.LiveCreateResult;
import com.alivc.videochat.demo.http.model.MNSConnectModel;
import com.alivc.videochat.demo.http.model.MNSModel;
import com.alivc.videochat.demo.http.model.MixStatusCode;
import com.alivc.videochat.demo.im.ImHelper;
import com.alivc.videochat.demo.im.ImManager;
import com.alivc.videochat.demo.im.model.MessageType;
import com.alivc.videochat.demo.im.model.MsgDataAgreeVideoCall;
import com.alivc.videochat.demo.im.model.MsgDataExitChatting;
import com.alivc.videochat.demo.im.model.MsgDataInvite;
import com.alivc.videochat.demo.im.model.MsgDataLiveClose;
import com.alivc.videochat.demo.im.model.MsgDataMergeStream;
import com.alivc.videochat.demo.im.model.MsgDataMixStatusCode;
import com.alivc.videochat.demo.im.model.MsgDataNotAgreeVideoCall;
import com.alivc.videochat.demo.im.model.MsgDataStartPublishStream;
import com.alivc.videochat.demo.ui.VideoChatStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import retrofit2.Call;

/**
 * 类的描述:
 */
public class LifecyclePublisherMgr extends ContextBase implements IPublisherMgr, ILifecycleListener {

    private static final String TAG = LifecyclePublisherMgr.class.getName();

    public static final String KEY_CHATTING_UID = "chatting_uid";   //正在连麦的用户ID

    public static final int MAX_RECONNECT_COUNT = 10;

    private static final long WAITING_FOR_MIX_SUCCESS_DELAY = 15 * 1000; //混流错误时等待重新混流成功的时间，超过这个时间会结束连麦

    /**
     * 自己发送邀请，对方超时未响应，则自己更新本地的连麦状态为未连麦
     */
    private static final int MSG_WHAT_INVITE_CHAT_TIMEOUT = 1;   //连麦邀请响应超时

    /**
     * 别人发送的邀请，自己超时未处理，自动回应不同意连麦，并且在自己的UI层给出提醒
     */
    private static final int MSG_WHAT_PROCESS_INVITING_TIMEOUT = 2;

    /**
     * 同意连麦后，等待混流成功超时
     */
    private static final int MSG_WHAT_MIX_STREAM_TIMEOUT = 3;

    /**
     * InternalError, MainStreamNotExist, MixStreamNotExist都认为是混流错误，
     */
    private static final int MSG_WHAT_MIX_STREAM_ERROR = 4;

    private static final int MSG_WHAT_MIX_STREAM_SUCCESS = 5;

    private static final int MSG_WHAT_MIX_STREAM_NOT_EXIST = 6;

    private static final int MSG_WHAT_MAIN_STREAM_NOT_EXIST = 7;

    private PublisherSDKHelper mSDKHelper;
    private MgrCallback mCallback;
    private Map<String, ChatSession> mChatSessionMap = new HashMap<>();

    private List<Call> mInviteCalls = new ArrayList<>();        //当前发起的邀请请求
    private Call mCreateLiveCall;

    private InviteServiceBI mInviteServiceBI = ServiceBIFactory.getInviteServiceBI();
    private LiveServiceBI mLiveServiceBI = ServiceBIFactory.getLiveServiceBI();

    private ImManager mImManager;
    private WebSocketConnectOptions mWSConnOpts;    //MNS建立WebSocket链接使用的参数
    private MnsControlBody mControlBody;            //服务端返回的组装WebSocketConnectOptions所使用信息

    private String mRoomID = null;
    private String mUID;


    private SurfaceView mMainSurfaceView = null;
    private SurfaceStatus mPreviewSurfaceStatus = SurfaceStatus.UNINITED;

    private String mTipString;
    private boolean mVideoChatApiCalling = false;

    private int mReconnectCount = 0;

    // --------------------------------------------------------------------------------------------------------
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_WHAT_INVITE_CHAT_TIMEOUT://连麦响应超时
                    if (mSessionHandler != null) {
                        mSessionHandler.onInviteChatTimeout();
                    }
                    break;
                case MSG_WHAT_PROCESS_INVITING_TIMEOUT:
                    if (mSessionHandler != null) {
                        mSessionHandler.onProcessInvitingTimeout();
                    }
                    break;
                case MSG_WHAT_MIX_STREAM_TIMEOUT:
                    if (mSessionHandler != null) {
                        mSessionHandler.onMixStreamTimeout();
                    }
                    break;
                case MSG_WHAT_MIX_STREAM_ERROR:
                    if (mSessionHandler != null) {
                        mSessionHandler.onMixStreamError();
                    }
                    break;
                case MSG_WHAT_MIX_STREAM_SUCCESS:
                    if (mSessionHandler != null) {
                        mSessionHandler.onMixStreamSuccess();
                    }
                    break;
                case MSG_WHAT_MIX_STREAM_NOT_EXIST:
                    if (mSessionHandler != null) {
                        mSessionHandler.onMixStreamNotExist();
                    }
                    break;
                case MSG_WHAT_MAIN_STREAM_NOT_EXIST:
                    if (mSessionHandler != null) {
                        mSessionHandler.onMainStreamNotExist();
                    }
                    break;
            }
        }
    };

    // --------------------------------------------------------------------------------------------------------

    public LifecyclePublisherMgr(Context context, MgrCallback callback, String uid, ImManager imManager) {
        super(context);

        this.mSDKHelper = new PublisherSDKHelper();

        this.mCallback = callback;
        this.mUID = uid;
        this.mImManager = imManager;
    }

    // --------------------------------------------------------------------------------------------------------

    @Override
    public void onCreate() {
        Context context = getContext();
        // 如果context为空则assert抛出的异常AssertionError
        assert (context != null);
        mSDKHelper.initRecorder(context, mOnErrorListener, mInfoListener); //初始化推流器
    }

    @Override
    public void onStart() {
    }

    @Override
    public void onResume() {
        // 下面两个if判断都是重新获取了焦点时才有用，第一次进入直播界面获取焦点时下面的判断不会通过
        if (mImManager != null && mControlBody != null && mWSConnOpts != null) {
            // asyncCreateLive方法被调用过了，界面失去了焦点取消了所有相关订阅，然后界面有重新获得了焦点，需要订阅
            initPublishMsgProcessor();
        }
        if (mPreviewSurfaceStatus != SurfaceStatus.UNINITED) {
            // mSDKHelper在失去焦点时暂停过，现在重新获取了焦点需要恢复推流
            mSDKHelper.resume();
        }
    }

    @Override
    public void onPause() {
        if (mImManager != null) {
            // 取消消息订阅
            mImManager.unRegister(MessageType.INVITE_CALLING);
            mImManager.unRegister(MessageType.AGREE_CALLING);
            mImManager.unRegister(MessageType.NOT_AGREE_CALLING);
            mImManager.unRegister(MessageType.CALLING_SUCCESS);
            mImManager.unRegister(MessageType.CALLING_FAILED);
            mImManager.unRegister(MessageType.TERMINATE_CALLING);
            mImManager.unRegister(MessageType.LIVE_COMPLETE);
            mImManager.unRegister(MessageType.MIX_STATUS_CODE);
            mImManager.unRegister(MessageType.START_PUSH);
            mImManager.unRegister(MessageType.EXIT_CHATTING);
            // 断开MNS链接
            mImManager.closeSession();
        }
        mSDKHelper.pause(); //暂停推流 or 连麦
    }

    @Override
    public void onStop() {

        // 在CreateLiveFragment获取推流地址是，如果用户不想直播了，结束了Fragment，那么这里就用用了可以结束网络请求
        if (mCreateLiveCall != null && ServiceBI.isCalling(mCreateLiveCall)) {
            mCreateLiveCall.cancel();
            mCreateLiveCall = null;
        }

        if (mInviteCalls.size() > 0) {
            for (Call call : mInviteCalls) {
                call.cancel();
            }
            mInviteCalls.clear();
        }
    }

    @Override
    public void onDestroy() {
        if (mChatSessionMap.size() > 0) {
            asyncTerminateAllChatting(null);
        }
        asyncCloseLive(null);           //结束直播
        // TODO by xinye : 退出连麦
        mSDKHelper.abortChat(null);     //防止因为某些原因没有停止连麦的情况，再次调用一次停止连麦
        mSDKHelper.stopPublish();   //防止因为某些原因没有停止推理的情况，再次调用一次停止推流
        mSDKHelper.releaseRecorder();//释放推流器资源
    }

    // --------------------------------------------------------------------------------------------------------

    @Override
    public void asyncStartPreview(SurfaceView previewSurfaceView, AsyncCallback callback) {
        this.mMainSurfaceView = previewSurfaceView;
        mMainSurfaceView.getHolder().addCallback(mMainSurfaceCallback);
    }

    @Override
    public void asyncInviteChatting(final List<String> playerUIDs, final AsyncCallback callback) {
        // ChatSession.MAX_SESSION_NUM是写死的最大连麦数
        if (mChatSessionMap.size() > ChatSession.MAX_SESSION_NUM) {//目前最多只支持同时连麦3个观众
            if (callback != null) {
                callback.onFailure(null, new ChatSessionException(ChatSessionException.ERROR_CHATTING_MAX_NUMBER));
            }
            return;
        }

        // 遍历看playerUIDs判断其元素是否已经连麦
        for (String playerUID : playerUIDs) {
            if (mChatSessionMap.containsKey(playerUID)) {//目前最多只支持同时连麦3个观众
                if (callback != null) {
                    callback.onFailure(null, new ChatSessionException(ChatSessionException.ERROR_CHATTING_ALREADY));
                }
                return;
            }
        }

        for (String playerUID : playerUIDs) {//检查邀请的用户是否有正在连麦的
            ChatSession chatSession = new ChatSession(mSessionHandler);
            if (chatSession.invite(mUID, playerUID) != ChatSession.RESULT_OK) {//当前有正在连麦的观众
                Bundle bundle = new Bundle();
                bundle.putString(KEY_CHATTING_UID, playerUID);
                callback.onFailure(bundle, new ChatSessionException(ChatSessionException.ERROR_CURR_CHATTING));
                return;
            }
            Log.d(TAG, "xiongbo21: put session after invite for " + playerUID + ", status = " + chatSession.getChatStatus());
            mChatSessionMap.put(playerUID, chatSession);
        }

        final int callIndex = mInviteCalls.size();
        final Call call = mInviteServiceBI.inviteCall(mUID,
                playerUIDs,
                InviteForm.TYPE_PIC_BY_PIC,
                FeedbackForm.INVITE_TYPE_ANCHOR, mRoomID,
                new ServiceBI.Callback() {
                    @Override
                    public void onResponse(int code, Object response) {
                        //邀请成功
                        for (String playerUID : playerUIDs) {//移除所有的Session
                            mChatSessionMap.get(playerUID).notifyInviteSuccess();   //通知ChatSession邀请成功
                            Log.d(TAG, "xiongbo21: notify invite success for " + playerUID + ", status = " + mChatSessionMap.get(playerUID).getChatStatus());
                        }
                        mInviteCalls.remove(callIndex);
                        if (callback != null) {
                            callback.onSuccess(null);
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        //邀请失败
                        for (String playerUID : playerUIDs) {//移除所有的Session
                            mChatSessionMap.remove(playerUID);
                            Log.d(TAG, "xiongbo21: remove chat session for " + playerUID);
                        }
                        mInviteCalls.remove(callIndex);
                        if (callback != null) {
                            callback.onFailure(null, t);
                        }
                    }
                });
        mInviteCalls.add(call);
    }

    @Override
    public void switchCamera() {
        mSDKHelper.switchCamera();
    }

    @Override
    public boolean switchBeauty() {
        return mSDKHelper.switchBeauty();
    }

    @Override
    public boolean switchFlash() {
        return mSDKHelper.switchFlash();
    }

    @Override
    public void zoom(float scaleFactor) {
        mSDKHelper.zoom(scaleFactor);
    }

    @Override
    public void autoFocus(float xRatio, float yRatio) {
        mSDKHelper.autoFocus(xRatio, yRatio);
    }

    @Override
    public void asyncCloseLive(final AsyncCallback callback) {
        mLiveServiceBI.closeLive(mRoomID, mUID, new ServiceBI.Callback() {
            @Override
            public void onResponse(int code, Object response) {
                if (callback != null) {
                    callback.onSuccess(null);
                }
                mSDKHelper.stopPublish();
            }

            @Override
            public void onFailure(Throwable t) {
                if (callback != null) {
                    callback.onFailure(null, t);
                }
            }
        });

        if (mMainSurfaceView != null) {
            mMainSurfaceView.getHolder().removeCallback(mMainSurfaceCallback);
        }
    }

    @Override
    public void asyncTerminateChatting(final String playerUID, final AsyncCallback callback) {
        if (mVideoChatApiCalling) {
            if (mCallback != null) {
                Bundle data = new Bundle();
                data.putString(DATA_KEY_PLAYER_ERROR_MSG, mTipString);
                mCallback.onEvent(TYPE_OPERATION_CALLED_ERROR, data);
            }
            return;
        }

        if (mChatSessionMap.size() > 0) {//当前正在连麦
            mInviteServiceBI.leaveCall(playerUID, mRoomID, new ServiceBI.Callback() {
                @Override
                public void onResponse(int code, Object response) {
                    if (callback != null) {
                        callback.onSuccess(null);
                    }
                    //清楚所有的session信息
                    final ChatSession chatSession = mChatSessionMap.get(playerUID);
                    if (chatSession != null) {
                        SurfaceView surfaceView = chatSession.getSurfaceView();
                        if (surfaceView != null) {
                            surfaceView.getHolder().removeCallback(chatSession.getSurfaceCallback());
                        }
                        ArrayList<String> playUrls = new ArrayList<String>();
                        playUrls.add(chatSession.getChatSessionInfo().getPlayUrl());
                        // TODO by xinye : 退出连麦
                        mVideoChatApiCalling = true;
                        mSDKHelper.abortChat(playUrls);
                        Log.e("xiongbo07", "开始REMOVE连麦... " + playerUID);
                        mChatSessionMap.remove(playerUID);
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    if (callback != null) {
                        callback.onFailure(null, t);
                    }
                }
            });
        }
    }

    @Override
    public void asyncTerminateAllChatting(final AsyncCallback callback) {
        if (mVideoChatApiCalling) {
            if (mCallback != null) {
                Bundle data = new Bundle();
                data.putString(DATA_KEY_PLAYER_ERROR_MSG, mTipString);
                mCallback.onEvent(TYPE_OPERATION_CALLED_ERROR, data);
            }
            return;
        }

        String key;
        SurfaceView surfaceView;
        ChatSession chatSession;
        if (mChatSessionMap.size() > 0) {//当前正在连麦
            mInviteServiceBI.terminateCall(mRoomID, new ServiceBI.Callback() {
                @Override
                public void onResponse(int code, Object response) {
                    if (callback != null) {
                        callback.onSuccess(null);
                    }
                    // TODO by xinye : 退出连麦
                    mVideoChatApiCalling = true;
                    Log.e("xiongbo07", "开始退出连麦...");
                    mSDKHelper.abortChat(null); //调用SDK中断连麦
                }

                @Override
                public void onFailure(Throwable t) {
                    if (callback != null) {
                        callback.onFailure(null, t);
                    }
                }
            });
            Iterator<String> chatSessionKeySet = mChatSessionMap.keySet().iterator();
            //清楚所有的session信息
            while (chatSessionKeySet.hasNext()) {
                key = chatSessionKeySet.next();
                chatSession = mChatSessionMap.get(key);
                surfaceView = chatSession.getSurfaceView();
                if (surfaceView != null) {
                    surfaceView.getHolder().removeCallback(chatSession.getSurfaceCallback());
                }
            }
            mChatSessionMap.clear();

        }
    }

    // --------------------------------------------------------------------------------------------------------

    @Override
    public void asyncCreateLive(String desc, final AsyncCallback callback) {
        // 先查看下请求网络获取推流地址的Call任务是否存在
        if (mCreateLiveCall != null && ServiceBI.isCalling(mCreateLiveCall)) {
            mCreateLiveCall.cancel();
            mCreateLiveCall = null;
        }
        // 创建请求网络获取推流地址的Call任务
        mCreateLiveCall = mLiveServiceBI.createLive(mUID, desc, new ServiceBI.Callback<LiveCreateResult>() {
            @Override
            public void onResponse(int code, LiveCreateResult response) {
                // 获取服务器创建的直播推流的房间号mRoomID
                mRoomID = response.getRoomID();

                // 给LifecycleCreateLivePresenterImpl回调结果
                // 将结果Bena封装进Bundle
                Bundle data = new Bundle();
                // 将Bundle回调给调用本方法的回调接口实例，内部的作用大致是将界面从创建直播切换到正在推流
                data.putSerializable(DATA_KEY_CREATE_LIVE_RESULT, response);
                if (callback != null) {
                    callback.onSuccess(data);
                }

                // 给LifecycleLiveRecordPresenterImpl回调结果
                if (mCallback != null) {
                    // 这个回调暂时没用
                    mCallback.onEvent(TYPE_LIVE_CREATED, data);
                }

                mCreateLiveCall = null;

                // 创建并获取推流地址成功，开始推流
                mSDKHelper.startPublishStream(response.getRtmpUrl());

                // 因为在直播中我们需要用到MNS，这属于直播的一部分，所以建立MNS链接是必要的。
                // 前面我们调用ImManager的init()方法只是进行了初始化，还没有正式进行MNS链接。
                // 而这里的WebSocketConnectOptions和MnsControlBody是我们在请求推流地址时一起获得的，
                // 这两个对象是我们建立MNS链接的必要参数
                mWSConnOpts = new WebSocketConnectOptions();
                MNSModel mnsModel = response.getMNSModel();
                MNSConnectModel mnsConnectModel = response.getMnsConnectModel();
                mWSConnOpts.setServerURI(mnsConnectModel.getTopicWSServerAddress());
                List<String> tags = new ArrayList<>();
                tags.add(mnsModel.getRoomTag());
                tags.add(mnsModel.getUserTag());
                mControlBody = new MnsControlBody.Builder()
                        .accessId(mnsConnectModel.getAccessID())
                        .accountId(mnsConnectModel.getAccountID())
                        .authorization("MNS " + mnsConnectModel.getAccessID() + ":" + mnsConnectModel.getAuthentication())
                        .date(mnsConnectModel.getDate())
                        .subscription(mnsModel.getTopic())
                        .topic(mnsModel.getTopic())
                        .messageType(MnsControlBody.MessageType.SUBSCRIBE)
                        .tags(tags)
                        .build();
                // 建立MNS链接
                initPublishMsgProcessor();
            }

            @Override
            public void onFailure(Throwable t) {
                // 获取推流地址的Call失败，释放Call，并调用回调接口
                if (callback != null) {
                    callback.onFailure(null, t);
                }
                mCreateLiveCall = null;
            }
        });
    }

    @Override
    public void launchChat(SurfaceView parterView, String playerUID) {
        if (mVideoChatApiCalling) {
            if (mCallback != null) {
                Bundle data = new Bundle();
                data.putString(DATA_KEY_PLAYER_ERROR_MSG, mTipString);
                mCallback.onEvent(TYPE_OPERATION_CALLED_ERROR, data);
            }
            return;
        }

        final ChatSession chatSession = mChatSessionMap.get(playerUID);
        Map<String, SurfaceView> urlSurfaceMap = new HashMap<>();
        urlSurfaceMap.put(chatSession.getChatSessionInfo().getPlayUrl(), parterView);
        // TODO by xinye : 发起连麦/ADD 连麦
        mVideoChatApiCalling = true;
        Log.e("xiongbo07", "开始发起连麦...");
        mSDKHelper.launchChats(urlSurfaceMap);
        chatSession.setSurfaceView(parterView);
    }

    // --------------------------------------------------------------------------------------------------------
    /**
     * 变量的描述: 主SurfaceView的变化监听回调接口实例
     */
    private SurfaceHolder.Callback mMainSurfaceCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            Log.d(TAG, "LiveActivity-->Preview surface created");
            //记录Surface的状态
            if (mPreviewSurfaceStatus == SurfaceStatus.UNINITED) {
                // 主SurfaceView第一次被创建，开启预览
                mPreviewSurfaceStatus = SurfaceStatus.CREATED;
                // 现在开启预览是让主播在正式推流前就看见自己要直播的画面
                mSDKHelper.startPreView(mMainSurfaceView);
            } else if (mPreviewSurfaceStatus == SurfaceStatus.DESTROYED) {
                // 主SurfaceView已经被销毁过，现在重新创建
                mPreviewSurfaceStatus = SurfaceStatus.RECREATED;
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Log.d(TAG, "LiveActivity-->Preview surface changed");
            mPreviewSurfaceStatus = SurfaceStatus.CHANGED;

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            Log.d(TAG, "LiveActivity-->Preview surface destroyed");
            mPreviewSurfaceStatus = SurfaceStatus.DESTROYED;
        }
    };

    // --------------------------------------------------------------------------------------------------------

    // 反馈邀请
    //@Override
    private void asyncFeedbackInviting(final String playerUID) {
        mInviteServiceBI.feedback(FeedbackForm.INVITE_TYPE_ANCHOR, FeedbackForm.INVITE_TYPE_WATCHER, playerUID, mUID,
                InviteForm.TYPE_PIC_BY_PIC, FeedbackForm.STATUS_AGREE, new ServiceBI.Callback<InviteFeedbackResult>() {
                    @Override
                    public void onResponse(int code, InviteFeedbackResult response) {
                        /**
                         * 所谓的短延时URL实际上就是未经转码的原始流播放地址也就是，主播连麦观众时，主播端看到的观众的小窗画面，应该使用的播放地址
                         *
                         * 注意：这里没有直接就开始播放小窗，是因为这个时候观众端实际上还没有推流成功，需要等到收到推流成功的通知才开始播放
                         */
                        ChatSession chatSession = mChatSessionMap.get(playerUID);
                        if (chatSession != null) {
                            chatSession.notifyFeedbackSuccess();
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        Log.d(TAG, "Publisher feedback failure", t);
                    }
                });
    }


    private SessionHandler mSessionHandler = new SessionHandler() {
        @Override
        public void onInviteChatTimeout() {
            mVideoChatApiCalling = false;
            if (mCallback != null) {
                mCallback.onEvent(TYPE_INVITE_TIMEOUT, null);
            }
        }

        @Override
        public void onProcessInvitingTimeout() {
//             feedbackInviting(false);  // 自动反馈不同意连麦
            mVideoChatApiCalling = false;
            if (mCallback != null) {
                mCallback.onEvent(TYPE_PROCESS_INVITING_TIMEOUT, null);
            }
        }

        @Override
        public void onMixStreamError() {
//            asyncTerminateAllChatting(null);
            mVideoChatApiCalling = false;
            if (mCallback != null) {
                mCallback.onEvent(TYPE_MIX_STREAM_ERROR, null);
            }
        }

        @Override
        public void onMixStreamTimeout() {
            mVideoChatApiCalling = false;
            if (mCallback != null) {
                mCallback.onEvent(TYPE_MIX_STREAM_TIMEOUT, null);
            }
        }

        @Override
        public void onMixStreamSuccess() {
            mVideoChatApiCalling = false;
            if (mCallback != null) {
                mCallback.onEvent(TYPE_MIX_STREAM_SUCCESS, null);
            }
        }

        @Override
        public void onMixStreamNotExist() {
            mVideoChatApiCalling = false;
            if (mCallback != null) {
                mCallback.onEvent(TYPE_MIX_STREAM_NOT_EXIST, null);
            }
        }

        @Override
        public void onMainStreamNotExist() {
            mVideoChatApiCalling = false;
            if (mCallback != null) {
                mCallback.onEvent(TYPE_MAIN_STREAM_NOT_EXIST, null);
            }
        }
    };

    // --------------------------------------------------------------------------------------------------------

    public AlivcPublisherPerformanceInfo getPublisherPerformanceInfo() {
        // return mSDKHelper.getPublisherPerformanceInfo();
        return new AlivcPublisherPerformanceInfo();
    }

    public AlivcPlayerPerformanceInfo getPlayerPerformanceInfo(String url) {
        // return mSDKHelper.getPlayerPerformanceInfo(url);
        return new AlivcPlayerPerformanceInfo();
    }

    // **************************************************** MNS链接的建立和注册各种订阅 ****************************************************

    /**
     * 方法描述: 建立MNS链接，然后注册订阅
     */
    private void initPublishMsgProcessor() {
        mImManager.createSession(mWSConnOpts, mControlBody);
        // 注册订阅
        mImManager.register(MessageType.INVITE_CALLING, mInviteFunc, MsgDataInvite.class);
        mImManager.register(MessageType.AGREE_CALLING, mAgreeFunc, MsgDataAgreeVideoCall.class);
        mImManager.register(MessageType.NOT_AGREE_CALLING, mNotAgreeFunc, MsgDataNotAgreeVideoCall.class);
        mImManager.register(MessageType.CALLING_SUCCESS, mMergeStreamSuccFunc, MsgDataMergeStream.class);
        mImManager.register(MessageType.CALLING_FAILED, mMergeStreamFailedFunc, MsgDataMergeStream.class);
        mImManager.register(MessageType.LIVE_COMPLETE, mLiveCloseFunc, MsgDataLiveClose.class);
        mImManager.register(MessageType.MIX_STATUS_CODE, mMixStatusCode, MsgDataMixStatusCode.class);
        mImManager.register(MessageType.START_PUSH, mPublishStreamFunc, MsgDataStartPublishStream.class);
        mImManager.register(MessageType.EXIT_CHATTING, mExitChattingFunc, MsgDataExitChatting.class);
    }

    /**
     * 连麦邀请的消息处理Action
     */
    ImHelper.Func<MsgDataInvite> mInviteFunc = new ImHelper.Func<MsgDataInvite>() {

        @Override
        public void action(final MsgDataInvite msgDataInvite) {
            if (mChatSessionMap.containsKey(msgDataInvite.getInviterUID())) {//已经处于连麦的用户，不接受再次邀请
//                if (mCallback != null) {
//                    callback.onFailure(null, new ChatSessionException(ChatSessionException.ERROR_CHATTING_ALREADY));
//                    mCallback.onEvent();
//                }
                ChatSession mChatSession = mChatSessionMap.get(msgDataInvite.getInviterUID());
//                UNCHAT,              //未连麦
//                        INVITE_FOR_RES,      //邀请连麦成功等待对方响应
//                        INVITE_RES_SUCCESS,
//                        INVITE_RES_FAILURE,
//                        RECEIVED_INVITE,     //收到邀请等待回复状态
//                        TRY_MIX,            //开始连麦等待混流成功
//                        MIX_SUCC,           //混流成功
                if (mChatSession.getChatStatus() == VideoChatStatus.MIX_SUCC ||
                        mChatSession.getChatStatus() == VideoChatStatus.TRY_MIX) {
                    return;
                }
            }

            ChatSession chatSession = new ChatSession(mSessionHandler);
            chatSession.notifyReceivedInviting(mUID, msgDataInvite.getInviterUID());
            mChatSessionMap.put(msgDataInvite.getInviterUID(), chatSession);
            //自动同意连麦
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            asyncFeedbackInviting(msgDataInvite.getInviterUID());
        }
    };
    /**
     * 同意连麦的消息处理Action
     */
    ImHelper.Func<MsgDataAgreeVideoCall> mAgreeFunc = new ImHelper.Func<MsgDataAgreeVideoCall>() {
        @Override
        public void action(final MsgDataAgreeVideoCall msgDataAgreeVideoCall) {
            final String inviteeUID = msgDataAgreeVideoCall.getInviteeUID();
            ChatSession chatSession = mChatSessionMap.get(inviteeUID);
            if (chatSession != null && chatSession.notifyAgreeInviting() == ChatSession.RESULT_OK) {
                Log.d(TAG, "xiongbo21: notify agree inviting for " + inviteeUID + ", status = " + chatSession.getChatStatus());
            }
        }
    };
    /**
     * 不同意连麦的消息处理Action
     */
    ImHelper.Func<MsgDataNotAgreeVideoCall> mNotAgreeFunc = new ImHelper.Func<MsgDataNotAgreeVideoCall>() {
        @Override
        public void action(final MsgDataNotAgreeVideoCall notAgreeVideoCall) {
            ChatSession chatSession = mChatSessionMap.get(notAgreeVideoCall.getInviteeUID());
            chatSession.notifyNotAgreeInviting(notAgreeVideoCall);
            mChatSessionMap.remove(chatSession);
        }
    };
    /**
     * 混流成功的消息处理Action
     */
    ImHelper.Func<MsgDataMergeStream> mMergeStreamSuccFunc = new ImHelper.Func<MsgDataMergeStream>() {

        @Override
        public void action(MsgDataMergeStream msgDataMergeStream) {
            Log.d(TAG, "LiveActivity -->Merge Success");
            //TODO:需要协调服务端讨论
        }
    };
    /**
     * 混流失败的消息处理Action
     */
    ImHelper.Func<MsgDataMergeStream> mMergeStreamFailedFunc = new ImHelper.Func<MsgDataMergeStream>() {

        @Override
        public void action(MsgDataMergeStream msgDataMergeStream) {
            //TODO：需要协调服务端讨论
        }
    };
    /**
     * 直播结束的消息处理Action
     */
    ImHelper.Func<MsgDataLiveClose> mLiveCloseFunc = new ImHelper.Func<MsgDataLiveClose>() {

        @Override
        public void action(MsgDataLiveClose msgDataLiveClose) {
//            if () {
//                stopPublish();
//                mLiveView.showLiveCloseUI();
//            }
        }
    };
    /**
     * 混流过程中产生的状态码的回调
     */
    ImHelper.Func<MsgDataMixStatusCode> mMixStatusCode = new ImHelper.Func<MsgDataMixStatusCode>() {
        @Override
        public void action(MsgDataMixStatusCode msgDataMixStatusCode) {
            if (msgDataMixStatusCode != null && !mChatSessionMap.isEmpty()) {
                String code = msgDataMixStatusCode.getCode();
                mHandler.removeMessages(MSG_WHAT_MIX_STREAM_ERROR);
                if (MixStatusCode.INTERNAL_ERROR.toString().equals(code)) {
                    mHandler.sendEmptyMessage(MSG_WHAT_MIX_STREAM_ERROR);
                } else if (MixStatusCode.MAIN_STREAM_NOT_EXIST.toString().equals(code)) {
                    mHandler.sendEmptyMessage(MSG_WHAT_MAIN_STREAM_NOT_EXIST);
                    mHandler.sendEmptyMessageDelayed(MSG_WHAT_MIX_STREAM_ERROR, WAITING_FOR_MIX_SUCCESS_DELAY);
                } else if (MixStatusCode.MIX_STREAM_NOT_EXIST.toString().equals(code)) {
                    mHandler.sendEmptyMessage(MSG_WHAT_MAIN_STREAM_NOT_EXIST);
                    mHandler.sendEmptyMessageDelayed(MSG_WHAT_MIX_STREAM_ERROR, WAITING_FOR_MIX_SUCCESS_DELAY);
                } else if (MixStatusCode.SUCCESS.toString().equals(code)) {
                    // 对于所有的session，全部设置未mix success
                    ChatSession session = mChatSessionMap.get(msgDataMixStatusCode.getMixUid());
                    if (session != null) {
                        session.setChatStatus(VideoChatStatus.MIX_SUCC);
                    }
                    mHandler.sendEmptyMessage(MSG_WHAT_MIX_STREAM_SUCCESS);
                }
                Log.d(TAG, "Mix statusCode: " + msgDataMixStatusCode.getCode());
            }
        }
    };
    /**
     * 开始推流的消息处理Action
     */
    ImHelper.Func<MsgDataStartPublishStream> mPublishStreamFunc = new ImHelper.Func<MsgDataStartPublishStream>() {
        @Override
        public void action(MsgDataStartPublishStream msgDataStartPublishStream) {
            Log.d(TAG, "LiveActivity -->Publish Success.");
            ChatSession chatSession = mChatSessionMap.get(msgDataStartPublishStream.getUid());
            if (chatSession != null) {
                Log.d(TAG, String.valueOf("xiongbo21: publis stream for " + msgDataStartPublishStream.getUid() + ", status = " +
                        chatSession.getChatStatus()));
            } else {
                Log.d(TAG, String.valueOf("xiongbo21: publis stream for " + msgDataStartPublishStream.getUid() + ", status = " +
                        VideoChatStatus.UNCHAT));
            }
            if (!mUID.equals(msgDataStartPublishStream.getUid())) {
                if (chatSession != null && chatSession.isTryMix() && chatSession.getChatSessionInfo() == null) {
                    ChatSessionInfo sessionInfo = new ChatSessionInfo();
                    //TODO:这里需要带上推流地址
                    sessionInfo.setPlayUrl(msgDataStartPublishStream.getPlayUrl());
                    chatSession.setChatSessionInfo(sessionInfo);
                    if (mCallback != null) {
                        Bundle data = new Bundle();
                        data.putString(DATA_KEY_INVITEE_UID, msgDataStartPublishStream.getUid());
                        Log.d(TAG, "LiveActivity -->Publish Success. send publish stream success.");
                        mCallback.onEvent(TYPE_PUBLISH_STREMA_SUCCESS, data);
                    }
                }
            }
        }
    };
    /**
     * 某人退出连麦的消息处理Action
     */
    ImHelper.Func<MsgDataExitChatting> mExitChattingFunc = new ImHelper.Func<MsgDataExitChatting>() {
        @Override
        public void action(MsgDataExitChatting msgDataExitChatting) {
            String playerUID = msgDataExitChatting.getUID();
            ChatSession chatSession = mChatSessionMap.get(playerUID);
            if (chatSession != null) {
                List<String> playUrls = new ArrayList<>();
                if (chatSession != null && chatSession.getChatSessionInfo() != null)
                    playUrls.add(chatSession.getChatSessionInfo().getPlayUrl());
                // TODO by xinye : 退出连麦
                mVideoChatApiCalling = true;
                Log.e("xiongbo07", "开始退出连麦...");
                int result = mSDKHelper.abortChat(playUrls);
                if (result < 0) {
                    mVideoChatApiCalling = false;
                }
                if (mCallback != null) {
                    Bundle data = new Bundle();
                    data.putString(DATA_KEY_PLAYER_UID, playerUID);
                    mCallback.onEvent(TYPE_SOMEONE_EXIT_CHATTING, data);
                }
            }
            mChatSessionMap.remove(playerUID);
        }
    };

    // **************************************************** 推流状态信息和错误监听接口实例 ****************************************************
    /**
     * 变量的描述: 推流错误监听器回调接口实现
     */
    AlivcVideoChatHost.OnErrorListener mOnErrorListener = new IVideoChatHost.OnErrorListener() {
        @Override
        public boolean onError(IVideoChatHost iVideoChatHost, int what, String url) {
            if (what == 0) {
                return false;
            }
            Log.d(TAG, "Live stream connection error-->" + what);

            //
            final Bundle data = new Bundle();
            data.putInt(DATA_KEY_PUBLISHER_ERROR_CODE, what);

            // 区分错误
            switch (what) {
                case MediaError.ALIVC_ERR_PLAYER_INVALID_INPUTFILE:// 播放无效的输入
                    Log.d(TAG, "encounter player invalid input file.");
                    if (mReconnectCount++ < MAX_RECONNECT_COUNT)
                        mSDKHelper.reconnect(url);
                    if (mCallback != null) {
                        mCallback.onEvent(TYPE_PLAYER_INVALID_INPUTFILE, null);
                    }
                    break;
                case MediaError.ALIVC_ERR_PLAYER_OPEN_FAILED:// 播放打开失败
                    Log.d(TAG, "encounter player open failed.");
                    if (mReconnectCount++ < MAX_RECONNECT_COUNT)
                        mSDKHelper.reconnect(url);
                    if (mCallback != null) {
                        mCallback.onEvent(TYPE_PLAYER_OPEN_FAILED, null);
                    }
                    break;
                case MediaError.ALIVC_ERR_PLAYER_NO_NETWORK:// 播放没有网络连接
                    Log.d(TAG, "encounter player no network.");
                    if (mReconnectCount++ < MAX_RECONNECT_COUNT)
                        mSDKHelper.reconnect(url);
                    if (mCallback != null) {
                        mCallback.onEvent(TYPE_PLAYER_NO_NETWORK, null);
                    }
                    break;
                case MediaError.ALIVC_ERR_PLAYER_TIMEOUT:// 播放超时
                    Log.d(TAG, "encounter player timeout, so call restartToPlayer");
                    if (mReconnectCount++ < MAX_RECONNECT_COUNT)
                        mSDKHelper.reconnect(url);
                    if (mCallback != null) {
                        mCallback.onEvent(TYPE_PLAYER_TIMEOUT, null);
                    }
                    break;
                case MediaError.ALIVC_ERR_PLAYER_READ_PACKET_TIMEOUT:// 播放读取数据超时
                    Log.d(TAG, "encounter player read packet timeout.");
                    if (mReconnectCount++ < MAX_RECONNECT_COUNT)
                        mSDKHelper.reconnect(url);
                    if (mCallback != null) {
                        mCallback.onEvent(TYPE_PLAYER_READ_PACKET_TIMEOUT, null);
                    }
                    break;

                case MediaError.ALIVC_ERR_PLAYER_NO_MEMORY:// 播放无足够内存
                case MediaError.ALIVC_ERR_PLAYER_INVALID_CODEC:// 播放不支持的解码格式
                case MediaError.ALIVC_ERR_PLAYER_NO_SURFACEVIEW:// 播放没有设置显示窗口
                    if (mCallback != null) {
                        mCallback.onEvent(TYPE_PLAYER_INTERNAL_ERROR, data);
                    }
                    // TODO by xinye : 退出连麦
//                    mSDKHelper.abortChat(null);
                    break;
                case MediaError.ALIVC_ERR_PUBLISHER_AUDIO_CAPTURE_DISABLED:// 音频采集关闭 音频被禁止
                case MediaError.ALIVC_ERR_PUBLISHER_AUDIO_CAPTURE_NO_DATA:// 音频采集失败 音频采集出错
                    if (mChatSessionMap.size() > 0) {
                        //TODO:遍历ChatSession，并且close 连麦
                    }
                    //TODO:关闭连麦
                    if (mCallback != null) {
                        mCallback.onEvent(TYPE_PUBLISHER_AUDIO_CAPTURE_FAILURE, data);
                    }

                    break;
                case MediaError.ALIVC_ERR_PUBLISHER_VIDEO_CAPTURE_NO_DATA:// 视频采集出错
                    if (mCallback != null) {
                        mCallback.onEvent(TYPE_PUBLISHER_VIDEO_CAPTURE_FAILURE, data);
                    }
                    break;
                case MediaError.ALIVC_ERR_PUBLISHER_VIDEO_CAPTURE_DISABLED: // 摄像头开启失败 视频被禁止
                    if (mCallback != null) {
                        mCallback.onEvent(TYPE_PUBLISHER_VIDEO_CAPTURE_FAILURE, data);
                    }
                    break;
                case MediaError.ALIVC_ERR_PUBLISHER_ENCODE_AUDIO_FAILED:// 音频编码失败
                case MediaError.ALIVC_ERR_PUBLISHER_VIDEO_ENCODER_INIT_FAILED:// 视频初始化失败
                case MediaError.ALIVC_ERR_PUBLISHER_MALLOC_FAILED:// 内存分配失败
                case MediaError.ALIVC_ERR_PUBLISHER_ILLEGAL_ARGUMENT:// 无效的参数
                    break;
                case MediaError.ALIVC_ERR_PUBLISHER_NETWORK_POOR:// 网络较慢
                    if (mCallback != null) {
                        mCallback.onEvent(TYPE_PUBLISHER_NETWORK_POOR, data);
                    }
                    break;
                case MediaError.ALIVC_ERR_PUBLISHER_NETWORK_UNCONNECTED:// 网络未连接
                    if (mCallback != null) {
                        mCallback.onEvent(TYPE_PUBLISHER_NETWORK_UNCONNECT, data);
                    }
                    break;
                case MediaError.ALIVC_ERR_PUBLISHER_SEND_DATA_TIMEOUT:// 发送数据超时
                    if (mCallback != null) {
                        mCallback.onEvent(TYPE_PUBLISHER_NETWORK_TIMEOUT, data);
                    }
                    break;
                case MediaError.ALIVC_ERR_PLAYER_AUDIO_PLAY:// 音频播放错误
                    if (mCallback != null) {
                        mCallback.onEvent(TYPE_PLAYER_AUDIO_PLAYER_ERROR, data);
                    }
                    break;
                case MediaError.ALIVC_ERR_MEMORY_POOR:// 内存不够
                    break;
                case MediaError.ALIVC_ERR_PUBLISHER_OPEN_FAILED:// 推流连接失败
                    break;
                case MediaError.ALIVC_ERR_PUBLISHER_AUDIO_ENCODER_INIT_FAILED:// 音频初始化失败
                    break;
                case MediaError.ALIVC_ERR_PUBLISHER_ENCODE_VIDEO_FAILED:// 视频编码失败
                    break;
                case MediaError.ALIVC_ERR_PUBLISHER_VIDEO_CAPTURE_FPS_SLOW:// 音频采集较慢
                    break;
                case MediaError.ALIVC_ERR_PLAYER_UNSUPPORTED:// 播放不支持的解码
                    break;
                default:
                    if (mCallback != null) {
                        mCallback.onEvent(TYPE_PUBLISHER_INTERNAL_ERROR, data);
                    }
                    break;
            }
            return false;
        }
    };
    /**
     * 变量的描述: 推流器状态信息监听器回调接口实现
     */
    AlivcVideoChatHost.OnInfoListener mInfoListener = new AlivcVideoChatHost.OnInfoListener() {

        @Override
        public boolean onInfo(IVideoChatHost iVideoChatHost, int what, String url) {
            Log.d(TAG, "LiveActivity --> what = " + what + ", extra = " + url);
            switch (what) {
                case MediaError.ALIVC_INFO_PUBLISH_NETWORK_GOOD:// 推流网络较好
                    if (mCallback != null) {
                        mCallback.onEvent(TYPE_PUBLISHER_NETWORK_GOOD, null);
                    }
                    break;
                case MediaError.ALIVC_INFO_PUBLISH_RECONNECT_FAILURE:// 重连失败
                    if (mCallback != null) {
                        Bundle data = new Bundle();
                        data.putInt(DATA_KEY_PUBLISHER_INFO_CODE, what);
                        mCallback.onEvent(TYPE_PUBLISHER_RECONNECT_FAILURE, data);
                    }
                    break;
                case MediaError.ALIVC_INFO_LAUNCH_CHAT_END:// launchChat结束
                    mVideoChatApiCalling = false;
                    Log.e("xiongbo07", "结束发起连麦...");
                    break;
                case MediaError.ALIVC_INFO_ABORT_CHAT_END:// abortChat结束
                    mVideoChatApiCalling = false;
                    Log.e("xiongbo07", "结束退出连麦...");
                    break;
                case MediaError.ALIVC_INFO_ADD_CHAT_END:// addChat结束
                    mVideoChatApiCalling = false;
                    Log.e("xiongbo07", "结束ADD连麦...");
                    break;
                case MediaError.ALIVC_INFO_REMOVE_CHAT_END:// removeChat结束
                    mVideoChatApiCalling = false;
                    Log.e("xiongbo07", "结束Remove连麦...");
                    break;
                case MediaError.ALIVC_INFO_PLAYER_PREPARED_PROCESS_FINISHED:// 播放准备完成通知
                    mReconnectCount = 0;
                    break;
                case MediaError.ALIVC_INFO_PLAYER_NETWORK_POOR:// 播放器网络差，不能及时下载数据包
                    if (mCallback != null) {
                        Bundle data = new Bundle();
                        data.putString(IPublisherMgr.DATA_KEY_PLAYER_ERROR_MSG, url);
                        mCallback.onEvent(TYPE_PLAYER_NETWORK_POOR, data);
                    }
                    break;
                case MediaError.ALIVC_INFO_ONLINE_CHAT_END:// onlineChat结束
                    break;
                case MediaError.ALIVC_INFO_OFFLINE_CHAT_END:// offlineChat结束
                    break;
                case MediaError.ALIVC_INFO_PUBLISH_RECONNECT_START:// 重连开始
                    break;
                case MediaError.ALIVC_INFO_PUBLISH_RECONNECT_SUCCESS:// 重连成功
                    break;
                case MediaError.ALIVC_INFO_PUBLISH_DISPLAY_FIRST_FRAME:// 推流首次显示通知
                    break;
                case MediaError.ALIVC_INFO_PUBLISH_START_SUCCESS:// 推流开始成功
                    break;
                case MediaError.ALIVC_INFO_PLAYER_FIRST_FRAME_RENDERED:// 播放首帧显示
                    break;
                case MediaError.ALIVC_INFO_PLAYER_BUFFERING_START:// 播放缓冲开始
                    break;
                case MediaError.ALIVC_INFO_PLAYER_BUFFERING_END:// 播放缓冲结束
                    break;
                case MediaError.ALIVC_INFO_PLAYER_INTERRUPT_PLAYING:// 播放被中断
                    break;
                case MediaError.ALIVC_INFO_PLAYER_STOP_PROCESS_FINISHED:// 播放结束通知
                    break;
            }
            return false;
        }
    };
}

