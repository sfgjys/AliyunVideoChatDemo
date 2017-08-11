package com.alivc.videochat.demo.im;

/**
 * Created by apple on 2016/12/8.
 */

public enum  ImConnectionStatus {
    UNCONNECT,  //未链接状态（初始状态）
    CONNECTING, //正在链接
    CONNECTED,  //链接成功
    WAITING_FOR_INTERNET;   //等待网络畅通
}
