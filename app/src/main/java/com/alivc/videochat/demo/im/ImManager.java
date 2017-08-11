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
 * Created by liujianghao on 16-8-3.
 */
public class ImManager implements IWebSocketActionListener,
        ImHelper.ConnectionStatusCallback, ConnectivityMonitor.ConnectivityChangedListener {
    private ImHelper mImHelper;
    private ConnectivityMonitor mConnectivityMonitor;
    private ImConnectionStatus mStatus = ImConnectionStatus.UNCONNECT;
    private AccountServiceBI mAccountServiceBI = ServiceBIFactory.getAccountServiceBI();
    private WebSocketConnectOptions mConnectOptions;
    private MnsControlBody mControlBody;
    private Context mContext;
    private Call<HttpResponse<MNSConnectModel>> mMnsInfoCall;
    private boolean needTryReconnect = false;

    public ImManager(Context context, ImHelper imHelper, ConnectivityMonitor monitor) {
        this.mImHelper = imHelper;
        this.mConnectivityMonitor = monitor;
        this.mContext = context;
    }

    public void init() {
        mImHelper.init(mContext);
    }

    public void createSession(WebSocketConnectOptions options, MnsControlBody body) {
        this.mConnectOptions = options;
        this.mControlBody = body;
        mConnectivityMonitor.addConnectivityStatusChangedListener(this);
        mImHelper.createSession(options, this, body, this);
        mStatus = ImConnectionStatus.CONNECTING;    //标记当前状态为正在链接
        needTryReconnect = true;
    }

    public <T> void register(int type, ImHelper.Func<T> func, Class<T> clazz) {
        mImHelper.register(type, func, clazz);
    }

    public void unRegister(int type) {
        mImHelper.unRegister(type);
    }


    public void closeSession() {
        mImHelper.closeSession();
        mConnectivityMonitor.removeConnectivityStatusChangedListener(this);
        mStatus = ImConnectionStatus.UNCONNECT;
        needTryReconnect = false;
    }


    @Override
    public void onConnectionLost(Throwable cause) {
        Log.d("WebSocket", "Mns connection lost");
        if (needTryReconnect) {//非人为断开
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

    private void reconnect() {
        if(mMnsInfoCall != null && mMnsInfoCall.isExecuted() && !mMnsInfoCall.isCanceled()) {
            mMnsInfoCall.cancel();
        }
        mMnsInfoCall = mAccountServiceBI.getMnsConnectionInfo(mControlBody.getTopic(), mMnsCallback);
    }

    private ServiceBI.Callback<MNSConnectModel> mMnsCallback = new ServiceBI.Callback<MNSConnectModel>() {
        @Override
        public void onResponse(int code, MNSConnectModel connectModel) {
            Log.d("WebSocket", "获取MNS链接信息成功，开始进行重连");
            mControlBody.setDate(connectModel.getDate());
            mControlBody.setAccountId(connectModel.getAccountID());
            mControlBody.setAccessId(connectModel.getAccessID());
            mControlBody.setAuthorization("MNS " + connectModel.getAccessID() + ":" + connectModel.getAuthentication());
            closeSession();
            createSession(mConnectOptions, mControlBody);
            mMnsInfoCall = null;
        }

        @Override
        public void onFailure(Throwable e) {
            Log.d("WebSocket", "获取MNS链接信息失败", e);
            onConnectionLost(e);    //请求链接信息失败后，认为是链接丢失，重新进行请求
            mMnsInfoCall = null;
        }
    };

    @Override
    public void onSuccess() {
        mStatus = ImConnectionStatus.CONNECTED;
        Log.d("WebSocket", "MNS connect successful");
    }

    @Override
    public void onFailure(Throwable exception) {
        mStatus = ImConnectionStatus.UNCONNECT;
        onConnectionLost(exception);
        Log.d("WebSocket", "MNS connect failed");
    }

    @Override
    public void onConnectivityStatusChanged(boolean isOnline) {
        if (isOnline && mStatus == ImConnectionStatus.WAITING_FOR_INTERNET) {//需要重连
            Log.d("WebSocket", "网络重新链接成功，开始MNS重连");
            reconnect();
        }
    }
}
