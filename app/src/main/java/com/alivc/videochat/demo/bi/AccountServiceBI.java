package com.alivc.videochat.demo.bi;

import com.alivc.videochat.demo.http.form.LoginForm;
import com.alivc.videochat.demo.http.form.MNSConnectionInfoForm;
import com.alivc.videochat.demo.http.model.HttpResponse;
import com.alivc.videochat.demo.http.model.LoginResult;
import com.alivc.videochat.demo.http.model.MNSConnectModel;
import com.alivc.videochat.demo.http.service.ServiceFactory;

import retrofit2.Call;

/**
 * 这是一个创建不同Call对象，并调用正式请求网络的processObservable方法的类
 */
public class AccountServiceBI extends ServiceBI {

    /**
     * 方法描述: 登录请求
     *
     * @param username 登录请求需要的参数
     * @param callback 登录请求结果监听回调接口
     * @return 返回登录请求专属的Call任务
     */
    public Call<HttpResponse<LoginResult>> login(String username, Callback<LoginResult> callback) {
        Call<HttpResponse<LoginResult>> call;
        // 分装传递参数为一个对象
        LoginForm loginForm = new LoginForm(username);
        // 根据请求参数去创建Call
        call = ServiceFactory.getAccountService().login(loginForm);
        // 正式发起网络请求
        processObservable(call, callback);
        return call;
    }

    /**
     * 获取MNS链接信息
     *
     * @param topic
     * @param callback
     * @return
     */
    public Call<HttpResponse<MNSConnectModel>> getMnsConnectionInfo(String topic, Callback<MNSConnectModel> callback) {
        Call<HttpResponse<MNSConnectModel>> call;
        MNSConnectionInfoForm form = new MNSConnectionInfoForm(topic, topic);
        call = ServiceFactory.getAccountService().getMnsConnectionInfo(form);
        processObservable(call, callback);
        return call;
    }
}
