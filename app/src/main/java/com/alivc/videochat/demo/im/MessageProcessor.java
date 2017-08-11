package com.alivc.videochat.demo.im;

import android.util.Log;

import com.alivc.videochat.demo.im.model.MessageBody;

import java.util.HashMap;

/**
 * Created by liujianghao on 16-8-3.
 */
public class MessageProcessor {
    private static final String TAG = "MessageProcessor";
    HashMap<Class<?>, MessageAction<?>> actions = new HashMap<>();
    MessageConverter mMsgConverter = new GsonMessageConverter();

    <T> void processMessage(Class<T> clazz, MessageBody m){
        T t = mMsgConverter.converter(clazz, m);
        MessageAction<T> action = (MessageAction<T>) actions.get(clazz);
        if(action != null) {
            action.onReceiveMessage(t);
        }else {
            Log.e(TAG, "received unknown message, no action for this one");
        }
    }

    public <T> void registerAction(Class<T> clazz, MessageAction<T> messageAction) {
        actions.put(clazz, messageAction);
    }

    public <T> void unRegisterAction(Class<T> clazz) {
        actions.remove(clazz);
    }
}
