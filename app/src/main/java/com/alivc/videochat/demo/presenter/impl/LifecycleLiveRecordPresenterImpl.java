package com.alivc.videochat.demo.presenter.impl;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;

import com.alivc.videochat.demo.logic.IPublisherManager;
import com.alivc.videochat.publisher.AlivcPublisherPerformanceInfo;
import com.alivc.videochat.demo.R;
import com.alivc.videochat.demo.base.AsyncCallback;
import com.alivc.videochat.demo.base.ContextBase;
import com.alivc.videochat.demo.im.ImManager;
import com.alivc.videochat.demo.logic.LifecyclePublisherManager;
import com.alivc.videochat.demo.logic.ManagerCallback;
import com.alivc.videochat.demo.presenter.ILifecycleLiveRecordPresenter;
import com.alivc.videochat.demo.presenter.view.ILiveRecordView;
import com.alivc.videochat.demo.ui.LogInfoFragment;
import com.alivc.videochat.demo.ui.adapter.LogInfoAdapter;
import com.alivc.videochat.demo.uitils.ToastUtils;

import java.util.List;

/**
 * 类的描述: 将生命周期和连麦操作交给LifecyclePublisherMgr对象执行，本类主要是对连麦直播操作的结果进行相对应的UI更新
 */
public class LifecycleLiveRecordPresenterImpl extends ContextBase implements ILifecycleLiveRecordPresenter {

    public static final String TAG = LifecycleLiveRecordPresenterImpl.class.getName();

    private LifecyclePublisherManager mPublisherMgr;

    /**
     * 变量的描述: 根据直播连麦操作的回调接口结果来按需求调用方法更新UI
     */
    private ILiveRecordView mView;

    public LifecycleLiveRecordPresenterImpl(Context context, ILiveRecordView view, String uid, ImManager imManager) {
        super(context);
        this.mPublisherMgr = new LifecyclePublisherManager(context, mPublisherCallback, uid, imManager);
        this.mView = view;
    }

    // --------------------------------------------------------------------------------------------------------

    @Override
    // 1
    public void onCreate() {
        mPublisherMgr.onCreate();
    }

    @Override
    // 2
    public void onStart() {
        mPublisherMgr.onStart();
    }

    @Override
    // 3
    public void onResume() {
        mPublisherMgr.onResume();
    }

    @Override
    public void onPause() {
        mPublisherMgr.onPause();
    }

    @Override
    public void onStop() {
        mPublisherMgr.onStop();
    }

    @Override
    public void onDestroy() {
        mPublisherMgr.onDestroy();
    }

    // --------------------------------------------------------------------------------------------------------

    @Override
    // 4  PublisherSDKHelper的startPreView方法是在主SurfaceView的状态监听器（SurfaceHolder.Callback）里进行调用了
    public void startPreview(SurfaceView previewSurf) {
        mPublisherMgr.asyncStartPreview(previewSurf, new AsyncCallback() {
            @Override
            public void onSuccess(Bundle bundle) {
            }

            @Override
            public void onFailure(Bundle bundle, Throwable e) {
            }
        });
    }

    @Override
    public void inviteChat(List<String> mPlayerUIDs) {
        mPublisherMgr.asyncInviteChatting(mPlayerUIDs, new AsyncCallback() {
            @Override
            public void onSuccess(Bundle bundle) {
                mView.showInviteVideoChatSuccessfulUI();
            }

            @Override
            public void onFailure(Bundle bundle, Throwable e) {
                mView.showInviteVideoChatFailedUI(e);
            }
        });
    }

    @Override
    public void switchCamera() {
        mPublisherMgr.switchCamera();
    }

    @Override
    public boolean switchBeauty() {
        return mPublisherMgr.switchBeauty();
    }

    @Override
    public boolean switchFlash() {
        return mPublisherMgr.switchFlash();
    }

    @Override
    public void zoom(float scaleFactor) {
        mPublisherMgr.zoom(scaleFactor);
    }

    @Override
    public void autoFocus(float xRatio, float yRatio) {
        mPublisherMgr.autoFocus(xRatio, yRatio);
    }

    @Override
    public void terminateChatting(final String playerUID) {
        mPublisherMgr.asyncTerminateChatting(playerUID, new AsyncCallback() {
            @Override
            public void onSuccess(Bundle bundle) {
                //展示中断连麦的UI
                mView.showTerminateChattingUI(playerUID);
            }

            @Override
            public void onFailure(Bundle bundle, Throwable e) {
                mView.showTerminateChattingUI(playerUID);
            }
        });
    }

