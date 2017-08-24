package com.alivc.videochat.demo.presenter;

import com.alivc.videochat.demo.base.ILifecycleListener;
import com.alivc.videochat.demo.ui.LogInfoFragment;

/**
 * 类的描述:
 */
public interface ILifecycleLivePlayPresenter extends ILifecycleListener {

    void enterLiveRoom(String liveRoomID);

    void invite();

    void switchCamera();

    boolean switchBeauty();

    boolean switchFlash();

    void exitChatting();

    void exitLiveRoom();

    void updateLog(LogInfoFragment.LogHandler logHandler);
}
