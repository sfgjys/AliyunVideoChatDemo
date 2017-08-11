package com.alivc.videochat.demo.logic;

import android.content.Context;
import android.util.Log;
import android.view.SurfaceView;

import com.alivc.videochat.publisher.AlivcPublisherPerformanceInfo;
import com.alivc.videochat.publisher.IMediaPublisher;
import com.alivc.videochat.publisher.MediaConstants;
import com.alivc.videochat.AlivcPlayerPerformanceInfo;
import com.alivc.videochat.AlivcVideoChatParter;
import com.alivc.videochat.IVideoChatParter;
import com.alivc.videochat.VideoScalingMode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by apple on 2017/1/8.
 */

public class PlayerSDKHelper {
    public static final String TAG = PlayerSDKHelper.class.getName();

    public boolean isPlaying = false;
    private boolean mIsPublishPaused = false;

    private boolean isBeautyOn = false;
    private boolean isFlashOn = false;
    private boolean isChatting = false;
    private boolean hasOnlineChats = false;

    AlivcVideoChatParter mChatParter;
    Map<String, String> mMediaParam = new HashMap<>();
    Map mFilterMap = new HashMap<>();
    MgrCallback mCallback;


    /**
     * 初始化播放器
     */
    public void initPlayer(Context context,
                           IVideoChatParter.OnErrorListener errorListener,
                           IVideoChatParter.OnInfoListener infoListener, MgrCallback callback) {
        mMediaParam.put(MediaConstants.PUBLISHER_PARAM_ORIGINAL_BITRATE, "" + 800000);
        mMediaParam.put(MediaConstants.PUBLISHER_PARAM_MIN_BITRATE, "" + 600000);
        mMediaParam.put(MediaConstants.PUBLISHER_PARAM_MAX_BITRATE, "" + 1000000);
        mChatParter = new AlivcVideoChatParter();
        mChatParter.setErrorListener(errorListener);
        mChatParter.init(context);
        mChatParter.setInfoListener(infoListener);
        mChatParter.setScalingMode(VideoScalingMode.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);

        //设置连麦预览/推流时开启美颜
        mFilterMap.put(AlivcVideoChatParter.ALIVC_FILTER_PARAM_BEAUTY_ON, Boolean.toString(true));
        mChatParter.setFilterParam(mFilterMap);
        mCallback = callback;
    }

    /**
     * 开始直播播放（大窗）
     * 9
     *
     * @param surfaceView
     */
    public void startToPlay(String playUrl, final SurfaceView surfaceView) {
        if (!isPlaying) {
            Log.d(TAG, "Call mChatParter.startToPlay()");
            mCallback.onEvent(IPlayerMgr.TYPE_PARTER_OPT_START, null);
            mChatParter.startToPlay(playUrl, surfaceView); //开始直播
            isPlaying = true;
        }
    }

    /**
     * 切换摄像头
     */
    public void switchCamera() {
        Log.d(TAG, "Call mChatParter.switchCamera()");
        if (mChatParter != null) {
            mChatParter.switchCamera();
        }
    }

    /**
     * 开启/关闭美颜
     *
     * @return
     */
    public boolean switchBeauty() {
        if (mChatParter != null) {
            mFilterMap.put(AlivcVideoChatParter.ALIVC_FILTER_PARAM_BEAUTY_ON, Boolean.toString(!isBeautyOn));
            mChatParter.setFilterParam(mFilterMap);
            isBeautyOn = !isBeautyOn;
        }
        return isBeautyOn;
    }

    /**
     * 开启／关闭闪光灯
     *
     * @return
     */
    public boolean switchFlash() {
        Log.d(TAG, "Call mChatParter.switchFlash()");
        if (mChatParter != null) {
            mChatParter.setFlashOn(!isFlashOn);
            isFlashOn = !isFlashOn;
        }
        return isFlashOn;
    }

