package com.alivc.videochat.demo.presenter.view;

import android.view.SurfaceView;

/**
 * 类的描述: 用于更新LiveActivity中的UI的接口，在LiveActivity中实现一个自定义的实例对象，在将这个对象传入LifecycleLiveRecordPresenterImpl中就可以根据直播周期去更新界面的UI
 */
public interface ILiveRecordView {


    /**
     * 方法描述: 获取空闲的ChattingViewHolder 设置SurfaceView和关闭按钮可见  将空闲的ChattingViewHolder与连麦的uid一起存储进mUsedViewHolderMap
     * 设置SurfaceView对应的关闭按钮的点击事件(根据连麦的uid调用关闭连麦的方法)
     *
     * @return 返回用于播放连麦人的SurfaceView
     */
    SurfaceView showChattingUI(String uid);

    /**
     * 方法描述: 当调用完sdk退出连麦后，我们也需要更新UI界面的连麦退出  分为退出所有连麦，和其中某个退出连麦
     *
     * @param playerUID 指定哪个连麦退出，playerUID没有值，则是退出所有正在连麦的
     */
    void showTerminateChattingUI(String playerUID);

    /**
     * 方法描述: 显示连麦邀请失败的UI---请求网络邀请观众进行连麦的请求失败了，所以界面需要展示失败的原因
     */
    void showInviteVideoChatFailedUI(Throwable e);

    /**
     * 方法描述: 因为主播在进行推流的时候一直没有成功，所以显示 提示主播退出直播的画面。
     * 注: 暂时没用
     */
    void showLiveCloseUI();

    // --------------------------------------------------------------------------------------------------------

    /**
     * 方法描述: 显示连麦邀请成功的UI--> 邀请观众进行连麦的网络请求发送成功，弹噶吐司说明下，并关闭选择连麦对象的对话框
     */
    void showInviteVideoChatSuccessfulUI();

    /**
     * 方法描述: 将参数对应的String资源通过吐司展示
     */
    void showToast(int msgId);

    // --------------------------------------------------------------------------------------------------------

    /**
     * 方法描述: 直播界面的直播或者连麦出现严重错误，需要弹出对话框显示错误原因，当用户点击确认按钮后，关闭直播界面，让用户自己决定是否重新进入
     */
    void showInterruptUI(int msgResID, int what);

    /**
     * 方法描述: 将参数通过对话框进行显示
     */
    void showInfoDialog(String msg);

    /**
     * 方法描述: MNS初始化出现问题，导致失败了，但是MNS又是直播中必须的功能，所以弹出对话框显示问题，让用户点击确认结束本界面
     * 注: 暂时没用
     */
    void showImInitFailedDialog(int tipResID, int errorType);

    // --------------------------------------------------------------------------------------------------------

    /**
     * 方法描述: 显示摄像头打开失败的UI
     */
    void showCameraOpenFailureUI();

    /**
     * 方法描述: 开闭美颜
     */
    void updateBeautyUI(boolean beautyOn);

    /**
     * 方法描述: 关闭直播界面
     */
    void finishActivity();

    /**
     * 方法描述: 显示关闭连麦失败的UI
     */
    void showCloseChatFailedUI();

    /**
     * 方法描述: 显示邀请连麦响应超时UI
     */
    void showInviteChattingTimeoutUI(String uid);

    /**
     * 方法描述: 提示没有权限
     */
    void showNoPermissionTip();

    /**
     * 方法描述: 显示连麦关闭通知对话框
     */
    void showChatCloseNotifyDialog(String name);

    /**
     * 方法描述: 隐藏中断的UI
     */
    void hideInterruptUI();
}
