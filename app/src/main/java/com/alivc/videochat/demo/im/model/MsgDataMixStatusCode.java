package com.alivc.videochat.demo.im.model;

import com.alivc.videochat.demo.http.HttpConstant;
import com.google.gson.annotations.SerializedName;

/**
 * Created by apple on 2016/12/6.
 */

public class MsgDataMixStatusCode {

    @SerializedName(HttpConstant.KEY_MAIN_MIX_ROOM_ID)
    private String mMainMixStreamID;
    @SerializedName(HttpConstant.KEY_MIX_ROOM_ID)
    private String mMixRoomID;
    @SerializedName(HttpConstant.KEY_MIX_TYPE)
    private String mMixType;
    @SerializedName(HttpConstant.KEY_MIX_TEMPLATE)
    private String mMixTemplate;
    @SerializedName(HttpConstant.KEY_MIX_MESSAGE)
    private String mMessage;
    @SerializedName(HttpConstant.KEY_MIX_CODE)
    private String mCode;
    @SerializedName(HttpConstant.KEY_MIX_UID)
    private String mMixUid;

    public String getMainMixStreamID() {
        return mMainMixStreamID;
    }

    public void setMainMixStreamID(String mainMixStreamID) {
        mMainMixStreamID = mainMixStreamID;
    }

    public String getMixRoomID() {
        return mMixRoomID;
    }

    public void setMixRoomID(String mixRoomID) {
        mMixRoomID = mixRoomID;
    }

    public String getMixType() {
        return mMixType;
    }

    public void setMixType(String mixType) {
        mMixType = mixType;
    }

    public String getMixTemplate() {
        return mMixTemplate;
    }

    public void setMixTemplate(String mixTemplate) {
        mMixTemplate = mixTemplate;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        mMessage = message;
    }

    public String getCode() {
        return mCode;
    }

    public void setCode(String code) {
        mCode = code;
    }

    public String getMixUid() {
        return mMixUid;
    }

    public void setMixUid(String mMixUid) {
        this.mMixUid = mMixUid;
    }
}
