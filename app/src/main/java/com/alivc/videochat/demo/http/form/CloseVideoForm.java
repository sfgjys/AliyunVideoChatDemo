package com.alivc.videochat.demo.http.form;

import com.alivc.videochat.demo.http.HttpConstant;
import com.google.gson.annotations.SerializedName;

public class CloseVideoForm {
    @SerializedName(HttpConstant.KEY_ROOM_ID)
    private String roomID;
    @SerializedName(HttpConstant.KEY_UID)
    private String mUID;

    public CloseVideoForm(String roomID, String UID) {
        this.roomID = roomID;
        mUID = UID;
    }

    public String getRoomID() {
        return roomID;
    }

    public void setRoomID(String roomID) {
        this.roomID = roomID;
    }

    public String getUID() {
        return mUID;
    }

    public void setUID(String UID) {
        mUID = UID;
    }
}
