package com.alivc.videochat.demo.http.form;

import com.alivc.videochat.demo.http.HttpConstant;
import com.google.gson.annotations.SerializedName;

/**
 * Created by liujianghao on 16-8-9.
 */
public class LoginForm {
    // HttpConstant.KEY_NAME是json数据中表现的字段
    @SerializedName(HttpConstant.KEY_NAME)
    private String username;

    public LoginForm(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
