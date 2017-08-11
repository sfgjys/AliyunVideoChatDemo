package com.alivc.videochat.demo.presenter;

import com.alivc.videochat.demo.base.ILifecycleListener;

/**
 * Created by apple on 2017/1/9.
 */

public interface ILifecycleCreateLivePresenter extends ILifecycleListener {
    //创建直播
    void createLive(String description);

    void switchBeauty();

    void switchCamera();
}
