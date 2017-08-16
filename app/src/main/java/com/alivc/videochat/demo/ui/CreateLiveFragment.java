package com.alivc.videochat.demo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.alivc.videochat.demo.R;
import com.alivc.videochat.demo.logic.LifecyclePublisherMgr;
import com.alivc.videochat.demo.presenter.ILifecycleCreateLivePresenter;
import com.alivc.videochat.demo.presenter.impl.LifecycleCreateLivePresenterImpl;
import com.alivc.videochat.demo.presenter.view.ICreateLiveView;
import com.alivc.videochat.demo.uitils.ToastUtils;

/**
 * 类的描述: 该Fragment是SurfaceView控件表层的一些控制直播连麦的按钮，礼物等操作的界面
 */
public class CreateLiveFragment extends Fragment implements View.OnClickListener {

    private EditText mEtDesc;
    private Button mBtnStartLive;
    private ImageView mBackBtn;
    private ImageView mSwitchCameraBtn;
    private ImageView mSwitchBeautyBtn;

    private OnPendingPublishListener mPendingPublishListener;
    private ILifecycleCreateLivePresenter mPresenter;

    //TODO：这里如果使用DI来做（比如dagger2）加入注入对象生命周期管理就不需要单独保存一个Mgr对象了，正常情况Mgr应该对UI层透明的
    private LifecyclePublisherMgr mPublisherMgr;

    public static CreateLiveFragment newInstance(LifecyclePublisherMgr publisherMgr) {
        CreateLiveFragment fragment = new CreateLiveFragment();
        fragment.mPublisherMgr = publisherMgr;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter = new LifecycleCreateLivePresenterImpl(getContext(), mPublisherMgr, mView);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_live, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mEtDesc = (EditText) view.findViewById(R.id.et_desc);
        mBtnStartLive = (Button) view.findViewById(R.id.btn_start_live);
        mBtnStartLive.setOnClickListener(this);
        mBackBtn = (ImageView) view.findViewById(R.id.iv_back);
        mBackBtn.setOnClickListener(this);
        mSwitchBeautyBtn = (ImageView) view.findViewById(R.id.switch_beauty);
        mSwitchBeautyBtn.setOnClickListener(this);
        mSwitchCameraBtn = (ImageView) view.findViewById(R.id.switch_camera);
        mSwitchCameraBtn.setOnClickListener(this);
    }


    @Override
    public void onStop() {
        super.onStop();
        mPresenter.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter = null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start_live:
                String uid = ((LiveActivity) getActivity()).getUid();
                if (TextUtils.isEmpty(uid)) {
                    ToastUtils.showToast(getContext(), R.string.not_login_tip);
                    gotoLogin();
                } else {
                    mBtnStartLive.setEnabled(false);
                    mPresenter.createLive(mEtDesc.getText().toString());
                }
                break;
            case R.id.iv_back: {
                getActivity().finish();
            }
            break;
            case R.id.switch_beauty: {
                mPresenter.switchBeauty();
            }
            break;
            case R.id.switch_camera: {
                mPresenter.switchCamera();
            }
            break;
        }
    }

    private void gotoLogin() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
    }


    ICreateLiveView mView = new ICreateLiveView() {

        @Override
        public void showPublishStreamUI(String roomID, String name, String uid) {
            if (mPendingPublishListener != null) {
                mBtnStartLive.setEnabled(true);
                mPendingPublishListener.onPendingPublish(roomID, name, uid);
            }
        }
    };

    public void setPendingPublishListener(OnPendingPublishListener listener) {
        this.mPendingPublishListener = listener;
    }

    public interface OnPendingPublishListener {
        void onPendingPublish(String roomID, String name,
                              String uid);
    }
}
