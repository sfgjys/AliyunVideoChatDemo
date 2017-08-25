package com.alivc.videochat.demo.im.model;

/**
 * 类的描述: 存储有从MNS服务器那获取的消息
 */
public class TextMessageBody extends MessageBody {
    private String message;

    public TextMessageBody() {
    }

    public TextMessageBody(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
