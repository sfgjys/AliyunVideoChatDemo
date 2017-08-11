package com.alivc.videochat.demo.http.model;

import com.alivc.videochat.demo.http.HttpConstant;
import com.google.gson.annotations.SerializedName;

/**
 * Created by liujianghao on 16-8-11.
 */
public class LoginResult {
    @SerializedName(HttpConstant.KEY_ID)
    private String id;
    @SerializedName(HttpConstant.KEY_NAME)
    private String name;
    @SerializedName("em")
    private IMUserInfo imUserInfo;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public IMUserInfo getImUserInfo() {
        return imUserInfo;
    }

    public void setImUserInfo(IMUserInfo imUserInfo) {
        this.imUserInfo = imUserInfo;
    }
}
