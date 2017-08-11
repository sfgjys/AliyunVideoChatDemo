package com.alivc.videochat.demo.http.service;


import com.alivc.videochat.demo.http.HttpConstant;
import com.alivc.videochat.demo.http.form.LoginForm;
import com.alivc.videochat.demo.http.form.MNSConnectionInfoForm;
import com.alivc.videochat.demo.http.model.HttpResponse;
import com.alivc.videochat.demo.http.model.LoginResult;
import com.alivc.videochat.demo.http.model.MNSConnectModel;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by liujianghao on 16-8-9.
 */
public interface AccountService {
    @POST(HttpConstant.URL_LOGIN)
    Call<HttpResponse<LoginResult>>
    login(@Body LoginForm form);

    @POST(HttpConstant.URL_WEBSOCKET_INFO)
    Call<HttpResponse<MNSConnectModel>>
    getMnsConnectionInfo(@Body MNSConnectionInfoForm form);
}
