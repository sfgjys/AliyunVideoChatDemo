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

    //显示连麦的UI,返回渲染播放连麦画面用的SurfaceView
    SurfaceView showChattingUI(String uid);

    //显示连麦邀请成功的UI
    void showInviteVideoChatSuccessfulUI();

    //显示连麦邀请失败的UI
    void showInviteVideoChatFailedUI(Throwable e);

    //显示中断连麦的UI
    void showTerminateChattingUI(String playerUID);

    void showInfoDialog(String msg);

    void showToast(int msgId);

    void updateBeautyUI(boolean beautyOn);

    void finishActivity();

    void showImInitFailedDialog(int tipResID, int errorType);

    void showNoPermissionTip();

    void showChatCloseNotifyDialog(String name);

    void showLiveCloseUI();

    void showCloseChatFailedUI();
}
