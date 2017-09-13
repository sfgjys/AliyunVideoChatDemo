package com.alivc.videochat.demo.ui.dialog;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;

import com.alivc.videochat.demo.R;

/**
 * 类的描述: 在对话框类重写展示和关闭对话框的方法，在方法中添加进isShow变量来确定对话框是否开启
 * 技巧:
 * 对于DialogFragment对话框而言，有时我们需要定制我们自己想要的对话框类型，不需要系统默认的样式。
 * 如此我们就可以调用DialogFragment的setStyle(int style,int theme)方法来定制我们自己需要的样式。
 * 注意style必须在onCreateView生命周期之前被调用，否则没有效果，而onCreate方法在onCreateView之前，
 * 所以可以在onCreate中调用setStyle(int style,int theme)方法，例子如下
 */
public class BaseTransparentDialog extends DialogFragment {
    private boolean isShow = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 参数一有几种常见的:STYLE_NORMAL(正常), STYLE_NO TITLE(没有标题), STYLE_NO FRAME(没有框架), STYLE_NO INPUT(没有输入).
        // 参数二可选的自定义主题。如果是0，将为您选择一个合适的主题(基于样式)。
        // 这里设置的样式就是没有边框的背景透明的对话框
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.LiveClose);
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        super.show(manager, tag);
        isShow = true;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        isShow = false;
    }

    public boolean isShow() {
        return isShow;
    }
}
