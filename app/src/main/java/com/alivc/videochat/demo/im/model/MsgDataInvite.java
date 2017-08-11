package com.alivc.videochat.demo.im.model;

import com.alivc.videochat.demo.http.HttpConstant;
import com.google.gson.annotations.SerializedName;

/**
 * Created by liujianghao on 16-8-18.
 */
public class MsgDataInvite {
    @SerializedName(HttpConstant.KEY_INVITER_UID)
    private String inviterUID;
    @SerializedName(HttpConstant.KEY_INVITER_NAME)
    private String inviterName;
    @SerializedName(HttpConstant.KEY_TYPE)
    private String type;
    @SerializedName(HttpConstant.KEY_INVITER_TYPE)
    private int inviterType;

    public String getInviterUID() {
        return inviterUID;
    }

    public void setInviterUID(String inviterUID) {
        this.inviterUID = inviterUID;
    }

    public String getInviterName() {
        return inviterName;
    }

    public void setInviterName(String inviterName) {
        this.inviterName = inviterName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getInviterType() {
        return inviterType;
    }

    public void setInviterType(int inviterType) {
        this.inviterType = inviterType;
    }
}
