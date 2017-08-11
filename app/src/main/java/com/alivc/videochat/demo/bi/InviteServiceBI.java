package com.alivc.videochat.demo.bi;

import com.alivc.videochat.demo.http.form.CloseVideoForm;
import com.alivc.videochat.demo.http.form.FeedbackForm;
import com.alivc.videochat.demo.http.form.InviteForm;
import com.alivc.videochat.demo.http.model.InviteFeedbackResult;
import com.alivc.videochat.demo.http.service.ServiceFactory;

import java.util.List;

import retrofit2.Call;

/**
 * Created by liujianghao on 16-8-2.
 */
public class InviteServiceBI extends ServiceBI {

    /**
     * invite video call
     *
     * @param inviterUID
     * @param inviteeUIDs
     * @param callback
     */
    public Call inviteCall(String inviterUID,
                                         List<String> inviteeUIDs,
                                         String type,
                                         int inviterType,
                                         String liveRoomId,
                                         Callback callback) {
        Call call;
        InviteForm form = new InviteForm(inviterUID, inviteeUIDs, type, inviterType, liveRoomId);
        call = ServiceFactory.getInviteService().invite(form);
        processObservable(call, callback);
        return call;
    }

    /**
     * feedback to inviting
     *
     * @param inviterUID
     * @param inviteeUID
     * @param callback
     */
    public Call feedback(
            int inviteeType,
            int inviterType,
            String inviterUID,
            String inviteeUID,
            String type,
            int status,
            Callback<InviteFeedbackResult> callback) {
        Call call;
        FeedbackForm form = new FeedbackForm.Builder()
                .inviterUID(inviterUID)
                .inviteeUID(inviteeUID)
                .inviteeType(inviteeType)
                .inviterType(inviterType)
                .type(type)
                .status(status).build();
        call = ServiceFactory.getInviteService().feedback(form);
        processObservable(call, callback);
        return call;
    }

    /**
     * terminate calling
     *
     * @param liveRoomID
     * @param callback
     */
    public Call terminateCall(String liveRoomID, Callback callback) {
        Call call;
        CloseVideoForm form = new CloseVideoForm(liveRoomID, null);
        call = ServiceFactory.getInviteService().closeVideoCall(form);
        processObservable(call, callback);
        return call;
    }

    /**
     * 观众退出连麦
     * @param uid
     * @param liveRoomID
     * @return
     */
    public Call leaveCall(String uid, String liveRoomID, Callback callback) {
        Call call;
        CloseVideoForm form = new CloseVideoForm(liveRoomID, uid);
        call = ServiceFactory.getInviteService().leaveChatting(form);
        processObservable(call, callback);
        return call;
    }
}
