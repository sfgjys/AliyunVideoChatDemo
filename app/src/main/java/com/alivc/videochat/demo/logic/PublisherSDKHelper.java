package com.alivc.videochat.demo.logic;

import android.content.Context;
import android.util.Log;
import android.view.SurfaceView;

import com.alibaba.livecloud.live.AlivcMediaFormat;
import com.alivc.videochat.VideoScalingMode;
import com.alivc.videochat.player.MediaPlayer;
import com.alivc.videochat.publisher.AlivcPublisherPerformanceInfo;
import com.alivc.videochat.publisher.IMediaPublisher;
import com.alivc.videochat.publisher.MediaConstants;
import com.alivc.videochat.AlivcPlayerPerformanceInfo;
import com.alivc.videochat.AlivcVideoChatHost;
import com.alivc.videochat.IVideoChatHost;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by apple on 2017/1/6.
 */

public class PublisherSDKHelper {
    private static final String TAG = PublisherSDKHelper.class.getName();
    private Map<String, String> mMediaParam = new HashMap<>();     //推流器参数
    private Map mFilterMap = new HashMap<>();                      //滤镜参数（目前只有美颜一个滤镜）
    private AlivcVideoChatHost mChatHost;

    private static final int STATUS_MASK = 0;
    private static final int STATUS_PAUSED = 1 << 1;
    private static final int STATUS_CHATTING = 1 << 2;
    private static final int STATUS_PUBLISHING = 1 << 3;
    private static final int STATUS_PREVIEW = 1 << 4;

    private boolean isBeautyOn = true;              //美颜是否开启
    private boolean isFlashOn = false;              //闪光灯是否开启

    private int mCameraFacing = AlivcMediaFormat.CAMERA_FACING_FRONT;
    private int mStatus = STATUS_MASK;

    private List<String> mChattingUrls = new ArrayList<>(); //正在参与连麦的URL

    /**
     * 初始化推流器
     */
    public void initRecorder(Context context, IVideoChatHost.OnErrorListener errorListener, IVideoChatHost.OnInfoListener infoListener) {
        //设置推流器推流相关参数
        mMediaParam.put(MediaConstants.PUBLISHER_PARAM_ORIGINAL_BITRATE, "" + 800000);        //初始码率
        mMediaParam.put(MediaConstants.PUBLISHER_PARAM_MIN_BITRATE, "" + 600000);        //最小码率
        mMediaParam.put(MediaConstants.PUBLISHER_PARAM_MAX_BITRATE, "" + 1000000);        //最大码率

        mChatHost = new AlivcVideoChatHost();
        Log.d(TAG, "Call mChatHost.init()");
        mChatHost.init(context);
        mChatHost.setScalingMode(VideoScalingMode.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);

        //设置错误信息回调
        mChatHost.setErrorListener(errorListener);

        //设置状态信息回调
        mChatHost.setInfoListener(infoListener);

        //设置美颜开启
        mFilterMap.put(AlivcVideoChatHost.ALIVC_FILTER_PARAM_BEAUTY_ON, Boolean.toString(true));
        mChatHost.setFilterParam(mFilterMap);
    }


    /**
     * 开启预览
     */
    public void startPreView(SurfaceView surf) {
        if ((mStatus & STATUS_PREVIEW) == STATUS_MASK) {
            Log.d(TAG, "Call mChatHost.prepareToPublish()");
            mChatHost.prepareToPublish(surf, 360, 640, mMediaParam);
            mStatus |= STATUS_PREVIEW;
            if (mCameraFacing == AlivcMediaFormat.CAMERA_FACING_FRONT) {
                mChatHost.setFilterParam(mFilterMap);
            }
        }
    }


    /**
     * 开始推流
     *
     * @param publishUrl
     */
    public void startPublishStream(String publishUrl) {
        //调用连麦SDK，开始推流
        if ((mStatus & STATUS_PUBLISHING) == STATUS_MASK) {
            Log.d(TAG, "Call mChatHost.startToPublish()");
            mChatHost.startToPublish(publishUrl);
            mStatus |= STATUS_PUBLISHING;
        }
    }

    /**
     * 切换摄像头
     */
    public void switchCamera() {
        if (mChatHost != null) {
            Log.d(TAG, "Call mChatHost.switchCamera");
            mChatHost.switchCamera();
        }
    }

    /**
     * 闪光灯开启/关闭
     *
     * @return
     */
    public boolean switchFlash() {
        if (mChatHost != null) {
            Log.d(TAG, "Call mChatHost.switchFlash(" + isFlashOn + ")");
            if (!isFlashOn) {
                mChatHost.setFlashOn(true);
            } else {
                mChatHost.setFlashOn(false);
            }
            isFlashOn = !isFlashOn;
        }
        return isFlashOn;
    }

