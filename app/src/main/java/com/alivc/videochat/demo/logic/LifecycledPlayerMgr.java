package com.alivc.videochat.demo.logic;

import android.content.Context;
import android.os.Bundle;
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
 * 类的描述: 是观众界面ui与连麦观众端核心代码的链接
 */
public class LifecycledPlayerMgr extends ContextBase implements IPlayerMgr, ILifecycleListener {
    private static final String TAG = LifecycledPlayerMgr.class.getName();

    public static final int MAX_RECONNECT_COUNT = 10;
    private ChatSession mChatSession;       //当前观众与主播连麦的会话
    private PlayerSDKHelper mSDKHelper;
    /**
     * 变量的描述: 其他观众参与连麦的连麦流程
     */
    private HashMap<String, ChatSession> mOtherChatSessionMap = new HashMap<>();    //

    private LiveServiceBI mLiveServiceBI = ServiceBIFactory.getLiveServiceBI();
    private InviteServiceBI mInviteServiceBI = ServiceBIFactory.getInviteServiceBI();
    private Call mEnterRoomCall;
    private Call mInviteCall;
    private Call mFeedbackCall;

    /**
     * 变量的描述: 从startPlay方法中获取渲染主播流的SurfaceView
     */
    private SurfaceView mHostPlaySurf;

    private ImManager mImManager;
    private MnsControlBody mMnsControlBody;
    private WebSocketConnectOptions mWSConnOpt;

    private String mUID;
    private Map<String, String> mUidMap = new HashMap<>();
    private String mPublisherUID;
    /**
     * 变量的描述: 从业务服务器中获取的主播播放地址
     */
    private String mPlayUrl;
    private String mLiveRoomID;

    private boolean isLoading = false;  //是否正在缓冲

    private MgrCallback mCallback;

    private String mTipString;
    /**
     * 变量的描述: 是否正在走关于连麦的API流程
     */
    private boolean mVideoChatApiCalling = false;

    private int mReconnectCount = 0;

    public LifecycledPlayerMgr(Context context, ImManager imManager, String uid, MgrCallback callback) {
        super(context);
        this.mSDKHelper = new PlayerSDKHelper();
        this.mImManager = imManager;
        this.mUID = uid;
        this.mCallback = callback;
    }

    // --------------------------------------------------------------------------------------------------------

    @Override
    public void onCreate() {
        Context context = getContext();
        if (context != null)
            mSDKHelper.initPlayer(context, mPlayerErrorListener, mPlayerInfoListener, mCallback);  //初始化播放器
    }

    @Override
    public void onStart() {
    }

    @Override
    public void onResume() {
        // 第一次进入本界面不会进入判断里
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
        // 暂停播放，方法里会先判断是否有过暂停
        mSDKHelper.resume();
    }

    @Override
    public void onPause() {
        int count = 0;
        // 等待连麦的api调用完毕
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
        if (mChatSession != null) {  // 当前正在连麦
            mInviteServiceBI.leaveCall(mUID, mLiveRoomID, null);
            mChatSession = null;
            mOtherChatSessionMap.clear();
        }
//        mSDKHelper.abortChat();         //防止因为某些原因没有停止连麦的情况，再次调用一次停止连麦
//        mSDKHelper.stopPlaying();       //防止因为某些原因没有停止播放的情况，再次调用一次停止播放
        mSDKHelper.releaseChatParter(); //释放播放器资源
    }

    // --------------------------------------------------------------------------------------------------------

