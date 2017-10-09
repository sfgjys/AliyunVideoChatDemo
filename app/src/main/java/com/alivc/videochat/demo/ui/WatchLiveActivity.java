package com.alivc.videochat.demo.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.sdk.mns.MNSClientImpl;
import com.alivc.videochat.demo.R;
import com.alivc.videochat.demo.base.BaseActivity;
import com.alivc.videochat.demo.base.FragmentInteraction;
import com.alivc.videochat.demo.exception.APIErrorCode;
import com.alivc.videochat.demo.exception.APIException;
import com.alivc.videochat.demo.exception.ImErrorCode;
import com.alivc.videochat.demo.exception.ImException;
import com.alivc.videochat.demo.im.ImHelper;
import com.alivc.videochat.demo.im.ImManager;
import com.alivc.videochat.demo.presenter.ILifecycleLivePlayPresenter;
import com.alivc.videochat.demo.presenter.impl.LifecycleLivePlayPresenterImpl;
import com.alivc.videochat.demo.presenter.view.ILivePlayView;
import com.alivc.videochat.demo.ui.dialog.LiveCloseDialog;
import com.alivc.videochat.demo.uitils.DensityUtil;
import com.alivc.videochat.demo.uitils.ToastUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 类的描述: 直播观看界面
 */
public class WatchLiveActivity extends BaseActivity implements View.OnClickListener, FragmentInteraction {

