package com.alivc.videochat.demo.im;

import android.content.Context;
import android.util.Log;

import com.alivc.videochat.demo.bi.AccountServiceBI;
import com.alivc.videochat.demo.bi.ServiceBI;
import com.alivc.videochat.demo.bi.ServiceBIFactory;
import com.alivc.videochat.demo.http.model.HttpResponse;
import com.alivc.videochat.demo.http.model.MNSConnectModel;
import com.alivc.videochat.demo.ui.ConnectivityMonitor;
import com.alivc.videochat.demo.uitils.NetworkUtil;
import com.alibaba.sdk.client.IWebSocketActionListener;
import com.alibaba.sdk.client.WebSocketConnectOptions;
import com.alibaba.sdk.mns.MnsControlBody;

import retrofit2.Call;

/**
 * 类的描述: Im即使通信管理类，有初始化，创建，重连，注册，注销，关闭的方法
 */
public class ImManager implements IWebSocketActionListener, ImHelper.ConnectionStatusCallback, ConnectivityMonitor.ConnectivityChangedListener {
    private ImHelper mImHelper;
    private ConnectivityMonitor mConnectivityMonitor;
    private ImConnectionStatus mStatus = ImConnectionStatus.UNCONNECT;
    private AccountServiceBI mAccountServiceBI = ServiceBIFactory.getAccountServiceBI();
    private WebSocketConnectOptions mConnectOptions;
    private MnsControlBody mControlBody;
    private Context mContext;
    private Call<HttpResponse<MNSConnectModel>> mMnsInfoCall;
    /**
     * 变量的描述: 需要试着再次链接
     */
    private boolean needTryReconnect = false;

    public ImManager(Context context, ImHelper imHelper, ConnectivityMonitor monitor) {
        this.mImHelper = imHelper;
        this.mConnectivityMonitor = monitor;
        this.mContext = context;
    }

    public void init() {
        mImHelper.init(mContext);
    }

    /**
     * 方法描述: 创建会话  其两个参数是MNSClient创建链接的必须参数
     *
     * @param options 需要MNSModel和MNSConnectModel的一些信息作为参数
     * @param body    需要MNSModel和MNSConnectModel的一些信息作为参数
     */
    public void createSession(WebSocketConnectOptions options, MnsControlBody body) {

        this.mConnectOptions = options;
        this.mControlBody = body;

        mConnectivityMonitor.addConnectivityStatusChangedListener(this);

        mImHelper.createSession(options, this, body, this);

        mStatus = ImConnectionStatus.CONNECTING;    // 标记当前Im状态为正在链接

        needTryReconnect = true;
    }

    // --------------------------------------------------------------------------------------------------------

    /**
     * 方法描述: 注册
     *
     * @param
     */
    public <T> void register(int type, ImHelper.Func<T> func, Class<T> clazz) {
        mImHelper.register(type, func, clazz);
    }

    /**
     * 方法描述: 注销
     *
     * @param
     */
    public void unRegister(int type) {
        mImHelper.unRegister(type);
    }

    // --------------------------------------------------------------------------------------------------------

    /**
     * 方法描述: 关闭会话
     */
    public void closeSession() {
        mImHelper.closeSession();
        mConnectivityMonitor.removeConnectivityStatusChangedListener(this);
        mStatus = ImConnectionStatus.UNCONNECT;
        needTryReconnect = false;
    }

    // --------------------------------------------------------------------------------------------------------

    /**
     * 方法描述: 重连
     */
    private void reconnect() {
        // 先清除网络请求任务Call
        if (mMnsInfoCall != null && mMnsInfoCall.isExecuted() && !mMnsInfoCall.isCanceled()) {
            mMnsInfoCall.cancel();
        }
        // 开启请求
        mMnsInfoCall = mAccountServiceBI.getMnsConnectionInfo(mControlBody.getTopic(), mMnsCallback);
    }

    /**
     * 变量的描述: 重连网络请求的结果回调
     */
    private ServiceBI.Callback<MNSConnectModel> mMnsCallback = new ServiceBI.Callback<MNSConnectModel>() {
        @Override
        public void onResponse(int code, MNSConnectModel connectModel) {
            Log.d("WebSocket", "获取MNS链接信息成功，开始进行重连");
            mControlBody.setDate(connectModel.getDate());
            mControlBody.setAccountId(connectModel.getAccountID());
            mControlBody.setAccessId(connectModel.getAccessID());
            mControlBody.setAuthorization("MNS " + connectModel.getAccessID() + ":" + connectModel.getAuthentication());
            // 先关闭上个创建失败的会话
            closeSession();

            createSession(mConnectOptions, mControlBody);

            // 重连成功清除重连Call
            mMnsInfoCall = null;
        }

        @Override
        public void onFailure(Throwable e) {
            Log.d("WebSocket", "获取MNS链接信息失败", e);
            onConnectionLost(e);    // 请求链接信息失败后，认为是链接丢失，重新进行请求
            mMnsInfoCall = null;
        }
    };


    // --------------------------------------------------------------------------------------------------------

    // ImHelper.ConnectionStatusCallback接口回调   监听MNS链接丢失的回调
    @Override
    public void onConnectionLost(Throwable cause) {
        Log.d("WebSocket", "Mns connection lost");
        if (needTryReconnect) {// 非人为断开 认为断开的是主动调用了closeSession方法
            mStatus = ImConnectionStatus.UNCONNECT; //链接断开
            //判断当前是否有网络，如果有则直接进行重连，如果没有则标记为有网络时进行重连
            if (NetworkUtil.hasNetwork(mContext)) {
                Log.d("WebSocket", "MNS链接断开，但是有网络，直接进行重连");
                reconnect();
            } else {
                mStatus = ImConnectionStatus.WAITING_FOR_INTERNET;  //标记等待网络畅通后重连
            }
        }
    }

    // --------------------------------------------------------------------------------------------------------

    // IWebSocketActionListener 接口回调  监听MNSClient创建链接的结果回调
    @Override
    public void onSuccess() { // 创建链接成功，改状态
        mStatus = ImConnectionStatus.CONNECTED;
        Log.d("WebSocket", "MNS connect successful");
    }

    @Override
    public void onFailure(Throwable exception) { // 创建链接失败，改状态，
        mStatus = ImConnectionStatus.UNCONNECT;
        // 并主动调用onConnectionLost方法提示链接丢失，并进行后续处理
        // 调用这个方法不代表链接丢失，而是创建失败与链接丢失的后续处理一样
        onConnectionLost(exception);
        Log.d("WebSocket", "MNS connect failed");
    }

    // --------------------------------------------------------------------------------------------------------

    /**
     * 方法描述: 通知本类，网络状态发生了改变
     *
     * @param isOnline true代表网络从断开变为链接， false网络从链接变为断开
     */
    @Override
    public void onConnectivityStatusChanged(boolean isOnline) {
        if (isOnline && mStatus == ImConnectionStatus.WAITING_FOR_INTERNET) {// 需要重连
            Log.d("WebSocket", "网络重新链接成功，开始MNS重连");
            reconnect();
        }
    }
}
