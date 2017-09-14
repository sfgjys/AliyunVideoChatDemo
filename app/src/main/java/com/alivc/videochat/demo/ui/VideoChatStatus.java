package com.alivc.videochat.demo.ui;

/**
 * Created by apple on 2016/12/5.
 */

public enum VideoChatStatus {
    /**
     * 变量的描述: 未连麦，未进行连麦流程
     */
    UNCHAT,              //
    /**
     * 变量的描述: 向服务器发送邀请人进行连麦的请求，请求网络邀请对方连麦
     */
    INVITE_FOR_RES,      //
    /**
     * 变量的描述: 向服务器发送邀请连麦的网络请求成功，邀请连麦成功，等待对方响应
     */
    INVITE_RES_SUCCESS,
    /**
     * 变量的描述: 向服务器发送邀请连麦的网络请求失败，邀请连麦失败
     */
    INVITE_RES_FAILURE,
    /**
     * 变量的描述: 收到邀请等待回复状态
     */
    RECEIVED_INVITE,     //
    /**
     * 变量的描述: 开始连麦等待混流成功
     */
    TRY_MIX,            //
    /**
     * 变量的描述: 混流成功
     */
    MIX_SUCC,           //
    /**
     * 变量的描述: 结束连麦请求
     */
    CLOSE_FOR_REQ;      //
}