    /**
     * 方法描述: 跳转至本界面的方法
     */
    public static void startActivity(Context context, String playUrl, String roomID, String anchorName, String anchorUID) {
        Intent intent = new Intent(context, WatchLiveActivity.class);
        Bundle bundle = new Bundle();
        // 直播列表Item的数据源LiveItemResult对象包含的roomId
        bundle.putString(ExtraConstant.EXTRA_ROOM_ID, roomID);
        // 直播列表Item的数据源LiveItemResult对象包含的rtmpPlayUrl
        bundle.putString(ExtraConstant.EXTRA_PLAY_URL, playUrl);
        // 直播列表Item的数据源LiveItemResult对象包含的name
        bundle.putString(ExtraConstant.EXTRA_NAME, anchorName);
        // 直播列表Item的数据源LiveItemResult对象包含的uid
        bundle.putString(ExtraConstant.EXTRA_ANCHOR_UID, anchorUID);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    // --------------------------------------------------------------------------------------------------------

    public static final int INTERACTION_TYPE_INVITE = 1;

    private static final String TAG = "WatchLivePresenter";
    /**
     * 变量的描述: 封装了连麦副麦 上 此控件用来显示其他连麦观众的短延迟播放
     */
    private ChattingViewHolder mLeftChattingHolder;
    /**
     * 变量的描述: 封装了连麦副麦 中 此控件用来显示其他连麦观众的短延迟播放
     */
    private ChattingViewHolder mMiddleChattingHolder;
    /**
     * 变量的描述: 封装了连麦副麦 下 此控件是用来给本观众进行推流用的
     */
    private ChattingViewHolder mRightChattingHolder;
    /**
     * 变量的描述: 用来存储刚封装好的ChattingViewHolder对象，且这里的ChattingViewHolder对象都是暂时没有使用的，使用后就存储在这了
     */
    private final TreeMap<Integer, ChattingViewHolder> mFreeHolderMap = new TreeMap<>();  //连麦小窗View容器，用来管理连麦的小窗
    /**
     * 变量的描述: 用来存储已经被使用的ChattingViewHolder，key值为对应的其他连麦uid
     */
    private Map<String, ChattingViewHolder> mUsedViewHolderMap = new HashMap<>();   //在使用的ViewHolder
    /**
     * 变量的描述: 主播放Surface
     */
    private SurfaceView mPlaySurfaceView;
    /**
     * 变量的描述: 根容器控件
     */
    private FrameLayout mRootContainer;
    /**
     * 变量的描述: 一个正在加载中的显示界面控件
     */
    private View mLoadingView = null;
    /**
     * 变量的描述: 展示性能的控件
     */
    private LinearLayout mLogContainer;
    /**
     * 变量的描述: 显示首帧耗时的时间
     */
    private TextView mTvFirstFrameTime;
    /**
     * 变量的描述: 直播界面结束对话框界面
     */
    private LiveCloseDialog mLiveCloseDialog = null;
    /**
     * 变量的描述: MNS使用失败提示对话框
     */
    private AlertDialog mImInitFailedDialog;
    /**
     * 变量的描述: 显示性能界面的Fragment
     */
    private LogInfoFragment mLogInfoFragment;
    /**
     * 变量的描述: 观看界面底部按钮Fragment
     */
    private WatchBottomFragment mBottomFragment;
    /**
     * 变量的描述: 网络状态变化监听广播
     */
    private ConnectivityMonitor mConnectivityMonitor = new ConnectivityMonitor();
    /**
     * 变量的描述: 耳麦状态变化监听广播
     */
    private HeadsetMonitor mHeadsetMonitor = new HeadsetMonitor();
    /**
     * 变量的描述: 观看界面的Presenter
     */
    private ILifecycleLivePlayPresenter mPresenter;
    /**
     * 变量的描述: 直播间ID
     */
    private String mLiveRoomID;

    /**
     * 类的描述: 存储连麦副麦的SurfaceView，对应的关闭ImageView，以及对应的indexF
     */
    private static class ChattingViewHolder {
        SurfaceView mSurfaceView;
        ImageView mIvClose;
        int mIndex;

        public ChattingViewHolder(SurfaceView surfaceView, ImageView ivClose, int index) {
            mSurfaceView = surfaceView;
            mIvClose = ivClose;
            mIndex = index;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 屏幕常量
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // 全屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // TODO MNS
        // MNSClientImpl是MNSClient接口的实现类 参数二ImManager的帮助类，参数三网络链接状态广播
        ImManager imManager = new ImManager(this, new ImHelper(new MNSClientImpl()), mConnectivityMonitor);
        // 初始化
        imManager.init();

        mPresenter = new LifecycleLivePlayPresenterImpl(this, mView, imManager, getUid());
        setLifecycleListener(mPresenter);

        // 以上代码必须在onCreate上面先写,代码内部功能是初始化播放器
        super.onCreate(savedInstanceState);

        // 获取传递过来的roomId
        mLiveRoomID = getIntent().getStringExtra(ExtraConstant.EXTRA_ROOM_ID);

        setContentView(R.layout.activity_watch_live);

        mRootContainer = (FrameLayout) findViewById(R.id.root_container);

        // --------------------------------------------------------------------------------------------------------

        // 以根部局root_container填充出一个 正在加载的转圈 控件
        mLoadingView = LayoutInflater.from(this).inflate(R.layout.fragment_live_video_loading, mRootContainer, false);

        // --------------------------------------------------------------------------------------------------------

        // 展示性能的控件
        mLogContainer = (LinearLayout) findViewById(R.id.log_container);

        // 显示首帧耗时的时间
        mTvFirstFrameTime = (TextView) findViewById(R.id.tv_value_first_frame_time);

        // 主播放Surface
        mPlaySurfaceView = (SurfaceView) findViewById(R.id.host_play_surface);

        // --------------------------------------------------------------------------------------------------------

        // 初始化连麦的副Surface,并用ChattingViewHolder进行封装
        mLeftChattingHolder = new ChattingViewHolder((SurfaceView) findViewById(R.id.parter_view_left), (ImageView) findViewById(R.id.iv_abort_chat_left), 3);
        mMiddleChattingHolder = new ChattingViewHolder((SurfaceView) findViewById(R.id.parter_view_middle), (ImageView) findViewById(R.id.iv_abort_chat_middle), 2);
        mRightChattingHolder = new ChattingViewHolder((SurfaceView) findViewById(R.id.parter_view_right), (ImageView) findViewById(R.id.iv_abort_chat_right), 1);

        // 设置副麦Surface将其覆盖在其他媒体上面
        mLeftChattingHolder.mSurfaceView.setZOrderMediaOverlay(true);
        mMiddleChattingHolder.mSurfaceView.setZOrderMediaOverlay(true);
        mRightChattingHolder.mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                System.out.println("xiongbo14 surface created.");
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                System.out.println("xiongbo14 surface changed.");
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                System.out.println("xiongbo14 surface destroyed.");
            }
        });
        mRightChattingHolder.mSurfaceView.setZOrderMediaOverlay(true);

