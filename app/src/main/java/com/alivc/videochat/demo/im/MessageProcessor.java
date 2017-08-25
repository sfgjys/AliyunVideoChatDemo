package com.alivc.videochat.demo.im;

import android.util.Log;

import com.alivc.videochat.demo.im.model.MessageBody;

import java.util.HashMap;

/**
 * 类的描述: 消息处理者
 */
public class MessageProcessor {
    private static final String TAG = "MessageProcessor";

    /**
     * 变量的描述: 该集合存储，一组对应的数据，一个类型的消息对应该类型的MessageAction接口实例对象
     */
    HashMap<Class<?>, MessageAction<?>> actions = new HashMap<>();

    MessageConverter mMsgConverter = new GsonMessageConverter();

    /**
     * 方法描述: 根据参数一从actions集合中获取其对应的MessageAction接口实例对象，在调用该接口实例对象的onReceiveMessage方法，将已经转化为参数一类型的json数据回调出去
     */
    <T> void processMessage(Class<T> clazz, MessageBody m) {
        T t = mMsgConverter.converter(clazz, m);

        MessageAction<T> action = (MessageAction<T>) actions.get(clazz);

        if (action != null) {
            action.onReceiveMessage(t);
        } else {
            Log.e(TAG, "received unknown message, no action for this one");
        }
    }

    /**
     * 方法描述: 将一组对应的数据(一个类型的消息对应该类型的MessageAction接口实例对象)存储下，在processMessage中可以使用
     */
    public <T> void registerAction(Class<T> clazz, MessageAction<T> messageAction) {
        actions.put(clazz, messageAction);
    }

    public <T> void unRegisterAction(Class<T> clazz) {
        actions.remove(clazz);
    }
}
