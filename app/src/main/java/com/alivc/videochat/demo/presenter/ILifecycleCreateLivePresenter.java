package com.alivc.videochat.demo.presenter;

import com.alivc.videochat.demo.base.ILifecycleListener;

/**
 * 类的描述: 该接口用于请求网络获取直播推流Url的界面时的操作者实例，也就是在创建直播间时的界面
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
