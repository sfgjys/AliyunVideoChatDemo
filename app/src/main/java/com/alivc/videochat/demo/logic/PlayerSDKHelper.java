package com.alivc.videochat.demo.logic;

import android.content.Context;
import android.util.Log;
import android.view.SurfaceView;

import com.alivc.videochat.publisher.AlivcPublisherPerformanceInfo;
import com.alivc.videochat.publisher.MediaConstants;
import com.alivc.videochat.AlivcPlayerPerformanceInfo;
import com.alivc.videochat.AlivcVideoChatParter;
import com.alivc.videochat.IVideoChatParter;
import com.alivc.videochat.VideoScalingMode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 类的描述: 本类初始化AlivcVideoChatParter连麦操作观众端控制类，并封装了连麦中的各种方法
 */
public class PlayerSDKHelper {
    public static final String TAG = PlayerSDKHelper.class.getName();

    /**
     * 变量的描述: 是否正在观看主播播放
     */
    public boolean isPlaying = false;
    /**
     * 变量的描述: 连麦的推流是否暂停
     */
    private boolean mIsPublishPaused = false;
    /**
     * 变量的描述: 美颜的开启
     */
    private boolean isBeautyOn = false;
    /**
     * 变量的描述: 闪光灯的开启
     */
    private boolean isFlashOn = false;
    /**
     * 变量的描述: 是否正在连麦中
     */
    private boolean isChatting = false;
    /**
     * 变量的描述: 是否开启了连麦
     */
    private boolean hasOnlineChats = false;
    /**
     * 变量的描述: 观众端核心控制类
     */
    AlivcVideoChatParter mChatParter;
    /**
     * 变量的描述: 连麦时推流的参数。使用Map的方式，以便于后续的扩展。目前可以设置的参数如下（在类MediaConstants中）：
     * MediaConstants.PUBLISHER_PARAM_UPLOAD_TIMEOUT：推流上传超时时间，单位ms。
     * MediaConstants.PUBLISHER_PARAM_CAMERA_POSITION ：选择前后摄像头。
     * MediaConstants.PUBLISHER_PARAM_AUDIO_SAMPLE_RATE：推流音频采样率，单位Hz。固定32000，暂不可调。
     * MediaConstants.PUBLISHER_PARAM_ORIGINAL_BITRATE：推流初始码率，单位Kbps。默认500。
     * MediaConstants.PUBLISHER_PARAM_MAX_BITRATE：推流最大码率，单位Kbps。默认1500。
     * MediaConstants.PUBLISHER_PARAM_MIN_BITRATE：推流最小码率，单位Kbps。默认200。
     * MediaConstants.PUBLISHER_PARAM_AUDIO_BITRATE：推流音频码率，单位Kbps。固定96，暂不可调。
     * MediaConstants.PUBLISHER_PARAM_VIDEO_FPS：推流视频码率。
     * MediaConstants.PUBLISHER_PARAM_SCREEN_ROTATION ：推流横屏/竖屏。
     * MediaConstants.PUBLISHER_PARAM_FRONT_CAMERA_MIRROR：前置摄像头是否镜像。
     */
    Map<String, String> mMediaParam = new HashMap<>();
    /**
     * 变量的描述: 滤镜相关的配置参数。使用Map的方式，以便于后续的扩展。目前只有一个美颜的滤镜，可以设置的参数如下：
     * MediaConstants.FILTER_PARAM_BEAUTY_ON：美颜是否开启，默认为true
     * MediaConstants.FILTER_PARAM_BEAUTY_WHITEN：美白程度[0,100]，默认为0
     * MediaConstants.FILTER_PARAM_BEAUTY_BUFFING：磨皮程度[0,35]，默认为0
     */
    Map<String, String> mFilterMap = new HashMap<>();
    /**
     * 方法描述: 将本类操作结果回调给 MgrCallback接口的实例 该接口实例是要根据播放核心类操作结果去更新Ui的，所以实例在LifecycleLivePlayPresenterImpl中实现
     */
    ManagerCallback mCallback;

