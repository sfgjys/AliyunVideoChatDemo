package com.alivc.videochat.demo.im;

/**
 * 类的描述: 用枚举来列举Im即时通讯的各种状态
 */
public enum ImConnectionStatus {
    /**
     * 变量的描述: Im即时通讯为 未链接状态（初始状态）
     */
    UNCONNECT,  //
    /**
     * 变量的描述: Im即时通讯为 正在链接
     */
    CONNECTING, //
    /**
     * 变量的描述: Im即时通讯为 链接成功
     */
    CONNECTED,  //
    /**
     * 变量的描述: Im即时通讯为 等待网络畅通
     */
    WAITING_FOR_INTERNET;   //
}
