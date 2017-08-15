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
 * 类的描述: 连通性监听广播，该广播是动态注册的，实例化对象后调用register方法才算开启广播，否则就是一个对象，如果需要监听广播内容就调用addConnectivityStatusChangedListener方法
 */
public class ConnectivityMonitor extends BroadcastReceiver {

    private static final String TAG = ConnectivityMonitor.class.getName();

    /**
     * 类的描述: 用于返回连通性变化的监听回调
     */
    public interface ConnectivityChangedListener {
        void onConnectivityStatusChanged(boolean isOnline);
    }

    // --------------------------------------------------------------------------------------------------------

    private IntentFilter mFilter = new IntentFilter();

    /**
     * 方法描述: 连通性监听广播构造，在构造时就将action添加IntentFilter去
     */
    public ConnectivityMonitor() {
        mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
    }

    /**
     * 方法描述: 广播的动态注册
     */
    public void register(Context context) {
        context.registerReceiver(this, mFilter);
    }

    /**
     * 方法描述: 广播的动态解绑
     */
    public void unRegister(Context context) {
        context.unregisterReceiver(this);
    }

    // --------------------------------------------------------------------------------------------------------

    private static final int MSG_WHAT_NETWORK_ONLINE = 1;
    private static final int MSG_WHAT_NETWORK_OFFLINE = 2;

    @Override
    public void onReceive(Context context, Intent intent) {
        // 网络链接状态管理
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        // 获取包含了网络的连接情况的对象
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        // 如果网络连接存在或者正在被建立的过程中则返回true，其他为false
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

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

    /**
     * 方法描述: 手机的网络又可以分为好多种
     */
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

    // --------------------------------------------------------------------------------------------------------

    private List<ConnectivityChangedListener> mChangedListeners = new ArrayList<>();

    /**
     * 方法描述: 实例化连通性变化监听回调接口，并将实例化的对象添加进监听集合
     */
    public void addConnectivityStatusChangedListener(ConnectivityChangedListener listener) {
        this.mChangedListeners.add(listener);
    }

    /**
     * 方法描述: 从集合中移除实例化连通性变化监听回调接口
     */
    public void removeConnectivityStatusChangedListener(ConnectivityChangedListener listener) {
        this.mChangedListeners.remove(listener);
    }

    // --------------------------------------------------------------------------------------------------------

    private Handler eventHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            // 监听集合要有监听对象，才能进行回调
            if (!mChangedListeners.isEmpty()) {
                switch (msg.what) {
                    case MSG_WHAT_NETWORK_ONLINE:
                        // 通知网络正在链接(主要是用于MSN)
                        for (ConnectivityChangedListener listener : mChangedListeners) {
                            listener.onConnectivityStatusChanged(true);
                        }
                        break;
                    case MSG_WHAT_NETWORK_OFFLINE:
                        // 通知网路断开了(主要是用于MSN)
                        for (ConnectivityChangedListener listener : mChangedListeners) {
                            listener.onConnectivityStatusChanged(false);
                        }
                        break;
                }
            }
        }
    };


}