    /**
     * 方法描述: 初始化播放器
     */
    public void initPlayer(Context context, IVideoChatParter.OnErrorListener errorListener, IVideoChatParter.OnInfoListener infoListener, ManagerCallback callback) {
        mMediaParam.put(MediaConstants.PUBLISHER_PARAM_ORIGINAL_BITRATE, "" + 800000);
        mMediaParam.put(MediaConstants.PUBLISHER_PARAM_MIN_BITRATE, "" + 600000);
        mMediaParam.put(MediaConstants.PUBLISHER_PARAM_MAX_BITRATE, "" + 1000000);


        mChatParter = new AlivcVideoChatParter();
        mChatParter.setErrorListener(errorListener);// 设置错误监听器。因为也有初始化失败的情况所以在初始化前就设置错误监听
        mChatParter.init(context);// 初始化AlivcVideoChatParter类。
        mChatParter.setInfoListener(infoListener);// 设置信息监听器。

        // 设置观众渲染模式。
        // 参数：VideoScalingMode mode，有以下几种模式：
        // VideoScalingMode.VIDEO_SCALING_MODE_SCALE_TO_FIT：适应屏幕显示
        // VideoScalingMode.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING：充满屏幕显示，保持比例，如果屏幕比例不对，则进行裁剪
        mChatParter.setScalingMode(VideoScalingMode.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);

        //设置连麦预览/推流时开启美颜
        mFilterMap.put(AlivcVideoChatParter.ALIVC_FILTER_PARAM_BEAUTY_ON, Boolean.toString(true));
        mChatParter.setFilterParam(mFilterMap);// 设置滤镜的相关参数。
        mCallback = callback;
    }

    /**
     * 方法描述: 释放AlivcVideoChatParter类。
     */
    public void releaseChatParter() {
        if (mChatParter != null) {
            Log.d(TAG, "Call mChatParter.release()");
            mChatParter.release();
        }
    }

    // --------------------------------------------------------------------------------------------------------

    /**
     * 方法描述: 开始观看直播。调用该函数将启动直播播放器，并播放获取到的直播流。开始直播播放（大窗）
     *
     * @param playUrl     直播地址。
     * @param surfaceView 直播播放器的渲染窗口。
     */
    public void startToPlay(String playUrl, final SurfaceView surfaceView) {
        if (!isPlaying) {
            Log.d(TAG, "Call mChatParter.startToPlay()");
            mCallback.onEvent(IPlayerManager.TYPE_PARTER_OPT_START, null);
            mChatParter.startToPlay(playUrl, surfaceView); //开始直播
            isPlaying = true;
        }
    }

    /**
     * 方法描述: 结束观看直播。调用该函数将关闭直播播放器，并销毁所有资源。
     * 备注: 若当前处在连麦状态下，需要先调用offlineChat函数结束连麦，然后在调用该函数结束观看直播
     */
    public void stopPlaying() {
        if (mChatParter != null && isPlaying) {
            Log.d(TAG, "Call mChatParter.stopPlaying()");
            mCallback.onEvent(IPlayerManager.TYPE_PARTER_OPT_START, null);
            mChatParter.stopPlaying();
            isPlaying = false;
        }
    }

    // --------------------------------------------------------------------------------------------------------

    /**
     * 方法描述: 开始多人连麦。调用该函数将开启音视频的采集设备、启动预览功能、启动音视频编码功能并将压缩后的音视频流上传。同时将播放地址切换到具备短延时功能的新地址，并且播放其他连麦中的流。
     * 备注: 这个连麦是主播没有连麦的前提下，如果有连麦了，在加连麦就是用addChats方法
     * 备注：目前视频编码采用的是软编码，多人连麦建议使用180x320分辨率（横屏推流的时候为320x180）。
     *
     * @param publisherUrl   连麦时推流的地址。
     * @param previewSurface 连麦时推流的预览窗口。
     * @param hostPlayUrl    主播播放短延时地址。
     * @param urlSurfaceMap  其他连麦流播放显示的窗口，和其对应的播放url存储在Map集合。
     */
    public void startLaunchChat(String publisherUrl, SurfaceView previewSurface, String hostPlayUrl, Map<String, SurfaceView> urlSurfaceMap) {
        /**
         * 注意： 这里推流输出视频尺寸必须是360 * 640
         */
        //TODO:这里需要SDK支持？？？？？
        if (!hasOnlineChats && !isChatting) {
            Log.d(TAG, "Call mChatParter.onlineChats() surface is valid ? " + previewSurface.getHolder().getSurface().isValid());
            mCallback.onEvent(IPlayerManager.TYPE_PARTER_OPT_START, null);// 回调内容暂时被注释了
            // 参数二三代表编码视频的宽和高。
            mChatParter.onlineChats(publisherUrl, 180, 320, previewSurface, mMediaParam, hostPlayUrl, urlSurfaceMap);// 返回值： 0表示成功；非0表示失败。
            hasOnlineChats = true;
            isChatting = true;
        } else if (isChatting) {
            // 增加连麦人数
            addChats(urlSurfaceMap);
        }
    }

