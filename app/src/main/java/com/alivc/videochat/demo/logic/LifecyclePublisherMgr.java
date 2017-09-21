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
 * 类的描述:11111
 */
public class LifecyclePublisherMgr extends ContextBase implements IPublisherMgr, ILifecycleListener {

    private static final String TAG = LifecyclePublisherMgr.class.getName();

    private static final String KEY_CHATTING_UID = "chatting_uid";   //正在连麦的用户ID

    private static final int MAX_RECONNECT_COUNT = 10;

    private static final long WAITING_FOR_MIX_SUCCESS_DELAY = 15 * 1000; //混流错误时等待重新混流成功的时间，超过这个时间会结束连麦

    /**
     * 变量的描述: Handler识别标识混流内部异常
     */
    private static final int MSG_WHAT_MIX_STREAM_ERROR = 4;
    /**
     * 变量的描述: Handler识别标识混流成功
     */
    private static final int MSG_WHAT_MIX_STREAM_SUCCESS = 5;
    /**
     * 变量的描述: Handler识别标识混流(连麦观众流)不存在
     */
    private static final int MSG_WHAT_MIX_STREAM_NOT_EXIST = 6;
    /**
     * 变量的描述: Handler识别标识主播流不存在
     */
    private static final int MSG_WHAT_MAIN_STREAM_NOT_EXIST = 7;

    private PublisherSDKHelper mSDKHelper;
    private MgrCallback mCallback;
    /**
     * 变量的描述: key为连麦观众的id，value为连麦观众所代表的连麦流程对象
     */
    private Map<String, ChatSession> mChatSessionMap = new HashMap<>();

    /**
     * 变量的描述: 本集合存储的是，主播向服务器发送邀请连麦通知的网络请求，当网络请求成功或者失败时移除，该集合的目的是在主播界面结束时停止还在请求网络的任务
     */
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

    /**
     * 变量的描述: 代表了是否正在调用连麦的正式API，该API包含了连麦，结束连麦等
     */
    private boolean mVideoChatApiCalling = false;