    /**
     * 方法描述: 暂时没人用
     */
    @Override
    public void terminateLive() {
        mPublisherMgr.asyncCloseLive(new AsyncCallback() {
            @Override
            public void onSuccess(Bundle bundle) {

            }

            @Override
            public void onFailure(Bundle bundle, Throwable e) {

            }
        });
    }

    /**
     * 方法描述: 暂时没人用
     */
    @Override
    public void terminateAllChatting() {
        mPublisherMgr.asyncTerminateAllChatting(new AsyncCallback() {
            @Override
            public void onSuccess(Bundle bundle) {
                mView.showTerminateChattingUI(null);
            }

            @Override
            public void onFailure(Bundle bundle, Throwable e) {

            }
        });
    }

    // --------------------------------------------------------------------------------------------------------

    /**
     * 变量的描述: 根据回调接口去更新UI,虽然有的直播连麦的操作有回调接口，但有的没有，这就需要本变量了
     */
    private ManagerCallback mPublisherCallback = new ManagerCallback() {
        @Override
        public void onEvent(int eventType, Bundle data) {
            switch (eventType) {

                // 当连麦观众推流成功后，正式开启连麦
                case IPublisherManager.TYPE_PUBLISH_STREMA_SUCCESS:// 某个连麦观众推流成功，服务端获取了推流对应的播放地址，并通过MNS发送给了主播
                    // 主播收到了连麦观众推流的播放地址，记录播放地址后，将其uid发送过来
                    // 在UI界面上获取一个空闲的SurfaceView，并将其与uid绑定在一起
                    String inviteeUID = data.getString(IPublisherManager.DATA_KEY_INVITEE_UID);
                    SurfaceView parterView = mView.showChattingUI(inviteeUID);      //显示连麦的UI
                    if (parterView != null) {
                        // 调用正式连麦的方法
                        mPublisherMgr.launchChat(parterView, inviteeUID);
                    }
                    break;

                // 由于某个连麦退出了，我们需要调用showTerminateChattingUI方法更新退出连麦的UI
                case IPublisherManager.TYPE_SOMEONE_EXIT_CHATTING:
                    String playerUID = data.getString(IPublisherManager.DATA_KEY_PLAYER_UID);
                    mView.showTerminateChattingUI(playerUID);
                    break;

                // 弹出对话框显示错误的操作内容
                case IPublisherManager.TYPE_OPERATION_CALLED_ERROR:
                    String msg = null;
                    if (data != null) {
                        msg = data.getString(IPublisherManager.DATA_KEY_CHATTING_ERROR_MSG, null);
                    }
                    mView.showInfoDialog(msg);
                    break;
                // --------------------------------------------------------------------------------------------------------
                // 弹出对话框显示错误信息，点击对话框的“确定”按钮可以退出观看界面
                case IPublisherManager.TYPE_PUBLISHER_NETWORK_UNCONNECT:
                    mView.showInterruptUI(R.string.error_publisher_network_unconnect, -400);
                    break;
                case IPublisherManager.TYPE_PUBLISHER_NETWORK_TIMEOUT:
                    mView.showInterruptUI(R.string.error_publisher_network_timeout, -406);
                    break;
                case IPublisherManager.TYPE_PLAYER_AUDIO_PLAYER_ERROR:
                    mView.showInterruptUI(R.string.error_publisher_network_unconnect, 412);
                    break;
                case IPublisherManager.TYPE_PLAYER_INTERNAL_ERROR://超时等状态需要提示连麦结束
                    mView.showInterruptUI(R.string.video_chatting_finished, data.getInt(IPublisherManager.DATA_KEY_PUBLISHER_ERROR_CODE));
                    break;
                case IPublisherManager.TYPE_PUBLISHER_AUDIO_CAPTURE_FAILURE:
                    mView.showInterruptUI(R.string.no_audio, data.getInt(IPublisherManager.DATA_KEY_PUBLISHER_ERROR_CODE));
                    break;
                case IPublisherManager.TYPE_PUBLISHER_INTERNAL_ERROR:
                    mView.showInterruptUI(R.string.publish_error, data.getInt(IPublisherManager.DATA_KEY_PUBLISHER_ERROR_CODE));
                    break;
                case IPublisherManager.TYPE_PUBLISHER_VIDEO_CAPTURE_FAILURE:
                    mView.showInterruptUI(R.string.error_video_capture, data.getInt(IPublisherManager.DATA_KEY_PUBLISHER_INFO_CODE));
                    break;
                // --------------------------------------------------------------------------------------------------------
                // 弹吐司显示错误或者状态信息
                case IPublisherManager.TYPE_PUBLISHER_RECONNECT_FAILURE:
                    mView.showToast(R.string.network_reconnect_failure);
                    break;
                case IPublisherManager.TYPE_MIX_STREAM_ERROR:
                    mView.showToast(R.string.mix_internal_error);
                    break;
                case IPublisherManager.TYPE_MIX_STREAM_NOT_EXIST:
                    mView.showToast(R.string.error_mix_stream_not_exist);
                    break;
                case IPublisherManager.TYPE_MIX_STREAM_SUCCESS:
                    mView.showToast(R.string.error_mix_stream_success);
                    break;
                case IPublisherManager.TYPE_MIX_STREAM_TIMEOUT:
                    mView.showToast(R.string.error_mix_stream_timeout);
                    break;
                case IPublisherManager.TYPE_MAIN_STREAM_NOT_EXIST:
                    mView.showToast(R.string.error_main_stream_not_exist);
                    break;
                case IPublisherManager.TYPE_PLAYER_INVALID_INPUTFILE:
                    mView.showToast(R.string.error_player_invalid_inputfile);
                    break;
                case IPublisherManager.TYPE_PLAYER_OPEN_FAILED:
                    mView.showToast(R.string.error_player_open_failed);
                    break;
                case IPublisherManager.TYPE_PLAYER_NO_NETWORK:
                    mView.showToast(R.string.error_player_no_network);
                    break;
                case IPublisherManager.TYPE_PLAYER_TIMEOUT:
                    mView.showToast(R.string.error_player_timeout);
                    break;
                case IPublisherManager.TYPE_PLAYER_READ_PACKET_TIMEOUT:
                    mView.showToast(R.string.error_player_read_packet_timeout);
                    break;
                case IPublisherManager.TYPE_INVITE_TIMEOUT:     // 邀请对方进行连麦，对方响应超时
                    ToastUtils.showToast(getContext(), R.string.invite_timeout_tip);   //提醒：对方长时间未响应，已取消连麦流程
                    break;
                case IPublisherManager.TYPE_PUBLISHER_NETWORK_GOOD:
                    ToastUtils.showToast(getContext(), R.string.good_network);
                    // mView.hideInterruptUI();
                    break;
                case IPublisherManager.TYPE_PLAYER_NETWORK_POOR:
                    if (data != null) {
                        String url = data.getString(IPublisherManager.DATA_KEY_PLAYER_URL);
                        ToastUtils.showToast(getContext(), "播放视频 " + url + " 网络差，可能造成延时");
                    }
                    // mView.hideInterruptUI();
                    break;
                case IPublisherManager.TYPE_PUBLISHER_NETWORK_POOR:
                    ToastUtils.showToast(getContext(), R.string.poor_network);
                    // mView.showInterruptUI(R.string.no_network, data.getInt(IPublisherManager.DATA_KEY_PUBLISHER_ERROR_CODE));
                    break;
                case IPublisherManager.TYPE_PROCESS_INVITING_TIMEOUT:   // 处理对方连麦邀请超时
                    ToastUtils.showToast(getContext(), R.string.inviting_process_timeout); //提醒超时未处理，已经自动拒绝对方的连麦邀
                    break;
                // --------------------------------------------------------------------------------------------------------
                // 下面的没有实际意义
                case IPublisherManager.TYPE_LIVE_CREATED:
                    break;
            }
        }
    };

