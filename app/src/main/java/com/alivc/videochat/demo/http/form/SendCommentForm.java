package com.alivc.videochat.demo.http.form;

import com.alivc.videochat.demo.http.HttpConstant;
import com.google.gson.annotations.SerializedName;

public class SendCommentForm {
    @SerializedName(HttpConstant.KEY_UID)
    private String uid;
    @SerializedName(HttpConstant.KEY_COMMENT)
    private String comment;
    @SerializedName(HttpConstant.KEY_ROOM_ID)
    private String roomID;

    public SendCommentForm(String uid,
                           String roomID,
                           String comment) {
        this.uid = uid;
        this.roomID = roomID;
        this.comment = comment;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getRoomID() {
        return roomID;
    }

    public void setRoomID(String roomID) {
        this.roomID = roomID;
    }
}
