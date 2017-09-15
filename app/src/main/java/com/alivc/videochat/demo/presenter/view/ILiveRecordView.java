package com.alivc.videochat.demo.presenter.view;

import android.view.SurfaceView;

/**
 * 类的描述: 用于更新LiveActivity中的UI的接口，在LiveActivity中实现一个自定义的实例对象，在将这个对象传入LifecycleLiveRecordPresenterImpl中就可以根据直播周期去更新界面的UI
 */
public interface ILiveRecordView {
    //隐藏中断的UI
    void hideInterruptUI();

    //显示中断的UI
    void showInterruptUI(int msgResID, int what);

    //显示摄像头打开失败的UI
    void showCameraOpenFailureUI();

    //显示邀请连麦响应超时UI
    void showInviteChattingTimeoutUI(String uid);

    /**
     * 方法描述: 获取空闲的ChattingViewHolder 设置SurfaceView和关闭按钮可见  将空闲的ChattingViewHolder与连麦的uid一起存储进mUsedViewHolderMap
     * 设置SurfaceView对应的关闭按钮的点击事件(根据连麦的uid调用关闭连麦的方法)
     *
     * @return 返回用于播放连麦人的SurfaceView
     */
    SurfaceView showChattingUI(String uid);

    //显示连麦邀请成功的UI
    void showInviteVideoChatSuccessfulUI();

    //显示连麦邀请失败的UI
    void showInviteVideoChatFailedUI(Throwable e);

    //显示中断连麦的UI
    void showTerminateChattingUI(String playerUID);

    /**
     * 方法描述: 将参数通过对话框进行显示
     */
    void showInfoDialog(String msg);

    /**
     * 方法描述: 将参数对应的String资源通过吐司展示
     */
    void showToast(int msgId);

    void updateBeautyUI(boolean beautyOn);

    void finishActivity();

    void showImInitFailedDialog(int tipResID, int errorType);

    void showNoPermissionTip();

    void showChatCloseNotifyDialog(String name);

    void showLiveCloseUI();

    void showCloseChatFailedUI();
}
