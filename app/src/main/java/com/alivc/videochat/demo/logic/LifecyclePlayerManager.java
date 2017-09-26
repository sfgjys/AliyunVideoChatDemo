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
import com.alivc.videochat.demo.http.result.InviteFeedbackResult;
import com.alivc.videochat.demo.http.result.MNSConnectModel;
import com.alivc.videochat.demo.http.result.MNSModel;
import com.alivc.videochat.demo.http.result.MixStatusCode;
import com.alivc.videochat.demo.http.result.WatchLiveResult;
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
public class LifecyclePlayerManager extends ContextBase implements IPlayerManager, ILifecycleListener {
    private static final String TAG = LifecyclePlayerManager.class.getName();
    /**
     * 变量的描述: 观众端与主播进行连麦的时候，其连麦流程是:未链接-->向服务器发送邀请主播进行连麦的请求(观众收到邀请)-->开始连麦等待混流成功-->混流成功
     * 这个连麦流程中，当本观众的连麦不再是未链接的状态时，MNS收到其他观众连麦推流成功的消息时，就会将其对应的播放地址存储进mUidMap集合中。
     * 当本观众连麦的时候，走到主播同意进行连麦或者是本观众同意主播的邀请的步骤时，会获得自己的推流地址以及主播的播放地址，还有其他已经正在连麦的观众播放地址，
     * 这时，我们就需要对mUidMap集合中的播放地址数据进行修改了，移除已经存在的播放地址。
     * 当本观众拿着推流地址和已经连麦的观众播放地址去连麦，且暂时没有推流成功的这段时间，又有其他观众连麦成功了，mUidMap集合存储了播放地址，
     * 当本观众推流成功了，这时就可以从mUidMap集合中获取新存储的播放地址，去调用连麦的核心代码---添加连麦
     */
    private Map<String, String> mUidMap = new HashMap<>();
    /**
     * 变量的描述: 连麦播放核心SDK调用类
     */
    private PlayerSDKHelper mPlayerSDKHelper;
    /**
     * 变量的描述: MNS管理类(初始化，链接服务器，注册和注销通知)
     */
    private ImManager mImManager;
    /**
     * 变量的描述: 登录用户的id(本观众的id)
     */
    private String mUID;
    /**
     * 变量的描述: 播放连麦这一模块结果回调接口实例
     */
    private ManagerCallback mManagerCallback;
    /**
     * 变量的描述: MNS链接服务器需要的参数
     */
    private MnsControlBody mMnsControlBody;
    /**
     * 变量的描述: MNS链接服务器需要的参数
     */
    private WebSocketConnectOptions mWSConnOpt;
    /**
     * 变量的描述: 是否正在走关于连麦的API流程
     */
    private boolean mVideoChatApiCalling = false;
    /**
     * 变量的描述: 直播间ID（在获取直播列表的时候，每个列表条目都包含了其代表的直播间的id，在点击观看的时候就传递到了观看界面中）
     */
    private String mLiveRoomID;
    /**
     * 变量的描述: 调用获取直播的播放地址方法，调用请求网络告诉业务服务器本观众退出直播间了的方法
     */
    private LiveServiceBI mLiveServiceBI = ServiceBIFactory.getLiveServiceBI();
    /**
     * 变量的描述: 邀请主播进行连麦；回答是否接受主播连麦的邀请；断开与主播正在进行的连麦
     */
    private InviteServiceBI mInviteServiceBI = ServiceBIFactory.getInviteServiceBI();
    /**
     * 变量的描述: 本观众与主播进行连麦的连麦流程会话对象
     */
    private ChatSession mChatSession;
    /**
     * 变量的描述: 存储其他参与连麦的观众的连麦流程会话对象
     */
    private HashMap<String, ChatSession> mOtherChatSessionMap = new HashMap<>();
    /**
     * 变量的描述: 获取主播直播的播放地址的网络请求
     */
    private Call mGetMainAnchorPlayUrlCall;
    /**
     * 变量的描述: 邀请主播进行连麦的网络请求
     */
    private Call mInviteChatCall;
    /**
     * 变量的描述: 回答是否接受主播连麦的网络请求
     */
    private Call mFeedbackCall;
    /**
     * 变量的描述: 从业务服务器中获取的主播播放地址
     */
    private String mPlayUrl;
    /**
     * 变量的描述: 主播ID（在获取主播播放地址的同时，也获取了主播的id）
     */
    private String mPublisherUID;
    /**
     * 变量的描述: 用于播放主播直播的SurfaceView控件
     */
    private SurfaceView mHostPlaySurf;
    /**
     * 变量的描述: 用于提示连麦流程中使用了什么连麦API
     */
    private String mUseChatApiString;


