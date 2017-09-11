package com.alivc.videochat.demo.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import static android.media.AudioManager.GET_DEVICES_ALL;

/**
 * 变量的描述: 插拔耳麦的监听广播
 */
public class HeadsetMonitor extends BroadcastReceiver {
    private static final String TAG = HeadsetMonitor.class.getName();
    private static final int MSG_WHAT_HEADSET_ON = 1;
    private static final int MSG_WHAT_HEADSET_OFF = 2;

    private IntentFilter mIntentFilter = null;
    private HeadSetStatusChangedListener mListener = null;

    public HeadsetMonitor() {
        // 用于区分插拔耳麦广播的删选器
        mIntentFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
    }

    public void register(Context context) {
        // 注册
        context.registerReceiver(this, mIntentFilter);
    }

    public void unRegister(Context context) {
        // 注销
        context.unregisterReceiver(this);
    }

    /**
     * 变量的描述: 设置耳麦插拔改变时被调用的回调接口实例
     */
    public void setHeadsetStatusChangedListener(HeadSetStatusChangedListener listener) {
        this.mListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        if (audioManager.isWiredHeadsetOn()) {
            Log.d(TAG, "耳机插入");
            mHandler.sendEmptyMessage(MSG_WHAT_HEADSET_ON);
        } else {
            mHandler.sendEmptyMessage(MSG_WHAT_HEADSET_OFF);
            Log.d(TAG, "耳机拔出");
        }

        // 需要 API 23
//        AudioDeviceInfo[] devices = audioManager.getDevices(AudioManager.GET_DEVICES_ALL);
//        for (AudioDeviceInfo audioDeviceInfo : devices) {
//            audioDeviceInfo.isSink();// 是否是输出设备
//            audioDeviceInfo.isSource();// 是否是输入设备
//            audioDeviceInfo.getType();// 获取音频设备的类型标识 如：TYPE_BUILTIN_SPEAKER(系统内置扬声器的类型标识)
//        }
    }

    /**
     * 变量的描述: 实例化主线程下的Handler，这样在监听回调中就可以更新UI了
     */
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_WHAT_HEADSET_ON:
                    if (mListener != null) {
                        mListener.onHeadsetStatusChanged(true);
                    }
                    break;
                case MSG_WHAT_HEADSET_OFF:
                    if (mListener != null) {
                        mListener.onHeadsetStatusChanged(false);
                    }
                    break;
            }
        }
    };

    public interface HeadSetStatusChangedListener {
        void onHeadsetStatusChanged(boolean on);
    }
}
