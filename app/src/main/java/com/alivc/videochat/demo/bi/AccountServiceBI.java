package com.alivc.videochat.demo.bi;

import com.alivc.videochat.demo.http.form.LoginForm;
import com.alivc.videochat.demo.http.form.MNSConnectionInfoForm;
import com.alivc.videochat.demo.http.model.HttpResponse;
import com.alivc.videochat.demo.http.model.LoginResult;
import com.alivc.videochat.demo.http.model.MNSConnectModel;
import com.alivc.videochat.demo.http.service.NetworkServiceFactory;

import retrofit2.Call;

/**
 * 这是一个创建不同Call对象，并调用正式请求网络的processObservable方法的类
 * 技巧:
 * new Retrofit.Builder().build().create(AccountNetworkService.class).login(此处传入请求网络需要的参数Bean类);获得一个Call对象
 * 使用这个Call对象正式请求网络，Retrofit的网络请求是靠Call来执行的，实例化Call是对网络请求进行配置的过程
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
        call = NetworkServiceFactory.getAccountService().login(loginForm);
        // 正式发起网络请求
        processObservable(call, callback);
        return call;
    }

    /**
     * 方法描述: 阿里MNS管理 获取web socket链接信息  这里是在重连时用的 不需要先获取MNSModel，因为已经有了请求网络需要的参数了
     */
    public Call<HttpResponse<MNSConnectModel>> getMnsConnectionInfo(String topic, Callback<MNSConnectModel> callback) {
        Call<HttpResponse<MNSConnectModel>> call;
        MNSConnectionInfoForm form = new MNSConnectionInfoForm(topic, topic);
        call = NetworkServiceFactory.getAccountService().getMnsConnectionInfo(form);
        processObservable(call, callback);
        return call;
    }
}
