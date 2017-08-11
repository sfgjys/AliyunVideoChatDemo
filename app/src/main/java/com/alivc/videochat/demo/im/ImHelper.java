package com.alivc.videochat.demo.im;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;

import com.alivc.videochat.demo.im.model.BaseMessage;
import com.alivc.videochat.demo.im.model.MessageBody;
import com.alivc.videochat.demo.im.model.TextMessageBody;
import com.alibaba.sdk.client.IWebSocketActionListener;
import com.alibaba.sdk.client.WebSocketCallback;
import com.alibaba.sdk.client.WebSocketConnectOptions;
import com.alibaba.sdk.client.wire.WebSocketFileMessage;
import com.alibaba.sdk.client.wire.WebSocketPayLoadMessage;
import com.alibaba.sdk.client.wire.WebSocketStringMessage;
import com.alibaba.sdk.mns.MNSClient;
import com.alibaba.sdk.mns.MnsControlBody;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Created by liujianghao on 16-8-12.
 */
public class ImHelper {
    private SparseArray<Func> mFuncList = new SparseArray<>();
    private SparseArray<Class> mTypeClazz = new SparseArray<>();
    private MNSClient mMNSClient;
    private ConnectionStatusCallback mStatusCallback;


    private MessageProcessor mProcessor = new MessageProcessor();

    public interface Func<T> {
        void action(T t);
    }

    public ImHelper(MNSClient mnsClient){
        this.mMNSClient = mnsClient;
    }

    public void init(Context context) {
        mMNSClient.init(context);
    }


    public void createSession(WebSocketConnectOptions options,
                              IWebSocketActionListener listener,
                              MnsControlBody body,
                              ConnectionStatusCallback callback) {
        this.mStatusCallback = callback;
        mMNSClient.createConnect(options, body, listener, mCallback);
        body.toString();
        mProcessor.registerAction(BaseMessage.class, mAction);
    }

    public <T> void register(int type, Func<T> func, Class<T> clazz) {
        mFuncList.put(type, func);
        mTypeClazz.put(type, clazz);
    }

    public void unRegister(int type) {
        mFuncList.remove(type);
        mTypeClazz.remove(type);
    }

    public void closeSession() {
        mProcessor.unRegisterAction(BaseMessage.class);
        mMNSClient.disconnect();
    }

    MessageAction<BaseMessage> mAction = new MessageAction<BaseMessage>() {
        @Override
        public void onReceiveMessage(BaseMessage data) {
            int type = data.getType();
            doFunc(mFuncList.get(type), data.getData(), mTypeClazz.get(type));
        }
    };

    final Gson gson = new Gson();
    public <T> T convert(JsonObject json, Class<T> clazz) {
       return gson.fromJson(json, clazz);
    }

    public <T> void doFunc(Func<T> func, JsonObject json, Class<T> clazz) {
        if(func != null && clazz != null) {
            T t = convert(json, clazz);
            func.action(t);
        }
    }



    private WebSocketCallback mCallback = new WebSocketCallback() {
        final int MSG_WHAT_RECEIVED_MESSAGE = 1;
        final int MSG_WHAT_CONNECTION_LOST = 2;
        private Handler mHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case MSG_WHAT_RECEIVED_MESSAGE:
                        mProcessor.processMessage(BaseMessage.class, (MessageBody) msg.obj);
                        break;
                    case MSG_WHAT_CONNECTION_LOST:
                        if(mStatusCallback != null) {
                            Log.d("WebSocket", "ImHelper-->MSG_WHAT_CONNECTION_LOST");
                            mStatusCallback.onConnectionLost((Throwable) msg.obj);
                        }
                        break;
                }
            }
        };
        @Override
        public void connectionLost(Throwable cause) {
            Log.d("WebSocket", "ImHelper.connectionLost");
            mHandler.obtainMessage(MSG_WHAT_CONNECTION_LOST, cause).sendToTarget();
        }

        @Override
        public void stringMessageArrived(WebSocketStringMessage message) {
            if(message != null) {
                TextMessageBody body = new TextMessageBody(message.getContent());
                mHandler.obtainMessage(MSG_WHAT_RECEIVED_MESSAGE, body).sendToTarget();
            }
        }

        @Override
        public void payLoadMessageArrived(WebSocketPayLoadMessage message) {

        }

        @Override
        public void fileMessageArrived(WebSocketFileMessage message) {

        }

        @Override
        public void deliveryComplete() {

        }
    };


    public interface ConnectionStatusCallback {
        void onConnectionLost(Throwable cause);
    }
}
