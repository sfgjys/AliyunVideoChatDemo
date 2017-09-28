package com.alivc.videochat.demo.presenter;

import com.alivc.videochat.demo.base.ILifecycleListener;
import com.alivc.videochat.demo.ui.LogInfoFragment;

/**
 * 接口的描述:
 */
public interface ILifecycleLivePlayPresenter extends ILifecycleListener {

    /**
     * 方法描述: 进入直播观看界面的时候就请求网络获取播放地址进行播放
     */
    void enterLiveRoom(String liveRoomID);

    /**
     * 方法描述: 摄像
     */
    void switchCamera();

    /**
     * 方法描述: 美颜
     */
    boolean switchBeauty();

    /**
     * 方法描述: 闪光灯
     */
    boolean switchFlash();

    /**
     * 方法描述: 观众主动要与主播进行连麦
     */
    void invite();

    /**
     * 方法描述: 观众主动断开与主播的连麦
     */
    void exitChatting();

    /**
     * 方法描述: 观众主动退出直播间  该方法暂时没人用
     */
    void exitLiveRoom();

    /**
     * 方法描述: 性能日志更新
     */
    void updateLog(LogInfoFragment.LogHandler logHandler);
}