        // 存储已经刚封装好的ChattingViewHolder
        mFreeHolderMap.put(mMiddleChattingHolder.mIndex, mMiddleChattingHolder);
        mFreeHolderMap.put(mLeftChattingHolder.mIndex, mLeftChattingHolder);

        // --------------------------------------------------------------------------------------------------------

        // 在主播放Surface上面开启交互界面Fragment ，开启的Fragment和主播的交互界面一样
        InteractionFragment interactionFragment = InteractionFragment.newInstance(
                getIntent().getStringExtra(ExtraConstant.EXTRA_ROOM_ID),
                getIntent().getStringExtra(ExtraConstant.EXTRA_NAME),
                getIntent().getStringExtra(ExtraConstant.EXTRA_ANCHOR_UID));

        // 交互界面底部按钮用一个有别于主播交互底部Fragment显示F
        mBottomFragment = WatchBottomFragment.newInstance(
                getIntent().getStringExtra(ExtraConstant.EXTRA_ROOM_ID),
                getIntent().getStringExtra(ExtraConstant.EXTRA_ANCHOR_UID));
        mBottomFragment.setRecordUIClickListener(mUIClickListener);

        interactionFragment.setImManger(imManager);

        interactionFragment.setBottomFragment(mBottomFragment);

        getSupportFragmentManager().beginTransaction().add(R.id.root_container, interactionFragment).commit();

        // --------------------------------------------------------------------------------------------------------

