package com.alivc.videochat.demo.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.util.Log;

import com.alivc.videochat.demo.R;

/**
 * Created by liujianghao on 16-9-8.
 */
public class AppSettings {
    private static final String TAG = "AppSettings";
    private final SharedPreferences mPref;
    private final Resources mResources;


    public AppSettings(Context context) {
        this.mPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        mResources = context.getResources();
    }

    public int getInt(String key, int def) {
        return mPref.getInt(key, def);
    }

    public String getString(String key, String def) {
        return mPref.getString(key, def);
    }

    public boolean getBoolean(String key, boolean def) {
        return mPref.getBoolean(key, def);
    }


    public String getPlayFormat(String def) {
        return getString(getResString(R.string.preference_key_play_format), def);
    }

    public int getPlayBufferDuration(int def) {
        return getInt(getResString(R.string.preference_key_play_buffer_duration), def);
    }

    public boolean isShowLogInfo(boolean def) {
        boolean result = getBoolean(getResString(R.string.preference_key_show_log_info), def);
        Log.d(TAG, "preference-key-show-log-info : "+result);
//        return result;
        return false;
    }

    private String getResString(int resID) {
        try {
            return mResources.getString(resID);
        }catch (Resources.NotFoundException e) {
            return "";
        }
    }


    private static final String PREF_NAME = "alivc-video-call-settings";

    public static final void configure(Context context, PreferenceManager pm, int resId) {
        pm.setDefaultValues(context, PREF_NAME, Context.MODE_PRIVATE, resId, false);
        pm.setSharedPreferencesName(PREF_NAME);
        pm.setSharedPreferencesMode(Context.MODE_PRIVATE);
    }

}
