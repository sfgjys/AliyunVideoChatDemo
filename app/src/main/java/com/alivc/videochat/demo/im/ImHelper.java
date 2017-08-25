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
 * 类的描述: MNS发送消息的核心类，注册，注销，处理MNS的消息，通过注册的内容发送MNS的消息
 */
public class ImHelper {

    public interface Func<T> {
        void action(T t);
    }

    /**
     * 类的描述: 对链接丢失做出回调反应的接口
     */
    public interface ConnectionStatusCallback {
        void onConnectionLost(Throwable cause);
    }

    private SparseArray<Func> mFuncList = new SparseArray<>();
    private SparseArray<Class> mTypeClazz = new SparseArray<>();
    /**
     * 变量的描述: MNS客户端
     */
    private MNSClient mMNSClient;
    /**
     * 变量的描述: 在链接丢失的时候做出回调
     */
    private ConnectionStatusCallback mStatusCallback;
    /**
     * 变量的描述: 消息处理者
     */
    private MessageProcessor mProcessor = new MessageProcessor();

    /**
     * 方法描述: 构造函数，将MNS客户端实现接口类对象传入本类
     */
    public ImHelper(MNSClient mnsClient) {
        this.mMNSClient = mnsClient;
    }

    /**
     * 方法描述: 初始化MNSClient消息客户端
     */
    public void init(Context context) {
        mMNSClient.init(context);
    }

    /**
     * 方法描述: 创建会话
     */
    public void createSession(WebSocketConnectOptions options, IWebSocketActionListener listener, MnsControlBody body, ConnectionStatusCallback callback) {

        this.mStatusCallback = callback;

        mMNSClient.createConnect(options, body, listener, mCallback);

        String string = body.toString();

        mProcessor.registerAction(BaseMessage.class, mAction);
    }

    // --------------------------------------------------------------------------------------------------------

    /**
     * 变量的描述: 解析消息，并将消息通过注册内容发送出去
     */
    MessageAction<BaseMessage> mAction = new MessageAction<BaseMessage>() {
        @Override
        public void onReceiveMessage(BaseMessage data) {
            // TODO Step 3 MNS发送消息
            // 根据BaseMessage的类型从mFuncList中寻找对应的Func接口实例，通这个实例的action方法，将MNS发送的消息传递出去
            int type = data.getType();
            doFunc(mFuncList.get(type), data.getData(), mTypeClazz.get(type));
        }
    };

    public <T> void doFunc(Func<T> func, JsonObject json, Class<T> clazz) {
        if (func != null && clazz != null) {
            T t = convert(json, clazz);
            // TODO Step 4 MNS发送消息
            func.action(t);
        }
    }


    final Gson gson = new Gson();

    /**
     * 变量的描述: 将参数一转换为参数二类型的对象
     */
    public <T> T convert(JsonObject json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }

    // --------------------------------------------------------------------------------------------------------

    /**
     * 方法描述: 注册
     */
    public <T> void register(int type, Func<T> func, Class<T> clazz) {
        mFuncList.put(type, func);
        mTypeClazz.put(type, clazz);
    }

    /**
     * 方法描述: 注销
     */
    public void unRegister(int type) {
        mFuncList.remove(type);
        mTypeClazz.remove(type);
    }

    // -------------------------------------------------------------------------------------------------------- \

    /**
     * 方法描述: 关闭会话
     */
    public void closeSession() {
        mProcessor.unRegisterAction(BaseMessage.class);
        mMNSClient.disconnect();
    }

    // --------------------------------------------------------------------------------------------------------

    private WebSocketCallback mCallback = new WebSocketCallback() {

        final int MSG_WHAT_RECEIVED_MESSAGE = 1;
        final int MSG_WHAT_CONNECTION_LOST = 2;

        private Handler mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case MSG_WHAT_RECEIVED_MESSAGE:// 收到的消息
                        // TODO Step 2 MNS发送消息
                        // 处理消息后，在将消息传递通过mAction的onReceiveMessage方法回调过来
                        mProcessor.processMessage(BaseMessage.class, (MessageBody) msg.obj);
                        break;

                    case MSG_WHAT_CONNECTION_LOST:// 链接丢失(曾经创建链接成功过)
                        if (mStatusCallback != null) {
                            Log.d("WebSocket", "ImHelper-->MSG_WHAT_CONNECTION_LOST");
                            mStatusCallback.onConnectionLost((Throwable) msg.obj);
                        }
                        break;
                }
            }
        };

        /**
         * 方法描述: MNS客户端创建的链接丢失
         * @param  cause 丢失原因
         */
        @Override
        public void connectionLost(Throwable cause) {
            Log.d("WebSocket", "ImHelper.connectionLost");
            // 通过Handler发送消息
            mHandler.obtainMessage(MSG_WHAT_CONNECTION_LOST, cause).sendToTarget();
        }

        /**
         * 方法描述: 字符串消息抵达
         * @param message 包含有抵达的字符串消息
         */
        @Override
        public void stringMessageArrived(WebSocketStringMessage message) {
            if (message != null) {
                TextMessageBody body = new TextMessageBody(message.getContent());
                // TODO Step 1 MNS发送消息
                // 通过Handler发送消息
                mHandler.obtainMessage(MSG_WHAT_RECEIVED_MESSAGE, body).sendToTarget();
                // sendToTarget发送给指定的目标，在这就是mHandler，因为Message就是从mHandler中获取的
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
}
