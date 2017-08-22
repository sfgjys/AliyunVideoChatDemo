package com.alivc.videochat.demo.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.alivc.videochat.demo.R;

/**
 * 类的描述: 直播界面中底部按键
 */
public class LiveBottomFragment extends Fragment implements View.OnClickListener {

    private ImageView mIvBeauty;
    private ImageView mIvCamera;
    private ImageView mIvFlash;
    private ImageView mIvInvite;

    private RecorderUIClickListener mUIClickListener;
    private View.OnClickListener mInviteClickListener;

    public static LiveBottomFragment newInstance() {
        return new LiveBottomFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_live_bottom, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mIvBeauty = (ImageView) view.findViewById(R.id.iv_beauty);
        mIvCamera = (ImageView) view.findViewById(R.id.iv_camera);
        mIvFlash = (ImageView) view.findViewById(R.id.iv_flash);
        mIvInvite = (ImageView) view.findViewById(R.id.iv_invite);
        mIvBeauty.setActivated(true);
        mIvCamera.setOnClickListener(this);
        mIvBeauty.setOnClickListener(this);
        mIvFlash.setOnClickListener(this);
        mIvInvite.setOnClickListener(this);
    }

    // **************************************************** 调用传递进来的监听接口实例的方法来响应按钮的点击事件 ****************************************************

    public void setRecorderUIClickListener(RecorderUIClickListener listener) {
        this.mUIClickListener = listener;
    }

    public void setOnInviteClickListener(View.OnClickListener listener) {
        this.mInviteClickListener = listener;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_camera:
                if (mUIClickListener != null) {
                    mUIClickListener.onSwitchCamera();
                }
                break;
            case R.id.iv_beauty:
                if (mUIClickListener != null) {
                    mIvBeauty.setActivated(mUIClickListener.onBeautySwitch());
                }
                break;
            case R.id.iv_flash:
                if (mUIClickListener != null) {
                    mUIClickListener.onFlashSwitch();
                }
                break;
            case R.id.iv_invite:
                if (mInviteClickListener != null) {
                    mInviteClickListener.onClick(v);
                }
                break;
        }
    }

    // --------------------------------------------------------------------------------------------------------

    // **************************************************** 让其他地方可以通过本Fragment的实例对象控制按钮的属性 ****************************************************

    public void setBeautyUI(boolean beautyOn) {
        if (mIvBeauty != null) {
            mIvBeauty.setActivated(beautyOn);
        }
    }

    public void setInviteUIEnable(boolean enable) {
        if (mIvInvite != null) {
            mIvInvite.setEnabled(enable);
        }
    }

    // --------------------------------------------------------------------------------------------------------

    public interface RecorderUIClickListener {
        /**
         * 方法描述: 摄像头切换
         */
        int onSwitchCamera();

        /**
         * 方法描述: 美颜
         */
        boolean onBeautySwitch();

        /**
         * 方法描述: 闪光灯
         */
        boolean onFlashSwitch();
    }
}
