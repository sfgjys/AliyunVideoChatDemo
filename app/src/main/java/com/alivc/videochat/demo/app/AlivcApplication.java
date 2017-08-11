package com.alivc.videochat.demo.app;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

/**
 * TODO 整个类都没什么具体用处
 */
public class AlivcApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // 注册一个广播
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(new ConnectivityChangedReceiver(), filter);
    }

    class ConnectivityChangedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 该广播没用
        }
    }

}
