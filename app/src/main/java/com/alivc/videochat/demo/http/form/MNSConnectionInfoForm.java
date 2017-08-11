package com.alivc.videochat.demo.http.form;

import com.alivc.videochat.demo.http.HttpConstant;
import com.google.gson.annotations.SerializedName;

/**
 * Created by liujianghao on 16-10-21.
 */

public class MNSConnectionInfoForm {
    @SerializedName(HttpConstant.KEY_TOPIC)
    private String mTopic;
    @SerializedName(HttpConstant.KEY_SUBSCRIPTION_NAME)
    private String mSubscriptionName;

    public String getTopic() {
        return mTopic;
    }

    public void setTopic(String topic) {
        mTopic = topic;
    }

    public String getSubscriptionName() {
        return mSubscriptionName;
    }

    public void setSubscriptionName(String subscriptionName) {
        mSubscriptionName = subscriptionName;
    }

    public MNSConnectionInfoForm(String topic, String subscriptionName) {
        mTopic = topic;
        mSubscriptionName = subscriptionName;
    }
}
