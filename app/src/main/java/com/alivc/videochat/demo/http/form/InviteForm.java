package com.alivc.videochat.demo.http.form;

import com.alivc.videochat.demo.http.HttpConstant;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by liujianghao on 16-8-2.
 */
public class InviteForm {
    public static final String TYPE_SIDE_BY_SIDE = "side_by_side";
    public static final String TYPE_PIC_BY_PIC = "picture_in_picture";

    @SerializedName(HttpConstant.KEY_INVITER_UID)
    private String inviterUID;

    private List<String> inviteeUIDList;

    @SerializedName(HttpConstant.KEY_INVITEE_UID_LIST)      //TODO:这里是为了兼容IOS乱码的问题，所以数组都要拼装成字符串
    private String mInviteeUIDs;

    @SerializedName(HttpConstant.KEY_TYPE)
    private String type;

    @SerializedName(HttpConstant.KEY_INVITER_TYPE)
    private int inviterType;

    @SerializedName(HttpConstant.KEY_LIVE_ROOM_ID)
    private String liveRoomId;

    public InviteForm(String inviterUID,
                      List<String> inviteeUIDList,
                      String type,
                      int inviterType,
                      String liveRoomId) {
        this.inviterUID = inviterUID;
        this.inviteeUIDList = inviteeUIDList;
        StringBuilder builder = new StringBuilder("");
        if(inviteeUIDList != null && inviteeUIDList.size() > 0) {
            for(String item:inviteeUIDList) {
                builder.append(item);
                builder.append("|");
            }
            int index = builder.lastIndexOf("|");
            if(index > 0) {
                builder.delete(index, builder.length());
            }
        }
        mInviteeUIDs = builder.toString();
        this.type = type;
        this.inviterType = inviterType;
        this.liveRoomId = liveRoomId;
    }

    public String getInviterUID() {
        return inviterUID;
    }

    public void setInviterUID(String inviterUID) {
        this.inviterUID = inviterUID;
    }

    public List<String> getInviteeUIDList() {
        return inviteeUIDList;
    }

    public void setInviteeUIDList(List<String> inviteeUIDList) {
        this.inviteeUIDList = inviteeUIDList;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getInviterType() {
        return inviterType;
    }

    public void setInviterType(int inviterType) {
        this.inviterType = inviterType;
    }

    public String getLiveRoomId() {
        return liveRoomId;
    }

    public void setLiveRoomId(String liveRoomId) {
        this.liveRoomId = liveRoomId;
    }

    public String getInviteeUIDs() {
        return mInviteeUIDs;
    }

    public void setInviteeUIDs(String inviteeUIDs) {
        mInviteeUIDs = inviteeUIDs;
    }
}