    private boolean isLoading = false;  //是否正在缓冲


    // --------------------------------------------------------------------------------------------------------

    public LifecyclePlayerManager(Context context, ImManager imManager, String uid, ManagerCallback callback) {
        super(context);
        this.mPlayerSDKHelper = new PlayerSDKHelper();
        this.mImManager = imManager;
        this.mUID = uid;
        this.mManagerCallback = callback;
    }

    // --------------------------------------------------------------------------------------------------------

    @Override
    public void onCreate() {
        Context context = getContext();
        if (context != null)
            mPlayerSDKHelper.initPlayer(context, mPlayerErrorListener, mPlayerInfoListener, mManagerCallback);  //初始化播放器
    }

    @Override
    public void onStart() {
    }

    @Override
    public void onResume() {
        // 第一次进入本界面不会进入判断里，mWSConnOpt和mMnsControlBody在请求网络获取主播播放地址的时候才会有值
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
        mPlayerSDKHelper.resume();
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

        // 注销消息
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
        // 断开MNS与服务器的链接
        mImManager.closeSession();

        // 方法内部会判断是否正在播放，是否已经暂停过
        mPlayerSDKHelper.pause(); //暂停播放 or 连麦
    }

    @Override
    public void onStop() {
    }

    @Override
    public void onDestroy() {
        if (mChatSession != null) {  // 当前正在连麦
            mInviteServiceBI.leaveCall(mUID, mLiveRoomID, null);
            mChatSession = null;
            mOtherChatSessionMap.clear();
        }
        // 请求网络告诉业务服务器本观众退出直播间了
        mLiveServiceBI.exitWatching(mLiveRoomID, mUID, null);

        mPlayerSDKHelper.abortChat();         // 防止因为某些原因没有停止连麦的情况，再次调用一次停止连麦
        mPlayerSDKHelper.stopPlaying();       // 防止因为某些原因没有停止播放的情况，再次调用一次停止播放
        mPlayerSDKHelper.releaseChatParter(); // 释放播放器资源
    }

    // --------------------------------------------------------------------------------------------------------

