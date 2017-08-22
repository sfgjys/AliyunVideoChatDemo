package com.alivc.videochat.demo.logic;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceView;

import com.alibaba.sdk.client.WebSocketConnectOptions;
import com.alibaba.sdk.mns.MNSClient;
import com.alibaba.sdk.mns.MnsControlBody;
import com.alivc.videochat.player.MediaPlayer;
import com.alivc.videochat.publisher.AlivcPublisherPerformanceInfo;
import com.alivc.videochat.publisher.MediaError;
import com.alivc.videochat.AlivcPlayerPerformanceInfo;
import com.alivc.videochat.AlivcVideoChatParter;
import com.alivc.videochat.IVideoChatParter;
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
import com.alivc.videochat.demo.http.model.MNSConnectModel;
import com.alivc.videochat.demo.http.model.MNSModel;
import com.alivc.videochat.demo.http.model.MixStatusCode;
import com.alivc.videochat.demo.http.model.WatchLiveResult;
import com.alivc.videochat.demo.im.ImHelper;
import com.alivc.videochat.demo.im.ImManager;
import com.alivc.videochat.demo.im.model.MessageType;
import com.alivc.videochat.demo.im.model.MsgDataAgreeVideoCall;
import com.alivc.videochat.demo.im.model.MsgDataCloseVideoCall;
import com.alivc.videochat.demo.im.model.MsgDataExitChatting;
import com.alivc.videochat.demo.im.model.MsgDataInvite;
import com.alivc.videochat.demo.im.model.MsgDataLiveClose;
import com.alivc.videochat.demo.im.model.MsgDataMergeStream;
import com.alivc.videochat.demo.im.model.MsgDataMixStatusCode;
import com.alivc.videochat.demo.im.model.MsgDataNotAgreeVideoCall;
import com.alivc.videochat.demo.im.model.MsgDataStartPublishStream;
import com.alivc.videochat.demo.im.model.ParterInfo;
import com.alivc.videochat.demo.ui.VideoChatStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import retrofit2.Call;

/**
 * Created by apple on 2017/1/6.
 */

public class LifecycledPlayerMgr extends ContextBase implements IPlayerMgr, ILifecycleListener {
    private static final String TAG = LifecycledPlayerMgr.class.getName();

    public static final int MAX_RECONNECT_COUNT = 10;
    private ChatSession mChatSession;       //当前观众与主播连麦的会话
    private PlayerSDKHelper mSDKHelper;
    private HashMap<String, ChatSession> mOtherChatSessionMap = new HashMap<>();    //其他观众参与连麦的会话

    private LiveServiceBI mLiveServiceBI = ServiceBIFactory.getLiveServiceBI();
    private InviteServiceBI mInviteServiceBI = ServiceBIFactory.getInviteServiceBI();
    private Call mEnterRoomCall;
    private Call mInviteCall;
    private Call mFeedbackCall;

    private SurfaceView mHostPlaySurf;      //渲染主播流的SurfaceView

    private ImManager mImManager;
    private MnsControlBody mMnsControlBody;
    private WebSocketConnectOptions mWSConnOpt;

    private String mUID;
    private Map<String, String> mUidMap = new HashMap<>();
    private String mPublisherUID;
    private String mPlayUrl;
    private String mLiveRoomID;

    private boolean isLoading = false;  //是否正在缓冲

    private MgrCallback mCallback;

    private String mTipString;
    private boolean mVideoChatApiCalling = false;

    private int mReconnectCount = 0;

    public LifecycledPlayerMgr(Context context, ImManager imManager, String uid,
                               MgrCallback callback) {
        super(context);
        this.mSDKHelper = new PlayerSDKHelper();
        this.mImManager = imManager;
        this.mUID = uid;
        this.mCallback = callback;
    }

    @Override
    public void onCreate() {
        Context context = getContext();
        assert (context != null);
        mSDKHelper.initPlayer(context, mPlayerErrorListener, mPlayerInfoListener, mCallback);  //初始化播放器
    }

    @Override
    public void onStart() {
    }

    @Override
    public void onResume() {
        if (mWSConnOpt != null && mMnsControlBody != null) {
            mImManager.createSession(mWSConnOpt, mMnsControlBody);
            mImManager.register(MessageType.LIVE_COMPLETE, mLiveCloseFunc, MsgDataLiveClose.class);
            mImManager.register(MessageType.START_PUSH, mPublishStreamFunc, MsgDataStartPublishStream.class);
            mImManager.register(MessageType.AGREE_CALLING, mAgreeFunc, MsgDataAgreeVideoCall.class);
            mImManager.register(MessageType.NOT_AGREE_CALLING, mNotAgreeFunc, MsgDataNotAgreeVideoCall.class);
            mImManager.register(MessageType.CALLING_SUCCESS, mMergeStreamSuccFunc, MsgDataMergeStream.class);
            mImManager.register(MessageType.CALLING_FAILED, mMergeStreamFailedFunc, MsgDataMergeStream.class);
            mImManager.register(MessageType.TERMINATE_CALLING, mCloseChatFunc, MsgDataCloseVideoCall.class);
            mImManager.register(MessageType.INVITE_CALLING, mInviteFunc, MsgDataInvite.class);
            mImManager.register(MessageType.MIX_STATUS_CODE, mMixStatusCodeFunc, MsgDataMixStatusCode.class);
            mImManager.register(MessageType.EXIT_CHATTING, mExitingChattingFunc, MsgDataExitChatting.class);
        }
        mSDKHelper.resume();
    }

