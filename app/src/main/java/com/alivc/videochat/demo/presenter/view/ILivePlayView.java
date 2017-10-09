package com.alivc.videochat.demo.presenter.view;

import android.view.SurfaceView;

import com.alivc.videochat.demo.exception.ImException;

import java.util.List;
import java.util.Map;

public interface ILivePlayView {

    /**
     * 方法描述: 获取渲染主播流使用的SurfaceView
     */
    SurfaceView getPlaySurfaceView();

    /**
     * 方法描述: 获取本观众用来推流的SurfaceView，该SurfaceView设置了点击右上角的关闭，可以退出连麦的操作，并返回本观众用来推流的SurfaceView
     */
    SurfaceView showLaunchChatUI();

    /**
     * 方法描述: 根据参数invteeUIDs来获取其他连麦用户播放的SurfaceView
     */
    Map<String, SurfaceView> getOtherParterViews(List<String> invteeUIDs);

    /**
     * 方法描述: 其他人退出连麦的UI,根据参数uid去移除对应的连麦UI
     */
    void showExitChattingUI(String inviteeUID);

    /**
     * 方法描述: 隐藏所有和连麦有关中的控件UI
     */
    void showSelfExitChattingUI();

    /**
     * 方法描述: 显示主播结束直播的界面的UI
     */
    void showLiveCloseUI();
    // --------------------------------------------------------------------------------------------------------

    /**
     * 方法描述: 观看界面在进行观看时出现了严重问题，没法继续观看下去了，这个时候需要弹出一个提示对话框显示错误，并且在用户点击确定后关闭观看界面
     */
    void showLiveInterruptUI(int msgRedID, int what);

    /**
     * 方法描述: 将方法的参数作为消息提示显示对话框
     */
    void showInfoDialog(String msg);

    /**
     * 方法描述: 显示正在加载的界面
     */
    void showLoading();

    /**
     * 方法描述: 隐藏正在加载的界面
     */
    void hideLoading();

    /**
     * 方法描述: 显示参数资源Id所对应的字符串
     */
    void showToast(int id);

    /**
     * 方法描述: 直接显示参数字符串
     */
    void showToast(String msg);

    /**
     * 方法描述: 显示邀请连麦的请求失败的UI，也就是弹个吐司
     */
    void showInviteRequestFailedUI(Throwable cause);
    // --------------------------------------------------------------------------------------------------------

    /**
     * 方法描述: 显示进入直播间失败的UI ------> 但是当前还没有具体内容
     */
    void showEnterLiveRoomFailure();

    /**
     * 方法描述: 暂时不知道时做什么的
     */
    void hideLiveInterruptUI();

    /**
     * 方法描述: 显示正在连麦的Loading View ------> 但是没人用
     */
    void showChattingView();

    /**
     * 方法描述: 隐藏正在连麦的Loading View，虽然被调用了 ------> 但是最终还是没人用
     */
    void hideChattingView();

    /**
     * 方法描述: 显示第一帧的时间 ------> 但是没人用
     */
    void showFirstFrameTime(long firstFrameTime);

    /**
     * 方法描述: 直播观看需要消息推送服务，如果在登录或者初始化MNS中出现问题时，请关闭本界面，重新进入 ------> 但是没人用
     */
    void showImInitInvalidDialog(ImException e);

    /**
     * 方法描述: 关闭视频聊天小视图 ------> 但是没人用
     */
    void closeVideoChatSmallView();

    /**
     * 方法描述: 显示邀请连麦的请求成功UI ------> 但是没人用
     */
    void showInviteRequestSuccessUI();

    /**
     * 方法描述: 显示关闭连麦失败的UI ------> 但是没人用
     */
    void showCloseChatFailedUI();

    /**
     * 方法描述: 显示开始连麦的按钮 ------> 但是没人用
     */
    void showOnlineChatBtn();

    /**
     * 方法描述: 显示结束连麦的按钮 ------> 但是没人用
     */
    void showOfflineChatBtn();
}
