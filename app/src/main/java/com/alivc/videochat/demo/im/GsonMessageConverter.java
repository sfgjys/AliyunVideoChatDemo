package com.alivc.videochat.demo.im;

import com.alivc.videochat.demo.im.model.MessageBody;
import com.alivc.videochat.demo.im.model.TextMessageBody;
import com.google.gson.Gson;

/**
 * 类的描述: 将json数据转换一个类对象
 */
public class GsonMessageConverter implements MessageConverter {

    /**
     * 方法描述: 这个方法使用的前提是参数二是TextMessageBody类型，从参数二中获取json数据字符串，将json字符串转换为参数一类型的对象
     */
    @Override
    public <T> T converter(Class<T> clazz, MessageBody m) {
        if (m != null) {
            if (m instanceof TextMessageBody) {
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
