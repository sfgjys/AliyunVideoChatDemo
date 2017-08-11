package com.alivc.videochat.demo.im;


import com.alivc.videochat.demo.im.model.MessageBody;

/**
 * Created by liujianghao on 16-8-3.
 */
public interface MessageConverter {
    <T> T converter(Class<T> clazz, MessageBody m);
}
