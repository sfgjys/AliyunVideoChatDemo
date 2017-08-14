package com.alivc.videochat.demo.http.form;

import com.alivc.videochat.demo.http.HttpConstant;
import com.google.gson.annotations.SerializedName;

/**
 * 类的描述: 分装房间id和登录成功后保存的信息的类
 */
public class WatchLiveForm {
    @SerializedName(HttpConstant.KEY_ROOM_ID)
    private String mRoomId;
    @SerializedName(HttpConstant.KEY_UID)
    private String mUid;

    public WatchLiveForm(String roomId, String uid) {
        mRoomId = roomId;
        mUid = uid;
    }

    public String getRoomId() {
        return mRoomId;
    }

    public void setRoomId(String roomId) {
        mRoomId = roomId;
    }

    public String getUid() {
        return mUid;
    }

    public void setUid(String uid) {
        mUid = uid;
    }
}
