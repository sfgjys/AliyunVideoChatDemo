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
 * Created by liujianghao on 16-8-3.
 */
public class WatchBottomFragment
        extends ActionFragment
        implements TextView.OnEditorActionListener,
        View.OnClickListener {

    private EditText mEtComment;
    private ImageView mIvComment;
    private ImageView mIvLike;
    private ImageView mIvCallAnchor;
    private ImageView mIvCamera;
    private ImageView mIvBeauty;
    private ImageView mIvFlash;

    private String mRoomID;
    private String mUID;

    private WatchBottomPresenter mWatchBottomPresenter;

    private LiveBottomFragment.RecorderUIClickListener mRecordUIClickListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_watch_bottom, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = getArguments();
        mRoomID = args.getString(ExtraConstant.EXTRA_ROOM_ID);
        mUID = ((BaseActivity) getActivity()).getUid();

        mEtComment = (EditText) view.findViewById(R.id.et_comment);
        mIvComment = (ImageView) view.findViewById(R.id.iv_comment);
        mIvLike = (ImageView) view.findViewById(R.id.iv_like);
        mIvCallAnchor = (ImageView) view.findViewById(R.id.iv_call_anchor);
        mIvCamera = (ImageView) view.findViewById(R.id.iv_camera);
        mIvBeauty = (ImageView) view.findViewById(R.id.iv_beauty);
        mIvFlash = (ImageView) view.findViewById(R.id.iv_flash);
        mIvComment.setOnClickListener(this);
        mIvLike.setOnClickListener(this);
        mIvCallAnchor.setOnClickListener(this);
        mIvCamera.setOnClickListener(this);
        mIvBeauty.setOnClickListener(this);
        mIvFlash.setOnClickListener(this);
        mEtComment.setOnEditorActionListener(this);
        mEtComment.getViewTreeObserver().addOnGlobalLayoutListener(mOnGlobalLayoutListener);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWatchBottomPresenter = new WatchBottomPresenter(getContext());
    }


    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        switch (actionId) {
            case EditorInfo.IME_ACTION_SEND:
                String comment = mEtComment.getText().toString();
                if (!TextUtils.isEmpty(comment)) {
                    mWatchBottomPresenter.sendComment(mUID, mRoomID, comment);
                    mEtComment.setText("");
                    hideCommentEditUI();
                }

                break;
        }
        return false;
    }


    public static WatchBottomFragment newInstance(String roomID, String anchorUID) {
        WatchBottomFragment fragment = new WatchBottomFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ExtraConstant.EXTRA_ROOM_ID, roomID);
        bundle.putString(ExtraConstant.EXTRA_ANCHOR_UID, anchorUID);
        fragment.setArguments(bundle);
        return fragment;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_comment:
                showCommentEditUI();
                break;
            case R.id.iv_like:
                mWatchBottomPresenter.sendLike(mRoomID, mUID);
                ((InteractionFragment) getParentFragment()).showLikeUI("", false, true);
                break;
            case R.id.iv_call_anchor:
                mActionListener.onPendingAction(WatchLiveActivity.INTERACTION_TYPE_INVITE, null);
                break;
            case R.id.iv_camera:
                if (mRecordUIClickListener != null) {
                    mRecordUIClickListener.onSwitchCamera();
                }
                break;
            case R.id.iv_beauty:
                if (mRecordUIClickListener != null) {
                    mIvBeauty.setActivated(mRecordUIClickListener.onBeautySwitch());
                }
                break;
            case R.id.iv_flash:
                if (mRecordUIClickListener != null) {
                    mRecordUIClickListener.onFlashSwitch();
                }
                break;
        }
    }


    public void setRecordUIClickListener(LiveBottomFragment.RecorderUIClickListener listener) {
        this.mRecordUIClickListener = listener;
    }


    public void showRecordView() {
        mIvBeauty.setVisibility(View.VISIBLE);
        mIvCamera.setVisibility(View.VISIBLE);
        mIvFlash.setVisibility(View.VISIBLE);
//        mIvCallAnchor.setEnabled(false);
    }

    /**
     * 隐藏推流时相关的操作UI（切换摄像头、美颜开关、闪光灯开关）
     */
    public void hideRecordView() {
        mIvBeauty.setVisibility(View.GONE);
        mIvCamera.setVisibility(View.GONE);
        mIvFlash.setVisibility(View.GONE);
//        mIvCallAnchor.setEnabled(true);
    }

    public void showCallAnchor(){
        mIvCallAnchor.setVisibility(View.VISIBLE);
    }

    public void hideCallAnchor() {
        mIvCallAnchor.setVisibility(View.INVISIBLE);
    }

    /**
     * 显示软键盘
     */
    private void showSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mEtComment, 0);
    }

    /**
     * 隐藏软键盘
     */
    private void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEtComment.getWindowToken(), 0);
    }

    /**
     * 显示评论编辑器
     */
    private void showCommentEditUI() {
        mEtComment.setVisibility(View.VISIBLE);
        mIvComment.setVisibility(View.GONE);
        mIvLike.setVisibility(View.GONE);
        mEtComment.post(openKeyboardRunnable);
        mEtComment.requestFocus();
    }

    /**
     * 隐藏评论编辑器
     */
    private void hideCommentEditUI() {
        hideSoftKeyboard();
        mEtComment.clearFocus();
        mEtComment.setVisibility(View.GONE);
        mIvComment.setVisibility(View.VISIBLE);
        mIvLike.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPause() {
        super.onPause();
        mOnGlobalLayoutListener.hasShowInputMethod = false;
    }


    private Runnable openKeyboardRunnable = new Runnable() {
        @Override
        public void run() {
            showSoftKeyboard();
        }
    };

    private InputMethodUIListener
            mOnGlobalLayoutListener = new InputMethodUIListener();



    /**
     * 通过对比RootView的height和windowVisibleDisplayFrame的bottom来计算root view的不可见高度
     * 如果不可见高度是0说明键盘处于隐藏的状态，反之属于弹出的状态
     */
    class InputMethodUIListener implements ViewTreeObserver.OnGlobalLayoutListener {
        boolean hasShowInputMethod = false;

        @Override
        public void onGlobalLayout() {
            Rect rootRect = new Rect();
            View view = getActivity().getWindow().getDecorView();
            view.getWindowVisibleDisplayFrame(rootRect);

            int rootInvisibleHeight = view.getRootView().getHeight() - rootRect.bottom;

            Log.d("GlobalLayout", "decorView.top = "
                    + view.getTop() + ", decorView.bottom = "
                    + view.getBottom() + ", viewHeight = " + rootInvisibleHeight);
            if (hasShowInputMethod && rootInvisibleHeight == 0) {//软件盘隐藏
                hideCommentEditUI();
                hasShowInputMethod = false;
            } else if (rootInvisibleHeight > 0) {
                hasShowInputMethod = true;
            }
        }
    }


}


