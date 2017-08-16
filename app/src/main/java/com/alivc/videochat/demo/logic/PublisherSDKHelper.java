package com.alivc.videochat.demo.logic;

import android.content.Context;
import android.util.Log;
import android.view.SurfaceView;

import com.alibaba.livecloud.live.AlivcMediaFormat;
import com.alivc.videochat.VideoScalingMode;
import com.alivc.videochat.publisher.AlivcPublisherPerformanceInfo;
import com.alivc.videochat.publisher.MediaConstants;
import com.alivc.videochat.AlivcPlayerPerformanceInfo;
import com.alivc.videochat.AlivcVideoChatHost;
import com.alivc.videochat.IVideoChatHost;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 类的描述: 本类初始化AlivcVideoChatHost连麦操作控制类，并封装了连麦中的各种方法
 */
public class PublisherSDKHelper {
    private static final String TAG = PublisherSDKHelper.class.getName();
    /**
     * 变量的描述: 连麦直播操作的各种方法封装对象
     */
    private AlivcVideoChatHost mChatHost;
    /**
     * 变量的描述: 存储有推流参数的集合 主播推流的参数。使用Map的方式，以便于后续的扩展
     * MediaConstants.PUBLISHER_PARAM_UPLOAD_TIMEOUT：推流上传超时时间，单位ms。
     * MediaConstants.PUBLISHER_PARAM_CAMERA_POSITION ：选择前后摄像头。
     * MediaConstants.PUBLISHER_PARAM_AUDIO_SAMPLE_RATE：推流音频采样率，单位Hz。固定32000，暂不可调。
     * MediaConstants.PUBLISHER_PARAM_ORIGINAL_BITRATE：推流初始码率，单位Kbps。默认500。
     * MediaConstants.PUBLISHER_PARAM_MAX_BITRATE：推流最大码率，单位Kbps。默认1500。
     * MediaConstants.PUBLISHER_PARAM_MIN_BITRATE：推流最小码率，单位Kbps。默认200。
     * MediaConstants.PUBLISHER_PARAM_AUDIO_BITRATE：推流音频码率，单位Kbps。固定96，暂不可调。
     * MediaConstants.PUBLISHER_PARAM_VIDEO_FPS：推流视频码率。
     * MediaConstants.PUBLISHER_PARAM_SCREEN_ROTATION：推流横屏/竖屏。
     * MediaConstants.PUBLISHER_PARAM_FRONT_CAMERA_MIRROR：前置摄像头是否镜像。
     */
    private Map<String, String> mMediaParam = new HashMap<>();     //推流器参数
    /**
     * 变量的描述: 存储滤镜参数
     * 可选参数:
     * MediaConstants.FILTER_PARAM_BEAUTY_ON：美颜是否开启，默认为true
     * MediaConstants.FILTER_PARAM_BEAUTY_WHITEN：美白程度[0,100]，默认为0
     * MediaConstants.FILTER_PARAM_BEAUTY_BUFFING：磨皮程度[0,35]，默认为0
     */
    private Map<String, String> mFilterMap = new HashMap<>();
    /**
     * 变量的描述: 美颜是否开启
     */
    private boolean isBeautyOn = true;
    /**
     * 变量的描述: 闪光灯是否开启
     */
    private boolean isFlashOn = false;
    /**
     * 变量的描述: 相机是前置摄像头
     */
    private int mCameraFacing = AlivcMediaFormat.CAMERA_FACING_FRONT;
    /**
     * 变量的描述: 存储正在参与连麦的URL的集合
     */
    private List<String> mChattingUrls = new ArrayList<>();
    /**
     * 变量的描述: 初始状态值
     */
    private static final int STATUS_MASK = 0;
    /**
     * 变量的描述: 暂停中 左移1位：2
     */
    private static final int STATUS_PAUSED = 1 << 1;//
    /**
     * 变量的描述: 聊天中 左移2位：4
     */
    private static final int STATUS_CHATTING = 1 << 2;//
    /**
     * 变量的描述: 推流中 左移3位：8
     */
    private static final int STATUS_PUBLISHING = 1 << 3;//
    /**
     * 变量的描述: 预览中 左移4位：16
     */
    private static final int STATUS_PREVIEW = 1 << 4;//
    /**
     * 变量的描述: 已int数值的二进制形式，各个位是否为1来判断状态
     */
    private int mStatus = STATUS_MASK;

