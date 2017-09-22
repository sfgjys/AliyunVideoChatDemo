package com.alivc.videochat.demo.http.service;

import com.alivc.videochat.demo.http.HttpConstant;
import com.alivc.videochat.demo.http.form.CloseVideoForm;
import com.alivc.videochat.demo.http.form.FeedbackForm;
import com.alivc.videochat.demo.http.form.InviteForm;
import com.alivc.videochat.demo.http.result.HttpResponse;
import com.alivc.videochat.demo.http.result.InviteFeedbackResult;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * 类的描述:
 */
public interface InviteNetworkService {

    @POST(HttpConstant.URL_INVITE_VIDEO)
    Call<HttpResponse<Object>> invite(@Body InviteForm form);// 邀请对方进行连麦

    @POST(HttpConstant.URL_INVITE_FEEDBACK)
    Call<HttpResponse<InviteFeedbackResult>> feedback(@Body FeedbackForm form);

    @POST(HttpConstant.URL_CLOSE_VIDEO_CALL)
    Call<HttpResponse<Object>> closeVideoCall(@Body CloseVideoForm form);// 关闭所有连麦

    @POST(HttpConstant.URL_LEAVE_VIDEO_CALL)
    Call<HttpResponse<Object>> leaveChatting(@Body CloseVideoForm form);// 观众离开连麦
}
