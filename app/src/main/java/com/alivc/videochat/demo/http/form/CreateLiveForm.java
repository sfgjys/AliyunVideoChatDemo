package com.alivc.videochat.demo.http.form;

import com.alivc.videochat.demo.http.HttpConstant;
import com.google.gson.annotations.SerializedName;

/**
 * 类的描述: 用于请求网络获取获取推流地址的参数Bean类
 */
public class CreateLiveForm {
    @SerializedName(HttpConstant.KEY_UID)
    private String uid;

    @SerializedName(HttpConstant.KEY_DESC)
    private String desc;

    public CreateLiveForm(String uid, String desc) {
        this.uid = uid;
        this.desc = desc;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
