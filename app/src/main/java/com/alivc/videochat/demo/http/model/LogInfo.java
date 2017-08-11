package com.alivc.videochat.demo.http.model;

/**
 * Created by liujianghao on 16-9-22.
 */
public class LogInfo {
    private String mLabel;
    private String mValue;

    public LogInfo(String label, String value) {
        mLabel = label;
        mValue = value;
    }

    public String getLabel() {
        return mLabel;
    }

    public void setLabel(String label) {
        mLabel = label;
    }

    public String getValue() {
        return mValue;
    }

    public void setValue(String value) {
        mValue = value;
    }
}
