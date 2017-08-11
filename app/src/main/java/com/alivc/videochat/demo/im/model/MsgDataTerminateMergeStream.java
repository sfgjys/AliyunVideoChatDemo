package com.alivc.videochat.demo.im.model;

import com.alivc.videochat.demo.http.HttpConstant;
import com.google.gson.annotations.SerializedName;

/**
 * Created by liujianghao on 16-9-7.
 */
public class MsgDataTerminateMergeStream {
    @SerializedName(HttpConstant.KEY_ROOM_ID)
    private String roomID;

    public String getRoomID() {
        return roomID;
    }

    public void setRoomID(String roomID) {
        this.roomID = roomID;
    }
}
