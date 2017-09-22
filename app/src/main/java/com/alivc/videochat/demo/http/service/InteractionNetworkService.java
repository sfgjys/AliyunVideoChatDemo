package com.alivc.videochat.demo.http.service;

import com.alivc.videochat.demo.http.HttpConstant;
import com.alivc.videochat.demo.http.form.SendCommentForm;
import com.alivc.videochat.demo.http.form.SendLikeForm;
import com.alivc.videochat.demo.http.result.HttpResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * 类的描述: 交互网络请求服务(包含:发送评论，点赞)
 */
public interface InteractionNetworkService {
    @POST(HttpConstant.URL_SEND_COMMEND)
    Call<HttpResponse<Object>> sendComment(@Body SendCommentForm form);// 发送给业务服务器观众的评论信息

    @POST(HttpConstant.URL_SEND_LIKE)
    Call<HttpResponse<Object>> sendLike(@Body SendLikeForm form);// 发送给业务服务器观众的点赞信息
}