    /**
     * 方法描述: 增加连麦人数。在此之前，主播正在当前观众正在连麦。新增的连麦人数可以是一个人，也可以是多个人。
     * 备注：必须调用函数onlineChats后才能调用该函数。这里增加的是其他连麦的观众
     *
     * @param urlSurfaceMap url所对应的SurfaceView窗口数组。
     */
    public void addChats(Map<String, SurfaceView> urlSurfaceMap) {
        //TODO:需要SDK支持？？？？？
        if (isChatting) {
            Log.d(TAG, "Call mChatParter.addChats()");
            mCallback.onEvent(IPlayerManager.TYPE_PARTER_OPT_START, null);
            mChatParter.addChats(urlSurfaceMap);// 返回值： 0表示成功；非0表示失败。
        }
    }

    /**
     * 方法描述: 减少连麦人数。在此之前，主播正在当前观众连麦。减少的连麦人数可以是一个人，也可以是多个人。
     * 备注：必须调用函数onlineChats后才能调用该函数。这里减少的是其他连麦观众
     *
     * @param playUrls 其他连麦流的地址，String集合
     */
    public int removeChats(List<String> playUrls) {
        //TODO:需要SDK支持？？？？？
        if (isChatting) {
            Log.d(TAG, "Call mChatParter.removeChats()");
            mCallback.onEvent(IPlayerManager.TYPE_PARTER_OPT_START, null);
            return mChatParter.removeChats(playUrls);// 返回值： 0表示成功；非0表示失败。
        }
        return -1;
    }

    /**
     * 方法描述: 结束连麦。调用该函数将结束观众的推流，销毁推流的所有资源，并将  ★ 播放地址切换到连麦之前的地址。
     * 备注: 结束本观众的连麦，
     */
    public void abortChat() {
        if (mChatParter != null && isChatting) {
            Log.d(TAG, "Call mChatParter.offlineChat()");
            mCallback.onEvent(IPlayerManager.TYPE_PARTER_OPT_START, null);
            mChatParter.offlineChat();// 返回值： 0表示成功；非0表示失败。
            isChatting = false;
            hasOnlineChats = false;
        }
    }

    // --------------------------------------------------------------------------------------------------------

    /**
     * 方法描述: 暂停播放或连麦。在播放或连麦过程中，观众如果退入后台、锁屏或有电话接入，可以调用本接口。
     */
    public void pause() {
        if (mChatParter != null && isPlaying && !mIsPublishPaused) {
            mChatParter.pause();
            Log.d(TAG, "Call mChatParter.pause()");
            mIsPublishPaused = true;
        }
    }

    /**
     * 方法描述: 恢复推流或连麦。在观看或连麦过程中，观众如果发生退入后台、锁屏或有电话接入的情况，希望能够回到前台继续播放或推流，可以调用本接口。
     * 备注：必须先调用pause，然后才能调用resume。
     */
    public void resume() {
        if (mChatParter != null && mIsPublishPaused) {
            Log.d(TAG, "Call mChatParter.resume()");
            mChatParter.resume(); //TODO:需要SDK支持？？？？？
            mIsPublishPaused = false;
        }
    }

    /**
     * 方法描述: 当播放视频超时或者遇到网络切换断流时，调用此函数进行重新连接打开。
     * 备注: 此函数不会有黑屏的情况。
     *
     * @param url 指定重新连接的播放地址。
     */
    public void reconnect(String url) {
//        if (isPlaying) {
        Log.d(TAG, "Call mChatParter.reconnect(" + url + ")");
        mChatParter.reconnect(url);
//        }
    }

