package com.alivc.videochat.demo.presenter;

import com.alivc.videochat.demo.base.ILifecycleListener;

/**
 * Created by apple on 2017/1/9.
 */

public interface ILifecycleCreateLivePresenter extends ILifecycleListener {
    /**
     * 方法描述: 请求网络获取直播推流URL
     *
     * @param description 描述这个推流url的直播标题
     */
    void createLive(String description);

    void switchBeauty();

    void switchCamera();
}