    @Override
    public void onPause() {
        int count = 0;
        while (mVideoChatApiCalling && count++ < 10) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        mImManager.unRegister(MessageType.LIVE_COMPLETE);
        mImManager.unRegister(MessageType.AGREE_CALLING);
        mImManager.unRegister(MessageType.NOT_AGREE_CALLING);
        mImManager.unRegister(MessageType.CALLING_FAILED);
        mImManager.unRegister(MessageType.CALLING_SUCCESS);
        mImManager.unRegister(MessageType.TERMINATE_CALLING);
        mImManager.unRegister(MessageType.INVITE_CALLING);
        mImManager.unRegister(MessageType.MIX_STATUS_CODE);
        mImManager.unRegister(MessageType.JOIN_CHATTING);
        mImManager.unRegister(MessageType.EXIT_CHATTING);
        mImManager.unRegister(MessageType.START_PUSH);
        mImManager.unRegister(MessageType.LIVE_COMPLETE);
        mImManager.closeSession();

        mSDKHelper.pause(); //暂停播放 or 连麦

    }

    @Override
    public void onStop() {
    }

    @Override
    public void onDestroy() {
        //TODO 在销毁的时候
        mLiveServiceBI.exitWatching(mLiveRoomID, mUID, null);
        if (mChatSession != null) {  //当前正在连麦
            mInviteServiceBI.leaveCall(mUID, mLiveRoomID, null);
            mChatSession = null;
            mOtherChatSessionMap.clear();
        }
//        mSDKHelper.abortChat();         //防止因为某些原因没有停止连麦的情况，再次调用一次停止连麦
//        mSDKHelper.stopPlaying();       //防止因为某些原因没有停止播放的情况，再次调用一次停止播放
        mSDKHelper.releaseChatParter(); //释放播放器资源
    }

