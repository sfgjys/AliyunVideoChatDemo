package com.alivc.videochat.demo.ui;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
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
import com.alivc.videochat.demo.app.AppSettings;
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
 * Created by liujianghao on 16-7-27.
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
    private TreeMap<Integer, ChattingViewHolder> mFreeHolderMap = new TreeMap<>();  //连麦小窗View容器，用来管理连麦的小窗
    /**
     * 变量的描述: 用来存储已经被使用的ChattingViewHolder，key值为对应的其他连麦uid
     */
    private Map<String, ChattingViewHolder> mUsedViewHolderMap = new HashMap<>();   //在使用的ViewHolder
    /**
     * 变量的描述: 主播放Surface
     */
    private SurfaceView mPlaySurfaceView;
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


    private LiveCloseDialog mLiveCloseDialog = null;
    private AlertDialog mImInitFailedDialog;
    private LogInfoFragment mLogInfoFragment;


    private WatchBottomFragment mBottomFragment;

    private String mIMFailedMessage;
    private DialogInterface.OnClickListener mIMFailedListener;
    private ConnectivityMonitor mConnectivityMonitor = new ConnectivityMonitor();
    private HeadsetMonitor mHeadsetMonitor = new HeadsetMonitor();

    private ILifecycleLivePlayPresenter mPresenter;
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
     * 显示IM消息通信初始化失败的Dialog
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

    /**
     * 方法描述: 修改参数控件topMargin属性为300 。为的是让参数控件部在手机屏幕上隐藏
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

//            case R.id.iv_abort_chat:
//                showChatCloseConfirmDialog();
//                break;
        }
    }

    // --------------------------------------------------------------------------------------------------------

    private ILivePlayView mView = new ILivePlayView() {
        @Override
        public SurfaceView getPlaySurfaceView() {
            return mPlaySurfaceView;
        }

        @Override
        public void showEnterLiveRoomFailure() {
        }

        @Override
        public void showLiveInterruptUI(int msgRedID, int what) {

            // 如果不在前台
            if (isFinishing()) {
                return;
            }
            hideLoading();  //隐藏正在加载的UI

            try {
                AlertDialog.Builder normalDialog =
                        new AlertDialog.Builder(WatchLiveActivity.this);
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

        /**
         * 方法描述: 将方法的参数作为消息提示显示对话框
         */
        @Override
        public void showInfoDialog(String msg) {
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
        public void showLoading() {
            if (mLoadingView != null && mLoadingView.getParent() == null) {
                mRootContainer.addView(mLoadingView);
            }
        }

        @Override
        public void hideLoading() {
            mRootContainer.removeView(mLoadingView);
        }

        @Override
        public void showToast(int id) {
            ToastUtils.showToast(WatchLiveActivity.this, id);
        }

        @Override
        public void showToast(String msg) {
            ToastUtils.showToast(WatchLiveActivity.this, msg);
        }

        @Override
        public void hideLiveInterruptUI() {
        }

        /**
         * 方法描述: 显示直播界面的UI
         */
        @Override
        public void showLiveCloseUI() {
            hideLoading();
            if (mLiveCloseDialog == null) {
                mLiveCloseDialog = LiveCloseDialog.newInstance(getString(R.string.live_finished));
            }
            if (!mLiveCloseDialog.isShow()) {
                mLiveCloseDialog.show(getSupportFragmentManager(), LiveCloseDialog.TAG);
            }
            showSelfExitChattingUI();
        }

        @Override
        public void showChattingView() {
            //TODO: 显示正在连麦的Loading View
            showLiveInterruptUI(R.string.chatting, Integer.MAX_VALUE);
        }

        @Override
        public void hideChattingView() {
            //TODO: 隐藏正在连麦的Loading View
            hideLiveInterruptUI();
        }

        @Override
        public void showFirstFrameTime(long firstFrameTime) {
            mTvFirstFrameTime.setText(firstFrameTime + "ms");
        }


        @Override
        public void showImInitInvalidDialog(ImException e) {
            if (e.getErrorCode() == ImErrorCode.NOT_LOGIN) {
                showImInitFailedDialog(getString(R.string.message_im_login_failed), mImLoginFailedListener);
            } else {
                showImInitFailedDialog(getString(R.string.message_push_init_failed), mImInitFailedListener);
            }
        }

        private DialogInterface.OnClickListener mImInitFailedListener = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        };

        private DialogInterface.OnClickListener mImLoginFailedListener = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
                Intent intent = new Intent(WatchLiveActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        };


        @Override
        public void closeVideoChatSmallView() {
            mBottomFragment.hideRecordView();
        }


        @Override
        public void showInviteRequestSuccessUI() {
            ToastUtils.showToast(WatchLiveActivity.this, R.string.invite_succeed);
        }

        @Override
        public void showInviteRequestFailedUI(Throwable cause) {
            if (cause instanceof APIException) {
                APIException ae = (APIException) cause;
                switch (ae.getErrorCode()) {
                    case APIErrorCode.ERROR_ROOM_INVITING:
                        ToastUtils.showToast(WatchLiveActivity.this, R.string.error_room_inviting);
                        break;
                    default:
                        ToastUtils.showToast(WatchLiveActivity.this, cause.getMessage());
                        break;
                }
            }
        }

        @Override
        public void showCloseChatFailedUI() {
            ToastUtils.showToast(WatchLiveActivity.this, R.string.close_video_chatting_failed);
        }

        /**
         * 方法描述: 获取本观众用来推流的SurfaceView，该SurfaceView设置了点击右上角的关闭，可以退出连麦的操作
         */
        @Override
        public SurfaceView showLaunchChatUI() {
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
        public void showOfflineChatBtn() {
            mRightChattingHolder.mIvClose.setVisibility(View.VISIBLE);
            mBottomFragment.hideCallAnchor();
        }

        @Override
        public void showOnlineChatBtn() {
            mBottomFragment.showCallAnchor();
//            mRightChattingHolder.mIvClose.setVisibility(View.INVISIBLE);

        }

        @Override
        public Map<String, SurfaceView> getOtherParterViews(List<String> inviteeUIDs) {
            Map<String, SurfaceView> uidSurfaceMap = new HashMap<>();
            ChattingViewHolder holder;
            if (inviteeUIDs == null || inviteeUIDs.size() == 0) {
                return uidSurfaceMap;
            }
            synchronized (mFreeHolderMap) {
                String inviteeUID;
                if (inviteeUIDs.size() <= mFreeHolderMap.size()) {
                    for (int i = 0; i < inviteeUIDs.size(); i++) {
                        inviteeUID = inviteeUIDs.get(i);

                        // 移除First的一对ChattingViewHolder,并作为返回值返回
                        holder = mFreeHolderMap.pollFirstEntry().getValue();

                        // 存储以确定使用的
                        mUsedViewHolderMap.put(inviteeUID, holder);

                        // 修改属性
                        showSurfaceView(holder.mSurfaceView);


                        // 以其他连麦观众的id为key，存储对应的SurfaceView
                        uidSurfaceMap.put(inviteeUID, holder.mSurfaceView);
                    }
                }
            }
            return uidSurfaceMap;
        }

        /**
         * 方法描述: 根据参数uid去移除对应的连麦UI
         */
        @Override
        public void showExitChattingUI(String inviteeUID) {
            ChattingViewHolder holder = mUsedViewHolderMap.get(inviteeUID);
            if (holder != null) {
                mFreeHolderMap.put(holder.mIndex, holder);
                mUsedViewHolderMap.remove(inviteeUID);
                Log.d(TAG, "showExitChattingUI, inviteeUID = " + inviteeUID);
                hideSurfaceView(holder.mSurfaceView);
            }
        }

        /**
         * 方法描述: 隐藏连麦使用的Surface
         */
        @Override
        public void showSelfExitChattingUI() {
            String key;
            ChattingViewHolder holder;
            //隐藏右下角的surface和小叉按钮
            mRightChattingHolder.mIvClose.setVisibility(View.GONE);
            hideSurfaceView(mRightChattingHolder.mSurfaceView);

            //其他正在连麦用户的界面也要隐藏
            if (mUsedViewHolderMap.size() > 0) {
                Iterator<String> keySetIt = mUsedViewHolderMap.keySet().iterator();
                while (keySetIt.hasNext()) {
                    key = keySetIt.next();
                    holder = mUsedViewHolderMap.get(key);
                    Log.d(TAG, "showSelfExitChattingUI, key inviteeUID = " + key);
                    hideSurfaceView(holder.mSurfaceView);
                    mFreeHolderMap.put(holder.mIndex, holder);
                }
                mUsedViewHolderMap.clear();
            }
            mBottomFragment.hideRecordView();
        }
    };
}
