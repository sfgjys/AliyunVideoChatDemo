package com.alivc.videochat.demo.http.form;

import com.alivc.videochat.demo.http.HttpConstant;
import com.google.gson.annotations.SerializedName;

/**
 * Created by liujianghao on 16-10-26.
 */

public class ExitWatchingForm {
    @SerializedName(HttpConstant.KEY_ROOM_ID)
    private String mRoomID;
    @SerializedName(HttpConstant.KEY_UID)
    private String mUid;

    public ExitWatchingForm(String roomID, String uid) {
        mRoomID = roomID;
        mUid = uid;
    }

    public String getRoomID() {
        return mRoomID;
    }

    public void setRoomID(String roomID) {
        mRoomID = roomID;
    }

    public String getUid() {
        return mUid;
    }

    public void setUid(String uid) {
        mUid = uid;
    }
}
