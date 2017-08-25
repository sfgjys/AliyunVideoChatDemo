package com.alivc.videochat.demo.presenter.view;

import android.view.SurfaceView;

import com.alivc.videochat.demo.exception.ImException;

import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Created by apple on 2017/1/9.
 */

public interface ILivePlayView {

    /**
     * 方法描述: 获取渲染主播流使用的SurfaceView
     */
    SurfaceView getPlaySurfaceView();

    //显示进入直播间失败的UI
    void showEnterLiveRoomFailure();

    //显示直播中断的UI
    void showLiveInterruptUI(int msgRedID, int what);

    void showInfoDialog(String msg);


    void showLoading();

    void hideLoading();

    void showToast(int id);

    void showToast(String msg);

    void hideLiveInterruptUI();

    void showLiveCloseUI();

    void showChattingView();

    void hideChattingView();

    void showFirstFrameTime(long firstFrameTime);

    void showImInitInvalidDialog(ImException e);

    void closeVideoChatSmallView();

    void showInviteRequestSuccessUI();

    void showInviteRequestFailedUI(Throwable cause);

    void showCloseChatFailedUI();

    //显示连麦的UI,返回预览的SurfaceView
    SurfaceView showLaunchChatUI();

    void showOfflineChatBtn();

    void showOnlineChatBtn();

    //获取其他连麦用户播放的SurfaceView
    Map<String, SurfaceView> getOtherParterViews(List<String> invteeUIDs);

    //其他人退出连麦的UI
    void showExitChattingUI(String inviteeUID);

    //自己退出连麦的UI
    void showSelfExitChattingUI();
}
