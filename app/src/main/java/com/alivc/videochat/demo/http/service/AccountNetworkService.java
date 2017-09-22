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
 * 类的描述: 登录网络请求，
 */
public interface AccountNetworkService {
    @POST(HttpConstant.URL_LOGIN)
    Call<HttpResponse<LoginResult>> login(@Body LoginForm form);// 将带有登录信息的对象参数传入获取Call任务对象

    @POST(HttpConstant.URL_WEBSOCKET_INFO)
    Call<HttpResponse<MNSConnectModel>> getMnsConnectionInfo(@Body MNSConnectionInfoForm form);// 阿里MNS管理 获取web socket链接信息
}
