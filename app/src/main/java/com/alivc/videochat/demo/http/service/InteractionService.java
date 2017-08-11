package com.alivc.videochat.demo.http.service;

import com.alivc.videochat.demo.http.HttpConstant;
import com.alivc.videochat.demo.http.form.SendCommentForm;
import com.alivc.videochat.demo.http.form.SendLikeForm;
import com.alivc.videochat.demo.http.model.HttpResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by liujianghao on 16-7-28.
 */
public interface InteractionService {

    @POST(HttpConstant.URL_SEND_COMMEND)
    Call<HttpResponse<Object>>
    sendComment(@Body SendCommentForm form);

    @POST(HttpConstant.URL_SEND_LIKE)
    Call<HttpResponse<Object>>
    sendLike(@Body SendLikeForm form);
}
