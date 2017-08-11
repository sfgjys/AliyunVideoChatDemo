package com.alivc.videochat.demo.im.model;

import com.alivc.videochat.demo.http.HttpConstant;
import com.google.gson.annotations.SerializedName;

/**
 * Created by liujianghao on 16-8-19.
 */
public class MsgDataNotAgreeVideoCall {
    @SerializedName(HttpConstant.KEY_INVITEE_UID)
    private String inviteeUID;
    @SerializedName(HttpConstant.KEY_INVITEE_NAME)
    private String inviteeName;


    public String getInviteeUID() {
        return inviteeUID;
    }

    public void setInviteeUID(String inviteeUID) {
        this.inviteeUID = inviteeUID;
    }

    public String getInviteeName() {
        return inviteeName;
    }

    public void setInviteeName(String inviteeName) {
        this.inviteeName = inviteeName;
    }
}
