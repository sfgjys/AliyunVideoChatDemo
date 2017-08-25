package com.alivc.videochat.demo.logic;

/**
 * Created by apple on 2017/1/5.
 */

public interface SessionHandler {
    /**
     * 方法描述: 网络请求邀请对方进行连麦，但10秒后都没有响应，响应超时，自动任务拒绝连麦
     */
    void onInviteChatTimeout();

    //收到连麦邀请处理超时
    void onProcessInvitingTimeout();

    //连麦混流错误(超时、CDN internal error code)，首先提示用户，并且结束连麦
    void onMixStreamError();

    // 连麦混流超时，首先提示用户，并且结束连麦
    void onMixStreamTimeout();

    void onMixStreamSuccess();

    void onMixStreamNotExist();

    void onMainStreamNotExist();
}
