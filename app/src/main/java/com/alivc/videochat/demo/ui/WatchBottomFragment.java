package com.alivc.videochat.demo.ui;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.alivc.videochat.demo.R;
import com.alivc.videochat.demo.base.ActionFragment;
import com.alivc.videochat.demo.base.BaseActivity;
import com.alivc.videochat.demo.presenter.WatchBottomPresenter;

/**
 * 类的描述: 观众端的观看交互界面的底部Fragment
 */
public class WatchBottomFragment extends ActionFragment implements View.OnClickListener {

    // TODO 软键盘把视频界面上顶了


    /**
     * 方法描述: 创建WatchBottomFragment对象。并将参数设置进Bundle，进行传递
     *
     * @param roomID    请求网络获取推流地址的结果LiveCreateResult对象的mRoomID
     * @param anchorUID 请求网络获取推流地址的结果LiveCreateResult对象的mUid
     */
    public static WatchBottomFragment newInstance(String roomID, String anchorUID) {
        WatchBottomFragment fragment = new WatchBottomFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ExtraConstant.EXTRA_ROOM_ID, roomID);
        bundle.putString(ExtraConstant.EXTRA_ANCHOR_UID, anchorUID);
        fragment.setArguments(bundle);
        return fragment;
    }

    /**
     * 变量的描述: 底部按钮事件监听回调接口实例
     */
    private LiveBottomFragment.RecorderUIClickListener mRecordUIClickListener;

    /**
     * 方法描述: 将底部按钮事件监听回调接口实例传递过来
     */
    public void setRecordUIClickListener(LiveBottomFragment.RecorderUIClickListener listener) {
        this.mRecordUIClickListener = listener;
    }

    /**
     * 变量的描述: 聊天评价编辑框
     */
    private EditText mEtComment;
    /**
     * 变量的描述: 开启编辑框的按钮
     */
    private ImageView mIvComment;
    /**
     * 变量的描述: 点赞按钮
     */
    private ImageView mIvLike;
    /**
     * 变量的描述: 邀请连麦按钮
     */
    private ImageView mIvCallAnchor;
    /**
     * 变量的描述: 切换摄像头的按钮
     */
    private ImageView mIvCamera;
    /**
     * 变量的描述: 美颜按钮
     */
    private ImageView mIvBeauty;
    /**
     * 变量的描述: 开关闪光灯按钮
     */
    private ImageView mIvFlash;

    private String mRoomID;
    private String mUID;

    private WatchBottomPresenter mWatchBottomPresenter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWatchBottomPresenter = new WatchBottomPresenter(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_watch_bottom, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // 获取数据交互需要的参数
        Bundle args = getArguments();
        mRoomID = args.getString(ExtraConstant.EXTRA_ROOM_ID);
        mUID = ((BaseActivity) getActivity()).getUid();

        // 初始化控件
        mEtComment = (EditText) view.findViewById(R.id.et_comment);
        mIvComment = (ImageView) view.findViewById(R.id.iv_comment);
        mIvLike = (ImageView) view.findViewById(R.id.iv_like);
        mIvCallAnchor = (ImageView) view.findViewById(R.id.iv_call_anchor);
        mIvCamera = (ImageView) view.findViewById(R.id.iv_camera);
        mIvBeauty = (ImageView) view.findViewById(R.id.iv_beauty);
        mIvFlash = (ImageView) view.findViewById(R.id.iv_flash);

        // 设置控件的点击事件
        mIvComment.setOnClickListener(this);
        mIvLike.setOnClickListener(this);
        mIvCallAnchor.setOnClickListener(this);
        mIvCamera.setOnClickListener(this);
        mIvBeauty.setOnClickListener(this);
        mIvFlash.setOnClickListener(this);
        mEtComment.setOnEditorActionListener(new TextView.OnEditorActionListener() {// 设置在我们编辑完之后点击软键盘上的回车键才会触发的监听事件
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_SEND:// 按下了发送键，但是有兼容性问题
                        // 获取要发送的内容
                        String comment = mEtComment.getText().toString();
                        // 判断是否为空
                        if (!TextUtils.isEmpty(comment)) {
                            // TODO 发送评论
                            mWatchBottomPresenter.sendComment(mUID, mRoomID, comment);
                            // 归零编辑框
                            mEtComment.setText("");
                            // 隐藏编辑框
                            hideCommentEditUI();
                        }
                        break;
                }
                return false;
            }
        });

        mEtComment.getViewTreeObserver().addOnGlobalLayoutListener(mOnGlobalLayoutListener);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_comment:
                // 显示评论编辑框
                showCommentEditUI();
                break;
            case R.id.iv_like:
                // TODO 发送点赞
                mWatchBottomPresenter.sendLike(mRoomID, mUID);
                // TODO 显示点赞动画
                ((InteractionFragment) getParentFragment()).showLikeUI("", false, true);
                break;
            case R.id.iv_call_anchor:
                // 开启连麦请求，具体操作在开启该Fragment的Activity中实现
                mActionListener.onPendingAction(WatchLiveActivity.INTERACTION_TYPE_INVITE, null);
                break;
            case R.id.iv_camera:
                // 用回调接口切换摄像头
                if (mRecordUIClickListener != null) {
                    mRecordUIClickListener.onSwitchCamera();
                }
                break;
            case R.id.iv_beauty:
                // 用回调接口开启美颜
                if (mRecordUIClickListener != null) {
                    mIvBeauty.setActivated(mRecordUIClickListener.onBeautySwitch());
                }
                break;
            case R.id.iv_flash:
                // 用回调接口开关闪光灯
                if (mRecordUIClickListener != null) {
                    mRecordUIClickListener.onFlashSwitch();
                }
                break;
        }
    }

    // --------------------------------------------------------------------------------------------------------

    /**
     * 方法描述: 显示软键盘
     */
    private void showSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mEtComment, 0);
    }

    private Runnable openKeyboardRunnable = new Runnable() {
        @Override
        public void run() {
            showSoftKeyboard();
        }
    };

    /**
     * 方法描述: 显示评论编辑器
     */
    private void showCommentEditUI() {
        mEtComment.setVisibility(View.VISIBLE);
        mIvComment.setVisibility(View.GONE);
        mIvLike.setVisibility(View.GONE);
        mEtComment.post(openKeyboardRunnable);// 异步开启软键盘
        mEtComment.requestFocus();
    }

    /**
     * 方法描述: 隐藏软键盘
     */
    private void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEtComment.getWindowToken(), 0);
    }

    /**
     * 方法描述: 隐藏评论编辑器  隐藏的情景：1，发送评论后 2，TODO
     */
    private void hideCommentEditUI() {
        hideSoftKeyboard();
        mEtComment.clearFocus();
        mEtComment.setVisibility(View.GONE);
        mIvComment.setVisibility(View.VISIBLE);
        mIvLike.setVisibility(View.VISIBLE);
    }

    // --------------------------------------------------------------------------------------------------------

    @Override
    public void onPause() {
        super.onPause();
        mOnGlobalLayoutListener.hasShowInputMethod = false;
    }

    // --------------------------------------------------------------------------------------------------------

    private InputMethodUIListener mOnGlobalLayoutListener = new InputMethodUIListener();

    /**
     * 通过对比RootView的height和windowVisibleDisplayFrame的bottom来计算root view的不可见高度
     * 如果不可见高度是0说明键盘处于隐藏的状态，反之属于弹出的状态
     */
    class InputMethodUIListener implements ViewTreeObserver.OnGlobalLayoutListener {
        /**
         * 变量的描述: 判断键盘处于何种状态，true属于弹出的状态，false处于隐藏的状态
         */
        boolean hasShowInputMethod = false;

        /**
         * 方法描述: 当mEtComment的布局位置发送了改变，会回调这个方法
         */
        @Override
        public void onGlobalLayout() {
            // 创建一个矩形类对象，其left,top,right,bottom如图(res->drawable-hdpi->)所示
            Rect rootRect = new Rect();
            // decorView是window中的最顶层view
            View decorView = getActivity().getWindow().getDecorView();
            // getWindowVisibleDisplayFrame方法可以使得方法的参数获取到程序显示的区域，包括标题栏，但不包括状态栏。
            // 这里就是为了获取软键盘显示后程序剩余面积的高度
            decorView.getWindowVisibleDisplayFrame(rootRect);

            int rootInvisibleHeight = decorView.getRootView().getHeight() - rootRect.bottom;

            Log.d("GlobalLayout", "decorView.top = " + decorView.getTop() + ", decorView.bottom = " + decorView.getBottom() + ", viewHeight = " + rootInvisibleHeight);

            // 如果rootInvisibleHeight等于0，说明软键盘隐藏了，就要隐藏mEtComment编辑框
            if (hasShowInputMethod && rootInvisibleHeight == 0) {
                hideCommentEditUI();
                hasShowInputMethod = false;
            } else if (rootInvisibleHeight > 0) {
                hasShowInputMethod = true;
            }
        }
    }

    // **************************************************** 对外开发的方法操作本界面的UI ****************************************************

    /**
     * 方法描述: 显示 美颜 摄像头 闪光灯 按钮
     */
    public void showRecordView() {
        mIvBeauty.setVisibility(View.VISIBLE);
        mIvCamera.setVisibility(View.VISIBLE);
        mIvFlash.setVisibility(View.VISIBLE);
//        mIvCallAnchor.setEnabled(false);
    }

    /**
     * 方法描述: 隐藏推流时相关的操作UI（切换摄像头、美颜开关、闪光灯开关）
     */
    public void hideRecordView() {
        mIvBeauty.setVisibility(View.GONE);
        mIvCamera.setVisibility(View.GONE);
        mIvFlash.setVisibility(View.GONE);
//        mIvCallAnchor.setEnabled(true);
    }

    /**
     * 方法描述: 显示连麦按钮
     */
    public void showCallAnchor() {
        mIvCallAnchor.setVisibility(View.VISIBLE);
    }

    /**
     * 方法描述: 隐藏连麦按钮
     */
    public void hideCallAnchor() {
        mIvCallAnchor.setVisibility(View.INVISIBLE);
    }
}


