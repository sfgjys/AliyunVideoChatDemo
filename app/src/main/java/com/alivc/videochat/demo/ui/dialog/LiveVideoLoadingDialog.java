package com.alivc.videochat.demo.ui.dialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alivc.videochat.demo.R;

/**
 * Created by liujianghao on 16-8-14.
 */
public class LiveVideoLoadingDialog extends BaseTransparentDialog{
    public static final String TAG = LiveVideoLoadingDialog.class.getName();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_live_video_loading, container, false);
    }

}