    /**
     * 方法描述: 初始化推流器
     *
     * @param errorListener 错误监听器回调接口实例
     * @param infoListener  状态信息回调接口实例
     */
    public void initRecorder(Context context, IVideoChatHost.OnErrorListener errorListener, IVideoChatHost.OnInfoListener infoListener) {
        //设置推流器推流相关参数
        mMediaParam.put(MediaConstants.PUBLISHER_PARAM_ORIGINAL_BITRATE, "" + 800000);      // 推流初始码率，单位Kbps。默认500。
        mMediaParam.put(MediaConstants.PUBLISHER_PARAM_MIN_BITRATE, "" + 600000);           // 推流最小码率，单位Kbps。默认200。
        mMediaParam.put(MediaConstants.PUBLISHER_PARAM_MAX_BITRATE, "" + 1000000);          // 推流最大码率，单位Kbps。默认1500。

        mChatHost = new AlivcVideoChatHost();

        Log.d(TAG, "Call mChatHost.init()");

        // 初始化AlivcVideoChatHost类
        mChatHost.init(context);

        // 设置主播及观众的渲染模式
        // VideoScalingMode mode，有以下几种模式：VIDEO_SCALING_MODE_SCALE_TO_FIT：适应屏幕显示   VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING：充满屏幕显示，保持比例，如果屏幕比例不对，则进行裁剪
        mChatHost.setScalingMode(VideoScalingMode.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);

        // 设置设置错误监听器回调
        mChatHost.setErrorListener(errorListener);

        // 设置状态信息回调
        mChatHost.setInfoListener(infoListener);

        // 添加(替换)美颜是否开启的参数，默认为true ，这里为开启
        mFilterMap.put(AlivcVideoChatHost.ALIVC_FILTER_PARAM_BEAUTY_ON, Boolean.toString(true));
        // 设置滤镜的相关参数，将滤镜参数放在map集合中
        mChatHost.setFilterParam(mFilterMap);
    }


    /**
     * 方法描述: 准备推流
     *
     * @param surf 推流或连麦过程中供主播预览的view。
     */
    public void startPreView(SurfaceView surf) {
        if ((mStatus & STATUS_PREVIEW) == STATUS_MASK) {
            Log.d(TAG, "Call mChatHost.prepareToPublish()");
            // 准备推流，建立预览界面
            // 参数二推流视频的宽。 参数三推流视频的高
            // 备注：目前视频编码采用的是软编码，软编码条件下只支持两种分辨率：360x640/180x320（横屏推流的时候为640x360,320x180）。
            // 本接口是同步接口。
            mChatHost.prepareToPublish(surf, 360, 640, mMediaParam);
            // 两者转换为二进制，按位或 赋值
            mStatus |= STATUS_PREVIEW;
            if (mCameraFacing == AlivcMediaFormat.CAMERA_FACING_FRONT) {
                // 设置滤镜的相关参数
                mChatHost.setFilterParam(mFilterMap);
            }
        }
    }


    /**
     * 方法描述: 开始推流，调用该函数将启动音视频的编码，并将压缩后的音视频流打包上传到服务器。
     *
     * @param publishUrl 主播推流地址。
     */
    public void startPublishStream(String publishUrl) {
        // 调用连麦SDK，开始推流
        if ((mStatus & STATUS_PUBLISHING) == STATUS_MASK) {
            Log.d(TAG, "Call mChatHost.startToPublish()");
            // 开启推流
            // 备注：此处的推流仅仅是主播单向的直播推流，与连麦这种双向互动没有关系。必须先调用函数prepareToPublish后才能调用该函数。
            // 本接口是同步接口。
            mChatHost.startToPublish(publishUrl);
            mStatus |= STATUS_PUBLISHING;
        }
    }

    /**
     * 方法描述: 切换摄像头,该函数可以在连麦的过程中随时进行调用。
     */
    public void switchCamera() {
        if (mChatHost != null) {
            Log.d(TAG, "Call mChatHost.switchCamera");
            mChatHost.switchCamera();
        }
    }

    /**
     * 方法描述: 闪光灯开启/关闭，该函数可以在连麦过程中随时进行调用。
     *
     * @return true闪光灯开启，false闪光灯关闭
     */
    public boolean switchFlash() {
        if (mChatHost != null) {
            Log.d(TAG, "Call mChatHost.switchFlash(" + isFlashOn + ")");
            if (!isFlashOn) {
                mChatHost.setFlashOn(true);// 打开
            } else {
                mChatHost.setFlashOn(false);// 关闭
            }
            isFlashOn = !isFlashOn;
        }
        return isFlashOn;
    }

    /**
     * 方法描述: 设置滤镜的相关参数 就是是否美颜 该函数可以在连麦过程中随时进行调用。
     *
     * @return true 美颜开启，false 美颜关闭
     */
    public boolean switchBeauty() {
        if (mChatHost != null) {
            if (!isBeautyOn) {
                // 开启美颜，替换value值
                mFilterMap.put(AlivcVideoChatHost.ALIVC_FILTER_PARAM_BEAUTY_ON, Boolean.toString(true));
            } else {
                // 关闭美颜，替换value值
                mFilterMap.put(AlivcVideoChatHost.ALIVC_FILTER_PARAM_BEAUTY_ON, Boolean.toString(false));
            }
            // 在这是重新设置滤镜的相关参数
            mChatHost.setFilterParam(mFilterMap);
            isBeautyOn = !isBeautyOn;
        }
        return isBeautyOn;
    }


