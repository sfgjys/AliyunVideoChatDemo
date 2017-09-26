package com.alivc.videochat.demo.http.form;

import com.alivc.videochat.demo.http.HttpConstant;
import com.google.gson.annotations.SerializedName;

public class FeedbackForm {
    public static final int STATUS_NOT_AGREE = 2;
    public static final int STATUS_AGREE = 1;

    /**
     * 变量的描述: 观看者
     */
    public static final int INVITE_TYPE_WATCHER = 1;
    /**
     * 变量的描述: 主播
     */
    public static final int INVITE_TYPE_ANCHOR = 2;

    @SerializedName(HttpConstant.KEY_INVITER_UID)
    private String inviterUID;

    @SerializedName(HttpConstant.KEY_INVITEE_UID)
    private String inviteeUID;

    @SerializedName(HttpConstant.KEY_TYPE)
    private String type;

    @SerializedName(HttpConstant.KEY_STATUS)
    private int status;

    @SerializedName(HttpConstant.KEY_INVITER_TYPE)
    private int inviterType;

    @SerializedName(HttpConstant.KEY_INVITEE_TYPE)
    private int inviteeType;

    public String getInviterUID() {
        return inviterUID;
    }

    public void setInviterUID(String inviterUID) {
        this.inviterUID = inviterUID;
    }

    public String getInviteeUID() {
        return inviteeUID;
    }

    public void setInviteeUID(String inviteeUID) {
        this.inviteeUID = inviteeUID;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getInviterType() {
        return inviterType;
    }

    public void setInviterType(int inviterType) {
        this.inviterType = inviterType;
    }

    public int getInviteeType() {
        return inviteeType;
    }

    public void setInviteeType(int inviteeType) {
        this.inviteeType = inviteeType;
    }

    public static class Builder {
        String inviterUID;
        String inviteeUID;
        String type;
        int status;
        int inviteeType;
        int inviterType;

        public FeedbackForm build() {
            FeedbackForm feedbackForm = new FeedbackForm();
            feedbackForm.status = status;
            feedbackForm.inviteeType = inviteeType;
            feedbackForm.inviteeUID = inviteeUID;
            feedbackForm.inviterType = inviterType;
            feedbackForm.inviterUID = inviterUID;
            feedbackForm.type = type;
            return feedbackForm;
        }

        public Builder status(int status) {
            this.status = status;
            return this;
        }

        public Builder inviteeUID(String uid) {
            this.inviteeUID = uid;
            return this;
        }

        public Builder inviterUID(String uid) {
            this.inviterUID = uid;
            return this;
        }

        public Builder inviterType(int type) {
            this.inviterType = type;
            return this;
        }

        public Builder inviteeType(int type) {
            this.inviteeType = type;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }
    }
}
