package com.alivc.videochat.demo.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by apple on 2016/12/2.
 */

public class ConnectivityMonitor extends BroadcastReceiver {
    private static final String TAG = ConnectivityMonitor.class.getName();

    private static final int MSG_WHAT_NETWORK_ONLINE = 1;
    private static final int MSG_WHAT_NETWORK_OFFLINE = 2;

    private List<ConnectivityChangedListener> mChangedListeners = new ArrayList<>();
    private IntentFilter mFilter = new IntentFilter();

    public ConnectivityMonitor() {
        mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
    }

    public void register(Context context) {
        context.registerReceiver(this, mFilter);
    }

    public void unRegister(Context context) {
        context.unregisterReceiver(this);
    }

    public void addConnectivityStatusChangedListener(ConnectivityChangedListener listener) {
        this.mChangedListeners.add(listener);
    }

    public void removeConnectivityStatusChangedListener(ConnectivityChangedListener listener) {
        this.mChangedListeners.remove(listener);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if (isConnected) {
            Log.d(TAG, "网络链接畅通");
            eventHandler.sendEmptyMessage(MSG_WHAT_NETWORK_ONLINE);
            switch (activeNetwork.getType()) {
                case ConnectivityManager.TYPE_WIFI:
                    Log.d(TAG, "当前网络类型为WIFI--TYPE_WIFI");
                    break;
                case ConnectivityManager.TYPE_BLUETOOTH:
                    Log.d(TAG, "当前网络类型为蓝牙--TYPE_BLUETOOTH");
                    break;
                case ConnectivityManager.TYPE_VPN:
                    Log.d(TAG, "当前网络类型为VPN代理--TYPE_VPN");
                    break;
                case ConnectivityManager.TYPE_DUMMY:
                    Log.d(TAG, "当前网络类型为TYPE_DUMMY");
                    break;
                case ConnectivityManager.TYPE_ETHERNET:
                    Log.d(TAG, "当前网络类型为以太网--TYPE_ETHERNET");
                    break;
                case ConnectivityManager.TYPE_WIMAX:
                    Log.d(TAG, "当前网络类型为TYPE_WIMAX");
                    break;
                case ConnectivityManager.TYPE_MOBILE:
                    detectMobileNetworkType(activeNetwork);
                    break;
                case ConnectivityManager.TYPE_MOBILE_DUN:
                    Log.d(TAG, "当前网络类型为TYPE_MOBILE_DUN");
                    break;
                default:
                    Log.d(TAG, "当前网络类型未知：" + activeNetwork.getTypeName());
            }
        } else {
            Log.d(TAG, "当前网络断开");
            eventHandler.sendEmptyMessage(MSG_WHAT_NETWORK_OFFLINE);
        }
    }

    private void detectMobileNetworkType(NetworkInfo activeNetwork) {
        String strSubTypeName = activeNetwork.getSubtypeName();
        switch (activeNetwork.getSubtype()) {
            //如果是2g类型
            case TelephonyManager.NETWORK_TYPE_GPRS: // 联通2g
            case TelephonyManager.NETWORK_TYPE_CDMA: // 电信2g
            case TelephonyManager.NETWORK_TYPE_EDGE: // 移动2g
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                Log.d(TAG, "当前网络类型为2G网络");
                break;
            //如果是3g类型
            case TelephonyManager.NETWORK_TYPE_EVDO_A: // 电信3g
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                Log.d(TAG, "当前网络类型为3G网络");
                break;
            //如果是4g类型
            case TelephonyManager.NETWORK_TYPE_LTE:
                Log.d(TAG, "当前网络类型为4G网络");
                break;
            default:
                //中国移动 联通 电信 三种3G制式
                if (strSubTypeName.equalsIgnoreCase("TD-SCDMA") || strSubTypeName.equalsIgnoreCase("WCDMA") || strSubTypeName.equalsIgnoreCase("CDMA2000")) {
                    Log.d(TAG, "当前网络类型为3G网络");
                } else {
                    Log.d(TAG, "当前网络断开");
                }
        }
    }

    public interface ConnectivityChangedListener {
        void onConnectivityStatusChanged(boolean isOnline);
    }

    private Handler eventHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (!mChangedListeners.isEmpty()) {
                switch (msg.what) {
                    case MSG_WHAT_NETWORK_ONLINE:
                        for (ConnectivityChangedListener listener : mChangedListeners) {
                            listener.onConnectivityStatusChanged(true);
                        }
                        break;
                    case MSG_WHAT_NETWORK_OFFLINE:
                        for (ConnectivityChangedListener listener : mChangedListeners) {
                            listener.onConnectivityStatusChanged(false);
                        }
                        break;
                }
            }
        }
    };

}
