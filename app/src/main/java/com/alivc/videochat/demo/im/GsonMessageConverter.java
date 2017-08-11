package com.alivc.videochat.demo.im;

import com.alivc.videochat.demo.im.model.MessageBody;
import com.alivc.videochat.demo.im.model.TextMessageBody;
import com.google.gson.Gson;

/**
 * Created by liujianghao on 16-8-5.
 */
public class GsonMessageConverter implements MessageConverter{
    @Override
    public <T> T converter(Class<T> clazz, MessageBody m) {
        if(m != null) {
            if(m instanceof TextMessageBody) {
                return converter(clazz, (TextMessageBody) m);
            }
        }
        return null;
    }

    private <T> T converter(Class<T> clazz, TextMessageBody m) {
        String message = m.getMessage();
        Gson gson = new Gson();
        T t = gson.fromJson(message, clazz);
        return t;
    }


}