    /**
     * 开始连麦
     */
    public void startLaunchChat(String publisherUrl,
                                SurfaceView previewSurface,
                                String hostPlayUrl,
                                Map<String, SurfaceView> urlSurfaceMap) {
        /**
         * 注意： 这里推流输出视频尺寸必须是360 * 640
         */
        //TODO:这里需要SDK支持
        if (!hasOnlineChats && !isChatting) {
            Log.d(TAG, "Call mChatParter.onlineChats() surface is valid ? " + previewSurface.getHolder().getSurface().isValid());
            mCallback.onEvent(IPlayerMgr.TYPE_PARTER_OPT_START, null);
            mChatParter.onlineChats(publisherUrl,
                    180,
                    320,
                    previewSurface, mMediaParam, hostPlayUrl, urlSurfaceMap);
            hasOnlineChats = true;
            isChatting = true;
        } else if (isChatting) {
            addChats(urlSurfaceMap);
        }

    }

    /**
     * 暂停播放 or 连麦
     */
    public void pause() {
        if (mChatParter != null && isPlaying && !mIsPublishPaused) {
            mChatParter.pause();
            Log.d(TAG, "Call mChatParter.pause()");
            mIsPublishPaused = true;
        }
    }

    /**
     * 继续播放 or 连麦
     */
    public void resume() {
        if (mChatParter != null && mIsPublishPaused) {
            Log.d(TAG, "Call mChatParter.resume()");
            mChatParter.resume(); //TODO:需要SDK支持
            mIsPublishPaused = false;
        }
    }

    /**
     * 重连
     *
     * @param url
     */
    public void reconnect(String url) {
//        if (isPlaying) {
        Log.d(TAG, "Call mChatParter.reconnect(" + url + ")");
        mChatParter.reconnect(url);
//        }
    }

    /**
     * 增加连麦
     */
    public void addChats(Map<String, SurfaceView> urlSurfaceMap) {
        //TODO:需要SDK支持
        if (isChatting) {
            Log.d(TAG, "Call mChatParter.addChats()");
            mCallback.onEvent(IPlayerMgr.TYPE_PARTER_OPT_START, null);
            mChatParter.addChats(urlSurfaceMap);
        }
    }

    /**
     * 移除连麦
     *
     * @param playUrls
     */
    public int removeChats(List<String> playUrls) {
        //TODO:需要SDK支持
        if (isChatting) {
            Log.d(TAG, "Call mChatParter.removeChats()");
            mCallback.onEvent(IPlayerMgr.TYPE_PARTER_OPT_START, null);
            return mChatParter.removeChats(playUrls);
        }
        return -1;
    }

    /**
     * 停止播放
     */
    public void stopPlaying() {
        if (mChatParter != null && isPlaying) {
            Log.d(TAG, "Call mChatParter.stopPlaying()");
            mCallback.onEvent(IPlayerMgr.TYPE_PARTER_OPT_START, null);
            mChatParter.stopPlaying();
            isPlaying = false;
        }
    }

    /**
     * 结束连麦
     */
    public void abortChat() {
        if (mChatParter != null && isChatting) {
            Log.d(TAG, "Call mChatParter.offlineChat()");
            mCallback.onEvent(IPlayerMgr.TYPE_PARTER_OPT_START, null);
            mChatParter.offlineChat();
            isChatting = false;
            hasOnlineChats = false;
        }
    }


    /**
     * 释放播放器资源
     */
    public void releaseChatParter() {
        if (mChatParter != null) {
            Log.d(TAG, "Call mChatParter.release()");
            mChatParter.release();
        }

    }

    public AlivcPublisherPerformanceInfo getPublisherPerformanceInfo() {
        if (mChatParter != null)
            return mChatParter.getPublisherPerformanceInfo();
        else
            return null;
    }

    public AlivcPlayerPerformanceInfo getPlayerPerformanceInfo(String url) {
        if (mChatParter != null)
            return mChatParter.getPlayerPerformanceInfo(url);
        else
            return null;
    }
}