    @Override // 进入观看界面的时候，就请求网络获取播放地址，并且建立MNS链接，然后注册订阅
    public void asyncEnterLiveRoom(String liveRoomID, final AsyncCallback callback) {
        this.mLiveRoomID = liveRoomID;
        if (mEnterRoomCall != null && ServiceBI.isCalling(mEnterRoomCall)) {
            mEnterRoomCall.cancel();
            mEnterRoomCall = null;
        }
        mEnterRoomCall = mLiveServiceBI.watchLive(liveRoomID, mUID, new ServiceBI.Callback<WatchLiveResult>() {
            @Override
            public void onResponse(int code, WatchLiveResult result) {
                // WatchLiveResult中的数据是通过两次网络请求获取的，其中MNSConnectModel是单独一次网络请求获取的结果，最后传递给了WatchLiveResult
                MNSModel mnsModel = result.getMNSModel();
                MNSConnectModel mnsConnectModel = result.getConnectModel();

                // 添加房间标记和用户标记
                List<String> tags = new ArrayList<>();
                tags.add(mnsModel.getRoomTag());
                tags.add(mnsModel.getUserTag());

                // 建造MnsControlBody对象
                mMnsControlBody = new MnsControlBody.Builder()
                        .accountId(mnsConnectModel.getAccountID())// 账户id？
                        .accessId(mnsConnectModel.getAccessID())// 访问id？
                        .date(mnsConnectModel.getDate())
                        .messageType(MnsControlBody.MessageType.SUBSCRIBE)// 消息类型：订阅？
                        .topic(mnsModel.getTopic())// 主题名称？
                        .subscription(mnsModel.getTopic())// 订阅名称？
                        .authorization("MNS " + mnsConnectModel.getAccessID() + ":" + mnsConnectModel.getAuthentication())// 授权？
                        .tags(tags)// 标记
                        .build();

                // 创建WebSocketConnectOptions对象并进行配置
                mWSConnOpt = new WebSocketConnectOptions();
                mWSConnOpt.setServerURI(mnsConnectModel.getTopicWSServerAddress());// 主题ws服务地址
                mWSConnOpt.setProtocol(MNSClient.SCHEMA);// 设定协议

                //TODO:这里需要优化一下
                mImManager.createSession(mWSConnOpt, mMnsControlBody);
                // 注册消息服务
                mImManager.register(MessageType.START_PUSH, mPublishStreamFunc, MsgDataStartPublishStream.class);
                mImManager.register(MessageType.AGREE_CALLING, mAgreeFunc, MsgDataAgreeVideoCall.class);
                mImManager.register(MessageType.NOT_AGREE_CALLING, mNotAgreeFunc, MsgDataNotAgreeVideoCall.class);
                mImManager.register(MessageType.CALLING_SUCCESS, mMergeStreamSuccFunc, MsgDataMergeStream.class);
                mImManager.register(MessageType.CALLING_FAILED, mMergeStreamFailedFunc, MsgDataMergeStream.class);
                mImManager.register(MessageType.TERMINATE_CALLING, mCloseChatFunc, MsgDataCloseVideoCall.class);
                mImManager.register(MessageType.INVITE_CALLING, mInviteFunc, MsgDataInvite.class);
                mImManager.register(MessageType.MIX_STATUS_CODE, mMixStatusCodeFunc, MsgDataMixStatusCode.class);
                mImManager.register(MessageType.EXIT_CHATTING, mExitingChattingFunc, MsgDataExitChatting.class);
                mImManager.register(MessageType.LIVE_COMPLETE, mLiveCloseFunc, MsgDataLiveClose.class);

                // 缓存直播信息
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

    @Override // 获取播放地址成功后，就正式开始播放
    public void startPlay(SurfaceView playSurf) {
        mHostPlaySurf = playSurf;
        mSDKHelper.startToPlay(mPlayUrl, mHostPlaySurf);
        Log.d(TAG, "Player surface status is created");
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

    @Override // 请求网络，告诉业务服务器观众要邀请主播进行连麦
    public void asyncInviteChatting(final AsyncCallback callback) throws ChatSessionException {
        mUidMap.clear();

        // 如果观众的连麦的状态管理器ChatSession的isActive方法返回true，在代表当前有正在进行的连麦（or 邀请）
        if (mChatSession != null && mChatSession.isActive()) {//
            if (mCallback != null) {
                Bundle data = new Bundle();
                data.putString(DATA_KEY_PLAYER_ERROR_MSG, "当前有正在进行的连麦（or 邀请）");
                mCallback.onEvent(TYPE_OPERATION_CALLED_ERROR, data);
                throw new ChatSessionException(ChatSessionException.ERROR_CURR_CHATTING);
            }
        }

        // 存储被邀请人的用户ID，也就是主播的id
        List<String> inviteeUIDs = new ArrayList<>();

        // 清空连麦请求网络任务
        if (mInviteCall != null && ServiceBI.isCalling(mInviteCall)) {
            mInviteCall.cancel();
            mInviteCall = null;
        }
        // 将主播的uid添加进uid集合中。这里只有主播是被邀请
        inviteeUIDs.add(mPublisherUID);

        mChatSession = new ChatSession(mChatSessionCallback);

        mChatSession.invite(mPublisherUID, mUID);
        // 请求网络去邀请别人进行连麦，业务服务器发送请求给被邀请人，被邀请人的结果发给业务服务器，业务服务器在通过MNS发送给邀请人也就是本用户
        mInviteCall = mInviteServiceBI.inviteCall(mUID, inviteeUIDs, InviteForm.TYPE_PIC_BY_PIC, FeedbackForm.INVITE_TYPE_WATCHER, mLiveRoomID, new ServiceBI.Callback() {
            @Override
            public void onResponse(int code, Object response) {// 告诉业务服务器观众要邀请主播进行连麦的消息 成功
                mChatSession.notifyInviteSuccess();
                if (callback != null) {
                    callback.onSuccess(null);
                }
            }

            @Override
            public void onFailure(Throwable t) {// 告诉业务服务器观众要邀请主播进行连麦的消息 失败
                mChatSession.notifyInviteFailure();
                if (callback != null) {
                    callback.onFailure(null, t);
                }
            }
        });
    }

    // --------------------------------------------------------------------------------------------------------

    @Override
    // 正式开始与主播连麦，参数一是连麦观众推流自己的视频，参数二是播放观众连麦前已经和主播进行连麦的观众
    // 这个方法如果是在本观众已经成功连麦后被调用，则其内部是只播放后连麦的人的视频
    public void launchChat(SurfaceView previewSurface, Map<String, SurfaceView> uidSurfaceMap) {
        if (mChatSession != null) {
            // 使用参数二uidSurfaceMap和本类变量mOtherChatSessionMap，存储一个对应了播放地址和Surface到集合中
            Map<String, SurfaceView> urlSurfaceMap = new HashMap<>();
            Iterator<String> uids = uidSurfaceMap.keySet().iterator();
            String uid;
            while (uids.hasNext()) {
                uid = uids.next();
                urlSurfaceMap.put(mOtherChatSessionMap.get(uid).getChatSessionInfo().getPlayUrl(), uidSurfaceMap.get(uid));
            }

            // TODO by xinye : 发起连麦/Add连麦
            if (!mVideoChatApiCalling) {

                mTipString = "开始发起/添加连麦!执行中...,请稍等";

                mVideoChatApiCalling = true;
                Log.e("xiongbo07", "开始发起连麦...");

                // 此方法暂时无用
                mChatSession.launchChat();

                mSDKHelper.startLaunchChat(mChatSession.getChatSessionInfo().getRtmpUrl(), previewSurface, mChatSession.getChatSessionInfo().getPlayUrl(), urlSurfaceMap);

            } else {
                // 发送回调显示对话框
                if (mCallback != null) {
                    Bundle data = new Bundle();
                    data.putString(DATA_KEY_PLAYER_ERROR_MSG, mTipString);
                    mCallback.onEvent(TYPE_OPERATION_CALLED_ERROR, data);
                }
            }
        }
    }

    // --------------------------------------------------------------------------------------------------------

    @Override // 结束本观众和主播的连麦，只是结束本观众和主播的连麦，其他观众和主播连麦管不着
    public void asyncTerminateChatting(final AsyncCallback callback) {

        if (mChatSession != null) {
            if (mVideoChatApiCalling) {
                // 在使用连麦的API，所以不能退出连麦
                if (mCallback != null) {
                    Bundle data = new Bundle();
                    data.putString(DATA_KEY_PLAYER_ERROR_MSG, mTipString);
                    mCallback.onEvent(TYPE_OPERATION_CALLED_ERROR, data);
                }
                return;
            }

            // 代码已经走过本方法了，修改连麦的状态为 未链接
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
         * 1、调用服务端停止连麦接口，是为了告诉别的观众和主播
         * 2、成功后调用SDK停止连麦接口，并且上报停止连麦成功的消息接口
         * 3、清空本地连麦相关信息（比如mOtherChattingViews、mOtherChattingPlayUrls）
         */
        mInviteServiceBI.leaveCall(mUID, mLiveRoomID, new ServiceBI.Callback<Object>() {

            @Override
            public void onResponse(int code, Object response) {
                // TODO by xinye : 退出连麦
                if (mChatSession != null) {
                    mTipString = "开始退出连麦!执行中...,请稍等";
                    mVideoChatApiCalling = true;
                    Log.e("xiongbo07", "开始退出连麦...");
                    // 修改连麦状态管理器中的状态
                    mChatSession.abortChat();
                }
                mSDKHelper.abortChat(); // 调用SDK中断连麦
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

    /**
     * 方法描述: 该方法暂时没人用
     */
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

    // --------------------------------------------------------------------------------------------------------

    /**
     * 方法描述: 将其他观众连麦推流成功后的uid传递给MgrCallback实例接口，在 MgrCallback 中可以通过uid获取对应的短延迟播发
     * 在MgrCallback回调中也是正式开启连麦
     */
    private void handlePublishStreamMsg(ArrayList<String> userIdList) {
        if (mCallback != null) {
            Bundle bundle = new Bundle();
            bundle.putStringArrayList(DATA_KEY_INVITEE_UID_LIST, userIdList);
            mCallback.onEvent(TYPE_OTHER_PEOPLE_JOIN_IN_CHATTING, bundle);
            Log.d(TAG, "WatchLiveActivity -->publish stream. type other people join in chatting");
        }
    }

    /**
     * 方法描述: 将别的连麦观众推流成功后的短延迟播放地址和uid存储进mOtherChatSessionMap集合中
     */
    private void addChatSession(String playUrl, String mUid) {
        ChatSession session = new ChatSession(mChatSessionCallback);
        ChatSessionInfo sessionInfo = new ChatSessionInfo();
        sessionInfo.setPlayerUID(mUid);
        sessionInfo.setPlayUrl(playUrl);
        session.setChatSessionInfo(sessionInfo);
        mOtherChatSessionMap.put(mUid, session);
    }

    // --------------------------------------------------------------------------------------------------------

    private ChatSessionCallback mChatSessionCallback = new ChatSessionCallback() {
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

    // --------------------------------------------------------------------------------------------------------

    /**
     * 方法描述: 获得与推流相关的性能参数
     */
    public AlivcPublisherPerformanceInfo getPublisherPerformanceInfo() {
//        return mSDKHelper.getPublisherPerformanceInfo();
        return new AlivcPublisherPerformanceInfo();
    }

    /**
     * 方法描述: 获得与播放相关的性能参数
     */
    public AlivcPlayerPerformanceInfo getPlayerPerformanceInfo(String url) {
//        return mSDKHelper.getPlayerPerformanceInfo(url);
        return new AlivcPlayerPerformanceInfo();
    }

    // --------------------------------------------------------------------------------------------------------

    // **************************************************** 对MNS的消息进行对应的执行操作 ****************************************************

    /**
     * 变量的描述: 观众进行连麦的时候，需要进行推流，将视频推出去，所以这里是对推流成功的消息进行处理Action
     * 备注：只作为本观众的话，这里的消息处理不需要，而如果是作为其他连麦观众中的其中一人，就需要进行处理
     */
    private ImHelper.Func<MsgDataStartPublishStream> mPublishStreamFunc = new ImHelper.Func<MsgDataStartPublishStream>() {
        @Override
        public void action(MsgDataStartPublishStream msgDataStartPublishStream) {
            // 获取推流成功的uid，该uid有可能是本观众的，也有可能是其他连麦观众的
            String uid = msgDataStartPublishStream.getUid();
            Log.d(TAG, "WatchLiveActivity -->Publish Success. " + uid);

            // 判断推流成功的 uid 是不是 本观众
            if (!mUID.equals(uid) && !uid.equals(mPublisherUID) && mChatSession != null) {
                // 本观众连麦成功了，但现有其他连麦用户推流成功
                Log.d(TAG, "WatchLiveActivity -->Publish Success. " + mChatSession.getChatStatus());

                // 1. 如果是本观众没有进行连麦，但是其他的观众进行了连麦，那么有可能进入本判断，这种情况不要管
                if (mChatSession.getChatStatus() == VideoChatStatus.UNCHAT) {
                    Log.d(TAG, "WatchLiveActivity -->Publish Success. unchat return");
                    return;
                }

                // 本观众正在进行连麦的流程的同时，有其他观众连麦推流成功了，那么我们将其他连麦观众的播放地址先进行存储
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

                // 3. 如果连麦发起中, 等待连麦发起完成再add chat

                // 存储数据，进行回调根据数据显示连麦者
                addChatSession(msgDataStartPublishStream.getPlayUrl(), uid);
                ArrayList<String> userIdList = new ArrayList<>();
                userIdList.add(uid);
                handlePublishStreamMsg(userIdList);


                Log.d(TAG, "WatchLiveActivity -->Publish Success. add chat " + uid);
            } else if (mUID.equals(uid)) {
                // 本观众正在进行连麦操作，推流成功

                // 如果在本观众进行连麦操作的时候，且没有推流成功前，有其他观众推流成功了，这时下面的代码就有意义了
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
    /**
     * 变量的描述: 连麦推流成功后，连麦过程中混流的状态码回调
     */
    private ImHelper.Func<MsgDataMixStatusCode> mMixStatusCodeFunc = new ImHelper.Func<MsgDataMixStatusCode>() {
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
                    // 如果是其他连麦人推流成功的话，改变其连麦状态
                    ChatSession session = mOtherChatSessionMap.get(msgDataMixStatusCode.getMixUid());
                    if (session != null) {
                        session.setChatStatus(VideoChatStatus.MIX_SUCC);
                    }
//                    mChatSession.notifyMixStreamSuccess();
                }
                Log.d(TAG, "Mix statusCode: " + msgDataMixStatusCode.getCode());
            }
            assert msgDataMixStatusCode != null;
            Log.d(TAG, "Mix status code:" + msgDataMixStatusCode.getCode());

        }
    };
    /**
     * 变量的描述: 被邀请连麦的人的同意连麦的消息被MNS服务端发送到了本MNS的客户端，在此处进行处理Action
     */
    private ImHelper.Func<MsgDataAgreeVideoCall> mAgreeFunc = new ImHelper.Func<MsgDataAgreeVideoCall>() {
        @Override
        public void action(final MsgDataAgreeVideoCall msgDataAgreeVideoCall) {
            // 如果超时了，那么对于主播的同意信息就不进行处理了,这是因为主播即使在10秒内同意了，但是经过业务服务器发来到观众这，已经超时了
            if (mChatSession == null || mChatSession.getChatStatus() == VideoChatStatus.UNCHAT) {
                return;
            }
            ChatSession chatSession;
            ChatSessionInfo sessionInfo;
            String inviteeUID;
            Bundle data = new Bundle();

            // ★★★★★★★★ 给在调用asyncInviteChatting方法时创建mChatSession对象添加本观众连麦时使用的推流地址和主播的短延迟播发地址信息(通过ChatSessionInfo对象)
            // 这里存储的是本观众连麦时使用的推流地址和主播的短延迟播发地址
            ChatSessionInfo chatSessionInfo = new ChatSessionInfo();
            chatSessionInfo.setRtmpUrl(msgDataAgreeVideoCall.getRtmpUrl());
            chatSessionInfo.setPlayUrl(msgDataAgreeVideoCall.getMainPlayUrl());
            // mChatSession是在调用asyncInviteChatting方法时创建的对象
            mChatSession.setChatSessionInfo(chatSessionInfo); // TODO 怎么可能此处为空？？？


            // 传递主播uid
            data.putString(DATA_KEY_INVITER_UID, mPublisherUID);


            // ★★★★★★★★ 给mCallback传递一个包含了 其他连麦观众的uid 数据的集合过去
            // 这里获取的是MNS传递过来的其他连麦观众的短延迟播放地址和其对应的uid
            List<ParterInfo> parterInfos = msgDataAgreeVideoCall.getParterInfos();
            ArrayList<String> inviteeUIDList = new ArrayList<>();
            if (parterInfos != null && parterInfos.size() > 0) {
                for (ParterInfo parterInfo : parterInfos) {
                    inviteeUID = parterInfo.getUID();
                    mUidMap.remove(inviteeUID);

                    // 将其他连麦观众的uid和对应的播放地址存储进一个ChatSessionInfo对象，在将ChatSessionInfo对象存储进一个新的ChatSession对象
                    chatSession = new ChatSession(mChatSessionCallback);// 这里用不到session的状态管理，只用到了信息缓存功能
                    sessionInfo = new ChatSessionInfo();
                    sessionInfo.setPlayerUID(inviteeUID);
                    sessionInfo.setPlayUrl(parterInfo.getPlayUrl());
                    chatSession.setChatSessionInfo(sessionInfo);

                    // 根据其他连麦观众的uid找对应的ChatSession对象
                    mOtherChatSessionMap.put(inviteeUID, chatSession);

                    inviteeUIDList.add(inviteeUID);
                }
                // 传递其他连麦观众的uid，如此可以根据uid从mOtherChatSessionMap获取对应的短延迟播放url
                data.putStringArrayList(DATA_KEY_INVITEE_UID_LIST, inviteeUIDList);
            }
            if (mCallback != null) {
                mCallback.onEvent(TYPE_START_CHATTING, data);
            }

            // ★★★★★★★★ 改变ChatSession的状态
            mChatSession.notifyParterAgreeInviting();
        }
    };
    /**
     * 变量的描述: 被邀请连麦的人的不同意连麦的消息被发送倒本MNS客户端，处理Action
     */
    private ImHelper.Func<MsgDataNotAgreeVideoCall> mNotAgreeFunc = new ImHelper.Func<MsgDataNotAgreeVideoCall>() {
        @Override
        public void action(final MsgDataNotAgreeVideoCall notAgreeVideoCall) {
            if (mChatSession != null) {
                mChatSession.notifyNotAgreeInviting(notAgreeVideoCall);
            }
        }
    };
    /**
     * 变量的描述: 混流成功的消息处理Action  这个只有在第一次进行连麦混流时才会被调用
     */
    private ImHelper.Func<MsgDataMergeStream> mMergeStreamSuccFunc = new ImHelper.Func<MsgDataMergeStream>() {
        @Override
        public void action(MsgDataMergeStream msgDataMergeStream) {
            System.out.println();
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
     * 变量的描述: 混流失败的消息处理Action
     */
    private ImHelper.Func<MsgDataMergeStream> mMergeStreamFailedFunc = new ImHelper.Func<MsgDataMergeStream>() {
        @Override
        public void action(MsgDataMergeStream msgDataMergeStream) {
//            if (mChatStatus == VideoChatStatus.TRY_MIX) {
//                mView.showToast(R.string.merge_stream_failed);
//            }
            System.out.println();
        }
    };
    /**
     * 变量的描述: 观众自己或者主播指定观众退出连麦时(这里的观众可以自己，也可以是其他连麦的观众)，消息处理Action
     */
    private ImHelper.Func<MsgDataExitChatting> mExitingChattingFunc = new ImHelper.Func<MsgDataExitChatting>() {
        @Override
        public void action(MsgDataExitChatting msgDataExitChatting) {
            Log.d(TAG, "Someone exit chatting");
            // 获取退出连麦的人的uid
            String inviteeUID = msgDataExitChatting.getUID();
            if (!mUID.equals(inviteeUID)) {// 其他人退出连麦
                // 如果本观众正在发起连麦,而这个时候其他观众退出连麦,不需要处理此消息
                if (mChatSession == null || (mChatSession.getChatStatus() != VideoChatStatus.MIX_SUCC && mChatSession.getChatStatus() != VideoChatStatus.TRY_MIX)) {
                    if (mChatSession != null)
                        Log.d(TAG, "chat session status = " + mChatSession.getChatStatus());
                    return;
                }

                // 从mOtherChatSessionMap移除对应退出连麦的uid,并将uid对应的播放地址存储进playUrls
                ChatSession chatSession;
                List<String> playUrls = new ArrayList<>();
                if ((chatSession = mOtherChatSessionMap.get(inviteeUID)) != null) {
                    playUrls.add(chatSession.getChatSessionInfo().getPlayUrl());
                    mOtherChatSessionMap.remove(inviteeUID);
                }
                // TODO by xinye : 其他观众退出连麦
                mVideoChatApiCalling = true;
                Log.e("xiongbo07", "开始Remove连麦...");
                int result = mSDKHelper.removeChats(playUrls);// 调用核心代码移除连麦
                if (result != 0) {
                    // 核心代码调用失败
                    mVideoChatApiCalling = false;
                } else {
                    // 核心代码调用成功
                    if (mCallback != null) {
                        Bundle data = new Bundle();
                        data.putString(DATA_KEY_INVITEE_UID, inviteeUID);
                        mCallback.onEvent(TYPE_OTHER_PEOPLE_EXIT_CHATTING, data);
                    }
                }
            } else {//自己退出连麦
                // TODO by xinye : 退出连麦
                // 下面的代码在asyncTerminateChatting方法中就已经做过了
                mSDKHelper.abortChat();
                mChatSession = null;
                if (mCallback != null) {
                    mCallback.onEvent(TYPE_SELF_EXIT_CHATTING, null);
                }
            }
        }
    };
    /**
     * 变量的描述: 结束所有正在连麦的时候，消息处理Action 这里是在主播端直接请求业务服务器结束所有连麦时，这里会接收到消息，其实质是让所有的连麦观众客户端代码中直接关闭自己的连麦
     */
    private ImHelper.Func<MsgDataCloseVideoCall> mCloseChatFunc = new ImHelper.Func<MsgDataCloseVideoCall>() {
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
     * 变量的描述: 观众观看直播时，主播结束直播的时候，消息处理Action
     */
    private ImHelper.Func<MsgDataLiveClose> mLiveCloseFunc = new ImHelper.Func<MsgDataLiveClose>() {
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
     * 变量的描述: 观众观看的主播对本观众发起了连麦邀请的时候，消息处理Action
     */
    private ImHelper.Func<MsgDataInvite> mInviteFunc = new ImHelper.Func<MsgDataInvite>() {
        @Override
        public void action(final MsgDataInvite msgDataInvite) {
            mUidMap.clear();
            mChatSession = new ChatSession(mChatSessionCallback);
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
     * 方法描述: 当本观众被主播邀请时，需要将观众是否进行连麦的结果反馈给业务服务器
     *
     * @param publisherUID 主播ID，也是邀请本观众进行连麦的id
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

                        // 更新连麦状态为开始混流， 等待混流成功
                        mChatSession.notifyFeedbackSuccess();

                        // 设置 这里存储的是本观众连麦时使用的推流地址和主播的短延迟播发地址
                        ChatSessionInfo chatSessionInfo = new ChatSessionInfo();
                        chatSessionInfo.setRtmpUrl(response.getRtmpUrl());
                        chatSessionInfo.setPlayUrl(response.getMainPlayUrl());
                        mChatSession.setChatSessionInfo(chatSessionInfo);

                        //  给mCallback传递一个包含了 其他连麦观众的uid 数据的集合过去
                        data.putString(DATA_KEY_INVITER_UID, publisherUID);
                        List<ParterInfo> parterInfos = response.getOtherParterInfos();
                        if (parterInfos != null && parterInfos.size() > 0) {
                            for (ParterInfo parterInfo : parterInfos) {
                                inviteeUID = parterInfo.getUID();
                                mUidMap.remove(inviteeUID);
                                chatSession = new ChatSession(mChatSessionCallback);//这里用不到session的状态管理，只用到了信息缓存功能
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
                            System.out.println();
                        }
                        if (mCallback != null) {
                            //TODO：收到邀请后，反馈失败了
                            System.out.println();
                        }
                    }
                });
    }

    //  --------------------------------------------------------------------------------------------------------

    // **************************************************** 错误和信息监听器 ****************************************************

    /**
     * 变量的描述: 播放器错误回调处理，错误监听器
     */
    AlivcVideoChatParter.OnErrorListener mPlayerErrorListener = new AlivcVideoChatParter.OnErrorListener() {
        /**
         * @param what what为错误代码
         */
        @Override
        public boolean onError(IVideoChatParter iVideoChatParter, int what, String url) {
            Log.d(TAG, "WatchLiveActivity-->error what = " + what + ", url = " + url);

            if (what == 0) {// 错误代码中没有0
                return false;
            }
            // TODO 做什么？
            if (mChatSession != null) {
                mTipString = null;
                // mChatSession.setOperationCompleted();
            }

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
                case MediaError.ALIVC_ERR_PLAYER_UNSUPPORTED:// 播放不支持的解码
                case MediaError.ALIVC_ERR_PLAYER_UNKNOW:// 播放出现未知的错误？？？？？？
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
                case MediaError.ALIVC_ERR_PUBLISHER_AUDIO_CAPTURE_DISABLED:// 音频采集失败。音频被禁止
                case MediaError.ALIVC_ERR_PUBLISHER_AUDIO_CAPTURE_NO_DATA:// 音频采集出错
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
                case MediaError.ALIVC_ERR_PUBLISHER_VIDEO_CAPTURE_NO_DATA:// 视频采集出错
                case MediaError.ALIVC_ERR_PUBLISHER_VIDEO_CAPTURE_DISABLED:// 视频被禁止
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
                case MediaError.ALIVC_ERR_PUBLISHER_ENCODE_AUDIO_FAILED:// 音频编码失败
                case MediaError.ALIVC_ERR_PUBLISHER_AUDIO_ENCODER_INIT_FAILED:// 音频初始化失败
                case MediaError.ALIVC_ERR_PUBLISHER_MALLOC_FAILED:// 内存分配失败
                case MediaError.ALIVC_ERR_PUBLISHER_ENCODE_VIDEO_FAILED:// 视频编码失败
                case MediaError.ALIVC_ERR_PUBLISHER_VIDEO_ENCODER_INIT_FAILED:// 视频初始化失败
                case MediaError.ALIVC_ERR_PUBLISHER_ILLEGAL_ARGUMENT:// 无效的参数
                    // mView.showLiveInterruptUI(R.string.network_busy, what);
                    break;
                case MediaError.ALIVC_ERR_PUBLISHER_NETWORK_POOR:// 网络较慢
                    if (mCallback != null) {
                        mCallback.onEvent(TYPE_PUBLISHER_NETWORK_POOR, null);
                    }
                    break;
                case MediaError.ALIVC_ERR_PUBLISHER_NETWORK_UNCONNECTED:// 网络未连接
                    if (mCallback != null) {
                        mCallback.onEvent(TYPE_PUBLISHER_NETWORK_UNCONNECT, null);
                    }
                    break;
                case MediaError.ALIVC_ERR_PUBLISHER_SEND_DATA_TIMEOUT:// 发送数据超时
                    if (mCallback != null) {
                        mCallback.onEvent(TYPE_PUBLISHER_NETWORK_TIMEOUT, null);
                    }
                    break;
                case MediaError.ALIVC_ERR_PLAYER_AUDIO_PLAY:// 音频播放错误
                    if (mCallback != null) {
                        Bundle data = new Bundle();
                        data.putInt(DATA_KEY_PLAYER_ERROR_CODE, what);
                        mCallback.onEvent(TYPE_PLAYER_AUDIO_PLAYER_ERROR, data);
                    }
                    break;
                case MediaError.ALIVC_ERR_MEMORY_POOR:// 内存不够
                    break;
                case MediaError.ALIVC_ERR_PUBLISHER_OPEN_FAILED:// 推流连接失败
                    break;
                case MediaError.ALIVC_ERR_PUBLISHER_VIDEO_CAPTURE_FPS_SLOW:// 音频采集较慢
                    break;
                default:
                    // mView.showLiveInterruptUI(R.string.error_unknown, what);
            }
            return true;
        }
    };

    /**
     * 变量的描述: 播放器状态回调处理，信息监听器：what为信息代码
     */
    AlivcVideoChatParter.OnInfoListener mPlayerInfoListener = new AlivcVideoChatParter.OnInfoListener() {

        @Override
        public boolean onInfo(IVideoChatParter iVideoChatParter, int what, String url) {
            Log.d(TAG, "WatchLiveActivity-->info what = " + what + ", url = " + url);
            switch (what) {
                case MediaPlayer.MEDIA_INFO_UNKNOW:// 未知
                    break;
                case MediaError.ALIVC_INFO_PLAYER_FIRST_FRAME_RENDERED:// 播放首帧显示
                    // 首帧显示时间
                    if (mCallback != null) {
                        mCallback.onEvent(TYPE_PLAYER_FIRST_FRAME_RENDER_SUCCESS, null);
                        mCallback.onEvent(TYPE_PARTER_OPT_END, null);
                    }
                    break;
                case MediaError.ALIVC_INFO_PUBLISH_DISPLAY_FIRST_FRAME:// 推流首次显示通知
                    break;
                case MediaError.ALIVC_INFO_PUBLISH_NETWORK_GOOD:// 推流网络较好
                case MediaError.ALIVC_INFO_PUBLISH_RECONNECT_START:// 重连开始
                case MediaError.ALIVC_INFO_PUBLISH_RECONNECT_SUCCESS:// 重连成功
                    break;
                case MediaError.ALIVC_INFO_PUBLISH_RECONNECT_FAILURE:// 重连失败
                    if (mCallback != null) {
                        Bundle data = new Bundle();
                        data.putInt(DATA_KEY_PUBLISHER_INFO_CODE, what);
                        mCallback.onEvent(TYPE_PUBLISHER_RECONNECT_FAILURE, data);
                    }
                    break;
                case MediaError.ALIVC_INFO_PLAYER_PREPARED_PROCESS_FINISHED:// 播放准备完成通知
                    mReconnectCount = 0;
                    break;
                case MediaError.ALIVC_INFO_PLAYER_INTERRUPT_PLAYING:// 播放被中断
                case MediaError.ALIVC_INFO_PLAYER_STOP_PROCESS_FINISHED:// 播放结束通知
                    break;
                case MediaError.ALIVC_INFO_ONLINE_CHAT_END:// onlineChat结束
                    mTipString = null;
                    mVideoChatApiCalling = false;
                    Log.e("xiongbo07", "发起连麦成功...");
                    break;
                case MediaError.ALIVC_INFO_OFFLINE_CHAT_END:// offlineChat结束
                    mTipString = null;
                    mVideoChatApiCalling = false;
                    Log.e("xiongbo07", "退出连麦成功...");
                    break;
                case MediaError.ALIVC_INFO_ADD_CHAT_END:// addChat结束
                    mTipString = null;
                    mVideoChatApiCalling = false;
                    Log.e("xiongbo07", "ADD连麦成功...");
                    break;
                case MediaError.ALIVC_INFO_REMOVE_CHAT_END:// removeChat结束
                    mTipString = null;
                    mVideoChatApiCalling = false;
                    Log.e("xiongbo07", "Remove连麦成功...");
                    break;
                case MediaError.ALIVC_INFO_PLAYER_NETWORK_POOR:// 播放器网络差，不能及时下载数据包
                    if (mCallback != null) {
                        Bundle data = new Bundle();
                        data.putString(IPublisherMgr.DATA_KEY_PLAYER_ERROR_MSG, url);
                        mCallback.onEvent(TYPE_PLAYER_NETWORK_POOR, data);
                    }
                    break;
                case MediaError.ALIVC_INFO_LAUNCH_CHAT_END:// launchChat结束
                    break;
                case MediaError.ALIVC_INFO_ABORT_CHAT_END:// abortChat结束
                    break;
                case MediaError.ALIVC_INFO_PUBLISH_START_SUCCESS:// 推流开始成功
                    break;
                case MediaError.ALIVC_INFO_PLAYER_BUFFERING_START:// 播放缓冲开始
                    //                        if (!isLoading) {
//                            mHandler.postDelayed(mShowInterruptRun, INTERRUPT_DELAY);
//                            isLoading = true;
//                        }
                    break;
                case MediaError.ALIVC_INFO_PLAYER_BUFFERING_END:// 播放缓冲结束
                    //                        if (isLoading) {
//                            mHandler.removeCallbacks(mShowInterruptRun);
//                             结束缓冲
//                            mView.hideLiveInterruptUI();
//                            isLoading = false;
//                        }
                    break;

            }
            Log.d(TAG, "MediaPlayer onInfo, what =" + what + ", url = " + url);
            return false;
        }
    };
}
