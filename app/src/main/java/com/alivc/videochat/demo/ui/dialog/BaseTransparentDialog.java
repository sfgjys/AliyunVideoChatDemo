package com.alivc.videochat.demo.ui.dialog;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;

import com.alivc.videochat.demo.R;

/**
 * 类的描述: 在对话框类重写展示和关闭对话框的方法，在方法中添加进isShow变量来确定对话框是否开启
 */
public class BaseTransparentDialog extends DialogFragment {
    private boolean isShow = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.LiveClose);
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        super.show(manager, tag);
        isShow = true;
    }

    public boolean isShow() {
        return isShow;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        isShow = false;
    }
}
