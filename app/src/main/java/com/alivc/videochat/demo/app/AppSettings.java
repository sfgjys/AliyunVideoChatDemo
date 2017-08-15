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

    private static final String PREF_NAME = "alivc-video-call-settings";
    private final SharedPreferences sharedPreferences;
    private final Resources mResources;


    /**
     * 方法描述: 内部代码获取SharedPreferences对象和资源对象赋值给本对象的成员变量
     */
    public AppSettings(Context context) {
        // 获取名字为PREF_NAME的私有SharedPreferences对象
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        // 获取资源对象
        mResources = context.getResources();
    }

    // **************************************************** 根据Key获取对应Value ****************************************************

    public int getInt(String key, int def) {
        return sharedPreferences.getInt(key, def);
    }

    public String getString(String key, String def) {
        return sharedPreferences.getString(key, def);
    }

    public boolean getBoolean(String key, boolean def) {
        return sharedPreferences.getBoolean(key, def);
    }

    // --------------------------------------------------------------------------------------------------------

    public String getPlayFormat(String def) {
        return getString(getResString(R.string.preference_key_play_format), def);
    }

    public int getPlayBufferDuration(int def) {
        return getInt(getResString(R.string.preference_key_play_buffer_duration), def);
    }

    /**
     * 方法描述: 这个方法固定返回false
     */
    public boolean isShowLogInfo(boolean def) {
        boolean result = getBoolean(getResString(R.string.preference_key_show_log_info), def);
        Log.d(TAG, "preference-key-show-log-info : " + result);
//        return result;
        return false;
    }

    // 根据资源Id获取对应字符串
    private String getResString(int resID) {
        try {
            return mResources.getString(resID);
        } catch (Resources.NotFoundException e) {
            return "";
        }
    }

    // --------------------------------------------------------------------------------------------------------

    public static void configure(Context context, PreferenceManager pm, int resId) {
        pm.setDefaultValues(context, PREF_NAME, Context.MODE_PRIVATE, resId, false);
        pm.setSharedPreferencesName(PREF_NAME);
        pm.setSharedPreferencesMode(Context.MODE_PRIVATE);
    }
}
