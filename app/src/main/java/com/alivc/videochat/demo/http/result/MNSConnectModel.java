package com.alivc.videochat.demo.http.result;

import com.alivc.videochat.demo.http.HttpConstant;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class MNSConnectModel implements Serializable {
    @SerializedName(HttpConstant.KEY_AUTHENTICATION)
    private String mAuthentication;
    @SerializedName(HttpConstant.KEY_WS_SERVER_ADDRESS)
    private String mTopicWSServerAddress;
    @SerializedName(HttpConstant.KEY_ACCOUNT_ID)
    private String mAccountID;
    @SerializedName(HttpConstant.KEY_ACCESS_ID)
    private String mAccessID;
    @SerializedName(HttpConstant.KEY_DATE)
    private String mDate;

    public String getAuthentication() {
        return mAuthentication;
    }

    public void setAuthentication(String authentication) {
        mAuthentication = authentication;
    }


    public String getAccountID() {
        return mAccountID;
    }

    public void setAccountID(String accountID) {
        mAccountID = accountID;
    }

    public String getAccessID() {
        return mAccessID;
    }

    public void setAccessID(String accessID) {
        mAccessID = accessID;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        mDate = date;
    }

    public String getTopicWSServerAddress() {
        return mTopicWSServerAddress;
    }

    public void setTopicWSServerAddress(String topicWSServerAddress) {
        mTopicWSServerAddress = topicWSServerAddress;
    }

    @Override
    public String toString() {
        return "MNSConnectModel{" +
                "mAuthentication='" + mAuthentication + '\'' +
                ", mTopicWSServerAddress='" + mTopicWSServerAddress + '\'' +
                ", mAccountID='" + mAccountID + '\'' +
                ", mAccessID='" + mAccessID + '\'' +
                ", mDate='" + mDate + '\'' +
                '}';
    }
}
