package com.alivc.videochat.demo.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

/**
 * Created by apple on 2016/12/7.
 */

public class HeadsetMonitor extends BroadcastReceiver{
    private static final String TAG = HeadsetMonitor.class.getName();
    private static final int MSG_WHAT_HEADSET_ON = 1;
    private static final int MSG_WHAT_HEADSET_OFF = 2;

    private IntentFilter mIntentFilter = null;
    private HeadSetStatusChangedListener mListener = null;

    public HeadsetMonitor() {
        mIntentFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
    }

    public void register(Context context) {
        context.registerReceiver(this, mIntentFilter);
    }

    public void unRegister(Context context) {
        context.unregisterReceiver(this);
    }

    public void setHeadsetStatusChangedListener(HeadSetStatusChangedListener listener) {
        this.mListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        AudioManager localAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        if(localAudioManager.isWiredHeadsetOn()) {
            Log.d(TAG, "耳机插入");
            mHandler.sendEmptyMessage(MSG_WHAT_HEADSET_ON);
        }else {
            mHandler.sendEmptyMessage(MSG_WHAT_HEADSET_OFF);
            Log.d(TAG, "耳机拔出");
        }
    }

    private Handler mHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_WHAT_HEADSET_ON:
                    if(mListener != null) {
                        mListener.onHeadsetStatusChanged(true);
                    }
                    break;
                case MSG_WHAT_HEADSET_OFF:
                    if(mListener != null) {
                        mListener.onHeadsetStatusChanged(false);
                    }
                    break;
            }
        }
    };

    public interface HeadSetStatusChangedListener{
        void onHeadsetStatusChanged(boolean on);
    }
}
