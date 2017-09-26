package com.alivc.videochat.demo.im.model;

/**
 * 类的描述: 接收MNS消息的各种功能模块
 */
public class MessageType {
    /**
     * 变量的描述: 邀请连麦
     */
    public static final int INVITE_CALLING = 1;
    /**
     * 变量的描述: 同意连麦
     */
    public static final int AGREE_CALLING = 2;
    /**
     * 变量的描述: 不同意连麦
     */
    public static final int NOT_AGREE_CALLING = 3;
    /**
     * 变量的描述: 混流结束
     */
    public static final int TERMINATE_MERGE_STREAM = 4;
    /**
     * 变量的描述: 混流成功
     */
    public static final int CALLING_SUCCESS = 5;
    /**
     * 变量的描述: 评论
     */
    public static final int COMMENT = 6;
    /**
     * 变量的描述: 点赞
     */
    public static final int LIKE = 7;
    /**
     * 变量的描述: 直播结束
     */
    public static final int LIVE_COMPLETE = 8;
    /**
     * 变量的描述: 开始推流
     */
    public static final int START_PUSH = 9;
    /**
     * 变量的描述: 中断退流
     */
    public static final int INTERRUPT_PUSH = 10;
    /**
     * 变量的描述: 进入环信聊天室
     */
    public static final int ENTER_CHAT_ROOM = 11;
    /**
     * 变量的描述: 退出环信聊天室
     */
    public static final int EXIT_CHAT_ROOM = 12;
    /**
     * 变量的描述: 聊天消息
     */
    public static final int CHAT_INFO = 13;
    /**
     * 变量的描述: 混流失败
     */
    public static final int CALLING_FAILED = 14;
    /**
     * 变量的描述: 终止连麦
     */
    public static final int TERMINATE_CALLING = 15;
    /**
     * 变量的描述: 连麦过程中各种状态码的回调
     */
    public static final int MIX_STATUS_CODE = 16;
    /**
     * 变量的描述: 混流中断
     */
    public static final int MIX_INTERRUPT = 17;
    /**
     * 变量的描述: 加入连麦
     */
    public static final int JOIN_CHATTING = 18;
    /**
     * 变量的描述: 退出连麦
     */
    public static final int EXIT_CHATTING = 19;
}
