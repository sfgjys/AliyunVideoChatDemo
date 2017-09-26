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
 * 类的描述: 邀请某人进行连麦；被邀请人回答是否进行连麦；主播断开所有正在进行连麦；某人准备退出连麦了（或者被强制退出）告诉下业务服务器
 */
public interface InviteNetworkService {

    @POST(HttpConstant.URL_INVITE_VIDEO)
    Call<HttpResponse<Object>> invite(@Body InviteForm form);// 向服务器发送邀请参数二所代表的用户进行连麦的请求,只是邀请并不是正式连麦

    @POST(HttpConstant.URL_INVITE_FEEDBACK)
    Call<HttpResponse<InviteFeedbackResult>> feedback(@Body FeedbackForm form);// 主播或者观众被邀请后，主播或观众是否同意。则需要调用此方法 反馈邀请 实质就是告诉服务器主播或观众是否同意被邀请进行连麦

    @POST(HttpConstant.URL_CLOSE_VIDEO_CALL)
    Call<HttpResponse<Object>> closeVideoCall(@Body CloseVideoForm form);// 如果主播界面还有连麦的，请求网络，告诉服务器我们将要断开所有连麦

    @POST(HttpConstant.URL_LEAVE_VIDEO_CALL)
    Call<HttpResponse<Object>> leaveChatting(@Body CloseVideoForm form);// 主播端是指定某个连麦退出(实质告诉业务服务器给连麦退出了，让服务器通过MNS通知其他观众)。观众端自己断开与主播的连麦(实质是告诉业务服务器本观众退出连麦，让服务器通过MNS通知主播和其他连麦观众)
}
