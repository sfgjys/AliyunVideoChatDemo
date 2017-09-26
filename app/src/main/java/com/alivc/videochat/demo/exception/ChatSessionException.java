package com.alivc.videochat.demo.exception;

public class ChatSessionException extends Throwable {
    /**
     * 变量的描述: 当前正在进行连麦流程
     */
    public static final int ERROR_CURR_CHATTING = 0x001;
    /**
     * 变量的描述: 当前连麦数量达到最大值
     */
    public static final int ERROR_CHATTING_MAX_NUMBER = 0x002;  //
    /**
     * 变量的描述: 邀请的观众正在与你进行连麦流程
     */
    public static final int ERROR_CHATTING_ALREADY = 0x003;  //
    /**
     * 变量的描述: 未知错误
     */
    public static final int ERROR_UNKNOWN = 0x000;          //

    private int mErrorCode = ERROR_UNKNOWN;

    public ChatSessionException(int code) {
        this.mErrorCode = code;
    }

    public int getErrorCode() {
        return this.mErrorCode;
    }
}
