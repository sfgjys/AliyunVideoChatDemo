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

public interface AccountService {
    @POST(HttpConstant.URL_LOGIN)
    Call<HttpResponse<LoginResult>> login(@Body LoginForm form);// 将带有登录信息的对象参数传入获取Call任务对象

    @POST(HttpConstant.URL_WEBSOCKET_INFO)
    Call<HttpResponse<MNSConnectModel>> getMnsConnectionInfo(@Body MNSConnectionInfoForm form);// 获取MNS链接信息
}
