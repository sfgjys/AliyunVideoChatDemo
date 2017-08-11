package com.alivc.videochat.demo.base;

/**
 * 类的描述: BaseActivity生命周期变化监听类
 */
public interface ILifecycleListener {
    void onCreate();

    void onStart();

    void onResume();

    void onPause();

    void onStop();

    void onDestroy();
}
