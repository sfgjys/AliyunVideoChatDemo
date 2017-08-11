package com.alivc.videochat.demo.im.model;

import com.alivc.videochat.demo.http.HttpConstant;
import com.google.gson.annotations.SerializedName;

/**
 * Created by apple on 2017/1/11.
 */

public class MsgDataExitChatting {
    @SerializedName(HttpConstant.KEY_MAIN_ROOM_ID)
    private String mMainRoomID;
    @SerializedName(HttpConstant.KEY_ROOM_ID)
    private String mRoomID;
    @SerializedName(HttpConstant.KEY_UID)
    private String mUID;
    @SerializedName(HttpConstant.KEY_NAME)
    private String mName;

    public String getMainRoomID() {
        return mMainRoomID;
    }

    public void setMainRoomID(String mainRoomID) {
        mMainRoomID = mainRoomID;
    }

    public String getRoomID() {
        return mRoomID;
    }

    public void setRoomID(String roomID) {
        mRoomID = roomID;
    }

    public String getUID() {
        return mUID;
    }

    public void setUID(String UID) {
        mUID = UID;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }
}
