package com.alivc.videochat.demo.http.form;

import com.alivc.videochat.demo.http.HttpConstant;
import com.google.gson.annotations.SerializedName;

/**
 * Created by liujianghao on 16-8-2.
 */
public class SendLikeForm {

    @SerializedName(HttpConstant.KEY_UID)
    private String uid;
    @SerializedName(HttpConstant.KEY_ROOM_ID)
    private String roomID;

    public SendLikeForm(String uid, String roomID) {
        this.uid = uid;
        this.roomID = roomID;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getRoomID() {
        return roomID;
    }

    public void setRoomID(String roomID) {
        this.roomID = roomID;
    }
}