    // --------------------------------------------------------------------------------------------------------

    /**
     * 方法描述: 切换摄像头。
     * 备注：该函数可以在连麦的过程中随时进行调用。
     */
    public void switchCamera() {
        Log.d(TAG, "Call mChatParter.switchCamera()");
        if (mChatParter != null) {
            mChatParter.switchCamera();
        }
    }

    /**
     * 方法描述: 设置滤镜的相关参数。
     * 备注：该函数可以在连麦过程中随时进行调用。
     */
    public boolean switchBeauty() {
        if (mChatParter != null) {
            mFilterMap.put(AlivcVideoChatParter.ALIVC_FILTER_PARAM_BEAUTY_ON, Boolean.toString(!isBeautyOn));
            mChatParter.setFilterParam(mFilterMap);// 设置滤镜的相关参数。
            isBeautyOn = !isBeautyOn;
        }
        return isBeautyOn;
    }

    /**
     * 方法描述: 设置是否打开闪关灯。
     * 备注：该函数可以在连麦过程中随时进行调用。
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
     * 方法描述: 获得与推流相关的性能参数
     *
     * @return 返回包含推流性能参数的对象
     * <p>
     * 备注：
     * mAudioEncodeBitrate： 音频编码速度，单位Kbps
     * mVideoEncodeBitrate： 视频编码速度，单位Kbps
     * mAudioUploadBitrate： 音频上传速度，单位kbps
     * mVideoUploadBitrate： 视频上传速度，单位kbps
     * mAudioPacketsInBuffer： 缓冲的音频帧数
     * mVideoPacketsInBuffer： 缓冲的视频帧数
     * mVideoEncodedFps： 视频编码帧率
     * mVideoUploadedFps： 视频上传帧率
     * mVideoCaptureFps： 视频采集帧率
     * mCurrentlyUploadedVideoFramePts： 当前上传的视频帧的pts，单位ms
     * mCurrentlyUploadedAudioFramePts： 当前上传的音频帧的pts，单位ms
     * mPreviousKeyFramePts： 上一个关键帧的pts，单位ms
     * mTotalFramesOfEncodedVideo： 视频编码总帧数
     * mTotalTimeOfEncodedVideo： 视频编码总耗时，单位ms
     * mTotalSizeOfUploadedPackets： 上传的音视频流总量，单位Kbyte
     * mTotalTimeOfPublishing： 当前推流的总时间，单位ms
     * mTotalFramesOfVideoUploaded： 上传的视频帧总数
     * mDropDurationOfVideoFrames： 视频丢帧的累计时长，单位ms
     * mVideoDurationFromeCaptureToUpload： 当前音频帧从采集到上传的耗时，单位ms
     * mAudioDurationFromeCaptureToUpload： 当前视频帧从采集到上传的耗时，单位ms
     */
    public AlivcPublisherPerformanceInfo getPublisherPerformanceInfo() {
        if (mChatParter != null)
            return mChatParter.getPublisherPerformanceInfo();
        else
            return null;
    }

    /**
     * 方法描述: 获得与播放相关的性能参数
     *
     * @param url 指定播放url的性能参数
     * @return 返回包含播放性能参数的对象
     * <p>
     * 备注：
     * mVideoPacketsInBuffer： 缓冲的视频帧数
     * mAudioPacketsInBuffer： 缓冲的音频帧数
     * mVideoDurationFromDownloadToRender： 视频从下载到播放的耗时，单位ms
     * mAudioDurationFromDownloadToRender： 音频从下载到播放的耗时，单位ms
     * mVideoPtsOfLastPacketInBuffer： 缓冲区中最后一帧视频的pts
     * mAudioPtsOfLastPacketInBuffer： 缓冲区中最后一帧音频的pts
     * mLiveDiscardDuration： 丢帧的总长度
     * mDowloadSpeed： packets的下载速度
     */
    public AlivcPlayerPerformanceInfo getPlayerPerformanceInfo(String url) {
        if (mChatParter != null)
            return mChatParter.getPlayerPerformanceInfo(url);
        else
            return null;
    }
}