    /**
     * 方法描述: 连麦重连
     *
     * @param url 指定重新连接的播放地址。
     */
    public void reconnect(String url) {
        Log.d(TAG, "Call mChatHost.reconnectChat(" + url + ")");
        // 当播放视频超时或者遇到网络切换断流时，调用此函数进行重新连接打开。
        // 此函数不会有黑屏的情况。
        mChatHost.reconnectChat(url);
    }


    /**
     * 方法描述: 摄像头缩放
     *
     * @param scaleFactor 大于0（大于0小于1，表示缩小，最小为原始大小；大于1表示放大）
     */
    public void zoom(float scaleFactor) {
        if (mChatHost != null) {
            Log.d(TAG, "Call mChatHost.zoom(" + scaleFactor + ")");
            // 摄像头放大倍率。调用该函数将对当前视频进行光学放大。放大后的视频将显示在预览窗口。
            // 该函数仅对后置摄像头有效。
            mChatHost.zoomCamera(scaleFactor);
        }
    }

    /**
     * 方法描述: 自动对焦
     *
     * @param x 所要聚焦的点所在屏幕x轴的位置/屏幕宽度
     * @param y 所要聚焦的点所在的屏幕y轴的位置/屏幕的长度
     */
    public void autoFocus(float x, float y) {
        if (mChatHost != null) {
            Log.d(TAG, "Call mChatHost.focusCameraAtAdjustedPoint(" + x + ", " + y + ")");
            // 聚焦到某个设置的点。调用该函数可以聚焦到预览窗口上人为指定的某个点。
            mChatHost.focusCameraAtAdjustedPoint(x, y);
        }
    }

    /**
     * 方法描述: 连麦（适用于多人）,方法内容有开启第一次连麦，也有在连麦的基础上在添加连麦
     *
     * @param urlSurfaceMap 多个副麦url播放地址所对应的窗口集合。
     */
    public void launchChats(Map<String, SurfaceView> urlSurfaceMap) {
        if ((mStatus & STATUS_CHATTING) == STATUS_MASK && mChattingUrls.size() == 0) {
            // 此时只是纯推流，还没有一个连麦

            Log.d(TAG, "Call mChatHost.launchChats()");
            // 开始连麦。在此之前，主播处于纯推流状态。主播可以对一个人发起连麦，也可以对多个人发起连麦。
            // 备注：必须调用函数startToPublish后才能调用该函数。
            // 该方法有返回值：0表示成功，非0表示失败。
            mChatHost.launchChats(urlSurfaceMap);
            mChattingUrls.addAll(urlSurfaceMap.keySet());
            mStatus |= STATUS_CHATTING;
        } else if (mChattingUrls.size() > 0) {
            // 此时至少有一个连麦

            Log.d(TAG, "Call mChatHost.addChats()");
            // 增加连麦人数。在此之前，主播可以有一个或多个副麦。新增的副麦人数可以是一个，也可以是多个。
            // 该方法有返回值：0表示成功，非0表示失败。
            // 备注：必须调用函数launchChats后才能调用该函数。
            mChatHost.addChats(urlSurfaceMap);
            mChattingUrls.addAll(urlSurfaceMap.keySet());
        }
    }

    /**
     * 方法描述: 暂停播放或连麦。在播放或连麦过程中，观众如果退入后台、锁屏或有电话接入，可以调用本接口。
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
     * 方法描述: 恢复推流或连麦。在观看或连麦过程中，观众如果发生退入后台、锁屏或有电话接入的情况，希望能够回到前台继续播放或推流，可以调用本接口。
     * 备注：必须先调用pause，然后才能调用resume。
     */
    public void resume() {
        if (mChatHost != null && (mStatus & STATUS_PAUSED) == STATUS_PAUSED) {
            Log.d(TAG, "Call mChatHost.resume()");
            mChatHost.resume();
            // 两个值转换为二进制，进行异或位运算，相同取0，不同取1
            mStatus ^= STATUS_PAUSED;
        }
    }

