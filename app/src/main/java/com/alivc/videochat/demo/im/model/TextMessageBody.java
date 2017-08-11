package com.alivc.videochat.demo.im.model;

/**
 * Created by liujianghao on 16-8-4.
 */
public class TextMessageBody extends MessageBody {
    private String message;

    public TextMessageBody(){}

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
