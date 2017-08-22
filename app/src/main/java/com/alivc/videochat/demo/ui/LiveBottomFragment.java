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
 * Created by liujianghao on 16-8-10.
 */
public class LiveBottomFragment extends Fragment implements View.OnClickListener {
    private ImageView mIvBeauty;
    private ImageView mIvCamera;
    private ImageView mIvFlash;
    private ImageView mIvInvite;

    private RecorderUIClickListener mUIClickListener;
    private View.OnClickListener mInviteClickListener;

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


    public static LiveBottomFragment newInstance() {
        return new LiveBottomFragment();
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

    public void setRecorderUIClickListener(RecorderUIClickListener listener) {
        this.mUIClickListener = listener;
    }

    public void setOnInviteClickListener(View.OnClickListener listener) {
        this.mInviteClickListener = listener;
    }

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


    public interface RecorderUIClickListener {
        /**
         * switch camera
         *
         * @return current camera id
         */
        int onSwitchCamera();

        /**
         * switch beauty
         *
         * @return true: beauty on , false: beauty off
         */
        boolean onBeautySwitch();

        /**
         * switch flash
         *
         * @return true: flash on, false: flash off;
         */
        boolean onFlashSwitch();
    }
}
