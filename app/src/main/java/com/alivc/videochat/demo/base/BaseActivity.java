package com.alivc.videochat.demo.base;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.alivc.videochat.demo.uitils.PreferenceUtil;

/**
 * 类的描述: 本应用共有五个Activity，除了SettingActivity，其他四个都是以本类为父类
 */
public class BaseActivity extends FragmentActivity {

    private ILifecycleListener mLifecycleListener;

    /**
     * 方法描述: 设置BaseActivity生命周期变化监听器
     *
     * @param lifecycleListener 自定义监听器
     */
    public void setLifecycleListener(ILifecycleListener lifecycleListener) {
        this.mLifecycleListener = lifecycleListener;
    }

    // --------------------------------------------------------------------------------------------------------

    /**
     * 变量的描述: 该变量是每一个子类实例都有其对应的一个对象变量
     */
    PreferenceUtil mPreferenceUtil = new PreferenceUtil(this);

    /**
     * 方法描述: 获取实例PreferenceUtil对象，正常情况一定有值不为null
     */
    public PreferenceUtil getPreferenceUtil() {
        return mPreferenceUtil;
    }

    // --------------------------------------------------------------------------------------------------------

    public static final String PREFERENCE_KEY_UID = "pre-key-uid";

    /**
     * 方法描述: 获取用户登录时保存的数据
     */
    public String getUid() {
        if (mPreferenceUtil != null) {
            return mPreferenceUtil.readString(PreferenceUtil.REF_USER_INFO, PREFERENCE_KEY_UID, "");
        } else {
            return "";
        }
    }

    // --------------------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (mLifecycleListener != null) {
            mLifecycleListener.onCreate();
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mLifecycleListener != null) {
            mLifecycleListener.onStart();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mLifecycleListener != null) {
            this.mLifecycleListener.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mLifecycleListener != null) {
            this.mLifecycleListener.onPause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mLifecycleListener != null) {
            this.mLifecycleListener.onStop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLifecycleListener != null) {
            this.mLifecycleListener.onDestroy();
        }
        // 当本类的子类Activity销毁时其对应的PreferenceUtil工具类需要回收
        mPreferenceUtil = null;
    }

}
