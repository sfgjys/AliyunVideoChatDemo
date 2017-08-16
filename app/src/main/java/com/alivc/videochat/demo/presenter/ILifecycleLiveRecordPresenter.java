package com.alivc.videochat.demo.presenter;

import android.view.SurfaceView;

import com.alivc.videochat.demo.base.ILifecycleListener;
import com.alivc.videochat.demo.logic.LifecyclePublisherMgr;
import com.alivc.videochat.demo.ui.LogInfoFragment;

import java.util.List;

/**
 * Created by apple on 2017/1/8.
 */

public interface ILifecycleLiveRecordPresenter extends ILifecycleListener {

    //TODO:这里应该增加DI（比如dagger2），并且使用对象生命周期管理，来实现注入，就不需要这些耦合存在了
    //TODO：如果不做DI，也要把对象的管理单独抽象出来，这里后期需要重构
    LifecyclePublisherMgr getPublisherMgr();

    void refreshLogInfo(LogInfoFragment.LogHandler logHandler);

    // --------------------------------------------------------------------------------------------------------

    /**
     * 方法描述: 开始预览
     */
    void startPreview(SurfaceView previewSurf);

    /**
     * 方法描述: 邀请连麦
     */
    void inviteChat(List<String> inviteeUIDs);

    /**
     * 方法描述: 切换摄像头
     */
    void switchCamera();

    /**
     * 方法描述: 切换美颜
     */
    boolean switchBeauty();

    /**
     * 方法描述: 切换闪光灯
     */
    boolean switchFlash();

    /**
     * 方法描述: 缩放
     */
    void zoom(float scaleFlator);

    /**
     * 方法描述: 对焦
     */
    void autoFocus(float xRatio, float yRatio);

    /**
     * 方法描述: 结束直播
     */
    void terminateLive();

    /**
     * 方法描述: 结束连麦
     */
    void terminateChatting(String playerUID);

    /**
     * 方法描述: 结束所有连麦
     */
    void terminateAllChatting();
}
