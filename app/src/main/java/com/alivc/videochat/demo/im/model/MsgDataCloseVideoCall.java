package com.alivc.videochat.demo.im.model;

import com.alivc.videochat.demo.http.HttpConstant;
import com.google.gson.annotations.SerializedName;

/**
 * Created by liujianghao on 16-9-24.
 */

public class MsgDataCloseVideoCall {
    @SerializedName(HttpConstant.KEY_ROOM_ID)
    private String roomID;
    @SerializedName(HttpConstant.KEY_UID)
    private String uid;
    @SerializedName(HttpConstant.KEY_NAME)
    private String name;


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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
