package com.alivc.videochat.demo.http.result;

import com.google.gson.annotations.SerializedName;

/**
 * 这是一个bean类，用于json解析的
 */
public class IMUserInfo {
    // @SerializedName是Gson jar包中的，当使用Gson解析的时候就会自动将“uuid”对应的值赋值到成员变量uuid属性上，
    // 同样，如果要将IMUserInfo对象生成json字符串，使用Gson生成的时候也会将userName的名字生成“uuid”。
    @SerializedName("uuid")
    private String uuid;
    @SerializedName("type")
    private String type;
    @SerializedName("created")
    private long createdTimestamp;
    @SerializedName("modified")
    private long modifiedTimestamp;
    @SerializedName("username")
    private String username;
    @SerializedName("activated")
    private boolean activated;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(long createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public long getModifiedTimestamp() {
        return modifiedTimestamp;
    }

    public void setModifiedTimestamp(long modifiedTimestamp) {
        this.modifiedTimestamp = modifiedTimestamp;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }
}