    /**
     * 美颜开启/关闭
     *
     * @return
     */
    public boolean switchBeauty() {
        if (mChatHost != null) {
            if (!isBeautyOn) {
                mFilterMap.put(AlivcVideoChatHost.ALIVC_FILTER_PARAM_BEAUTY_ON, Boolean.toString(true));
            } else {
                mFilterMap.put(AlivcVideoChatHost.ALIVC_FILTER_PARAM_BEAUTY_ON, Boolean.toString(false));
            }
            mChatHost.setFilterParam(mFilterMap);
            isBeautyOn = !isBeautyOn;
        }

        return isBeautyOn;
    }

    /**
     * 连麦重连
     */
    public void reconnect(String url) {
        Log.d(TAG, "Call mChatHost.reconnectChat(" + url + ")");
        mChatHost.reconnectChat(url);
    }


    /**
     * 摄像头缩放
     *
     * @param scaleFactor
     */
    public void zoom(float scaleFactor) {
        if (mChatHost != null) {
            Log.d(TAG, "Call mChatHost.zoom(" + scaleFactor + ")");
            mChatHost.zoomCamera(scaleFactor);
        }
    }

    /**
     * 自动对焦
     *
     * @param x
     * @param y
     */
    public void autoFocus(float x, float y) {
        if (mChatHost != null) {
            Log.d(TAG, "Call mChatHost.focusCameraAtAdjustedPoint(" + x + ", " + y + ")");
            mChatHost.focusCameraAtAdjustedPoint(x, y);
        }
    }

    /**
     * 连麦
     */
    public void launchChats(Map<String, SurfaceView> urlSurfaceMap) {
        if ((mStatus & STATUS_CHATTING) == STATUS_MASK && mChattingUrls.size() == 0) {
            Log.d(TAG, "Call mChatHost.launchChats()");
            mChatHost.launchChats(urlSurfaceMap);
            mChattingUrls.addAll(urlSurfaceMap.keySet());
            mStatus |= STATUS_CHATTING;
        } else if (mChattingUrls.size() > 0) {
            Log.d(TAG, "Call mChatHost.addChats()");
            mChatHost.addChats(urlSurfaceMap);
            mChattingUrls.addAll(urlSurfaceMap.keySet());
        }
    }

    /**
     * 暂停推流 or 连麦
     */
    public void pause() {
        if (mChatHost != null && (mStatus & STATUS_PAUSED) == STATUS_MASK) {
            Log.d(TAG, "Call mChatHost.pause()");
            //暂停推流
            mChatHost.pause();
            mStatus |= STATUS_PAUSED;
        }
    }

    /**
     * 恢复推流/连麦
     */
    public void resume() {
        if (mChatHost != null && (mStatus & STATUS_PAUSED) == STATUS_PAUSED) {
            Log.d(TAG, "Call mChatHost.resume()");
            mChatHost.resume();
            mStatus ^= STATUS_PAUSED;
        }
    }

    /**
     * 终止连麦
     */
    public int abortChat(List<String> urls) {
        Log.d(TAG, "abort chat status " + mStatus);
        if (mChatHost != null && (mStatus & STATUS_CHATTING) == STATUS_CHATTING) {
            if (urls == null || urls.size() == 0) {
                Log.d(TAG, "Call mChatHost.abortChat()");
                mChatHost.abortChat();
                mChattingUrls.clear();
                mStatus ^= STATUS_CHATTING;
            } else {
                Log.d(TAG, "Call mChatHost.removeChats()");
                mChatHost.removeChats(urls);
                mChattingUrls.removeAll(urls);
                if (mChattingUrls.size() == 0) {
                    mStatus ^= STATUS_CHATTING;
                }
            }
            return 0;
        } else {
            return -1;
        }
    }

    /**
     * 停止推流
     */
    public void stopPublish() {
        if (null != mChatHost && (mStatus & STATUS_PREVIEW) == STATUS_PREVIEW) {
            Log.d(TAG, "Call mChatHost.stopPublishing()");
            mChatHost.stopPublishing();
            Log.d(TAG, "Call mChatHost.finishPublishing()");
            mChatHost.finishPublishing();
            mStatus = STATUS_MASK;
        }
    }


    /**
     * 释放推流器资源
     */
    public void releaseRecorder() {
        if (mChatHost != null) {
            Log.d(TAG, "Call mChatHost.release()");
            mChatHost.release();
            mChatHost = null;
        }
    }

    public AlivcPublisherPerformanceInfo getPublisherPerformanceInfo() {
        if (mChatHost != null)
            return mChatHost.getPublisherPerformanceInfo();
        else
            return null;
    }

    public AlivcPlayerPerformanceInfo getPlayerPerformanceInfo(String url) {
        if (mChatHost != null)
            return mChatHost.getPlayerPerformanceInfo(url);
        else
            return null;
    }
}
