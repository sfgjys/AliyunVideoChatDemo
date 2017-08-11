package com.alivc.videochat.demo.exception;

/**
 * Created by apple on 2017/1/4.
 */

public class ChatSessionException extends Throwable{
    public static final int ERROR_CURR_CHATTING = 0x001;    //当前正在连麦
    public static final int ERROR_CHATTING_MAX_NUMBER = 0x002;  //当前连麦数量达到最大值
    public static final int ERROR_CHATTING_ALREADY = 0x003;  //邀请的观众整在连麦中
    public static final int ERROR_UNKNOWN = 0x000;          //未知错误
    private int mErrorCode = ERROR_UNKNOWN;

    public ChatSessionException(int code) {
        this.mErrorCode = code;
    }

    public int getErrorCode() {
        return this.mErrorCode;
    }
}
