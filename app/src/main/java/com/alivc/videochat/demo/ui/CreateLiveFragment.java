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
import com.alivc.videochat.demo.logic.LifecyclePublisherManager;
import com.alivc.videochat.demo.presenter.ILifecycleCreateLivePresenter;
import com.alivc.videochat.demo.presenter.impl.LifecycleCreateLivePresenterImpl;
import com.alivc.videochat.demo.presenter.view.ICreateLiveView;
import com.alivc.videochat.demo.uitils.ToastUtils;

/**
 * 类的描述: 该Fragment是用于请求网络获取直播推流地址的界面，在直播界面上开启的
 */
public class CreateLiveFragment extends Fragment implements View.OnClickListener {

    /**
     * 变量的描述: 填写直播主题的对话框
     */
    private EditText mEtDesc;
    /**
     * 变量的描述: 开启直播的按钮
     */
    private Button mBtnStartLive;
    /**
     * 变量的描述: 结束Fragment返回Activity的按钮
     */
    private ImageView mBackBtn;
    /**
     * 变量的描述: 摄像头切换的按钮
     */
    private ImageView mSwitchCameraBtn;
    /**
     * 变量的描述: 开启美颜的按钮
     */
    private ImageView mSwitchBeautyBtn;
    private ILifecycleCreateLivePresenter mPresenter;

    private OnPendingPublishListener mPendingPublishListener;

    //TODO：这里如果使用DI来做（比如dagger2）加入注入对象生命周期管理就不需要单独保存一个Mgr对象了，正常情况Mgr应该对UI层透明的
    private LifecyclePublisherManager mPublisherMgr;

    /**
     * 方法描述: 方法内是创建一个本类对象，并且在将参数赋值给本类对象，方便本类对象去进行创建LifecycleCreateLivePresenterImpl对象
     */
    public static CreateLiveFragment newInstance(LifecyclePublisherManager publisherMgr) {
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
        // 当本界面不可见时，停止获取推流地址的网络请求
        mPresenter.onStop();
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
            // 让开启直播的按钮再次可以按，并将结果回调给OnPendingPublishListener接口的实例
            if (mPendingPublishListener != null) {
                mBtnStartLive.setEnabled(true);
                mPendingPublishListener.onPendingPublish(roomID, name, uid);
            }
        }
    };

    public void setPendingPublishListener(OnPendingPublishListener listener) {
        this.mPendingPublishListener = listener;
    }

    /**
     * 类的描述: 将通过请求网络获取到的推流地址通过回调返回给直播界面进行UI更新
     */
    public interface OnPendingPublishListener {
        void onPendingPublish(String roomID, String name, String uid);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter = null;
    }
}