    @Override // 进入观看界面的时候，就请求网络获取播放地址，并且建立MNS链接，然后注册订阅
    public void asyncEnterLiveRoom(String liveRoomID, final AsyncCallback callback) {
        this.mLiveRoomID = liveRoomID;
        // 防止重复请求网络
        if (mGetMainAnchorPlayUrlCall != null && ServiceBI.isCalling(mGetMainAnchorPlayUrlCall)) {
            mGetMainAnchorPlayUrlCall.cancel();
            mGetMainAnchorPlayUrlCall = null;
        }
        // 请求网络
        mGetMainAnchorPlayUrlCall = mLiveServiceBI.watchLive(liveRoomID, mUID, new ServiceBI.Callback<WatchLiveResult>() {
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

                // --------------------------------------------------------------------------------------------------------

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
        mPlayerSDKHelper.startToPlay(mPlayUrl, mHostPlaySurf);
        Log.d(TAG, "用于播放主播直播的SurfaceView控件被创建了");
    }

    @Override
    public void switchCamera() {
        mPlayerSDKHelper.switchCamera();
    }

    @Override
    public boolean switchBeauty() {
        return mPlayerSDKHelper.switchBeauty();
    }

    @Override
    public boolean switchFlash() {
        return mPlayerSDKHelper.switchFlash();
    }

    @Override // 请求网络，告诉业务服务器观众要邀请主播进行连麦
    public void asyncInviteChatting(final AsyncCallback callback) throws ChatSessionException {
        mUidMap.clear();

        // 如果观众的连麦的状态管理器ChatSession的isActive方法返回true，则代表观众正在进行与主播的连麦（or 邀请）
        if (mChatSession != null && mChatSession.isActive()) {//
            if (mManagerCallback != null) {
                Bundle data = new Bundle();
                data.putString(DATA_KEY_PLAYER_ERROR_MSG, mUseChatApiString);
                mManagerCallback.onEvent(TYPE_OPERATION_CALLED_ERROR, data);
                // 上面的回调是使用对话框显示异常原因，而这里则是抛出异常显示原因
                throw new ChatSessionException(ChatSessionException.ERROR_CURR_CHATTING);
            }
        }

        // 存储被邀请人的用户ID，也就是主播的id，其实就一个主播id，集合没有意义，但是核心sdk需要
        List<String> inviteeUIDs = new ArrayList<>();
        // 将主播的uid添加进uid集合中。这里只有主播是被邀请
        inviteeUIDs.add(mPublisherUID);

        // 清空连麦请求网络任务，防止重复请求网络
        if (mInviteChatCall != null && ServiceBI.isCalling(mInviteChatCall)) {
            mInviteChatCall.cancel();
            mInviteChatCall = null;
        }

        mChatSession = new ChatSession(mChatSessionCallback);
        mChatSession.invite(mPublisherUID, mUID);
        // 请求网络去邀请别人进行连麦，业务服务器发送请求给被邀请人，被邀请人的结果发给业务服务器，业务服务器在通过MNS发送给邀请人也就是本用户
        mInviteChatCall = mInviteServiceBI.inviteCall(mUID, inviteeUIDs, InviteForm.TYPE_PIC_BY_PIC, FeedbackForm.INVITE_TYPE_WATCHER, mLiveRoomID, new ServiceBI.Callback<Object>() {
            @Override
            public void onResponse(int code, Object response) {// 告诉业务服务器观众要邀请主播进行连麦的消息 成功

                mUseChatApiString = "当前观众正在进行与主播的连麦（邀请主播来连麦）";

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
    public void launchChat(SurfaceView previewSurface, Map<String, SurfaceView> otherUidSurfaceMap) {
        if (mChatSession != null) {
            // 使用参数二uidSurfaceMap和本类变量mOtherChatSessionMap，存储一个对应了其他连麦观众的播放地址和Surface到集合中
            Map<String, SurfaceView> urlSurfaceMap = new HashMap<>();
            Iterator<String> uids = otherUidSurfaceMap.keySet().iterator();
            String uid;
            while (uids.hasNext()) {
                uid = uids.next();
                urlSurfaceMap.put(mOtherChatSessionMap.get(uid).getChatSessionInfo().getPlayUrl(), otherUidSurfaceMap.get(uid));
            }

            if (!mVideoChatApiCalling) {

                // 这里代表开始调用onlineChat方法，当onlineChats方法调用完后会修改mVideoChatApiCalling为false
                mVideoChatApiCalling = true;
                Log.e(TAG + "---API", "开始发起连麦...");

                mUseChatApiString = "正在进行连麦中(调用了onlineChats方法)";

                // 在观众端中ChatSessionInfo的推流地址是给观众用的，ChatSessionInfo的播放地址是主播直播的播放地址
                mPlayerSDKHelper.startLaunchChat(mChatSession.getChatSessionInfo().getRtmpUrl(), previewSurface, mChatSession.getChatSessionInfo().getPlayUrl(), urlSurfaceMap);
            } else {
                // 发送回调显示异常对话框
                if (mManagerCallback != null) {
                    Bundle data = new Bundle();
                    data.putString(DATA_KEY_PLAYER_ERROR_MSG, mUseChatApiString);
                    mManagerCallback.onEvent(TYPE_OPERATION_CALLED_ERROR, data);
                }
            }
        }
    }

    // --------------------------------------------------------------------------------------------------------

    @Override // 结束本观众和主播的连麦，只是结束本观众和主播的连麦，其他观众和主播连麦管不着
    public void asyncTerminateChatting(final AsyncCallback callback) {

        if (mChatSession != null) {// 正在连麦流程中
            if (mVideoChatApiCalling) {
                // 在使用连麦的API，所以不能退出连麦
                if (mManagerCallback != null) {
                    Bundle data = new Bundle();
                    data.putString(DATA_KEY_PLAYER_ERROR_MSG, mUseChatApiString);
                    mManagerCallback.onEvent(TYPE_OPERATION_CALLED_ERROR, data);
                }
                return;
            }

            // 代码已经走过本方法了，已经修改连麦的状态为 未链接
            if (!mChatSession.isActive()) {
                if (mManagerCallback != null) {
                    Bundle data = new Bundle();
                    data.putString(DATA_KEY_PLAYER_ERROR_MSG, "请耐心等待退出连麦过程执行完,不要重复点击退出连麦");
                    mManagerCallback.onEvent(TYPE_OPERATION_CALLED_ERROR, data);
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
                if (mChatSession != null) {
                    // 这里代表开始调用offlineChat方法，当offlineChat方法调用完后会修改mVideoChatApiCalling为false
                    mVideoChatApiCalling = true;
                    Log.e(TAG + "---API", "开始退出连麦...");
                    // 修改连麦状态管理器中的状态
                    mChatSession.abortChat();
                }
                mUseChatApiString = "正在退出连麦(调用了offlineChat方法)";
                mPlayerSDKHelper.abortChat(); // 调用SDK中断连麦
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

                // 显示请求网络失败的原因
                if (mManagerCallback != null) {
                    Bundle data = new Bundle();
                    data.putString(DATA_KEY_PLAYER_ERROR_MSG, t.getLocalizedMessage());
                    mManagerCallback.onEvent(TYPE_OPERATION_CALLED_ERROR, data);
                }
            }
        });
    }

    @Override
    public void asyncTerminatePlaying(final AsyncCallback callback) {
        // 调用服务端停止播放接口
        mPlayerSDKHelper.stopPlaying();
    }

    /**
     * 方法描述: 该方法暂时没人用
     */
    @Override
    public void asyncExitRoom(final AsyncCallback callback) {
        mLiveServiceBI.exitWatching(mLiveRoomID, mUID, new ServiceBI.Callback<Object>() {
            @Override
            public void onResponse(int code, Object response) {
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
     * 方法描述: 将其他观众连麦推流成功后的uid传递给MgrCallback实例接口，在 ManagerCallback 中可以通过uid获取对应的短延迟播发
     * 在MgrCallback回调中也是正式开启连麦
     * 注意: 首先本法调用的前提是: 本观众已经调用了正式连麦的方法(startLaunchChat),就等着推流成功了，
     * 两种情况:1、在本观众推流成功前没有其他连麦观众推流成功。如此当有其他连麦的观众调用本方法，开启正式正式连麦的方法(startLaunchChat)，其实其内部调用的时添加连麦的放
     * 2、在本观众推流成功前有其他连麦观众推流成功。如此这些先一步推流成功的连麦观众，我们先保存其播放地址，当本观众连麦成功后，在开启正式正式连麦的方法(startLaunchChat)，其实其内部调用的时添加连麦的放
     */
    private void handlePublishStreamMsg(ArrayList<String> userIdList) {
        if (mManagerCallback != null) {
            Bundle bundle = new Bundle();
            bundle.putStringArrayList(DATA_KEY_INVITEE_UID_LIST, userIdList);
            mManagerCallback.onEvent(TYPE_OTHER_PEOPLE_JOIN_IN_CHATTING, bundle);
            Log.d(TAG, "WatchLiveActivity -->本观众连麦推流成功，展示其他连麦观众的播放");
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

    /**
     * 方法描述: 获得与推流相关的性能参数
     */
    public AlivcPublisherPerformanceInfo getPublisherPerformanceInfo() {
//        return mPlayerSDKHelper.getPublisherPerformanceInfo();
        return new AlivcPublisherPerformanceInfo();
    }

    /**
     * 方法描述: 获得与播放相关的性能参数
     */
    public AlivcPlayerPerformanceInfo getPlayerPerformanceInfo(String url) {
//        return mPlayerSDKHelper.getPlayerPerformanceInfo(url);
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
            Log.d(TAG, "WatchLiveActivity -->出现推流成功的ID: " + uid);

            // 判断推流成功的 uid 是不是 本观众
            if (!mUID.equals(uid) && !uid.equals(mPublisherUID) && mChatSession != null) {
                Log.d(TAG, "WatchLiveActivity -->出现推流成功，本观众的连麦流程状态为: " + mChatSession.getChatStatus());

                // 1. 如果是本观众没有进行连麦，但是其他的观众进行了连麦，那么有可能进入本判断，这种情况不要管
                if (mChatSession.getChatStatus() == VideoChatStatus.UNCHAT) {
                    Log.d(TAG, "WatchLiveActivity -->出现推流成功,但是本观众没有进行连麦流程");
                    return;
                }

                // 本观众正在进行连麦的流程的同时(但是本观众还没有连麦成功，也没有尝试混流)，
                // 有其他观众连麦推流成功了，那么我们将其他连麦观众的播放地址先进行存储，然后退出
                // TODO 这里的判断有一定的问题
                if (mChatSession.getChatStatus() != VideoChatStatus.MIX_SUCC && mChatSession.getChatStatus() != VideoChatStatus.TRY_MIX) {
                    // 进入队列
                    mUidMap.put(uid, msgDataStartPublishStream.getPlayUrl());
                    Log.d(TAG, "WatchLiveActivity -->出现推流成功,将推流成功的信息先进行存储");
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
                // 代码走到这里说明，本观众已经连麦成功了。现在有其他的观众连麦推流成功，需要我们将连麦成功的其他观众显示出来
                addChatSession(msgDataStartPublishStream.getPlayUrl(), uid);
                ArrayList<String> userIdList = new ArrayList<>();
                userIdList.add(uid);
                handlePublishStreamMsg(userIdList);

                Log.d(TAG, "WatchLiveActivity -->出现推流成功,将推流成功的添加进连麦，该连麦的ID: " + uid);
            } else if (mUID.equals(uid)) {
                // 本观众正在进行连麦操作，推流成功

                // 如果在本观众进行连麦操作的时候，且没有推流成功前，有其他观众推流成功了，这时下面的代码就有意义了
                // 先将先我们一步推流成功的其他连麦观众存储进mOtherChatSessionMap，这样本观众的连麦就可以显示比本观众早推流成功的播放了
                ArrayList<String> userIdList = new ArrayList<>();
                for (String userId : mUidMap.keySet()) {
                    userIdList.add(userId);
                    Log.d(TAG, "WatchLiveActivity -->本观众推流成功,添加后来推流程成功的其他连麦观众的信息: " + userId);
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
                Log.d(TAG, "混流状态码: " + msgDataMixStatusCode.getCode());
            }
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
            // 存储其他已经连麦成功的观众的UID的集合
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
            if (mManagerCallback != null) {
                mManagerCallback.onEvent(TYPE_START_CHATTING, data);
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
        }
    };
    /**
     * 变量的描述: 混流失败的消息处理Action
     */
    private ImHelper.Func<MsgDataMergeStream> mMergeStreamFailedFunc = new ImHelper.Func<MsgDataMergeStream>() {
        @Override
        public void action(MsgDataMergeStream msgDataMergeStream) {
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
                // 这里代表的是使用的了SDK的核心api---removeChats方法
                mVideoChatApiCalling = true;
                Log.e(TAG + "---API", "开始Remove连麦...");
                int result = mPlayerSDKHelper.removeChats(playUrls);// 调用核心代码移除连麦
                if (result != 0) {
                    // 核心代码调用失败
                    mVideoChatApiCalling = false;
                } else {
                    // 核心代码调用成功
                    if (mManagerCallback != null) {
                        Bundle data = new Bundle();
                        data.putString(DATA_KEY_INVITEE_UID, inviteeUID);
                        mManagerCallback.onEvent(TYPE_OTHER_PEOPLE_EXIT_CHATTING, data);
                    }
                }
            } else {//自己退出连麦
                mPlayerSDKHelper.abortChat();
                mChatSession = null;
                if (mManagerCallback != null) {
                    mManagerCallback.onEvent(TYPE_SELF_EXIT_CHATTING, null);
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
            // 这里的结束所有正在连麦，其实就是让本观众自己的连麦退出
            if (mChatSession != null)
                mChatSession.abortChat();
            mPlayerSDKHelper.abortChat(); //结束连麦
            if (mManagerCallback != null) {
                mManagerCallback.onEvent(TYPE_PUBLISHER_TERMINATE_CHATTING, null);
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
                mPlayerSDKHelper.abortChat();
                mChatSession = null;
                mOtherChatSessionMap.clear();
            }
            if (mManagerCallback != null) {
                mManagerCallback.onEvent(TYPE_LIVE_CLOSE, null);
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

        // 通过网络请求告诉业务服务器本观众同意与主播进行连麦
        mFeedbackCall = mInviteServiceBI.feedback(FeedbackForm.INVITE_TYPE_WATCHER, FeedbackForm.INVITE_TYPE_ANCHOR, publisherUID, mUID, InviteForm.TYPE_PIC_BY_PIC, FeedbackForm.STATUS_AGREE, new ServiceBI.Callback<InviteFeedbackResult>() {
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
                if (mManagerCallback != null) {
                    mManagerCallback.onEvent(TYPE_START_CHATTING, data);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                if (mChatSession != null) {
                    System.out.println();
                }
                if (mManagerCallback != null) {
                    // TODO 收到邀请后，反馈失败了
                    System.out.println();
                }
            }
        });
    }

    //  --------------------------------------------------------------------------------------------------------

    private ChatSessionCallback mChatSessionCallback = new ChatSessionCallback() {
        @Override
        public void onInviteChatTimeout() {
            // 发起连麦邀请之后，10秒之内还收不到反馈，则按照超时处理，认为对方已经拒绝
            mVideoChatApiCalling = false;

            if (mChatSession != null) {
                mChatSession.notifyNotAgreeInviting(null);
            }
            if (mManagerCallback != null) {
                mManagerCallback.onEvent(TYPE_INVITE_CHAT_TIMEOUT, null);
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
            if (mManagerCallback != null) {
                mManagerCallback.onEvent(TYPE_MIX_STREAM_ERROR, null);
            }
        }

        @Override
        public void onMixStreamTimeout() {
            mVideoChatApiCalling = false;
            if (mManagerCallback != null) {
                mManagerCallback.onEvent(TYPE_MIX_STREAM_TIMEOUT, null);
            }
        }

        @Override
        public void onMixStreamSuccess() {
            mVideoChatApiCalling = false;
            if (mManagerCallback != null) {
                mManagerCallback.onEvent(TYPE_MIX_STREAM_SUCCESS, null);
            }
        }

        @Override
        public void onMixStreamNotExist() {
            mVideoChatApiCalling = false;
            if (mManagerCallback != null) {
                mManagerCallback.onEvent(TYPE_MIX_STREAM_NOT_EXIST, null);
            }
        }

        @Override
        public void onMainStreamNotExist() {
            mVideoChatApiCalling = false;
            if (mManagerCallback != null) {
                mManagerCallback.onEvent(TYPE_MAIN_STREAM_NOT_EXIST, null);
            }
        }
    };

    // **************************************************** 错误和信息监听器 ****************************************************
    /**
     * 变量的描述: 重连计数
     */
    private int mReconnectCount = 0;
    /**
     * 变量的描述: 最大的重连计数
     */
    private static final int MAX_RECONNECT_COUNT = 10;
    /**
     * 变量的描述: 播放器错误回调处理，错误监听器
     */
    AlivcVideoChatParter.OnErrorListener mPlayerErrorListener = new AlivcVideoChatParter.OnErrorListener() {
        /**
         * @param what what为错误代码
         */
        @Override
        public boolean onError(IVideoChatParter iVideoChatParter, int what, String url) {
            Log.d(TAG, "WatchLiveActivity在调用连麦播放核心SDK的时候出现的错误码为: " + what + ", 播放错误的播放地址 = " + url);

            if (what == 0) {// 错误代码中没有0
                return false;
            }
            // TODO 做什么？
            if (mChatSession != null) {
                mUseChatApiString = null;
            }

            switch (what) {
                // 下面几个错误都是先进行重连，然后弹吐司
                case MediaError.ALIVC_ERR_PLAYER_INVALID_INPUTFILE:
                    Log.d(TAG, "播放无效的输入流");
                    if (mReconnectCount++ < MAX_RECONNECT_COUNT)
                        mPlayerSDKHelper.reconnect(url);
                    if (mManagerCallback != null) {
                        mManagerCallback.onEvent(TYPE_PLAYER_INVALID_INPUTFILE, null);
                    }
                    break;
                case MediaError.ALIVC_ERR_PLAYER_OPEN_FAILED:
                    Log.d(TAG, "播放打开失败，流打开失败");
                    if (mReconnectCount++ < MAX_RECONNECT_COUNT)
                        mPlayerSDKHelper.reconnect(url);
                    if (mManagerCallback != null) {
                        mManagerCallback.onEvent(TYPE_PLAYER_OPEN_FAILED, null);
                    }
                    break;
                case MediaError.ALIVC_ERR_PLAYER_NO_NETWORK:
                    Log.d(TAG, "播放器没有网络连接");
                    if (mReconnectCount++ < MAX_RECONNECT_COUNT)
                        mPlayerSDKHelper.reconnect(url);
                    if (mManagerCallback != null) {
                        mManagerCallback.onEvent(TYPE_PLAYER_NO_NETWORK, null);
                    }
                    break;
                case MediaError.ALIVC_ERR_PLAYER_TIMEOUT:
                    Log.d(TAG, "播放器网络超时");
                    if (mReconnectCount++ < MAX_RECONNECT_COUNT)
                        mPlayerSDKHelper.reconnect(url);
                    if (mManagerCallback != null) {
                        mManagerCallback.onEvent(TYPE_PLAYER_TIMEOUT, null);
                    }
                    break;
                case MediaError.ALIVC_ERR_PLAYER_READ_PACKET_TIMEOUT:
                    Log.d(TAG, "播放读取(下载)数据超时");
                    if (mReconnectCount++ < MAX_RECONNECT_COUNT)
                        mPlayerSDKHelper.reconnect(url);
                    if (mManagerCallback != null) {
                        mManagerCallback.onEvent(TYPE_PLAYER_READ_PACKET_TIMEOUT, null);
                    }
                    break;

                // --------------------------------------------------------------------------------------------------------

                // 下面几个错误都是弹出个对话框显示错误，点击确定时会关闭观看界面，让用户重新进入
                case MediaError.ALIVC_ERR_PLAYER_NO_MEMORY:// 播放无足够内存
                case MediaError.ALIVC_ERR_PLAYER_INVALID_CODEC:// 播放不支持的解码格式
                case MediaError.ALIVC_ERR_PLAYER_NO_SURFACEVIEW:// 播放没有设置显示窗口
                case MediaError.ALIVC_ERR_PLAYER_UNSUPPORTED:// 播放不支持的解码
                case MediaError.ALIVC_ERR_PLAYER_UNKNOW:// 播放出现未知的错误？？？？？？
                    // 播放出错，结束本观看界面
                    if (mManagerCallback != null) {
                        Bundle data = new Bundle();
                        data.putInt(DATA_KEY_PLAYER_ERROR_CODE, what);
                        mManagerCallback.onEvent(TYPE_PLAYER_INTERNAL_ERROR, data);
                    }
                    // 播放出错，结束正在进行的连麦
                    if (mChatSession != null && mChatSession.isMixing()) {  // 如果正在连麦则结束连麦
                        asyncTerminateChatting(null);   // 结束连麦
                        if (mManagerCallback != null) {
                            mManagerCallback.onEvent(TYPE_CHATTING_FINISHED, null);
                        }
                    }
                    // 播放出错，结束连麦播放器，释放资源
                    asyncTerminatePlaying(null);
                    break;
                case MediaError.ALIVC_ERR_PUBLISHER_AUDIO_CAPTURE_DISABLED:// 音频采集失败。音频被禁止
                case MediaError.ALIVC_ERR_PUBLISHER_AUDIO_CAPTURE_NO_DATA:// 音频采集出错
                    // 只有连麦的时候需要进行推流，才要采集音频
                    if (mChatSession != null) {
                        Log.d(TAG, "音频采集失败，结束连麦");
                        if (mManagerCallback != null) {
                            Bundle data = new Bundle();
                            data.putInt(DATA_KEY_PLAYER_ERROR_CODE, what);
                            mManagerCallback.onEvent(TYPE_PUBLISHER_NO_AUDIO_DATA, data);
                        }
                    } else {
                        // 没有进行连麦的时候，采不采集音频无所谓
                        Log.d(TAG, "音频采集失败，但是当前没有处于连麦状态");
                    }
                    break;
                case MediaError.ALIVC_ERR_PUBLISHER_VIDEO_CAPTURE_NO_DATA:// 视频采集出错
                case MediaError.ALIVC_ERR_PUBLISHER_VIDEO_CAPTURE_DISABLED:// 视频被禁止
                    // 只有连麦的时候需要进行推流，才要采集视频
                    if (mChatSession != null) {
                        Log.d(TAG, "视频采集失败，结束连麦");
                        if (mManagerCallback != null) {
                            Bundle data = new Bundle();
                            data.putInt(DATA_KEY_PLAYER_ERROR_CODE, what);
                            mManagerCallback.onEvent(TYPE_PUBLISHER_NO_VIDEO_DATA, data);
                        }
                    } else {
                        // 没有进行连麦的时候，采不采集视频无所谓
                        Log.d(TAG, "视频采集失败，但是当前没有处于连麦状态");
                    }
                    break;
                case MediaError.ALIVC_ERR_PLAYER_AUDIO_PLAY:// 音频播放错误
                    if (mManagerCallback != null) {
                        Bundle data = new Bundle();
                        data.putInt(DATA_KEY_PLAYER_ERROR_CODE, what);
                        mManagerCallback.onEvent(TYPE_PLAYER_AUDIO_PLAYER_ERROR, data);
                    }
                    break;

                // --------------------------------------------------------------------------------------------------------

                // 弹吐司说明下
                case MediaError.ALIVC_ERR_PUBLISHER_NETWORK_POOR:// 网络较慢
                    if (mManagerCallback != null) {
                        mManagerCallback.onEvent(TYPE_PUBLISHER_NETWORK_POOR, null);
                    }
                    break;
                case MediaError.ALIVC_ERR_PUBLISHER_NETWORK_UNCONNECTED:// 网络未连接
                    if (mManagerCallback != null) {
                        mManagerCallback.onEvent(TYPE_PUBLISHER_NETWORK_UNCONNECT, null);
                    }
                    break;
                case MediaError.ALIVC_ERR_PUBLISHER_SEND_DATA_TIMEOUT:// 推流网络超时，发送数据超时
                    if (mManagerCallback != null) {
                        mManagerCallback.onEvent(TYPE_PUBLISHER_NETWORK_TIMEOUT, null);
                    }
                    break;

                // --------------------------------------------------------------------------------------------------------

                // 没有做出具体响应
                case MediaError.ALIVC_ERR_MEMORY_POOR:// 内存不够
                case MediaError.ALIVC_ERR_PUBLISHER_OPEN_FAILED:// 推流连接失败
                case MediaError.ALIVC_ERR_PUBLISHER_VIDEO_CAPTURE_FPS_SLOW:// 音频采集较慢
                case MediaError.ALIVC_ERR_PUBLISHER_ENCODE_AUDIO_FAILED:// 音频编码失败
                case MediaError.ALIVC_ERR_PUBLISHER_AUDIO_ENCODER_INIT_FAILED:// 音频初始化失败
                case MediaError.ALIVC_ERR_PUBLISHER_MALLOC_FAILED:// 内存分配失败
                case MediaError.ALIVC_ERR_PUBLISHER_ENCODE_VIDEO_FAILED:// 视频编码失败
                case MediaError.ALIVC_ERR_PUBLISHER_VIDEO_ENCODER_INIT_FAILED:// 视频初始化失败
                case MediaError.ALIVC_ERR_PUBLISHER_ILLEGAL_ARGUMENT:// 无效的参数
                    System.out.println();
                    break;
                default:
                    System.out.println();
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
            Log.d(TAG, "WatchLiveActivity-->播放器状态信息码 = " + what + ", 播放器的URL = " + url);
            switch (what) {
                case MediaError.ALIVC_INFO_PLAYER_FIRST_FRAME_RENDERED:// 播放首帧显示
                    // 首帧显示时间
                    if (mManagerCallback != null) {
                        // 这两个回调都没实际意义，没有任何操作
                        mManagerCallback.onEvent(TYPE_PLAYER_FIRST_FRAME_RENDER_SUCCESS, null);
                        mManagerCallback.onEvent(TYPE_PARTER_OPT_END, null);
                    }
                    break;

                // --------------------------------------------------------------------------------------------------------

                // 弹个吐司说明下
                case MediaError.ALIVC_INFO_PUBLISH_RECONNECT_FAILURE:// 重连失败
                    if (mManagerCallback != null) {
                        Bundle data = new Bundle();
                        data.putInt(DATA_KEY_PLAYER_INFO_CODE, what);
                        mManagerCallback.onEvent(TYPE_PUBLISHER_RECONNECT_FAILURE, data);
                    }
                    break;
                case MediaError.ALIVC_INFO_PLAYER_NETWORK_POOR:// 播放器网络差，不能及时下载数据包
                    if (mManagerCallback != null) {
                        Bundle data = new Bundle();
                        data.putString(IPlayerManager.DATA_KEY_PLAYER_NETWORK_BAD, url);
                        mManagerCallback.onEvent(TYPE_PLAYER_NETWORK_POOR, data);
                    }
                    break;

                // --------------------------------------------------------------------------------------------------------

                // 播放准备完成通知，这里的完成有可能是重连了多少次后的完成，所以需要归零
                case MediaError.ALIVC_INFO_PLAYER_PREPARED_PROCESS_FINISHED:// 播放准备完成通知
                    mReconnectCount = 0;
                    break;

                // -------------------------------------------------------------------------------------------------------- 

                // 调用SDK的API结束响应
                case MediaError.ALIVC_INFO_ONLINE_CHAT_END:// onlineChat结束
                    mUseChatApiString = null;
                    mVideoChatApiCalling = false;
                    Log.e(TAG + "---API", "调用连麦推流SDK的开始连麦的API结束");
                    break;
                case MediaError.ALIVC_INFO_ADD_CHAT_END:// addChat结束
                    mUseChatApiString = null;
                    mVideoChatApiCalling = false;
                    Log.e(TAG + "---API", "调用连麦推流SDK的添加连麦的API结束");
                    break;
                case MediaError.ALIVC_INFO_REMOVE_CHAT_END:// removeChat结束
                    mUseChatApiString = null;
                    mVideoChatApiCalling = false;
                    Log.e(TAG + "---API", "调用连麦推流SDK的移除连麦的API结束");
                    break;
                case MediaError.ALIVC_INFO_OFFLINE_CHAT_END:// offlineChat结束
                    mUseChatApiString = null;
                    mVideoChatApiCalling = false;
                    Log.e(TAG + "---API", "调用连麦推流SDK的结束连麦的API结束");
                    break;

                // -------------------------------------------------------------------------------------------------------- 

                // 没有做出具体响应
                case MediaError.ALIVC_INFO_LAUNCH_CHAT_END:// launchChat结束
                case MediaError.ALIVC_INFO_ABORT_CHAT_END:// abortChat结束
                case MediaError.ALIVC_INFO_PUBLISH_START_SUCCESS:// 推流开始成功
                case MediaError.ALIVC_INFO_PLAYER_BUFFERING_START:// 播放缓冲开始
                case MediaError.ALIVC_INFO_PLAYER_BUFFERING_END:// 播放缓冲结束
                case MediaError.ALIVC_INFO_PLAYER_INTERRUPT_PLAYING:// 播放被中断
                case MediaError.ALIVC_INFO_PLAYER_STOP_PROCESS_FINISHED:// 播放结束通知
                case MediaError.ALIVC_INFO_PUBLISH_NETWORK_GOOD:// 推流网络较好
                case MediaError.ALIVC_INFO_PUBLISH_RECONNECT_START:// 重连开始
                case MediaError.ALIVC_INFO_PUBLISH_RECONNECT_SUCCESS:// 重连成功
                case MediaError.ALIVC_INFO_PUBLISH_DISPLAY_FIRST_FRAME:// 推流首次显示通知
                case MediaPlayer.MEDIA_INFO_UNKNOW:// 未知
                    System.out.println();
                    break;
            }
            return false;
        }
    };
}