        // 设置这个已经被废弃了，现在是在需要的时候会自动设置
        mPlaySurfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_GPU);// 适用于GPU加速的Surface
        mPlaySurfaceView.getHolder().setKeepScreenOn(true);// 设置控件常亮

    }

    @Override
    protected void onResume() {
        super.onResume();
        // 有焦点时代码中就获取播放流，成功后开启播放
        mPresenter.enterLiveRoom(mLiveRoomID);

        mConnectivityMonitor.register(this);        //注册对网络状态的监听
        mHeadsetMonitor.register(this);        //注册对耳机状态的监听

        boolean isShowLogInfo = false;
        if (isShowLogInfo) {
            showLogInfoUI();
        } else {
            dismissLogInfoUI();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mConnectivityMonitor.unRegister(this);
        mHeadsetMonitor.unRegister(this);
        dismissLogInfoUI();
    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "WatchLiveActivity--> onDestroy");
    }

    // --------------------------------------------------------------------------------------------------------

    // **************************************************** 性能日志展示 ****************************************************
    LogInfoFragment.LogRefreshListener mRefreshListener = new LogInfoFragment.LogRefreshListener() {
        @Override
        public void onPendingRefresh() {
            if (mPresenter != null && mLogInfoFragment != null) {
                LogInfoFragment.LogHandler logHandler = mLogInfoFragment.getLogHandler();
                mPresenter.updateLog(logHandler);
            }
        }
    };

    /**
     * 展示性能日志信息
     */
    public void showLogInfoUI() {
        mLogContainer.setVisibility(View.VISIBLE);
        if (mLogInfoFragment == null) {
            mLogInfoFragment = new LogInfoFragment();
            mLogInfoFragment.setRefreshListener(mRefreshListener);
        }
        getSupportFragmentManager().beginTransaction().add(R.id.log_container, mLogInfoFragment).commitAllowingStateLoss();
    }

    /**
     * 隐藏性能日志信息
     */
    public void dismissLogInfoUI() {
        mLogContainer.setVisibility(View.GONE);
        if (mLogInfoFragment != null) {
            getSupportFragmentManager().beginTransaction().remove(mLogInfoFragment).commit();
        }
    }

    // --------------------------------------------------------------------------------------------------------

    /**
     * 方法描述: Fragment中的Action事件，比如点击邀请按钮
     *
     * @param actionType 用于区分Action事件
     */
    @Override
    public void onPendingAction(int actionType, Bundle bundle) {
        switch (actionType) {
            case INTERACTION_TYPE_INVITE:// 这里是WatchBottomFragment中发起的Action事件
                mPresenter.invite(); //发起连麦邀请
        }
    }

    // --------------------------------------------------------------------------------------------------------

    /**
     * 变量的描述: MNS出现错误的信息
     */
    private String mIMFailedMessage;
    /**
     * 变量的描述: 点击对话框确定按钮的监听回调，因为有两种失败情况，所以这个监听要作为参数
     */
    private DialogInterface.OnClickListener mIMFailedListener;

    /**
     * 显示IM消息通信登录或者初始化失败的Dialog
     */
    private void showImInitFailedDialog(String message, DialogInterface.OnClickListener listener) {
        mIMFailedListener = listener;
        mIMFailedMessage = message;
        if (mImInitFailedDialog == null) {
            mImInitFailedDialog = new AlertDialog.Builder(WatchLiveActivity.this)
                    .setTitle(R.string.prompt)
                    .create();
            mImInitFailedDialog.setCanceledOnTouchOutside(false);
        }
        mImInitFailedDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.sure), mIMFailedListener);
        mImInitFailedDialog.setMessage(mIMFailedMessage);
        mImInitFailedDialog.show();
    }

    // --------------------------------------------------------------------------------------------------------

    /**
     * 方法描述: 修改参数控件topMargin属性为300 。为的是让参数控件在手机屏幕上隐藏
     */
    private void hideSurfaceView(SurfaceView surfaceView) {
        Log.d(TAG, "hide SurfaceView :" + surfaceView.toString());
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) surfaceView.getLayoutParams();
        layoutParams.topMargin = DensityUtil.dp2px(WatchLiveActivity.this, 300);
        surfaceView.requestLayout();
    }

    /**
     * 方法描述: 修改SurfaceView的topMargin属性为0.让其显示在手机屏幕上
     */
    private void showSurfaceView(SurfaceView surfaceView) {
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) surfaceView.getLayoutParams();
        layoutParams.topMargin = DensityUtil.dp2px(WatchLiveActivity.this, 0);
        surfaceView.requestLayout();// 来实现重绘当前View和父View，甚至更上层的View
    }

    // --------------------------------------------------------------------------------------------------------

    /**
     * 变量的描述: 观看界面的底部按钮监听回调实例
     */
    private LiveBottomFragment.RecorderUIClickListener mUIClickListener = new LiveBottomFragment.RecorderUIClickListener() {
        @Override
        public int onSwitchCamera() {
            mPresenter.switchCamera();
            return -1;
        }

        @Override
        public boolean onBeautySwitch() {
            return mPresenter.switchBeauty();
        }

        @Override
        public boolean onFlashSwitch() {
            return mPresenter.switchFlash();
        }
    };

    // --------------------------------------------------------------------------------------------------------

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_close:
                onBackPressed();
                break;
        }
    }

    // --------------------------------------------------------------------------------------------------------

    private ILivePlayView mView = new ILivePlayView() {
        // **************************************************** 观看和连麦的核心UI更新 ****************************************************
        @Override
        public SurfaceView getPlaySurfaceView() {  // ----------------获取播放主播直播的SurfaceView
            return mPlaySurfaceView;
        }

        @Override
        public SurfaceView showLaunchChatUI() {// ----------------获取本观众用来推流的SurfaceView，该SurfaceView设置了点击右上角的关闭，可以退出连麦的操作，并返回本观众用来推流的SurfaceView
            showSurfaceView(mRightChattingHolder.mSurfaceView);
            // 设置右上角的关闭，可以退出连麦的操作
            mRightChattingHolder.mIvClose.setVisibility(View.VISIBLE);
            mRightChattingHolder.mIvClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPresenter.exitChatting();
                    mBottomFragment.hideRecordView();
                }
            });
            mBottomFragment.showRecordView();
            return mRightChattingHolder.mSurfaceView;
        }

        @Override
        public Map<String, SurfaceView> getOtherParterViews(List<String> inviteeUIDs) {// ----------------根据参数invteeUIDs来获取其他连麦用户播放的SurfaceView
            Map<String, SurfaceView> uidSurfaceMap = new HashMap<>();
            ChattingViewHolder holder;

            // 如果参数集合的大小为0，说明暂时没有其他连麦人，直接返回空内容的uidSurfaceMap集合
            if (inviteeUIDs == null || inviteeUIDs.size() == 0) {
                return uidSurfaceMap;
            }

            synchronized (mFreeHolderMap) {
                String inviteeUID;
                // 因为连麦的人数最多就3人，而mFreeHolderMap中存储的是2个其他人连麦时用到的SurfaceView
                // 所以inviteeUIDs集合的大小不会超过2
                if (inviteeUIDs.size() <= mFreeHolderMap.size()) {

                    // 从mFreeHolderMap中获取需要的数据，在根据参数inviteeUIDs将数据存储进 mUsedViewHolderMap(用来记录) 和 uidSurfaceMap(作为方法返回值)
                    for (int i = 0; i < inviteeUIDs.size(); i++) {
                        inviteeUID = inviteeUIDs.get(i);

                        // 移除First的一对ChattingViewHolder,并作为返回值返回
                        holder = mFreeHolderMap.pollFirstEntry().getValue();

                        // 存储以确定使用的
                        mUsedViewHolderMap.put(inviteeUID, holder);

                        // 修改属性，让SurfaceView可见
                        showSurfaceView(holder.mSurfaceView);

                        // 以其他连麦观众的id为key，存储对应的SurfaceView
                        uidSurfaceMap.put(inviteeUID, holder.mSurfaceView);
                    }
                }
            }
            return uidSurfaceMap;
        }

        @Override
        public void showExitChattingUI(String inviteeUID) { // ----------------根据参数uid去移除对应的连麦UI
            ChattingViewHolder holder = mUsedViewHolderMap.get(inviteeUID);
            // 隐藏uid对应的SurfaceView控件，并从mUsedViewHolderMap集合中移除，添加进mFreeHolderMap集合
            if (holder != null) {
                mFreeHolderMap.put(holder.mIndex, holder);
                mUsedViewHolderMap.remove(inviteeUID);
                Log.d(TAG, "移除的连麦UI的uid是: " + inviteeUID);
                hideSurfaceView(holder.mSurfaceView);
            }
        }

        @Override
        public void showSelfExitChattingUI() { // ----------------隐藏所有和连麦有关的控件UI
            String key;
            ChattingViewHolder holder;
            // 隐藏本观众用于连麦的控件的关闭按钮
            mRightChattingHolder.mIvClose.setVisibility(View.GONE);
            // 隐藏本观众用于连麦的控件
            hideSurfaceView(mRightChattingHolder.mSurfaceView);

            // 其他正在连麦用户的界面也要隐藏
            if (mUsedViewHolderMap.size() > 0) {
                Iterator<String> keySetIt = mUsedViewHolderMap.keySet().iterator();
                while (keySetIt.hasNext()) {
                    key = keySetIt.next();
                    holder = mUsedViewHolderMap.get(key);
                    Log.d(TAG, "showSelfExitChattingUI, key inviteeUID = " + key);
                    // 隐藏 播放其他连麦观众的界面的控件
                    hideSurfaceView(holder.mSurfaceView);
                    // 从集合中的移除
                    mFreeHolderMap.put(holder.mIndex, holder);
                }
                mUsedViewHolderMap.clear();
            }

            // 隐藏本观众连麦时使用的摄像头，美颜和闪光灯按钮
            mBottomFragment.hideRecordView();
        }

        @Override
        public void showLiveCloseUI() { // ----------------显示主播结束直播的界面的UI
            // 直播结束时，要显示的UI
            hideLoading();
            if (mLiveCloseDialog == null) {
                mLiveCloseDialog = LiveCloseDialog.newInstance(getString(R.string.live_finished));
            }
            if (!mLiveCloseDialog.isShow()) {
                mLiveCloseDialog.show(getSupportFragmentManager(), LiveCloseDialog.TAG);
            }
            showSelfExitChattingUI();
        }
        // --------------------------------------------------------------------------------------------------------

        // **************************************************** 正在加载的界面UI ****************************************************
        @Override
        public void showLoading() {
            // 显示正在加载的界面
            if (mLoadingView != null && mLoadingView.getParent() == null) {
                mRootContainer.addView(mLoadingView);
            }
        }

        @Override
        public void hideLoading() {
            // 隐藏正在加载的界面
            mRootContainer.removeView(mLoadingView);
        }
        // --------------------------------------------------------------------------------------------------------

        // **************************************************** 对话框形式 ****************************************************
        @Override
        public void showInfoDialog(String msg) {
            // 将方法的参数作为消息提示显示对话框
            AlertDialog.Builder normalDialog = new AlertDialog.Builder(WatchLiveActivity.this);
            normalDialog.setTitle("消息提示");
            if (msg != null) {
                normalDialog.setMessage(msg);
            } else {
                normalDialog.setMessage("连麦接口执行中...,请稍等");
            }
            normalDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            normalDialog.setCancelable(false);
            normalDialog.show();
        }

        @Override
        public void showLiveInterruptUI(int msgRedID, int what) {

            hideLoading();  // 隐藏正在加载的UI

            // 如果观看界面不在前台
            if (isFinishing()) {
                return;
            }

            // 显示一个提示对话框，使用参数代表的错误信息进行显示，当用户点击确定时会关闭本界面
            try {
                AlertDialog.Builder normalDialog = new AlertDialog.Builder(WatchLiveActivity.this);
                normalDialog.setTitle("错误提示");
                normalDialog.setMessage(getString(msgRedID) + ", ErrorCode: " + what);
                normalDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                normalDialog.setCancelable(false);
                normalDialog.show();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        // --------------------------------------------------------------------------------------------------------

        // **************************************************** 吐司形式 ****************************************************
        @Override
        public void showToast(int id) {
            // 显示参数资源Id所对应的字符串
            ToastUtils.showToast(WatchLiveActivity.this, id);
        }

        @Override
        public void showToast(String msg) {
            // 直接显示参数字符串
            ToastUtils.showToast(WatchLiveActivity.this, msg);
        }

        @Override
        public void showInviteRequestFailedUI(Throwable cause) {
            if (cause instanceof APIException) {
                // 获取传递过来的异常对象
                APIException ae = (APIException) cause;
                switch (ae.getErrorCode()) {
                    case APIErrorCode.ERROR_ROOM_INVITING:
                        // 直播间连麦中出现异常
                        ToastUtils.showToast(WatchLiveActivity.this, R.string.error_room_inviting);
                        break;
                    default:
                        ToastUtils.showToast(WatchLiveActivity.this, cause.getMessage());
                        break;
                }
            }
        }
        // --------------------------------------------------------------------------------------------------------

        // **************************************************** 下面的方法没有用 ****************************************************
        @Override
        public void showOfflineChatBtn() {
            mRightChattingHolder.mIvClose.setVisibility(View.VISIBLE);
            mBottomFragment.hideCallAnchor();
        }

        @Override
        public void showOnlineChatBtn() {
            mBottomFragment.showCallAnchor();
            // mRightChattingHolder.mIvClose.setVisibility(View.INVISIBLE);
        }

        @Override
        public void showCloseChatFailedUI() {
            ToastUtils.showToast(WatchLiveActivity.this, R.string.close_video_chatting_failed);
        }

        @Override
        public void showInviteRequestSuccessUI() {
            ToastUtils.showToast(WatchLiveActivity.this, R.string.invite_succeed);
        }

        @Override
        public void closeVideoChatSmallView() {
            mBottomFragment.hideRecordView();
        }

        @Override
        public void showImInitInvalidDialog(ImException e) {
            if (e.getErrorCode() == ImErrorCode.NOT_LOGIN) {
                // IM服务器登陆失败，请退出重新登陆
                showImInitFailedDialog(getString(R.string.message_im_login_failed), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        Intent intent = new Intent(WatchLiveActivity.this, LoginActivity.class);
                        startActivity(intent);
                    }
                });
            } else {
                // 消息推送初始化失败，请退出重新进入
                showImInitFailedDialog(getString(R.string.message_push_init_failed), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
            }
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void showFirstFrameTime(long firstFrameTime) {
            mTvFirstFrameTime.setText(firstFrameTime + "ms");
        }

        @Override
        public void hideChattingView() {
        }

        @Override
        public void showChattingView() {
        }

        @Override
        public void hideLiveInterruptUI() {
        }

        @Override
        public void showEnterLiveRoomFailure() {
        }
    };
}
