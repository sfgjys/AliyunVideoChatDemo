package com.alivc.videochat.demo.exception;

public class APIErrorCode {
    /**
     * 变量的描述: 参数错误
     */
    public static final int ERROR_ILLEGAL_ARGUMENT = 2001;
    /**
     * 变量的描述: 用户不存在
     */
    public static final int ERROR_USER_NOT_EXIST = 2002;
    /**
     * 变量的描述: 直播不存在
     */
    public static final int ERROR_ROOM_NOT_EXIST = 2003;
    /**
     * 变量的描述: 直播已存在
     */
    public static final int ERROR_ROOM_HAS_EXISTED = 2004;
    /**
     * 变量的描述: 访问用户信息失败
     */
    public static final int ERROR_GET_USER_INFO_FAILED = 2010;
    /**
     * 变量的描述: 访问直播信息失败
     */
    public static final int ERROR_GET_ROOM_INFO_FAILED = 2020;
    /**
     * 变量的描述: 创建聊天室失败（环信）
     */
    public static final int ERROR_CREATE_IM_ROOM_FAILED = 2040;
    /**
     * 变量的描述: 发送IM消息失败（环信）
     */
    public static final int ERROR_SEND_IM_MSG_FAILED = 2050;
    /**
     * 变量的描述: 混流错误
     */
    public static final int ERROR_MIX_STREAM = 3000;
    /**
     * 变量的描述: 访问邀麦信息失败
     */
    public static final int ERROR_GET_INVITE_INFO_FAILED = 3010;
    /**
     * 变量的描述: 直播间连麦中
     */
    public static final int ERROR_ROOM_INVITING = 3020;
    /**
     * 变量的描述: 混流无效参数
     */
    public static final int ERROR_MIX_ILLIGAL_ARGUMENT = 3400;
    /**
     * 变量的描述: 混流主流不存在
     */
    public static final int ERROR_MAIN_STREAM_NOT_EXIST = 3404;
    /**
     * 变量的描述: 混流内部错误
     */
    public static final int ERROR_MIX_INTERNAL = 3500;
    /**
     * 变量的描述: 直播状态设置错误
     */
    public static final int ERROR_LIVE_STATUS = 4010;
    /**
     * 变量的描述: 未知错误
     */
    public static final int ERROR_UNKNOWN = -10001;
}
