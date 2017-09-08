package com.alivc.videochat.demo.ui;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.alibaba.sdk.mns.MNSClientImpl;
import com.alivc.videochat.demo.R;
import com.alivc.videochat.demo.app.AppSettings;
import com.alivc.videochat.demo.base.BaseActivity;
import com.alivc.videochat.demo.base.FragmentInteraction;
import com.alivc.videochat.demo.exception.APIErrorCode;
import com.alivc.videochat.demo.exception.APIException;
import com.alivc.videochat.demo.exception.ChatSessionException;
import com.alivc.videochat.demo.exception.ImErrorCode;
import com.alivc.videochat.demo.http.model.LiveItemResult;
import com.alivc.videochat.demo.im.ImHelper;
import com.alivc.videochat.demo.im.ImManager;
import com.alivc.videochat.demo.presenter.ILifecycleLiveRecordPresenter;
import com.alivc.videochat.demo.presenter.impl.LifecycleLiveRecordPresenterImpl;
import com.alivc.videochat.demo.presenter.view.ILiveRecordView;
import com.alivc.videochat.demo.ui.dialog.AnchorListDialog;
import com.alivc.videochat.demo.ui.dialog.LiveCloseDialog;
import com.alivc.videochat.demo.uitils.DensityUtil;
import com.alivc.videochat.demo.uitils.ToastUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * 类的描述: 主播直播页面---主要是消息显示和冒泡动画，底部按键是另一个Fragment
 */
