package com.alivc.videochat.demo.im.model;

/**
 * Created by liujianghao on 16-8-11.
 */
public class MessageType {
    public static final int INVITE_CALLING = 1;         //邀请连麦
    public static final int AGREE_CALLING = 2;          //同意连麦
    public static final int NOT_AGREE_CALLING = 3;      //不同意连麦
    public static final int TERMINATE_MERGE_STREAM = 4; //混流结束
    public static final int CALLING_SUCCESS = 5;        //混流成功
    public static final int COMMENT = 6;                //评论
    public static final int LIKE = 7;                   //点赞
    public static final int LIVE_COMPLETE = 8;          //直播结束
    public static final int START_PUSH = 9;             //开始推流
    public static final int INTERRUPT_PUSH = 10;        //中断退流
    public static final int ENTER_CHAT_ROOM = 11;       //进入环信聊天室
    public static final int EXIT_CHAT_ROOM = 12;        //退出环信聊天室
    public static final int CHAT_INFO = 13;             //聊天消息
    public static final int CALLING_FAILED = 14;        //混流失败
    public static final int TERMINATE_CALLING = 15;     //终止连麦
    public static final int MIX_STATUS_CODE = 16;       //连麦过程中各种状态码的回调
    public static final int MIX_INTERRUPT = 17;         //混流中断
    public static final int JOIN_CHATTING = 18;         //加入连麦
    public static final int EXIT_CHATTING = 19;         //退出连麦

}
