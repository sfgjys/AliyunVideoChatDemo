package com.alivc.videochat.demo.ui;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.alivc.videochat.demo.app.AppSettings;

/**
 * Created by liujianghao on 16-9-8.
 */
public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppSettings.configure(this, getPreferenceManager(), com.alivc.videochat.demo.R.xml.fragment_settings_preference);
        addPreferencesFromResource(com.alivc.videochat.demo.R.xml.fragment_settings_preference);
    }

//    @Override
//    public void onBuildHeaders(List<Header> target) {
//        super.onBuildHeaders(target);
//        loadHeadersFromResource(R.xml.headers_setting_preference, target);
//    }
}
