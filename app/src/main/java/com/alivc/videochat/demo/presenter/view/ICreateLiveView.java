package com.alivc.videochat.demo.presenter.view;

/**
 * 类的描述: 本接口作用是在CreateLiveFragment中实例化，在LifecycleCreateLivePresenterImpl中调用showPublishStreamUI方法，将请求推流地址的结果回调给CreateLiveFragment
 */
public interface ICreateLiveView {
    // 显示推流的UI
    void showPublishStreamUI(String roomID, String name, String uid);
}
