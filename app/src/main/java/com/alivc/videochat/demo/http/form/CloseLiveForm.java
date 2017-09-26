package com.alivc.videochat.demo.http.form;

import com.alivc.videochat.demo.http.HttpConstant;
import com.google.gson.annotations.SerializedName;

public class CloseLiveForm {

    @SerializedName(HttpConstant.KEY_ROOM_ID)
    private String roomID;
    @SerializedName(HttpConstant.KEY_UID)
    private String uid;

    public CloseLiveForm(String roomID, String uid) {
        this.roomID = roomID;
        this.uid = uid;
    }

    public String getRoomID() {
        return roomID;
    }

    public void setRoomID(String roomID) {
        this.roomID = roomID;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
