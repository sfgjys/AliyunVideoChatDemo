package com.alivc.videochat.demo.bi;

import com.alivc.videochat.demo.http.form.LoginForm;
import com.alivc.videochat.demo.http.form.MNSConnectionInfoForm;
import com.alivc.videochat.demo.http.model.HttpResponse;
import com.alivc.videochat.demo.http.model.LoginResult;
import com.alivc.videochat.demo.http.model.MNSConnectModel;
import com.alivc.videochat.demo.http.service.ServiceFactory;

import retrofit2.Call;

/**
 * Created by liujianghao on 16-8-9.
 */
public class AccountServiceBI extends ServiceBI {

    /**
     * 登陆
     * @param username
     * @param callback
     * @return
     */
    public Call<HttpResponse<LoginResult>> login(String username, Callback<LoginResult> callback) {
        Call<HttpResponse<LoginResult>> call;
        LoginForm loginForm = new LoginForm(username);
        call = ServiceFactory.getAccountService().login(loginForm);
        processObservable(call, callback);
        return call;
    }

    /**
     * 获取MNS链接信息
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
