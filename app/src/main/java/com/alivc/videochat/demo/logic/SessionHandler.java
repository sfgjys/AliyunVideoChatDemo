package com.alivc.videochat.demo.logic;

/**
 * Created by apple on 2017/1/5.
 */

public interface SessionHandler {
    //邀请对方长时间未响应
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