public class LiveActivity extends BaseActivity implements View.OnClickListener, FragmentInteraction {

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, LiveActivity.class);
        context.startActivity(intent);
    }

    public static final int INTERACTION_TYPE_INVITE = 1; //邀请连麦的Action

    private final String TAG = "LiveActivity";

    /**
     * 变量的描述: 主播推流的SurfaceView
     */
    private SurfaceView mPreviewSurfaceView;    //推流预览的SurfaceView
    /**
     * 变量的描述: 存储的是：包含连麦人播放显示控件的对象
     */
    private TreeMap<Integer, ChattingViewHolder> mFreeHolderMap = new TreeMap<>();// 连麦小窗View容器，用来管理连麦的小窗
    private Map<String, ChattingViewHolder> mUsedViewHolderMap = new HashMap<>();   //在使用的ViewHolder
    /**
     * 变量的描述: 完整事件控件
     */
    private View mFullEventView;
    /**
     * 变量的描述: 关闭主播直播界面的ImageView控件
     */
    private ImageView mIvClose;


    private ILifecycleLiveRecordPresenter mLiveRecordPresenter = null;

    private int mPreviewWidth = 0;
    private int mPreviewHeight = 0;

    private ImManager mImManager;
    private LogInfoFragment mLogInfoFragment;
    private LiveBottomFragment mLiveBottomFragment;
    private LiveCloseDialog mLiveCloseDialog;
    private AlertDialog mImInitFailedDialog;


    private String mRoomID = null;

    private ConnectivityMonitor mConnectivityMonitor = new ConnectivityMonitor();
    private HeadsetMonitor mHeadsetMonitor = new HeadsetMonitor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 常量全屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // TODO 实例化了一个MNS客户端，该客户端是用于主播直播是的消息交互
        // MNSClientImpl是MNSClient接口的实现类，而MNSClient 是 MNS 服务的 Android 客户端，它为调用者提供了一系列的方法，可以用来操作，管理队列（queue）和消息（message）
        mImManager = new ImManager(this, new ImHelper(new MNSClientImpl()), mConnectivityMonitor);
        // 初始化
        mImManager.init();
        // TODO 将MNS客户端传入到直播操作类中根据实际情况使用MNS客户端
        // 创建直播模块生命周期管理类，并将其注入倒本Activity
        mLiveRecordPresenter = new LifecycleLiveRecordPresenterImpl(this, mView, getUid(), mImManager);
        setLifecycleListener(mLiveRecordPresenter); //注意：这个方法必须在super.onCreate()之前调用 因为super.onCreate()调用的是父类的，而在父类中mLiveRecordPresenter有设置

        // 内部是调用了PublisherSDKHelper的初始化推流器的方法
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_live);

        mPreviewSurfaceView = (SurfaceView) findViewById(R.id.surface_view);

        mIvClose = (ImageView) findViewById(R.id.iv_close);

        // ---------------------------------------------触摸mFullEventView控件，根据触摸事件去动态决定mLiveRecordPresenter是聚焦还是缩放-----------------------------------------------------------

        // 用户在该控件中进行触摸事件，然后让触摸事件给聚焦和缩放操作对象提前消费，在聚焦和缩放操作对象的监听中解析触摸事件，根据触摸事件的内容使用mLiveRecordPresenter操纵聚焦或者缩放
        mFullEventView = findViewById(R.id.full_event_view);
        // 因为mFullEventView的初始宽高和mPreviewSurfaceView宽高一致，所以这里其实获得是mPreviewSurfaceView的初始宽高
        // mFullEventView的高宽就是SurfaceView的高宽，在聚焦的时候有用
        mPreviewHeight = mFullEventView.getHeight();
        mPreviewWidth = mFullEventView.getWidth();

        // --------------------------------------------------------------------------------------------------------

        // 三个连麦在主播直播界面上进行播放的SurfaceView
        SurfaceView parterViewLeft = (SurfaceView) findViewById(R.id.parter_view_left);
        SurfaceView parterViewMiddle = (SurfaceView) findViewById(R.id.parter_view_middle);
        SurfaceView parterViewRight = (SurfaceView) findViewById(R.id.parter_view_right);
        // 三个连麦播放关闭的ImageView
        ImageView closeChattingLeft = (ImageView) findViewById(R.id.iv_abort_chat_left);
        ImageView closeChattingMiddle = (ImageView) findViewById(R.id.iv_abort_chat_middle);
        ImageView closeChattingRight = (ImageView) findViewById(R.id.iv_abort_chat_right);

        //初始化小窗容器
        mFreeHolderMap.put(0, new ChattingViewHolder(parterViewRight, closeChattingRight, 0));
        mFreeHolderMap.put(1, new ChattingViewHolder(parterViewMiddle, closeChattingMiddle, 1));
        mFreeHolderMap.put(2, new ChattingViewHolder(parterViewLeft, closeChattingLeft, 2));

        // 不论这个SurfaceView是否在其他SurfaceView的上面，这个api的作用通常就是将其覆盖在其他媒体上面  注意:执行此api请确定在layout.addView(thisSurface)之后。
        parterViewLeft.setZOrderMediaOverlay(true);
        parterViewMiddle.setZOrderMediaOverlay(true);
        parterViewRight.setZOrderMediaOverlay(true);


        // 在R.id.root_container上开启Fragment
        // 创建CreateLiveFragment对象，在创建对象的同时通过mLiveRecordPresenter获取LifecyclePublisherMgr对象，并赋值给其成员变量
        CreateLiveFragment createLiveFragment = CreateLiveFragment.newInstance(mLiveRecordPresenter.getPublisherMgr());
        createLiveFragment.setPendingPublishListener(mPendingPublishListener);
        getSupportFragmentManager().beginTransaction().add(R.id.root_container, createLiveFragment).commit();


        // 对焦
        mDetector = new GestureDetector(this, mGestureDetector);
        // 缩放
        mScaleDetector = new ScaleGestureDetector(this, mScaleGestureListener);
        mFullEventView.setOnTouchListener(mOnTouchListener);

        // 技巧: 这里addCallback可以添加多个监听，当mPreviewSurfaceView有变化了，那就是所有的监听都会响应
        mPreviewSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                System.out.println();
                // 当mPreviewSurfaceView宽高改变时，重置宽高属性，让聚焦的聚焦率可以随着mPreviewSurfaceView的宽高改变而改变
                mPreviewHeight = height;
                mPreviewWidth = width;
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
            }
        });
    }
    // **************************************************** 触摸事件操作 ****************************************************
    /**
     * 变量的描述: 对焦操作对象
     */
    private GestureDetector mDetector;
    /**
     * 变量的描述: 缩放操作对象
     */
    private ScaleGestureDetector mScaleDetector;
    /**
     * 变量的描述: 对焦监听事件接口实现
     */
    private GestureDetector.OnGestureListener mGestureDetector = new GestureDetector.OnGestureListener() {
        @Override
        public boolean onDown(MotionEvent motionEvent) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent motionEvent) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent motionEvent) {
            System.out.println();
            if (mPreviewWidth > 0 && mPreviewHeight > 0) {
                float x = motionEvent.getX() / mPreviewWidth;
                float y = motionEvent.getY() / mPreviewHeight;
                mLiveRecordPresenter.autoFocus(x, y);
            }
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent motionEvent) {

        }

        @Override
        public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
            return false;
        }
    };
    /**
     * 变量的描述: 缩放监听回调接口实现
     */
    private ScaleGestureDetector.OnScaleGestureListener mScaleGestureListener = new ScaleGestureDetector.OnScaleGestureListener() {
        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            System.out.println("scaleFactor: " + scaleGestureDetector.getScaleFactor());
            mLiveRecordPresenter.zoom(scaleGestureDetector.getScaleFactor());// 缩放放大比例不确定
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
        }
    };
    /**
     * 变量的描述: 触摸事件监听回调接口实现
     * 技巧:
     * Activity中的触摸事件是有Activity进行分发的，先发给最顶级的父类控件，在发给父类下一经控件，一直分发到最底层的控件（也就是屏幕最上层的控件）
     * 然后在由最底层控件开始响应处理事件，如果处理了就完结，如果没处理就传递给它的上一级控件，一直传到顶级父类控件
     */
    private View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            // 让对焦和缩放直接消费掉触摸事件
            mDetector.onTouchEvent(motionEvent);// 手动触发mDetector和mScaleDetector的触摸监听事件
            mScaleDetector.onTouchEvent(motionEvent);
            return true;// true 代表触摸事件被处理掉了，不继续往下传了，否则为false，将触摸事件交给下一个控件处理
        }
    };
    // --------------------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------------------

    /**
     * 类的描述: 封装连麦人播放显示控件的Bean类
     */
    private static class ChattingViewHolder {
        SurfaceView mParterView;
        ImageView mCloseChattingBtn;
        int mIndex;

        public ChattingViewHolder(SurfaceView parterView, ImageView closeChattingBtn, int index) {
            mParterView = parterView;
            mCloseChattingBtn = closeChattingBtn;
            mIndex = index;
        }
    }

    // --------------------------------------------------------------------------------------------------------
    // **************************************************** 自定义UI更新内容 ****************************************************
    private ILiveRecordView mView = new ILiveRecordView() {
        @Override
        public void hideInterruptUI() {
        }

        @Override
        public void showInterruptUI(int msgResID, int what) {
            if (isFinishing())
                return;

            try {
                AlertDialog.Builder normalDialog =
                        new AlertDialog.Builder(LiveActivity.this);
                normalDialog.setTitle("错误提示");
                normalDialog.setMessage(getString(msgResID) + ", ErrorCode: " + what);
                normalDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });

                normalDialog.show();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

        @Override
        public void showCameraOpenFailureUI() {
            ToastUtils.showToast(LiveActivity.this, R.string.camera_open_failure_for_live);
            finish();
        }

        @Override
        public void showInviteChattingTimeoutUI(String uid) {
//            mLiveBottomFragment.setInviteUIEnable(true);    //显示连麦对方响应超时的UI
        }

        @Override
        public SurfaceView showChattingUI(final String uid) {
            ChattingViewHolder viewHolder = null;
            //显示
            synchronized (mFreeHolderMap) {
                if (mFreeHolderMap.size() > 0) {
                    viewHolder = mFreeHolderMap.pollFirstEntry().getValue();
                }
            }
            if (viewHolder == null) {
                //当前的三个小窗都已经被占用了
                Log.e(TAG, "No enough surfaceview to show chatting!");
                return null;
            }
            viewHolder.mParterView.setVisibility(View.VISIBLE);
            viewHolder.mCloseChattingBtn.setVisibility(View.VISIBLE);
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) viewHolder.mParterView.getLayoutParams();
            layoutParams.topMargin = DensityUtil.dp2px(LiveActivity.this, 0);

            mUsedViewHolderMap.put(uid, viewHolder);
            viewHolder.mCloseChattingBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mLiveRecordPresenter.terminateChatting(uid);
                }
            });
            showSurfaceView(viewHolder.mParterView);
            return viewHolder.mParterView;
        }

        @Override
        public void showInviteVideoChatSuccessfulUI() {
            ToastUtils.showToast(LiveActivity.this, R.string.invite_succeed);
            dismissAnchorListDialog();                  //关闭选择连麦对象的Dialog
//            mLiveBottomFragment.setInviteUIEnable(false);   //禁用底部的连麦按钮
        }

        @Override
        public void showInviteVideoChatFailedUI(Throwable e) {
//            mLiveBottomFragment.setInviteUIEnable(true);   //启用底部的连麦按钮
            if (e instanceof APIException) {
                APIException ae = (APIException) e;
                switch (ae.getErrorCode()) {
                    case APIErrorCode.ERROR_ROOM_INVITING:
                        ToastUtils.showToast(LiveActivity.this, R.string.error_room_inviting);        //对方正在与别人连麦
                        break;
                    default:
                        ToastUtils.showToast(LiveActivity.this, R.string.invite_failed);              //其他原因导致的连麦失败
                        break;
                }
            } else if (e instanceof ChatSessionException) {
                ChatSessionException ce = (ChatSessionException) e;
                switch (ce.getErrorCode()) {
                    case ChatSessionException.ERROR_CHATTING_ALREADY:
                        ToastUtils.showToast(LiveActivity.this, R.string.error_chatting_already);        //对方正在与别人连麦
                        break;
                    case ChatSessionException.ERROR_CHATTING_MAX_NUMBER:
                        ToastUtils.showToast(LiveActivity.this, R.string.error_chatting_already);
                        break;
                }
            }
        }

        @Override
        public void showTerminateChattingUI(String playerUID) {
            Log.e(TAG, "showTerminateChattingUI[" + playerUID + "] is called!");
            String key;
            ChattingViewHolder holder;
            if (playerUID == null) {
                Iterator<String> keySetIt = mUsedViewHolderMap.keySet().iterator();
                while (keySetIt.hasNext()) {
                    key = keySetIt.next();
                    holder = mUsedViewHolderMap.get(key);
                    hideSurfaceView(holder.mParterView);
                    holder.mCloseChattingBtn.setVisibility(View.GONE);
                    mFreeHolderMap.put(holder.mIndex, holder);
                }
                mUsedViewHolderMap.clear();
            } else {
                holder = mUsedViewHolderMap.get(playerUID);
                if (holder != null) {
                    hideSurfaceView(holder.mParterView);
                    holder.mCloseChattingBtn.setVisibility(View.GONE);
                    mFreeHolderMap.put(holder.mIndex, holder);
                    mUsedViewHolderMap.remove(playerUID);
                } else {
                    // 被重复调用
//                    throw new RuntimeException("parter view holder is null.");
                }

            }
        }

        @Override
        public void showToast(int msgId) {
            ToastUtils.showToast(LiveActivity.this, msgId);
        }

        @Override
        public void showInfoDialog(String msg) {
            AlertDialog.Builder normalDialog =
                    new AlertDialog.Builder(LiveActivity.this);
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

            normalDialog.show();
        }

        @Override
        public void updateBeautyUI(boolean beautyOn) {
            if (mLiveBottomFragment != null) {
                mLiveBottomFragment.setBeautyUI(true);
            }
        }

        @Override
        public void finishActivity() {
            finish();
        }

        @Override
        public void showImInitFailedDialog(final int tipResID, final int errorType) {
            if (mImInitFailedDialog == null) {
                mImInitFailedDialog = new AlertDialog.Builder(LiveActivity.this)
                        .setTitle(R.string.prompt)
                        .create();
                mImInitFailedDialog.setCanceledOnTouchOutside(false);
                mImInitFailedDialog.setMessage(getString(tipResID));
            }
            switch (errorType) {
                case ImErrorCode.NOT_LOGIN:
                    mImInitFailedDialog.setButton(DialogInterface.BUTTON_POSITIVE,
                            getString(R.string.sure),
                            mImLoginFailedListener);
                    break;
                case ImErrorCode.UNKNOWN:
                    mImInitFailedDialog.setButton(DialogInterface.BUTTON_POSITIVE,
                            getString(R.string.sure),
                            mImInitFailedListener);
                    break;
            }
            mImInitFailedDialog.show();
        }


        @Override
        public void showNoPermissionTip() {
        }

        @Override
        public void showChatCloseNotifyDialog(String name) {
        }


        @Override
        public void showLiveCloseUI() {
            hideInterruptUI();      //隐藏其他的提示UI
            if (mLiveCloseDialog == null) {
                mLiveCloseDialog = LiveCloseDialog.newInstance(getString(R.string.live_cannot_publish));
            }
            if (!mLiveCloseDialog.isShow()) {
                mLiveCloseDialog.show(getSupportFragmentManager(), LiveCloseDialog.TAG);
            }
        }


        @Override
        public void showCloseChatFailedUI() {
            ToastUtils.showToast(LiveActivity.this, R.string.close_chat_failed_for_new_chat);
        }
    };

    // --------------------------------------------------------------------------------------------------------

    @Override
    protected void onResume() {// 因为在onCreate开启了一个Fragment，所以是先走完了Fragment的onStart后才走Activity的onStart，onResume，接着在走Fragment的onResume
        super.onResume();

        mLiveRecordPresenter.startPreview(mPreviewSurfaceView); //开启预览

        // 本Activity需要两个广播起作用
        mConnectivityMonitor.register(getApplicationContext());   // 注册对网络状态的监听
        mHeadsetMonitor.register(getApplicationContext());        // 注册对耳机状态的监听

        //根据设置，判断是否要显示推流性能log
        boolean isShowLogInfo = false;
        if (isShowLogInfo) {
            addLogInfoFragment();
        } else {
            removeLogInfoFragment();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        mConnectivityMonitor.unRegister(getApplicationContext());        //取消对网络状态的监听
        mHeadsetMonitor.unRegister(getApplicationContext());        //取消对耳机状态的监听
        removeLogInfoFragment();  //移除性能Log UI
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mFullEventView.setOnTouchListener(null);
    }


    @Override
    public void onBackPressed() {
//        if (mLivePresenter != null) {
//            if (mLivePresenter.isChatting()) {
//                mLivePresenter.closeLiveChat();
//                mLivePresenter.abortChat(false);
//            }
//            if (mLivePresenter.isLive()) {
//                mLivePresenter.stopPublish();
//                mLivePresenter.closeLive();
//            }
//        }
        finish();
    }

    // **************************************************** 获取推流地址推流成功后的反应 ****************************************************

    /**
     * 变量的描述: 请求网络获取推流地址成功后，回调过来的结果内容
     */
    public CreateLiveFragment.OnPendingPublishListener mPendingPublishListener = new CreateLiveFragment.OnPendingPublishListener() {
        @Override
        public void onPendingPublish(String roomID, String name, String uid) {
            mRoomID = roomID;
            changeUI2Publishing(roomID, name, uid); // 将界面从创建直播切换到正在推流
        }
    };

    /**
     * 方法描述: 将界面从创建直播切换到正在推流
     *
     * @param roomID 请求网络获取推流地址的结果LiveCreateResult对象的mRoomID
     * @param name   请求网络获取推流地址的结果LiveCreateResult对象的mName
     * @param uid    请求网络获取推流地址的结果LiveCreateResult对象的mUid
     */
    private void changeUI2Publishing(String roomID, String name, String uid) {
        mIvClose.setVisibility(View.VISIBLE);

        // 创建InteractionFragment对象
        InteractionFragment interactionFragment = InteractionFragment.newInstance(roomID, name, uid);
        // TODO MNS
        interactionFragment.setImManger(mImManager);

        // 创建LiveBottomFragment对象
        mLiveBottomFragment = LiveBottomFragment.newInstance();
        // 设置在LiveBottomFragment中进行点击事件的监听回调
        mLiveBottomFragment.setRecorderUIClickListener(mUIClickListener);
        mLiveBottomFragment.setOnInviteClickListener(mInviteClickListener);

        // 将创建设置好属性的LiveBottomFragment对象传入InteractionFragment对象中使用
        interactionFragment.setBottomFragment(mLiveBottomFragment);

        // 开启Fragment
        getSupportFragmentManager().beginTransaction().replace(R.id.root_container, interactionFragment).commit();
    }


    /**
     * 变量的描述: 底部操作按钮的click事件响应,具体的就是美颜闪光灯摄像头切换
     */
    private LiveBottomFragment.RecorderUIClickListener mUIClickListener = new LiveBottomFragment.RecorderUIClickListener() {
        @Override
        public int onSwitchCamera() { //切换摄像头
            mLiveRecordPresenter.switchCamera();
            return -1;
        }

        @Override
        public boolean onBeautySwitch() {   // 美颜开/关
            mPreviewSurfaceView.setZOrderOnTop(true);
            return mLiveRecordPresenter.switchBeauty();
        }

        @Override
        public boolean onFlashSwitch() {    // 闪光灯开/关
            return mLiveRecordPresenter.switchFlash();
        }
    };

    /**
     * 变量的描述: 点击邀请按钮的事件响应
     */
    private View.OnClickListener mInviteClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showAnchorListDialog(mRoomID);
        }
    };

    // --------------------------------------------------------------------------------------------------------

    // **************************************************** 邀请连麦的代码 ****************************************************

    private AnchorListDialog mAnchorListDialog;

    /**
     * 方法描述: 显示连麦对象(主播/观众)Dialog
     *
     * @param roomID 请求网络获取推流地址的结果LiveCreateResult对象的mRoomID
     */
    private void showAnchorListDialog(String roomID) {
        if (mAnchorListDialog == null) {
            mAnchorListDialog = AnchorListDialog.newInstance(roomID);
        }
        if (!mAnchorListDialog.isShow()) {
            // 显示对话框
            mAnchorListDialog.show(getSupportFragmentManager(), AnchorListDialog.class.getName());
        }
    }

    /**
     * 关闭连麦对象Dialog
     */
    private void dismissAnchorListDialog() {
        if (mAnchorListDialog != null) {
            mAnchorListDialog.dismiss();
        }
    }

    @Override
    public void onPendingAction(int actionType, Bundle bundle) {
        switch (actionType) {
            case INTERACTION_TYPE_INVITE://用户选择了连麦对象，并且点击选择了要与其连麦的Action
                if (bundle != null) {
                    // 获取从对话框中选择的观众或者直播的数据，从中获取其uid
                    LiveItemResult userData = (LiveItemResult) bundle.getSerializable(AnchorListFragment.KEY_LIVE_ITEM_DATA);
                    ArrayList<String> inviteeUIDs = new ArrayList<>();
                    assert userData != null;
                    inviteeUIDs.add(userData.getUid());
                    // 使用uid的集合去发起邀请连麦的请求
                    mLiveRecordPresenter.inviteChat(inviteeUIDs); // TODO 发起连麦邀请
                }
//                mLiveBottomFragment.setInviteUIEnable(false);  //禁用邀麦的按钮
        }
    }

    // --------------------------------------------------------------------------------------------------------

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
            Intent intent = new Intent(LiveActivity.this, LoginActivity.class);
            startActivity(intent);
        }
    };


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

    /**
     * 增加性能日志展示
     */
    public void addLogInfoFragment() {
        if (mLogInfoFragment == null) {
            mLogInfoFragment = new LogInfoFragment();
            mLogInfoFragment.setRefreshListener(mRefreshListener);
        }
        getSupportFragmentManager().beginTransaction()
                .add(R.id.log_container, mLogInfoFragment)
                .commitAllowingStateLoss();
    }

    /**
     * 移除性能日志展示
     */
    public void removeLogInfoFragment() {
        if (mLogInfoFragment != null) {
            getSupportFragmentManager().beginTransaction().remove(mLogInfoFragment).commit();
        }
    }


    LogInfoFragment.LogRefreshListener mRefreshListener = new LogInfoFragment.LogRefreshListener() {
        @Override
        public void onPendingRefresh() {
            if (mLogInfoFragment != null && mLiveRecordPresenter != null) {
                LogInfoFragment.LogHandler logHandler = mLogInfoFragment.getLogHandler();
                mLiveRecordPresenter.refreshLogInfo(logHandler);
            }
        }
    };


    private void hideSurfaceView(SurfaceView surfaceView) {
        Log.d(TAG, "hide SurfaceView :" + surfaceView.toString());
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) surfaceView.getLayoutParams();
        layoutParams.topMargin = DensityUtil.dp2px(LiveActivity.this, 300);
        surfaceView.requestLayout();
    }

    private void showSurfaceView(SurfaceView surfaceView) {
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) surfaceView.getLayoutParams();
        layoutParams.topMargin = DensityUtil.dp2px(LiveActivity.this, 0);
        surfaceView.requestLayout();
    }


}
