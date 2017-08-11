package com.alivc.videochat.demo.uitils;

import android.content.Context;
import android.content.SharedPreferences;

import java.lang.ref.WeakReference;

/**
 * 类的描述: SharedPreferences存储读取数据的工具类
 */
public class PreferenceUtil {

    public static final String REF_PLAYER_INFO = "pre-player-info";

    /**
     * 变量的描述: 存储用户登录时的数据的xml文件的名称
     */
    public static final String REF_USER_INFO = "pre-user-info";

    private static final int SUCCESS = 0;
    private static final int FAILED = -1;

    // --------------------------------------------------------------------------------------------------------

    private WeakReference<Context> mContextRef;

    /**
     * 方法描述: 将Context实例放入弱引用对象中，方便释放Context
     */
    public PreferenceUtil(Context context) {
        mContextRef = new WeakReference<>(context);
    }

    /**
     * 方法描述: 获取弱引用对象中的Context实例
     */
    private Context getContext() {
        if (mContextRef != null) {
            return mContextRef.get();
        }
        return null;
    }

    // --------------------------------------------------------------------------------------------------------

    public int readInt(String refName, String key, int defValue) {
        Context context = getContext();
        if (context == null) {
            return defValue;
        }
        SharedPreferences sp = context.getSharedPreferences(refName, Context.MODE_PRIVATE);
        return sp.getInt(key, defValue);
    }

    /**
     * 方法描述: 获取SharedPreferences存储的String类型的数据
     *
     * @param refName  存储数据的xml文件的名字
     * @param key      存储数据的key值
     * @param defValue 获取存储数据时，没有获得值时的返回值
     * @return 返回存储的String数据
     */
    public String readString(String refName, String key, String defValue) {
        Context context = getContext();
        if (context == null) {
            return defValue;
        }
        SharedPreferences sp = context.getSharedPreferences(refName, Context.MODE_PRIVATE);
        return sp.getString(key, defValue);
    }

    public double readFloat(String refName, String key, float defValue) {
        Context context = getContext();
        if (context == null) {
            return defValue;
        }
        SharedPreferences sp = context.getSharedPreferences(refName, Context.MODE_PRIVATE);
        return sp.getFloat(key, defValue);
    }

    public boolean readBoolean(String refName, String key, boolean defValue) {
        Context context = getContext();
        if (context == null) {
            return defValue;
        }
        SharedPreferences sp = context.getSharedPreferences(refName, Context.MODE_PRIVATE);
        return sp.getBoolean(key, defValue);
    }

    // --------------------------------------------------------------------------------------------------------

    public int write(String refName, String key, int value) {
        Context context = getContext();
        if (context != null) {
            SharedPreferences sp = context.getSharedPreferences(refName, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putInt(key, value);
            editor.apply();
            return SUCCESS;
        }
        return FAILED;
    }

    /**
     * 方法描述: 通过SharedPreferences工具类存储String类型的数据
     * @return  0成功 -1失败
     */
    public int write(String refName, String key, String value) {
        Context context = getContext();
        if (context != null) {
            SharedPreferences sp = context.getSharedPreferences(refName, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(key, value);
            editor.apply();
            return SUCCESS;
        }
        return FAILED;
    }

    public int write(String refName, String key, float value) {
        Context context = getContext();
        if (context != null) {
            SharedPreferences sp = context.getSharedPreferences(refName, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putFloat(key, value);
            editor.apply();
            return SUCCESS;
        }
        return FAILED;
    }

    public int write(String refName, String key, boolean value) {
        Context context = getContext();
        if (context != null) {
            SharedPreferences sp = context.getSharedPreferences(refName, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean(key, value);
            editor.apply();
            return SUCCESS;
        }
        return FAILED;
    }

    // --------------------------------------------------------------------------------------------------------

    /**
     * 方法描述: 更新弱引用对象中的Context实例
     */
    public void updateContext(Context context) {
        if (mContextRef != null) {
            mContextRef.clear();
        }
        mContextRef = new WeakReference<>(context);
    }
}
