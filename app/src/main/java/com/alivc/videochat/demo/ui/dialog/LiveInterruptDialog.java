package com.alivc.videochat.demo.ui.dialog;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alivc.videochat.demo.R;

/**
 * Created by Liujianghao on 2016/1/15.
 */
public class LiveInterruptDialog extends BaseTransparentDialog {
    public static final String TAG = LiveInterruptDialog.class.getName();
    public static final String EXTRA_INTERRUPT_TIP = "extra_interrupt_tip";
    private ImageView mIvBackground;
    private TextView mTvInterruptTip;
    private String mTipText;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args =  getArguments();
        mTipText = args.getString(EXTRA_INTERRUPT_TIP);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_live_interrupt, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mIvBackground = (ImageView) view.findViewById(R.id.iv_background);
        ((AnimationDrawable)mIvBackground.getBackground()).start();
        mTvInterruptTip = (TextView) view.findViewById(R.id.tv_interrupt_tip);
        mTvInterruptTip.setText(mTipText);
    }

    public static LiveInterruptDialog newInstance(String tipText) {
        Bundle args = new Bundle();
        args.putString(EXTRA_INTERRUPT_TIP, tipText);
        LiveInterruptDialog fragment = new LiveInterruptDialog();
        fragment.setArguments(args);
        return fragment;
    }

    public void setTip(String tip) {
        if(mTvInterruptTip != null
                && mTvInterruptTip.isActivated()) {
            mTipText = tip;
            mTvInterruptTip.setText(mTipText);
        }
    }
}