    /**
     * 方法描述:结束连麦（可以是选择多个连麦中的一个结束，也可以是结束所有连麦）
     *
     * @param urls 所有退出连麦的副麦的播放地址，NSURL数组。退出连麦的可以是选择多个中的一个url，也可以是所有连麦url。
     *             参数集合传null或者空数据代表清空所有连麦，集合有数据则是指定连麦结束
     * @return 值为-1代表结束连麦异常
     */
    public int abortChat(List<String> urls) {
        Log.d(TAG, "abort chat status " + mStatus);
        if (mChatHost != null && (mStatus & STATUS_CHATTING) == STATUS_CHATTING) {
            if (urls == null || urls.size() == 0) {
                Log.d(TAG, "Call mChatHost.abortChat()");
                // 结束所有正在运行的连麦。调用该函数将关闭播放器，销毁用于播放的窗口。
                // 本接口是同步接口。
                mChatHost.abortChat();
                mChattingUrls.clear();
                mStatus ^= STATUS_CHATTING;
            } else {
                Log.d(TAG, "Call mChatHost.removeChats()");
                // 减少连麦人数。在此之前，主播可以有一个或多个副麦。减少的副麦人数可以是一个，也可以是多个。
                // removeChats的参数如果是所有连麦，那么调用removeChats与abortChat功能相同。
                // 备注：本接口是同步接口。本接口是同步接口。
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
            // 结束推流。调用该函数将结束本次的直播推流，并关闭音视频编码功能，但采集、滤镜功能仍然运行，预览功能仍然保留。
            // 备注：若在连麦状态下调用该函数，则sdk会先停止连麦，再结束推流。
            // 本接口为同步接口。
            // 对应startToPublish方法
            mChatHost.stopPublishing();
            Log.d(TAG, "Call mChatHost.finishPublishing()");
            // 退出推流直播。调用该函数将停止采集、滤镜功能，销毁预览窗口，释放所有资源。
            // 备注：若调用了函数startToPublish，则必须调用函数stopPublishing以后才可以调用该函数。
            // 本接口为同步接口。
            // 对应prepareToPublish方法
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
            // 释放AlivcVideoChatHost类。
            mChatHost.release();
            mChatHost = null;
        }
    }


    // --------------------------------------------------------------------------------------------------------

    /**
     * 方法描述: 获得与推流相关的性能参数对象
     */
    public AlivcPublisherPerformanceInfo getPublisherPerformanceInfo() {
//        mAudioEncodeBitrate： 音频编码速度，单位Kbps
//        mVideoEncodeBitrate： 视频编码速度，单位Kbps
//        mAudioUploadBitrate： 音频上传速度，单位kbps
//        mVideoUploadBitrate： 视频上传速度，单位kbps
//        mAudioPacketsInBuffer： 缓冲的音频帧数
//        mVideoPacketsInBuffer： 缓冲的视频帧数
//        mVideoEncodedFps： 视频编码帧率
//        mVideoUploadedFps： 视频上传帧率
//        mVideoCaptureFps： 视频采集帧率
//        mCurrentlyUploadedVideoFramePts： 当前上传的视频帧的pts，单位ms
//        mCurrentlyUploadedAudioFramePts： 当前上传的音频帧的pts，单位ms
//        mPreviousKeyFramePts： 上一个关键帧的pts，单位ms
//        mTotalFramesOfEncodedVideo： 视频编码总帧数
//        mTotalTimeOfEncodedVideo： 视频编码总耗时，单位ms
//        mTotalSizeOfUploadedPackets： 上传的音视频流总量，单位Kbyte
//        mTotalTimeOfPublishing： 当前推流的总时间，单位ms
//        mTotalFramesOfVideoUploaded： 上传的视频帧总数
//        mDropDurationOfVideoFrames： 视频丢帧的累计时长，单位ms
//        mVideoDurationFromeCaptureToUpload： 当前音频帧从采集到上传的耗时，单位ms
//        mAudioDurationFromeCaptureToUpload： 当前视频帧从采集到上传的耗时，单位ms
        if (mChatHost != null)
            return mChatHost.getPublisherPerformanceInfo();
        else
            return null;
    }

    /**
     * 方法描述: 获得与播放相关的性能参数
     *
     * @param url 指定播放url的性能参数
     */
    public AlivcPlayerPerformanceInfo getPlayerPerformanceInfo(String url) {
//        mVideoPacketsInBuffer： 缓冲的视频帧数
//        mAudioPacketsInBuffer： 缓冲的音频帧数
//        mVideoDurationFromDownloadToRender： 视频从下载到播放的耗时，单位ms
//        mAudioDurationFromDownloadToRender： 音频从下载到播放的耗时，单位ms
//        mVideoPtsOfLastPacketInBuffer： 缓冲区中最后一帧视频的pts
//        mAudioPtsOfLastPacketInBuffer： 缓冲区中最后一帧音频的pts
//        mLiveDiscardDuration： 丢帧的总长度
//        mDowloadSpeed： packets的下载速度
        if (mChatHost != null)
            return mChatHost.getPlayerPerformanceInfo(url);
        else
            return null;
    }
}
