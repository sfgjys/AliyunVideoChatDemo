package com.alivc.videochat.demo.im;

/**
 * Created by liujianghao on 16-8-3.
 */
public interface MessageAction<T> {
    void onReceiveMessage(T data);
}
