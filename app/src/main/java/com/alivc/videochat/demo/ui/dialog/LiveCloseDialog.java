package com.alivc.videochat.demo.ui.dialog;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alivc.videochat.demo.R;

/**
 * 类的描述: 直播结束时显示的Ui对话框
 */
public class LiveCloseDialog extends BaseTransparentDialog implements View.OnClickListener {
    public static final String TAG = LiveCloseDialog.class.getName();
    private static final String EXTRA_MESSAGE = "extra-message";

    private TextView mTvBack;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    backClick();
                    return true;
                } else {
                    return false;
                }
            }
        });
        return inflater.inflate(R.layout.dialog_fragment_live_close, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTvBack = (TextView) view.findViewById(R.id.tv_back);
        TextView tvFinish = (TextView) view.findViewById(R.id.tv_finished);
        tvFinish.setText(getArguments().getString(EXTRA_MESSAGE, getString(R.string.live_finished)));
        mTvBack.setOnClickListener(this);
    }

    public void backClick() {
        mTvBack.callOnClick();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_back:
                getActivity().finish();
                break;
        }
    }

    public static LiveCloseDialog newInstance(String message) {
        LiveCloseDialog dialog = new LiveCloseDialog();
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_MESSAGE, message);
        dialog.setArguments(bundle);
        return dialog;
    }
}
