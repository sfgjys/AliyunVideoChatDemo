package com.alivc.videochat.demo.bi;

import com.alivc.videochat.demo.http.form.CloseVideoForm;
import com.alivc.videochat.demo.http.form.FeedbackForm;
import com.alivc.videochat.demo.http.form.InviteForm;
import com.alivc.videochat.demo.http.result.HttpResponse;
import com.alivc.videochat.demo.http.result.InviteFeedbackResult;
import com.alivc.videochat.demo.http.service.NetworkServiceFactory;

import java.util.List;
import java.util.Objects;

import retrofit2.Call;

public class InviteServiceBI extends ServiceBI {

    /**
     * 方法描述: 向服务器发送邀请参数二所代表的用户进行连麦的请求,只是邀请并不是正式连麦
     *
     * @param inviterUID  进行邀请连麦的用户的ID
     * @param inviteeUIDs 被邀请连麦的用户的ID  被邀请连麦ID 用‘|’分割 例如: ‘1|2|3’ or ‘2’
     * @param inviterType 邀请人是否为观众 #1是观众 2 主播
     * @param liveRoomId  直播间ID
     */
    public Call inviteCall(String inviterUID, List<String> inviteeUIDs, String type, int inviterType, String liveRoomId, Callback<Object> callback) {
        Call<HttpResponse<Object>> call;
        InviteForm form = new InviteForm(inviterUID, inviteeUIDs, type, inviterType, liveRoomId);
        call = NetworkServiceFactory.getInviteService().invite(form);
        processObservable(call, callback);
        return call;
    }

    /**
     * 方法描述: 主播或者观众被邀请后，主播或观众同意了。则需要调用此方法 反馈邀请 实质就是告诉服务器主播或观众是否同意被邀请进行连麦
     *
     * @param inviterUID  邀请连麦ID
     * @param inviteeUID  被邀请连麦ID
     * @param inviteeType 被邀请是否为观众 #1是观众 2 主播
     * @param inviterType 邀请是否为观众 #1是观众 2 主播
     * @param type        这是个啥？？？？？？？？？
     * @param status      是否同意状态 #1同意 2 不同意
     */
    public Call feedback(int inviteeType, int inviterType, String inviterUID, String inviteeUID, String type, int status, Callback<InviteFeedbackResult> callback) {
        Call<HttpResponse<InviteFeedbackResult>> call;
        FeedbackForm form = new FeedbackForm.Builder()
                .inviterUID(inviterUID)
                .inviteeUID(inviteeUID)
                .inviteeType(inviteeType)
                .inviterType(inviterType)
                .type(type)
                .status(status).build();
        call = NetworkServiceFactory.getInviteService().feedback(form);
        processObservable(call, callback);
        return call;
    }

    /**
     * 方法描述: 如果主播界面还有连麦的，请求网络，告诉服务器我们将要断开所有连麦
     *
     * @param liveRoomID 主播房间id
     * @param callback   请求网络的结果回调接口实例
     */
    public Call terminateCall(String liveRoomID, Callback<Object> callback) {
        Call<HttpResponse<Object>> call;
        CloseVideoForm form = new CloseVideoForm(liveRoomID, null);
        call = NetworkServiceFactory.getInviteService().closeVideoCall(form);
        processObservable(call, callback);
        return call;
    }

    /**
     * 方法描述: 主播端是指定某个连麦退出(实质告诉业务服务器给连麦退出了，让服务器通过MNS通知其他观众)。观众端自己断开与主播的连麦(实质是告诉业务服务器本观众退出连麦，让服务器通过MNS通知主播和其他连麦观众)
     *
     * @param uid        退出连麦的观众的uid
     * @param liveRoomID 观众所退出的连麦的所在房间id
     */
    public Call leaveCall(String uid, String liveRoomID, Callback<Object> callback) {
        Call<HttpResponse<Object>> call;
        CloseVideoForm form = new CloseVideoForm(liveRoomID, uid);
        call = NetworkServiceFactory.getInviteService().leaveChatting(form);
        processObservable(call, callback);
        return call;
    }
}