    private int mReconnectCount = 0;
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
    /**
     * 变量的描述: 用来发送混流结果并做出相应响应的Handler
     */
    private final Handler mMixFlowResultHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_WHAT_MIX_STREAM_ERROR: // 混流内部异常
                    if (mChatSessionCallback != null) {
                        mChatSessionCallback.onMixStreamError();
                    }
                    break;
                case MSG_WHAT_MIX_STREAM_SUCCESS:// 混流成功
                    if (mChatSessionCallback != null) {
                        mChatSessionCallback.onMixStreamSuccess();
                    }
                    break;
                case MSG_WHAT_MIX_STREAM_NOT_EXIST:// 混流(连麦观众流)不存在
                    if (mChatSessionCallback != null) {
                        mChatSessionCallback.onMixStreamNotExist();
                    }
                    break;
                case MSG_WHAT_MAIN_STREAM_NOT_EXIST:// 主播流不存在
                    if (mChatSessionCallback != null) {
                        mChatSessionCallback.onMainStreamNotExist();
                    }
                    break;
            }
        }
    };
    // --------------------------------------------------------------------------------------------------------
    /**
     * 变量的描述: 连麦流程的回调接口实例，对各种连麦流程中的状态做出更新对应UI的回调
     */
    private ChatSessionCallback mChatSessionCallback = new ChatSessionCallback() {
        @Override
        public void onInviteChatTimeout() {
            mVideoChatApiCalling = false;
            if (mCallback != null) {
                mCallback.onEvent(TYPE_INVITE_TIMEOUT, null);
            }
        }

        @Override
        public void onProcessInvitingTimeout() {// 等待主播是否同意被邀请进行连麦，超时了
            mVideoChatApiCalling = false;
            if (mCallback != null) {
                mCallback.onEvent(TYPE_PROCESS_INVITING_TIMEOUT, null);
            }
        }

        @Override
        public void onMixStreamTimeout() {
            mVideoChatApiCalling = false;
            if (mCallback != null) {
                mCallback.onEvent(TYPE_MIX_STREAM_TIMEOUT, null);
            }
        }

        // --------------------------------------------------------------------------------------------------------

        @Override
        public void onMixStreamError() {
//            asyncTerminateAllChatting(null);
            mVideoChatApiCalling = false;
            if (mCallback != null) {
                mCallback.onEvent(TYPE_MIX_STREAM_ERROR, null);
            }
        }

        @Override
        public void onMixStreamSuccess() {
            // 混流成功，连麦api使用完毕
            mVideoChatApiCalling = false;
            if (mCallback != null) {
                // 回调返回混流成功的结果
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
        if (context != null)
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
            mImManager.unRegister(MessageType.TERMINATE_CALLING);
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

        // 在CreateLiveFragment获取推流地址时，如果用户不想直播了，结束了Fragment，那么这里就用用了可以结束网络请求
        if (mCreateLiveCall != null && ServiceBI.isCalling(mCreateLiveCall)) {
            mCreateLiveCall.cancel();
            mCreateLiveCall = null;
        }

        // 停止还在向服务器发送邀请连麦的通知的网络请求
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

    @Override // 在进入直播界面的时候就开启预览让用户看见自己所要直播的画面
    public void asyncStartPreview(SurfaceView previewSurfaceView, AsyncCallback callback) {
        this.mMainSurfaceView = previewSurfaceView;
        mMainSurfaceView.getHolder().addCallback(mMainSurfaceCallback);
    }

    @Override // 请求网络获取推流地址，如此asyncStartPreview方法开启的预览才能通过推流地址直播出去
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

    @Override // 当用户点击连麦时，请求业务服务器，发送邀请连麦的请求
    public void asyncInviteChatting(final List<String> playerUIDs, final AsyncCallback callback) {
        // ChatSession.MAX_SESSION_NUM是写死的最大连麦数
        if (mChatSessionMap.size() >= ChatSession.MAX_SESSION_NUM) {// 目前最多只支持同时进行连麦流程的只有3个观众
            if (callback != null) {
                callback.onFailure(null, new ChatSessionException(ChatSessionException.ERROR_CHATTING_MAX_NUMBER));
            }
            return;
        }

        // 遍历看playerUIDs判断其元素是否已经正在和主播进行连麦流程
        for (String playerUID : playerUIDs) {
            if (mChatSessionMap.containsKey(playerUID)) {
                if (callback != null) {
                    callback.onFailure(null, new ChatSessionException(ChatSessionException.ERROR_CHATTING_ALREADY));
                }
                return;
            }
        }

        for (String playerUID : playerUIDs) {// 检查邀请的用户是否有正在进行连麦流程的
            ChatSession chatSession = new ChatSession(mChatSessionCallback);
            if (chatSession.invite(mUID, playerUID) != ChatSession.RESULT_OK) {
                // 当前playerUID所代表的观众正在进行连麦流程
                Bundle bundle = new Bundle();
                bundle.putString(KEY_CHATTING_UID, playerUID);
                callback.onFailure(bundle, new ChatSessionException(ChatSessionException.ERROR_CURR_CHATTING));
                return;
            }
            Log.d(TAG, "xiongbo21: put session after invite for " + playerUID + ", status = " + chatSession.getChatStatus());
            mChatSessionMap.put(playerUID, chatSession);
        }

        final int callIndex = mInviteCalls.size();
        //  向服务器发送邀请参数二所代表的用户进行连麦的请求
        final Call call = mInviteServiceBI.inviteCall(mUID, playerUIDs, InviteForm.TYPE_PIC_BY_PIC, FeedbackForm.INVITE_TYPE_ANCHOR, mRoomID,
                new ServiceBI.Callback() {
                    @Override
                    public void onResponse(int code, Object response) {
                        // 向服务器发送邀请的请求成功
                        for (String playerUID : playerUIDs) {
                            mChatSessionMap.get(playerUID).notifyInviteSuccess();   // 通知ChatSession 发送邀请的网络请求成功
                            Log.d(TAG, "xiongbo21: notify invite success for " + playerUID + ", status = " + mChatSessionMap.get(playerUID).getChatStatus());
                        }
                        mInviteCalls.remove(callIndex);// 网络请求完成，移除
                        if (callback != null) {
                            callback.onSuccess(null);
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        // 向服务器发送邀请的请求失败
                        for (String playerUID : playerUIDs) {
                            mChatSessionMap.remove(playerUID);// 因为失败，所以移除playerUID所代表的连麦流程
                            Log.d(TAG, "xiongbo21: remove chat session for " + playerUID);
                        }
                        mInviteCalls.remove(callIndex);// 网络请求完成，移除
                        if (callback != null) {
                            callback.onFailure(null, t);
                        }
                    }
                });
        mInviteCalls.add(call);// 添加网络请求，以便于在特定的时候进行操作
    }

    @Override // 正式的开启连麦，因为时主播界面的连麦，所以其内部主要是播放连麦人的推流视频
    public void launchChat(SurfaceView parterView, String playerUID) {
        // 正常的时候 mVideoChatApiCalling 在这里是false
        if (mVideoChatApiCalling) {
            if (mCallback != null) {
                Bundle data = new Bundle();
                data.putString(DATA_KEY_PLAYER_ERROR_MSG, TAG + "的launchChat方法中mVideoChatApiCalling出现异常");
                mCallback.onEvent(TYPE_OPERATION_CALLED_ERROR, data);
            }
            return;
        }

        // 通过uid获取ChatSession，在通过ChatSession获得播放地址
        final ChatSession chatSession = mChatSessionMap.get(playerUID);
        Map<String, SurfaceView> urlSurfaceMap = new HashMap<>();
        // 将播放地址和SurfaceView一起存入Map集合
        urlSurfaceMap.put(chatSession.getChatSessionInfo().getPlayUrl(), parterView);
        //  发起连麦/ADD 连麦的api
        mVideoChatApiCalling = true;
        Log.e("xiongbo07", "开始发起连麦...");
        // 使用Map集合正式开始调用连麦方法
        mSDKHelper.launchChats(urlSurfaceMap);
        // 连麦核心方法调用没有异常后，将SurfaceView存入连麦流程对象
        chatSession.setSurfaceView(parterView);
    }

    @Override // 结束指定uid的连麦
    public void asyncTerminateChatting(final String playerUID, final AsyncCallback callback) {
        // 正常的时候 mVideoChatApiCalling 在这里是false
        if (mVideoChatApiCalling) {
            if (mCallback != null) {
                Bundle data = new Bundle();
                data.putString(DATA_KEY_PLAYER_ERROR_MSG, TAG + "的launchChat方法中mVideoChatApiCalling出现异常");
                mCallback.onEvent(TYPE_OPERATION_CALLED_ERROR, data);
            }
            return;
        }

        if (mChatSessionMap.size() > 0) {// 连麦流程对象集合中有数据，说明当前有连麦流程正在进行中
            // 请求网络，向服务器发送主播将要结束哪个指定的连麦
            mInviteServiceBI.leaveCall(playerUID, mRoomID, new ServiceBI.Callback<Object>() {
                @Override
                public void onResponse(int code, Object response) {
                    if (callback != null) {
                        callback.onSuccess(null);
                    }
                    // 向服务器发送成功，那么主播需要调用API结束连麦自己这边的连麦，观众端那里从MNS获取到信息后自动调用API结束连麦
                    //清楚所有的session信息
                    final ChatSession chatSession = mChatSessionMap.get(playerUID);
                    if (chatSession != null) {
                        SurfaceView surfaceView = chatSession.getSurfaceView();
                        if (surfaceView != null) {
                            surfaceView.getHolder().removeCallback(chatSession.getSurfaceCallback());
                        }
                        ArrayList<String> playUrls = new ArrayList<>();
                        playUrls.add(chatSession.getChatSessionInfo().getPlayUrl());
                        // 退出连麦,调用了连麦的API
                        mVideoChatApiCalling = true;
                        mSDKHelper.abortChat(playUrls);
                        Log.e("xiongbo07", "开始REMOVE连麦... " + playerUID);
                        mChatSessionMap.remove(playerUID);
                        // 这里不用使用回调接口更新退出连麦的UI，因为这里本身就是一个回调
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

    @Override // 在直播界面被销毁时，如果还有正在连麦的观众，那么就结束所有连麦
    public void asyncTerminateAllChatting(final AsyncCallback callback) {
        // 正常的时候 mVideoChatApiCalling 在这里是false
        if (mVideoChatApiCalling) {
            if (mCallback != null) {
                Bundle data = new Bundle();
                data.putString(DATA_KEY_PLAYER_ERROR_MSG, TAG + "的launchChat方法中mVideoChatApiCalling出现异常");
                mCallback.onEvent(TYPE_OPERATION_CALLED_ERROR, data);
            }
            return;
        }

        String key;
        SurfaceView surfaceView;
        ChatSession chatSession;
        if (mChatSessionMap.size() > 0) {// 当前还有正在连麦
            // 请求网络，告诉服务器我们将要断开所有连麦
            mInviteServiceBI.terminateCall(mRoomID, new ServiceBI.Callback() {
                @Override
                public void onResponse(int code, Object response) {
                    if (callback != null) {
                        callback.onSuccess(null);
                    }
                    // 调用API结束所有连麦
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
            // 清除集合中的所有连麦流程对象
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

    @Override // 请求网络，告诉业务服务器直播将要被关闭
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

        // 移除主SurfaceView的监听接口
        if (mMainSurfaceView != null) {
            mMainSurfaceView.getHolder().removeCallback(mMainSurfaceCallback);
        }
    }

    // --------------------------------------------------------------------------------------------------------

    // --------------------------------------------------------------------------------------------------------

    public AlivcPublisherPerformanceInfo getPublisherPerformanceInfo() {
        // return mSDKHelper.getPublisherPerformanceInfo();
        return new AlivcPublisherPerformanceInfo();
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
        mImManager.register(MessageType.MIX_STATUS_CODE, mMixStatusCode, MsgDataMixStatusCode.class);
        mImManager.register(MessageType.START_PUSH, mPublishStreamFunc, MsgDataStartPublishStream.class);
        mImManager.register(MessageType.EXIT_CHATTING, mExitChattingFunc, MsgDataExitChatting.class);
    }

    /**
     * 变量的描述: 观众发起请求和主播进行连麦，这里会收到MNS的消息，被邀请连麦的消息处理Action
     */
    private ImHelper.Func<MsgDataInvite> mInviteFunc = new ImHelper.Func<MsgDataInvite>() {
        @Override
        public void action(final MsgDataInvite msgDataInvite) {
            if (mChatSessionMap.containsKey(msgDataInvite.getInviterUID())) {// 判断发起连麦的观众是否处于连麦流程，是就不接受再次邀请
                // TODO 这里的处理不是很明白
                ChatSession mChatSession = mChatSessionMap.get(msgDataInvite.getInviterUID());
                if (mChatSession.getChatStatus() == VideoChatStatus.MIX_SUCC ||
                        mChatSession.getChatStatus() == VideoChatStatus.TRY_MIX) {
                    return;
                }
            }

            // 将邀请主播进行连麦的观众生成连麦流程
            ChatSession chatSession = new ChatSession(mChatSessionCallback);
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
     * 方法描述: 主播被观众邀请，主播同意了。则需要调用此方法 反馈邀请 实质就是告诉服务器主播是否同意被邀请进行连麦
     */
    private void asyncFeedbackInviting(final String playerUID) {
        mInviteServiceBI.feedback(FeedbackForm.INVITE_TYPE_ANCHOR, FeedbackForm.INVITE_TYPE_WATCHER, playerUID, mUID, InviteForm.TYPE_PIC_BY_PIC, FeedbackForm.STATUS_AGREE, new ServiceBI.Callback<InviteFeedbackResult>() {
            @Override
            public void onResponse(int code, InviteFeedbackResult response) {
                // 所谓的短延时URL实际上就是未经转码的原始流播放地址也就是，主播连麦观众时，主播端看到的观众的小窗画面，应该使用的播放地址
                // 注意：这里没有直接就开始播放小窗，是因为这个时候观众端实际上还没有推流成功，需要等到收到推流成功的通知才开始播放
                ChatSession chatSession = mChatSessionMap.get(playerUID);
                // 修改进行邀请的观众的连麦流程的状态为尝试混流(其实就是等待其推流成功)
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

    /**
     * 变量的描述: 主播向观众发起连麦的邀请，观众同意时，MNS发送消息过来，观众同意连麦的消息处理Action
     */
    private ImHelper.Func<MsgDataAgreeVideoCall> mAgreeFunc = new ImHelper.Func<MsgDataAgreeVideoCall>() {
        @Override
        public void action(final MsgDataAgreeVideoCall msgDataAgreeVideoCall) {
            // 从MNS传递过来的数据源中取出同意进行连麦的观众的UID
            final String inviteeUID = msgDataAgreeVideoCall.getInviteeUID();
            // 获取 同意进行连麦的观众 的连麦流程对象
            ChatSession chatSession = mChatSessionMap.get(inviteeUID);
            // 对连麦流程的状态进行更新
            if (chatSession != null && chatSession.notifyAgreeInviting() == ChatSession.RESULT_OK) {
                Log.d(TAG, "xiongbo21: notify agree inviting for " + inviteeUID + ", status = " + chatSession.getChatStatus());
            }
        }
    };
    /**
     * 变量的描述: 主播向观众发起连麦的邀请，观众不同意时，MNS发送消息过来，观众不同意连麦的消息处理Action
     */
    private ImHelper.Func<MsgDataNotAgreeVideoCall> mNotAgreeFunc = new ImHelper.Func<MsgDataNotAgreeVideoCall>() {
        @Override
        public void action(final MsgDataNotAgreeVideoCall notAgreeVideoCall) {
            // 获取 不同意进行连麦的观众 的连麦流程对象
            ChatSession chatSession = mChatSessionMap.get(notAgreeVideoCall.getInviteeUID());
            // 对连麦流程的状态进行更新
            chatSession.notifyNotAgreeInviting(notAgreeVideoCall);
            // 移除 不同意进行连麦的观众 的连麦流程对象
            mChatSessionMap.remove(notAgreeVideoCall.getInviteeUID());
        }
    };
    /**
     * 变量的描述: 当主播和连麦的人推流成功时(可获取推流对应的播放地址)，MNS会接收从服务器发送过来的消息，这里我们主要处理连麦的人推流成功
     */
    private ImHelper.Func<MsgDataStartPublishStream> mPublishStreamFunc = new ImHelper.Func<MsgDataStartPublishStream>() {
        @Override
        public void action(MsgDataStartPublishStream msgDataStartPublishStream) {
            Log.d(TAG, "LiveActivity -->推流成功");
            ChatSession chatSession = mChatSessionMap.get(msgDataStartPublishStream.getUid());
            if (chatSession != null) {
                Log.d(TAG, String.valueOf("推流成功的连麦ID: " + msgDataStartPublishStream.getUid() + ", 直播的状态是连麦中，具体状态为: " + chatSession.getChatStatus()));
            } else {
                Log.d(TAG, String.valueOf("推流成功的主播ID: " + msgDataStartPublishStream.getUid() + ", 直播的状态是: 未进行连麦中"));
            }
            // 推流成功的id不是主播id才能进入if判断
            if (!mUID.equals(msgDataStartPublishStream.getUid())) {
                // 推流成功的id是之前存储的对应的ChatSession(连麦流程对象),并且该连麦流程对象的 ChatSessionInfo(连麦流程信息对象)为null
                if (chatSession != null && chatSession.isTryMix() && chatSession.getChatSessionInfo() == null) {
                    // 将MNS收的连麦人的播放地址存储进ChatSessionInfo，在将ChatSessionInfo存储进连麦流程对象中，方便代码下次可以通过uid获取对应的播放地址
                    ChatSessionInfo sessionInfo = new ChatSessionInfo();
                    sessionInfo.setPlayUrl(msgDataStartPublishStream.getPlayUrl());
                    chatSession.setChatSessionInfo(sessionInfo);

                    // 将这里的UID回调给mCallback，让mCallback的实例去更新UI界面，并正式开启连麦方法
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
     * 变量的描述: 某个正在进行连麦的人退出连麦的消息处理Action，这个退出有主播主动踢掉某人，也有某人主动退出，
     * 但是如果是主播主动踢掉的，那么获取的ChatSession是null。因为在主播主动踢人时就已经调用api退出其连麦，也清空了对应的ChatSession
     * 这里主要是对那些主动退出连麦的人进行反应
     */
    private ImHelper.Func<MsgDataExitChatting> mExitChattingFunc = new ImHelper.Func<MsgDataExitChatting>() {
        @Override
        public void action(MsgDataExitChatting msgDataExitChatting) {
            String playerUID = msgDataExitChatting.getUID();
            ChatSession chatSession = mChatSessionMap.get(playerUID);
            if (chatSession != null) {
                List<String> playUrls = new ArrayList<>();
                if (chatSession.getChatSessionInfo() != null) {
                    playUrls.add(chatSession.getChatSessionInfo().getPlayUrl());
                }
                // 退出连麦,调用api退出
                mVideoChatApiCalling = true;
                Log.e("xiongbo07", "开始退出连麦...");
                int result = mSDKHelper.abortChat(playUrls);
                if (result < 0) {
                    mVideoChatApiCalling = false;
                }
                // 通过mCallback回调更新UI界面的退出连麦
                if (mCallback != null) {
                    Bundle data = new Bundle();
                    data.putString(DATA_KEY_PLAYER_UID, playerUID);
                    mCallback.onEvent(TYPE_SOMEONE_EXIT_CHATTING, data);
                }
            }
            mChatSessionMap.remove(playerUID);
        }
    };
    /**
     * 变量的描述: 连麦的时候(不管成功，还是失败)，混流过程中产生的状态码的回调
     */
    private ImHelper.Func<MsgDataMixStatusCode> mMixStatusCode = new ImHelper.Func<MsgDataMixStatusCode>() {
        @Override
        public void action(MsgDataMixStatusCode msgDataMixStatusCode) {
            // MNS接收的数据不为空，还有连麦流程对象
            if (msgDataMixStatusCode != null && !mChatSessionMap.isEmpty()) {
                String code = msgDataMixStatusCode.getCode();

                // 接收MNS的消息有可能是重新混流成功的消息，所以将上次消息开启的混流内部异常的handler消息移除
                mMixFlowResultHandler.removeMessages(MSG_WHAT_MIX_STREAM_ERROR);

                if (MixStatusCode.INTERNAL_ERROR.toString().equals(code)) {// 网络异常
                    // 因为服务器端进行混流的网络异常，导致 混流内部异常 ，将结果发送出去
                    mMixFlowResultHandler.sendEmptyMessage(MSG_WHAT_MIX_STREAM_ERROR);
                } else if (MixStatusCode.MAIN_STREAM_NOT_EXIST.toString().equals(code)) {// 主播放流不存在
                    // 发送连麦主播流不存在的结果，在handler中根据结果进行反应
                    mMixFlowResultHandler.sendEmptyMessage(MSG_WHAT_MAIN_STREAM_NOT_EXIST);
                    // 虽然混流失败了，但是可以等待其重新混流，如果超过等待时间，则发送混流内部异常的结果
                    mMixFlowResultHandler.sendEmptyMessageDelayed(MSG_WHAT_MIX_STREAM_ERROR, WAITING_FOR_MIX_SUCCESS_DELAY);
                } else if (MixStatusCode.MIX_STREAM_NOT_EXIST.toString().equals(code)) {// 混流不存在
                    // 发送连麦主播流不存在的结果，在handler中根据结果进行反应
                    mMixFlowResultHandler.sendEmptyMessage(MSG_WHAT_MIX_STREAM_NOT_EXIST);
                    // 虽然混流失败了，但是可以等待其重新混流，如果超过等待时间，则发送混流内部异常的结果
                    mMixFlowResultHandler.sendEmptyMessageDelayed(MSG_WHAT_MIX_STREAM_ERROR, WAITING_FOR_MIX_SUCCESS_DELAY);

                } else if (MixStatusCode.SUCCESS.toString().equals(code)) {// 混流成功
                    // 经过了上面的if判断，走到这说明混流暂时是成功的
                    // 对于所有的连麦流程对象，全部设置未mix success混流成功
                    String mixUid = msgDataMixStatusCode.getMixUid();
                    ChatSession session = mChatSessionMap.get(mixUid);
                    if (session != null) {
                        // TODO 这里获取的uid一直是空的，所以没法重新设置连麦流程的状态
                        session.setChatStatus(VideoChatStatus.MIX_SUCC);
                    }
                    // 发送混流成功的结果
                    mMixFlowResultHandler.sendEmptyMessage(MSG_WHAT_MIX_STREAM_SUCCESS);
                }
                Log.d(TAG, "Mix statusCode: " + msgDataMixStatusCode.getCode());
            }
        }
    };
    // **************************************************** 推流状态信息和错误监听接口实例 ****************************************************
    /**
     * 变量的描述: 推流错误监听器回调接口实现
     */
    private AlivcVideoChatHost.OnErrorListener mOnErrorListener = new IVideoChatHost.OnErrorListener() {
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
                        System.out.println();
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
    private AlivcVideoChatHost.OnInfoListener mInfoListener = new AlivcVideoChatHost.OnInfoListener() {

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