    // --------------------------------------------------------------------------------------------------------

    @Override
    public LifecyclePublisherManager getPublisherMgr() {
        return mPublisherMgr;
    }

    @Override
    public void refreshLogInfo(LogInfoFragment.LogHandler logHandler) {
        if (mPublisherMgr != null) {
            AlivcPublisherPerformanceInfo publishInfo = mPublisherMgr.getPublisherPerformanceInfo();
//            AlivcPlayerPerformanceInfo playInfo = mPublisherMgr.getPlayerPerformanceInfo();
            String BITRATE_UNIT = "Kbps";
            String FRAME_RATE_UNIT = "fps";
            String PTS_UNIT = "ms";
            String DURATION_UNIT = "ms";
            String DELAY_UNIT = "ms";
            logHandler.updateValue(LogInfoAdapter.LogItem.AUDIO_ENCODE_BITRATE, publishInfo.getAudioEncodeBitrate() + BITRATE_UNIT);
            logHandler.updateValue(LogInfoAdapter.LogItem.VIDEO_ENCODE_BITRATE, publishInfo.getVideoEncodeBitrate() + BITRATE_UNIT);
            logHandler.updateValue(LogInfoAdapter.LogItem.AUDIO_UPLOAD_BITRATE, publishInfo.getAudioUploadBitrate() + BITRATE_UNIT);
            logHandler.updateValue(LogInfoAdapter.LogItem.VIDEO_UPLOAD_BITRATE, publishInfo.getVideoUploadBitrate() + BITRATE_UNIT);
            logHandler.updateValue(LogInfoAdapter.LogItem.AUDIO_FRAMES_IN_QUEUE, String.valueOf(publishInfo.getAudioPacketsInBuffer()));
            logHandler.updateValue(LogInfoAdapter.LogItem.VIDEO_FRAMES_IN_QUEUE, String.valueOf(publishInfo.getVideoPacketsInBuffer()));
            logHandler.updateValue(LogInfoAdapter.LogItem.VIDEO_ENCODE_FRAME_RATE, publishInfo.getVideoEncodeBitrate() + BITRATE_UNIT);
            logHandler.updateValue(LogInfoAdapter.LogItem.VIDEO_UPLOAD_FRAME_RATE, publishInfo.getVideoUploadedFps() + FRAME_RATE_UNIT);
            logHandler.updateValue(LogInfoAdapter.LogItem.VIDEO_CAPTURE_FRAME_RATE, publishInfo.getVideoCaptureFps() + FRAME_RATE_UNIT);
            logHandler.updateValue(LogInfoAdapter.LogItem.CURRENT_VIDEO_PTS, publishInfo.getCurrentlyUploadedVideoFramePts() + PTS_UNIT);
            logHandler.updateValue(LogInfoAdapter.LogItem.CURRENT_AUDIO_PTS, publishInfo.getCurrentlyUploadedAudioFramePts() + PTS_UNIT);
            logHandler.updateValue(LogInfoAdapter.LogItem.PREVIOUS_I_FRAME_PTS, publishInfo.getPreviousKeyFramePts() + PTS_UNIT);
            logHandler.updateValue(LogInfoAdapter.LogItem.VIDEO_ENCODE_FRAMES, String.valueOf(publishInfo.getTotalFramesOfEncodedVideo()));
            logHandler.updateValue(LogInfoAdapter.LogItem.VIDEO_ENCODE_DURATIONS, publishInfo.getTotalTimeOfEncodedVideo() + DURATION_UNIT);
            logHandler.updateValue(LogInfoAdapter.LogItem.UPLOAD_PACKETS_SIZE, String.valueOf(publishInfo.getTotalSizeOfUploadedPackets()));
            logHandler.updateValue(LogInfoAdapter.LogItem.UPLOAD_PACKETS_DURATIONS, publishInfo.getTotalTimeOfPublishing() + DURATION_UNIT);
            logHandler.updateValue(LogInfoAdapter.LogItem.UPLOAD_VIDEO_FRAMES, String.valueOf(publishInfo.getTotalFramesOfVideoUploaded()));
            logHandler.updateValue(LogInfoAdapter.LogItem.DROPPED_VIDEO_DURATIONS, publishInfo.getDropDurationOfVideoFrames() + DURATION_UNIT);
            logHandler.updateValue(LogInfoAdapter.LogItem.VIDEO_CAPTURE_TO_UPLOAD_DELAY, publishInfo.getVideoDurationFromeCaptureToUpload() + DELAY_UNIT);
            logHandler.updateValue(LogInfoAdapter.LogItem.AUDIO_CAPTURE_TO_UPLOAD_DELAY, publishInfo.getAudioDurationFromeCaptureToUpload() + DURATION_UNIT);
            logHandler.notifyUpdate();
        }
    }
}