    @Override
    public void asyncEnterLiveRoom(String liveRoomID, final AsyncCallback callback) {
        this.mLiveRoomID = liveRoomID;
        if (mEnterRoomCall != null && ServiceBI.isCalling(mEnterRoomCall)) {
            mEnterRoomCall.cancel();
            mEnterRoomCall = null;
        }
        mEnterRoomCall = mLiveServiceBI.watchLive(liveRoomID, mUID, new ServiceBI.Callback<WatchLiveResult>() {
            @Override
            public void onResponse(int code, WatchLiveResult result) {
                MNSModel mnsModel = result.getMNSModel();
                MNSConnectModel mnsConnectModel = result.getConnectModel();
                List<String> tags = new ArrayList<>();
                tags.add(mnsModel.getRoomTag());
                tags.add(mnsModel.getUserTag());
                mMnsControlBody = new MnsControlBody.Builder()
                        .accountId(mnsConnectModel.getAccountID())
                        .accessId(mnsConnectModel.getAccessID())
                        .date(mnsConnectModel.getDate())
                        .messageType(MnsControlBody.MessageType.SUBSCRIBE)
                        .topic(mnsModel.getTopic())
                        .subscription(mnsModel.getTopic())
                        .authorization("MNS " + mnsConnectModel.getAccessID() + ":" + mnsConnectModel.getAuthentication())
                        .tags(tags)
                        .build();
                mWSConnOpt = new WebSocketConnectOptions();
                mWSConnOpt.setServerURI(mnsConnectModel.getTopicWSServerAddress());
                mWSConnOpt.setProtocol(MNSClient.SCHEMA);
                //TODO:这里需要优化一下
                mImManager.createSession(mWSConnOpt, mMnsControlBody);
                mImManager.register(MessageType.START_PUSH, mPublishStreamFunc, MsgDataStartPublishStream.class);
//                        mImManager.register(MessageType.LIVE_COMPLETE, mLiveCloseFunc, MsgDataLiveClose.class);
                mImManager.register(MessageType.AGREE_CALLING, mAgreeFunc, MsgDataAgreeVideoCall.class);
                mImManager.register(MessageType.NOT_AGREE_CALLING, mNotAgreeFunc, MsgDataNotAgreeVideoCall.class);
                mImManager.register(MessageType.CALLING_SUCCESS, mMergeStreamSuccFunc, MsgDataMergeStream.class);
                mImManager.register(MessageType.CALLING_FAILED, mMergeStreamFailedFunc, MsgDataMergeStream.class);
                mImManager.register(MessageType.TERMINATE_CALLING, mCloseChatFunc, MsgDataCloseVideoCall.class);
                mImManager.register(MessageType.INVITE_CALLING, mInviteFunc, MsgDataInvite.class);
                mImManager.register(MessageType.MIX_STATUS_CODE, mMixStatusCodeFunc, MsgDataMixStatusCode.class);
                mImManager.register(MessageType.EXIT_CHATTING, mExitingChattingFunc, MsgDataExitChatting.class);
                mImManager.register(MessageType.LIVE_COMPLETE, mLiveCloseFunc, MsgDataLiveClose.class);
                //缓存直播信息
                mPlayUrl = result.getPlayUrl();
                mPublisherUID = result.getUid();
                if (callback != null) {
                    callback.onSuccess(null);
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

    @Override
    public void startPlay(SurfaceView playSurf) {
        mHostPlaySurf = playSurf;
        mSDKHelper.startToPlay(mPlayUrl, mHostPlaySurf);
        Log.d(TAG, "Player surface status is created");
    }

    @Override
    public void asyncInviteChatting(final AsyncCallback callback) throws ChatSessionException {
        mUidMap.clear();
        if (mChatSession != null && mChatSession.isActive()) {//当前有正在进行的连麦（or 邀请）
            if (mCallback != null) {
                Bundle data = new Bundle();
                data.putString(DATA_KEY_PLAYER_ERROR_MSG, "当前有正在进行的连麦（or 邀请）");
                mCallback.onEvent(TYPE_OPERATION_CALLED_ERROR, data);
                throw new ChatSessionException(ChatSessionException.ERROR_CURR_CHATTING);
            }
        }
        List<String> inviteeUIDs = new ArrayList<>();
        if (mInviteCall != null && ServiceBI.isCalling(mInviteCall)) {
            mInviteCall.cancel();
            mInviteCall = null;
        }
        inviteeUIDs.add(mPublisherUID);
        mChatSession = new ChatSession(mSessionHandler);
        mChatSession.invite(mPublisherUID, mUID);
        mInviteCall = mInviteServiceBI.inviteCall(mUID, inviteeUIDs,
                InviteForm.TYPE_PIC_BY_PIC, FeedbackForm.INVITE_TYPE_WATCHER, mLiveRoomID, new ServiceBI.Callback() {
                    @Override
                    public void onResponse(int code, Object response) {
                        mChatSession.notifyInviteSuccess();
                        if (callback != null) {
                            callback.onSuccess(null);
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        mChatSession.notifyInviteFailure();
                        if (callback != null) {
                            callback.onFailure(null, t);
                        }
                    }
                });
    }

    @Override
    public void launchChat(SurfaceView previewSurface, Map<String, SurfaceView> uidSurfaceMap) {
        if (mChatSession != null) {
            Map<String, SurfaceView> urlSurfaceMap = new HashMap<>();
            Iterator<String> uids = uidSurfaceMap.keySet().iterator();
            String uid;
            while (uids.hasNext()) {
                // TODO
                uid = uids.next();
                urlSurfaceMap.put(mOtherChatSessionMap.get(uid).getChatSessionInfo().getPlayUrl()
                        , uidSurfaceMap.get(uid));
            }
            // TODO by xinye : 发起连麦/Add连麦
            if (!mVideoChatApiCalling) {
                mTipString = "开始发起/添加连麦!执行中...,请稍等";
                mVideoChatApiCalling = true;
                Log.e("xiongbo07", "开始发起连麦...");
                mChatSession.launchChat();
                mSDKHelper.startLaunchChat(mChatSession.getChatSessionInfo().getRtmpUrl(),
                        previewSurface,
                        mChatSession.getChatSessionInfo().getPlayUrl(),
                        urlSurfaceMap);
            } else {
                if (mCallback != null) {
                    Bundle data = new Bundle();
                    data.putString(DATA_KEY_PLAYER_ERROR_MSG, mTipString);
                    mCallback.onEvent(TYPE_OPERATION_CALLED_ERROR, data);
                }
            }
        }
    }

    /**
     * 连麦反馈
     *
     * @param publisherUID
     */
    private void asyncFeedbackInviting(final String publisherUID) {
        if (ServiceBI.isCalling(mFeedbackCall)) {
            mFeedbackCall.cancel();
        }
        mChatSession.feedbactInviting(true);
        mFeedbackCall = mInviteServiceBI.feedback(FeedbackForm.INVITE_TYPE_WATCHER,
                FeedbackForm.INVITE_TYPE_ANCHOR, publisherUID, mUID,
                InviteForm.TYPE_PIC_BY_PIC, FeedbackForm.STATUS_AGREE, new ServiceBI.Callback<InviteFeedbackResult>() {
                    @Override
                    public void onResponse(int code, InviteFeedbackResult response) {
                        ChatSession chatSession;
                        ChatSessionInfo sessionInfo;
                        String inviteeUID;
                        ArrayList<String> inviteeUIDList = new ArrayList<>();
                        Bundle data = new Bundle();
                        mChatSession.notifyFeedbackSuccess();
                        ChatSessionInfo chatSessionInfo = new ChatSessionInfo();
                        chatSessionInfo.setRtmpUrl(response.getRtmpUrl());
                        chatSessionInfo.setPlayUrl(response.getMainPlayUrl());
                        mChatSession.setChatSessionInfo(chatSessionInfo);
                        data.putString(DATA_KEY_INVITER_UID, publisherUID);
                        List<ParterInfo> parterInfos = response.getOtherParterInfos();
                        if (parterInfos != null && parterInfos.size() > 0) {
                            for (ParterInfo parterInfo : parterInfos) {
                                inviteeUID = parterInfo.getUID();
                                mUidMap.remove(inviteeUID);
                                chatSession = new ChatSession(mSessionHandler);//这里用不到session的状态管理，只用到了信息缓存功能
                                sessionInfo = new ChatSessionInfo();
                                sessionInfo.setPlayerUID(inviteeUID);
                                sessionInfo.setPlayUrl(parterInfo.getPlayUrl());
                                chatSession.setChatSessionInfo(sessionInfo);
                                mOtherChatSessionMap.put(inviteeUID, chatSession);
                                inviteeUIDList.add(inviteeUID);
                            }
                            data.putStringArrayList(DATA_KEY_INVITEE_UID_LIST, inviteeUIDList);
                        }
                        if (mCallback != null) {
                            mCallback.onEvent(TYPE_START_CHATTING, data);
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        if (mChatSession != null) {
//                            mChatSession.
                        }
                        if (mCallback != null) {
                            //TODO：收到邀请后，反馈失败了
                        }
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
    public void asyncTerminateChatting(final AsyncCallback callback) {

        if (mChatSession != null) {
            if (mVideoChatApiCalling) {
                if (mCallback != null) {
                    Bundle data = new Bundle();
                    data.putString(DATA_KEY_PLAYER_ERROR_MSG, mTipString);
                    mCallback.onEvent(TYPE_OPERATION_CALLED_ERROR, data);
                }
                return;
            }

            if (!mChatSession.isActive()) {
                if (mCallback != null) {
                    Bundle data = new Bundle();
                    data.putString(DATA_KEY_PLAYER_ERROR_MSG, "请耐心等待退出连麦过程执行完,不要重复点击退出连麦");
                    mCallback.onEvent(TYPE_OPERATION_CALLED_ERROR, data);
                }
                return;
            }
        }
        /*
         * 退出连麦流程：
         * 1、调用服务端停止连麦接口
         * 2、成功后调用SDK停止连麦接口，并且上报停止连麦成功的消息接口
         * 3、清空本地连麦相关信息（比如mOtherChattingViews、mOtherChattingPlayUrls）
         */
        mInviteServiceBI.leaveCall(mUID, mLiveRoomID, new ServiceBI.Callback() {

            @Override
            public void onResponse(int code, Object response) {
                // TODO by xinye : 退出连麦
                if (mChatSession != null) {
                    mTipString = "开始退出连麦!执行中...,请稍等";
                    mVideoChatApiCalling = true;
                    Log.e("xiongbo07", "开始退出连麦...");
                    mChatSession.abortChat();
                }
                mSDKHelper.abortChat(); //调用SDK中断连麦
                if (callback != null) {
                    callback.onSuccess(null);
                }
                //清空本地混存的连麦相关信息
                mOtherChatSessionMap.clear();
                mChatSession = null;
            }

            @Override
            public void onFailure(Throwable t) {
                if (callback != null) {
                    callback.onFailure(null, t);
                }

                if (mCallback != null) {
                    Bundle data = new Bundle();
                    data.putString(DATA_KEY_PLAYER_ERROR_MSG, t.getLocalizedMessage());
                    mCallback.onEvent(TYPE_OPERATION_CALLED_ERROR, data);
                }
            }
        });
    }

    @Override
    public void asyncTerminatePlaying(final AsyncCallback callback) {
        //调用服务端停止播放接口
        mSDKHelper.stopPlaying();
    }

    @Override
    public void asyncExitRoom(final AsyncCallback callback) {
        mLiveServiceBI.exitWatching(mLiveRoomID, mUID, new ServiceBI.Callback() {
            @Override
            public void onResponse(int code, Object response) {
                // TODO 调用SDK停止播放接口
//                mSDKHelper.stopPlaying();
                if (callback != null) {
                    callback.onSuccess(null);
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


    /**
     * 播放器错误回调处理
     */
    AlivcVideoChatParter.OnErrorListener mPlayerErrorListener = new AlivcVideoChatParter.OnErrorListener() {

        @Override
        public boolean onError(IVideoChatParter iVideoChatParter, int what, String url) {
            Log.d(TAG, "WatchLiveActivity-->error what = " + what + ", url = " + url);

            if (what == 0) {
                return false;
            }
            if (mChatSession != null) {
                mTipString = null;
//                mChatSession.setOperationCompleted();
            }
            switch (what) {
                case MediaError.ALIVC_ERR_PLAYER_INVALID_INPUTFILE:
                    Log.d(TAG, "encounter player invalid input file.");
                    if (mReconnectCount++ < MAX_RECONNECT_COUNT)
                        mSDKHelper.reconnect(url);
                    if (mCallback != null) {
                        mCallback.onEvent(TYPE_PLAYER_INVALID_INPUTFILE, null);
                    }
                    break;
                case MediaError.ALIVC_ERR_PLAYER_OPEN_FAILED:
                    Log.d(TAG, "encounter player open failed.");
                    if (mReconnectCount++ < MAX_RECONNECT_COUNT)
                        mSDKHelper.reconnect(url);
                    if (mCallback != null) {
                        mCallback.onEvent(TYPE_PLAYER_OPEN_FAILED, null);
                    }
                    break;
                case MediaError.ALIVC_ERR_PLAYER_NO_NETWORK:
                    Log.d(TAG, "encounter player no network.");
                    if (mReconnectCount++ < MAX_RECONNECT_COUNT)
                        mSDKHelper.reconnect(url);
                    if (mCallback != null) {
                        mCallback.onEvent(TYPE_PLAYER_NO_NETWORK, null);
                    }
                    break;
                case MediaError.ALIVC_ERR_PLAYER_TIMEOUT:
                    Log.d(TAG, "encounter player timeout, so call restartToPlayer");
                    if (mReconnectCount++ < MAX_RECONNECT_COUNT)
                        mSDKHelper.reconnect(url);
                    if (mCallback != null) {
                        mCallback.onEvent(TYPE_PLAYER_TIMEOUT, null);
                    }
                    break;
                case MediaError.ALIVC_ERR_PLAYER_READ_PACKET_TIMEOUT:
                    Log.d(TAG, "encounter player read packet timeout.");
                    if (mReconnectCount++ < MAX_RECONNECT_COUNT)
                        mSDKHelper.reconnect(url);
                    if (mCallback != null) {
                        mCallback.onEvent(TYPE_PLAYER_READ_PACKET_TIMEOUT, null);
                    }
                    break;
                case MediaError.ALIVC_ERR_PLAYER_NO_MEMORY:
                case MediaError.ALIVC_ERR_PLAYER_INVALID_CODEC:
                case MediaError.ALIVC_ERR_PLAYER_NO_SURFACEVIEW:
                case MediaError.ALIVC_ERR_PLAYER_UNSUPPORTED:
                case MediaError.ALIVC_ERR_PLAYER_UNKNOW:
                    if (mCallback != null) {
                        Bundle data = new Bundle();
                        data.putInt(DATA_KEY_PLAYER_ERROR_CODE, what);
                        mCallback.onEvent(TYPE_PLAYER_INTERNAL_ERROR, data);
                    }
                    if (mChatSession != null && mChatSession.isMixing()) {  //如果正在连麦则结束连麦
                        asyncTerminateChatting(null);   //结束连麦
                        if (mCallback != null) {
                            mCallback.onEvent(TYPE_CHATTING_FINISHED, null);
                        }
                    }
                    asyncTerminatePlaying(null);

                    break;
                case MediaError.ALIVC_ERR_PUBLISHER_AUDIO_CAPTURE_DISABLED://音频采集失败
                case MediaError.ALIVC_ERR_PUBLISHER_AUDIO_CAPTURE_NO_DATA:
                    if (mChatSession != null) {
                        Log.d(TAG, "音频采集失败，结束连麦");
                        if (mCallback != null) {
                            Bundle data = new Bundle();
                            data.putInt(DATA_KEY_PLAYER_ERROR_CODE, what);
                            mCallback.onEvent(TYPE_PUBLISHER_NO_AUDIO_DATA, data);
                        }
                    } else {
                        Log.d(TAG, "音频采集失败，但是当前没有处于连麦状态");
                    }
                    break;
                case MediaError.ALIVC_ERR_PUBLISHER_VIDEO_CAPTURE_NO_DATA:
                case MediaError.ALIVC_ERR_PUBLISHER_VIDEO_CAPTURE_DISABLED:
                    // TODO
                    if (mChatSession != null) {
                        Log.d(TAG, "视频采集失败，结束连麦");
                        if (mCallback != null) {
                            Bundle data = new Bundle();
                            data.putInt(DATA_KEY_PLAYER_ERROR_CODE, what);
                            mCallback.onEvent(TYPE_PUBLISHER_NO_VIDEO_DATA, data);
                        }
                    } else {
                        Log.d(TAG, "视频采集失败，但是当前没有处于连麦状态");
                    }
                    break;
                case MediaError.ALIVC_ERR_PUBLISHER_ENCODE_AUDIO_FAILED:
                case MediaError.ALIVC_ERR_PUBLISHER_AUDIO_ENCODER_INIT_FAILED:
                case MediaError.ALIVC_ERR_PUBLISHER_MALLOC_FAILED:
                case MediaError.ALIVC_ERR_PUBLISHER_ENCODE_VIDEO_FAILED:
                case MediaError.ALIVC_ERR_PUBLISHER_VIDEO_ENCODER_INIT_FAILED:
                case MediaError.ALIVC_ERR_PUBLISHER_ILLEGAL_ARGUMENT:
//                    mView.showLiveInterruptUI(R.string.network_busy, what);
                    break;
                case MediaError.ALIVC_ERR_PUBLISHER_NETWORK_POOR:
                    if (mCallback != null) {
                        mCallback.onEvent(TYPE_PUBLISHER_NETWORK_POOR, null);
                    }
                    break;
                case MediaError.ALIVC_ERR_PUBLISHER_NETWORK_UNCONNECTED:
                    if (mCallback != null) {
                        mCallback.onEvent(TYPE_PUBLISHER_NETWORK_UNCONNECT, null);
                    }
                    break;
                case MediaError.ALIVC_ERR_PUBLISHER_SEND_DATA_TIMEOUT:
                    if (mCallback != null) {
                        mCallback.onEvent(TYPE_PUBLISHER_NETWORK_TIMEOUT, null);
                    }
                    break;
                case MediaError.ALIVC_ERR_PLAYER_AUDIO_PLAY:
                    if (mCallback != null) {
                        Bundle data = new Bundle();
                        data.putInt(DATA_KEY_PLAYER_ERROR_CODE, what);
                        mCallback.onEvent(TYPE_PLAYER_AUDIO_PLAYER_ERROR, data);
                    }
                    break;
                default:
//                    mView.showLiveInterruptUI(R.string.error_unknown, what);
            }
            return true;
        }
    };


    /**
     * 播放器状态回调处理
     */
    AlivcVideoChatParter.OnInfoListener mPlayerInfoListener = new AlivcVideoChatParter.OnInfoListener() {

        @Override
        public boolean onInfo(IVideoChatParter iVideoChatParter, int what, String url) {
            Log.d(TAG, "WatchLiveActivity-->info what = " + what + ", url = " + url);
            switch (what) {
                case MediaPlayer.MEDIA_INFO_UNKNOW:
                    // 未知
                    break;
                case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                    //开始缓冲
//                        if (!isLoading) {
//                            mHandler.postDelayed(mShowInterruptRun, INTERRUPT_DELAY);
//                            isLoading = true;
//                        }

                    break;
                case MediaPlayer.MEDIA_INFO_BUFFERING_END:
//                        if (isLoading) {
//                            mHandler.removeCallbacks(mShowInterruptRun);
//                             结束缓冲
//                            mView.hideLiveInterruptUI();
//                            isLoading = false;
//                        }
                    break;
                case MediaError.ALIVC_INFO_PLAYER_FIRST_FRAME_RENDERED:
                    // 首帧显示时间
                    if (mCallback != null) {
                        mCallback.onEvent(TYPE_PLAYER_FIRST_FRAME_RENDER_SUCCESS, null);
                        mCallback.onEvent(TYPE_PARTER_OPT_END, null);
                    }
                    break;
                case MediaError.ALIVC_INFO_PUBLISH_DISPLAY_FIRST_FRAME:
                    break;
                case MediaError.ALIVC_INFO_PUBLISH_NETWORK_GOOD:
                case MediaError.ALIVC_INFO_PUBLISH_RECONNECT_START:
                case MediaError.ALIVC_INFO_PUBLISH_RECONNECT_SUCCESS:
                    break;
                case MediaError.ALIVC_INFO_PUBLISH_RECONNECT_FAILURE:
                    if (mCallback != null) {
                        Bundle data = new Bundle();
                        data.putInt(DATA_KEY_PUBLISHER_INFO_CODE, what);
                        mCallback.onEvent(TYPE_PUBLISHER_RECONNECT_FAILURE, data);
                    }
                    break;
                case MediaError.ALIVC_INFO_PLAYER_PREPARED_PROCESS_FINISHED:
                    mReconnectCount = 0;
                    break;
                case MediaError.ALIVC_INFO_PLAYER_INTERRUPT_PLAYING:
                case MediaError.ALIVC_INFO_PLAYER_STOP_PROCESS_FINISHED:
                    break;
                case MediaError.ALIVC_INFO_ONLINE_CHAT_END:
                    mTipString = null;
                    mVideoChatApiCalling = false;
                    Log.e("xiongbo07", "发起连麦成功...");
                    break;
                case MediaError.ALIVC_INFO_OFFLINE_CHAT_END:
                    mTipString = null;
                    mVideoChatApiCalling = false;
                    Log.e("xiongbo07", "退出连麦成功...");
                    break;
                case MediaError.ALIVC_INFO_ADD_CHAT_END:
                    mTipString = null;
                    mVideoChatApiCalling = false;
                    Log.e("xiongbo07", "ADD连麦成功...");
                    break;
                case MediaError.ALIVC_INFO_REMOVE_CHAT_END:
                    mTipString = null;
                    mVideoChatApiCalling = false;
                    Log.e("xiongbo07", "Remove连麦成功...");
                    break;
                case MediaError.ALIVC_INFO_PLAYER_NETWORK_POOR:
                    if (mCallback != null) {
                        Bundle data = new Bundle();
                        data.putString(IPublisherMgr.DATA_KEY_PLAYER_ERROR_MSG, url);
                        mCallback.onEvent(TYPE_PLAYER_NETWORK_POOR, data);
                    }
                    break;

            }
            Log.d(TAG, "MediaPlayer onInfo, what =" + what + ", url = " + url);
            return false;
        }
    };

    private Handler mHandler = new Handler();

    /**
     * 结束连麦的消息处理Action
     */
    ImHelper.Func<MsgDataCloseVideoCall> mCloseChatFunc = new ImHelper.Func<MsgDataCloseVideoCall>() {
        @Override
        public void action(MsgDataCloseVideoCall msgDataCloseVideoCall) {
            // TODO by xinye : 退出连麦
            if (mChatSession != null)
                mChatSession.abortChat();
            mSDKHelper.abortChat(); //结束连麦
            if (mCallback != null) {
                mCallback.onEvent(TYPE_PUBLISHER_TERMINATE_CHATTING, null);
            }
        }
    };


    /**
     * 混流成功的消息处理Action
     */
    private ImHelper.Func<MsgDataMergeStream> mMergeStreamSuccFunc = new ImHelper.Func<MsgDataMergeStream>() {
        @Override
        public void action(MsgDataMergeStream msgDataMergeStream) {
//            if (mChatStatus == VideoChatStatus.TRY_MIX) {
//                if (mRoomID.equals(msgDataMergeStream.getInviteeRoomID())) {
//                    mChatRoomID = msgDataMergeStream.getInviterRoomID();
//                } else if (mRoomID.equals(msgDataMergeStream.getInviterRoomID())) {
//                    mChatRoomID = msgDataMergeStream.getInviteeRoomID();
//                } else {
//                    mView.showToast(R.string.merge_stream_failed);
//                    return;
//                }
//                if (TextUtils.isEmpty(mChatRoomID)) {
//                    Log.d(TAG, "Merge stream succeed, but mChatRoomID is null");
//                }
//
//                updateChatState(VideoChatStatus.MIX_SUCC);  //更新当前连麦状态为混流成功
//            }
        }
    };

    /**
     * 混流失败的消息处理Action
     */
    private ImHelper.Func<MsgDataMergeStream> mMergeStreamFailedFunc = new ImHelper.Func<MsgDataMergeStream>() {
        @Override
        public void action(MsgDataMergeStream msgDataMergeStream) {
//            if (mChatStatus == VideoChatStatus.TRY_MIX) {
//                mView.showToast(R.string.merge_stream_failed);
//            }
        }
    };

    /**
     * 不同意连麦的消息处理Action
     */
    ImHelper.Func<MsgDataNotAgreeVideoCall> mNotAgreeFunc = new ImHelper.Func<MsgDataNotAgreeVideoCall>() {
        @Override
        public void action(final MsgDataNotAgreeVideoCall notAgreeVideoCall) {
            if (mChatSession != null) {
                mChatSession.notifyNotAgreeInviting(notAgreeVideoCall);
            }
        }
    };

    /**
     * 同意连麦的消息处理Action
     */
    ImHelper.Func<MsgDataAgreeVideoCall> mAgreeFunc = new ImHelper.Func<MsgDataAgreeVideoCall>() {
        @Override
        public void action(final MsgDataAgreeVideoCall msgDataAgreeVideoCall) {
            // 如果超时了，那么对于主播的同意信息就不进行处理了
            if (mChatSession == null || mChatSession.getChatStatus() == VideoChatStatus.UNCHAT) {
                return;
            }
            ChatSession chatSession;
            ChatSessionInfo sessionInfo;
            String inviteeUID;
            Bundle data = new Bundle();

            ChatSessionInfo chatSessionInfo = new ChatSessionInfo();
            chatSessionInfo.setRtmpUrl(msgDataAgreeVideoCall.getRtmpUrl());
            chatSessionInfo.setPlayUrl(msgDataAgreeVideoCall.getMainPlayUrl());

            mChatSession.setChatSessionInfo(chatSessionInfo); // TODO 怎么可能此处为空？？？
            data.putString(DATA_KEY_INVITER_UID, mPublisherUID);
            List<ParterInfo> parterInfos = msgDataAgreeVideoCall.getParterInfos();
            ArrayList<String> inviteeUIDList = new ArrayList<>();
            if (parterInfos != null && parterInfos.size() > 0) {
                for (ParterInfo parterInfo : parterInfos) {
                    inviteeUID = parterInfo.getUID();
                    mUidMap.remove(inviteeUID);
                    chatSession = new ChatSession(mSessionHandler);//这里用不到session的状态管理，只用到了信息缓存功能
                    sessionInfo = new ChatSessionInfo();
                    sessionInfo.setPlayerUID(inviteeUID);
                    sessionInfo.setPlayUrl(parterInfo.getPlayUrl());
                    chatSession.setChatSessionInfo(sessionInfo);
                    mOtherChatSessionMap.put(inviteeUID, chatSession);
                    inviteeUIDList.add(inviteeUID);
                }
                data.putStringArrayList(DATA_KEY_INVITEE_UID_LIST, inviteeUIDList);
            }
            if (mCallback != null) {
                mCallback.onEvent(TYPE_START_CHATTING, data);
            }
            mChatSession.notifyParterAgreeInviting();

        }

    };

    /**
     * 连麦邀请的消息处理Action
     */
    ImHelper.Func<MsgDataInvite> mInviteFunc = new ImHelper.Func<MsgDataInvite>() {

        @Override
        public void action(final MsgDataInvite msgDataInvite) {
            mUidMap.clear();
            mChatSession = new ChatSession(mSessionHandler);
            mChatSession.notifyReceivedInviting(msgDataInvite.getInviterUID(), mUID);
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
     * 结束直播的消息处理Action
     */
    ImHelper.Func<MsgDataLiveClose> mLiveCloseFunc = new ImHelper.Func<MsgDataLiveClose>() {
        @Override
        public void action(MsgDataLiveClose msgDataLiveClose) {
            if (mChatSession != null) {
                // TODO by xinye : 退出连麦
                mSDKHelper.abortChat();
                mChatSession = null;
                mOtherChatSessionMap.clear();
            }
            if (mCallback != null) {
                mCallback.onEvent(TYPE_LIVE_CLOSE, null);
            }
        }
    };

    /**
     * 连麦过程中CDN的状态码回调
     */
    ImHelper.Func<MsgDataMixStatusCode> mMixStatusCodeFunc = new ImHelper.Func<MsgDataMixStatusCode>() {
        @Override
        public void action(MsgDataMixStatusCode msgDataMixStatusCode) {
            if (msgDataMixStatusCode != null && mChatSession != null) {
                String code = msgDataMixStatusCode.getCode();
                if (MixStatusCode.INTERNAL_ERROR.toString().equals(code)) {
                    mChatSession.notifyInternalError();
                } else if (MixStatusCode.MAIN_STREAM_NOT_EXIST.toString().equals(code)) {
                    mChatSession.notifyMainStreamNotExist();
                } else if (MixStatusCode.MIX_STREAM_NOT_EXIST.toString().equals(code)) {
                    mChatSession.notifyMixStreamNotExist();
                } else if (MixStatusCode.SUCCESS.toString().equals(code)) {
                    ChatSession session = mOtherChatSessionMap.get(msgDataMixStatusCode.getMixUid());
                    if (session != null) {
                        session.setChatStatus(VideoChatStatus.MIX_SUCC);
                    }
//                    mChatSession.notifyMixStreamSuccess();
                }
                Log.d(TAG, "Mix statusCode: " + msgDataMixStatusCode.getCode());
            }
            Log.d(TAG, "Mix status code:" + msgDataMixStatusCode.getCode());

        }
    };

    //推流成功的消息处理Action
    ImHelper.Func<MsgDataStartPublishStream> mPublishStreamFunc = new ImHelper.Func<MsgDataStartPublishStream>() {
        @Override
        public void action(MsgDataStartPublishStream msgDataStartPublishStream) {
            String uid = msgDataStartPublishStream.getUid();
            Log.d(TAG, "WatchLiveActivity -->Publish Success. " + uid);
            if (!mUID.equals(uid) && !uid.equals(mPublisherUID)
                    && mChatSession != null) {//有其他连麦用户推流成功
                Log.d(TAG, "WatchLiveActivity -->Publish Success. " + mChatSession.getChatStatus());
                // 1. 如果未连麦,直接忽略
                if (mChatSession.getChatStatus() == VideoChatStatus.UNCHAT) {
                    Log.d(TAG, "WatchLiveActivity -->Publish Success. unchat return");
                    return;
                }

                if (mChatSession.getChatStatus() != VideoChatStatus.MIX_SUCC && mChatSession.getChatStatus() != VideoChatStatus.TRY_MIX) {
                    // 进入队列
                    mUidMap.put(uid, msgDataStartPublishStream.getPlayUrl());
                    Log.d(TAG, "WatchLiveActivity -->Publish Success. put to map");
                    return;
                }
                // 2. 如果连麦中,add chat
//                int count = 0;
//                while ((mChatSession.getChatStatus() != VideoChatStatus.MIX_SUCC) && count++ < 10) {
//                    try {
//                        Thread.sleep(200);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }

                if (mChatSession.getChatStatus() != VideoChatStatus.MIX_SUCC && mChatSession.getChatStatus() != VideoChatStatus.TRY_MIX) {
                    Log.d(TAG, "WatchLiveActivity -->Publish Success. not mix success return");
                    return;
                }
                // 3. 如果连麦发起中, 等待连麦发起完成再add chat
                addChatSession(msgDataStartPublishStream.getPlayUrl(), uid);
                ArrayList<String> userIdList = new ArrayList<>();
                userIdList.add(uid);
                handlePublishStreamMsg(userIdList);
                Log.d(TAG, "WatchLiveActivity -->Publish Success. add chat " + uid);
            } else if (mUID.equals(uid)) {
                ArrayList<String> userIdList = new ArrayList<>();
                for (String userId : mUidMap.keySet()) {
                    userIdList.add(userId);
                    Log.d(TAG, "WatchLiveActivity -->Publish Success. add chat " + userId);
                    addChatSession(mUidMap.get(userId), userId);
                }
                if (!userIdList.isEmpty()) {
                    handlePublishStreamMsg(userIdList);
                    mUidMap.clear();

                }
            }
        }
    };

    private void handlePublishStreamMsg(ArrayList<String> userIdList) {
        if (mCallback != null) {
            Bundle bundle = new Bundle();
            bundle.putStringArrayList(DATA_KEY_INVITEE_UID_LIST, userIdList);
            mCallback.onEvent(TYPE_OTHER_PEOPLE_JOIN_IN_CHATTING, bundle);
            Log.d(TAG, "WatchLiveActivity -->publish stream. type other people join in chatting");
        }
    }

    private void addChatSession(String playUrl, String mUid) {
        ChatSession session = new ChatSession(mSessionHandler);
        ChatSessionInfo sessionInfo = new ChatSessionInfo();
        sessionInfo.setPlayerUID(mUid);
        sessionInfo.setPlayUrl(playUrl);
        session.setChatSessionInfo(sessionInfo);
        mOtherChatSessionMap.put(mUid, session);
    }


    /**
     * 退出连麦的消息处理Action
     */
    ImHelper.Func<MsgDataExitChatting> mExitingChattingFunc = new ImHelper.Func<MsgDataExitChatting>() {
        @Override
        public void action(MsgDataExitChatting msgDataExitChatting) {
            Log.d(TAG, "Someone exit chatting");
            String inviteeUID = msgDataExitChatting.getUID();
            if (!mUID.equals(inviteeUID)) {//其他人退出连麦
                // 如果本观众正在发起连麦,而这个时候其他观众退出连麦,不需要处理此消息
                if (mChatSession == null || (mChatSession.getChatStatus() != VideoChatStatus.MIX_SUCC && mChatSession.getChatStatus() != VideoChatStatus.TRY_MIX)) {
                    if (mChatSession != null)
                        Log.d(TAG, "chat session status = " + mChatSession.getChatStatus());
                    return;
                }
                ChatSession chatSession;
                List<String> playUrls = new ArrayList<>();
                if ((chatSession = mOtherChatSessionMap.get(inviteeUID)) != null) {
                    playUrls.add(chatSession.getChatSessionInfo().getPlayUrl());
                    mOtherChatSessionMap.remove(inviteeUID);
                }
                // TODO by xinye : 其他观众退出连麦
                mVideoChatApiCalling = true;
                Log.e("xiongbo07", "开始Remove连麦...");
                int result = mSDKHelper.removeChats(playUrls);
                if (result != 0) {
                    mVideoChatApiCalling = false;
                } else {
                    if (mCallback != null) {
                        Bundle data = new Bundle();
                        data.putString(DATA_KEY_INVITEE_UID, inviteeUID);
                        mCallback.onEvent(TYPE_OTHER_PEOPLE_EXIT_CHATTING, data);
                    }
                }
            } else {//自己退出连麦
                // TODO by xinye : 退出连麦
                mSDKHelper.abortChat();
                mChatSession = null;
                if (mCallback != null) {
                    mCallback.onEvent(TYPE_SELF_EXIT_CHATTING, null);
                }
            }

        }
    };

    private SessionHandler mSessionHandler = new SessionHandler() {
        @Override
        public void onInviteChatTimeout() {
            // 发起连麦邀请之后，10秒之内还收不到反馈，则按照超时处理，认为对方已经拒绝
            mVideoChatApiCalling = false;
            if (mChatSession != null) {
                mChatSession.notifyNotAgreeInviting(null);
            }

            if (mCallback != null) {
                mCallback.onEvent(TYPE_INVITE_CHAT_TIMEOUT, null);
            }
        }

        @Override
        public void onProcessInvitingTimeout() {
            // TODO by xinye 收到邀请之后，10秒之内没有处理的话，按照超时处理
            mVideoChatApiCalling = false;
        }

        @Override
        public void onMixStreamError() {
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

    public AlivcPublisherPerformanceInfo getPublisherPerformanceInfo() {
//        return mSDKHelper.getPublisherPerformanceInfo();
        return new AlivcPublisherPerformanceInfo();
    }

    public AlivcPlayerPerformanceInfo getPlayerPerformanceInfo(String url) {
//        return mSDKHelper.getPlayerPerformanceInfo(url);
        return new AlivcPlayerPerformanceInfo();
    }
}
