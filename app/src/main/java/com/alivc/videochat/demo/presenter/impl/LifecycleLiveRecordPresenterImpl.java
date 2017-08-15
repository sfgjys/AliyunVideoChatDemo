package com.alivc.videochat.demo.presenter.impl;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;

import com.alivc.videochat.publisher.AlivcPublisherPerformanceInfo;
import com.alivc.videochat.demo.R;
import com.alivc.videochat.demo.base.AsyncCallback;
import com.alivc.videochat.demo.base.ContextBase;
import com.alivc.videochat.demo.im.ImManager;
import com.alivc.videochat.demo.logic.IPublisherMgr;
import com.alivc.videochat.demo.logic.LifecyclePublisherMgr;
import com.alivc.videochat.demo.logic.MgrCallback;
import com.alivc.videochat.demo.presenter.ILifecycleLiveRecordPresenter;
import com.alivc.videochat.demo.presenter.view.ILiveRecordView;
import com.alivc.videochat.demo.ui.LogInfoFragment;
import com.alivc.videochat.demo.ui.adapter.LogInfoAdapter;
import com.alivc.videochat.demo.uitils.ToastUtils;

import java.util.List;

/**
 * Created by apple on 2017/1/7.
 */

public class LifecycleLiveRecordPresenterImpl extends ContextBase implements ILifecycleLiveRecordPresenter {
    public static final String TAG = LifecycleLiveRecordPresenterImpl.class.getName();

    private LifecyclePublisherMgr mPublisherMgr;
    private ILiveRecordView mView;

    public LifecycleLiveRecordPresenterImpl(Context context, ILiveRecordView view, String uid, ImManager imManager) {
        super(context);
        this.mPublisherMgr = new LifecyclePublisherMgr(context, mPublisherCallback, uid, imManager);
        this.mView = view;
    }

    @Override
    public void onCreate() {
        mPublisherMgr.onCreate();
    }

    @Override
    public void onStart() {
        mPublisherMgr.onStart();
    }

    @Override
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

    @Override
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

    @Override
    public LifecyclePublisherMgr getPublisherMgr() {
        return mPublisherMgr;
    }

    public void refreshLogInfo(LogInfoFragment.LogHandler logHandler) {
        if (mPublisherMgr != null) {
            AlivcPublisherPerformanceInfo publishInfo = mPublisherMgr.getPublisherPerformanceInfo();
//            AlivcPlayerPerformanceInfo playInfo = mPublisherMgr.getPlayerPerformanceInfo();
            String BITRATE_UNIT = "Kbps";
            String FRAME_RATE_UNIT = "fps";
            String PTS_UNIT = "ms";
            String DURATION_UNIT = "ms";
            String DELAY_UNIT = "ms";
            logHandler.updateValue(LogInfoAdapter.LogItem.AUDIO_ENCODE_BITRATE,
                    publishInfo.getAudioEncodeBitrate() + BITRATE_UNIT);
            logHandler.updateValue(LogInfoAdapter.LogItem.VIDEO_ENCODE_BITRATE,
                    publishInfo.getVideoEncodeBitrate() + BITRATE_UNIT);
            logHandler.updateValue(LogInfoAdapter.LogItem.AUDIO_UPLOAD_BITRATE,
                    publishInfo.getAudioUploadBitrate() + BITRATE_UNIT);
            logHandler.updateValue(LogInfoAdapter.LogItem.VIDEO_UPLOAD_BITRATE,
                    publishInfo.getVideoUploadBitrate() + BITRATE_UNIT);
            logHandler.updateValue(LogInfoAdapter.LogItem.AUDIO_FRAMES_IN_QUEUE,
                    String.valueOf(publishInfo.getAudioPacketsInBuffer()));
            logHandler.updateValue(LogInfoAdapter.LogItem.VIDEO_FRAMES_IN_QUEUE,
                    String.valueOf(publishInfo.getVideoPacketsInBuffer()));
            logHandler.updateValue(LogInfoAdapter.LogItem.VIDEO_ENCODE_FRAME_RATE,
                    publishInfo.getVideoEncodeBitrate() + BITRATE_UNIT);
            logHandler.updateValue(LogInfoAdapter.LogItem.VIDEO_UPLOAD_FRAME_RATE,
                    publishInfo.getVideoUploadedFps() + FRAME_RATE_UNIT);
            logHandler.updateValue(LogInfoAdapter.LogItem.VIDEO_CAPTURE_FRAME_RATE,
                    publishInfo.getVideoCaptureFps() + FRAME_RATE_UNIT);
            logHandler.updateValue(LogInfoAdapter.LogItem.CURRENT_VIDEO_PTS,
                    publishInfo.getCurrentlyUploadedVideoFramePts() + PTS_UNIT);
            logHandler.updateValue(LogInfoAdapter.LogItem.CURRENT_AUDIO_PTS,
                    publishInfo.getCurrentlyUploadedAudioFramePts() + PTS_UNIT);
            logHandler.updateValue(LogInfoAdapter.LogItem.PREVIOUS_I_FRAME_PTS,
                    publishInfo.getPreviousKeyFramePts() + PTS_UNIT);
            logHandler.updateValue(LogInfoAdapter.LogItem.VIDEO_ENCODE_FRAMES,
                    String.valueOf(publishInfo.getTotalFramesOfEncodedVideo()));
            logHandler.updateValue(LogInfoAdapter.LogItem.VIDEO_ENCODE_DURATIONS,
                    publishInfo.getTotalTimeOfEncodedVideo() + DURATION_UNIT);
            logHandler.updateValue(LogInfoAdapter.LogItem.UPLOAD_PACKETS_SIZE,
                    String.valueOf(publishInfo.getTotalSizeOfUploadedPackets()));
            logHandler.updateValue(LogInfoAdapter.LogItem.UPLOAD_PACKETS_DURATIONS,
                    publishInfo.getTotalTimeOfPublishing() + DURATION_UNIT);
            logHandler.updateValue(LogInfoAdapter.LogItem.UPLOAD_VIDEO_FRAMES,
                    String.valueOf(publishInfo.getTotalFramesOfVideoUploaded()));
            logHandler.updateValue(LogInfoAdapter.LogItem.DROPPED_VIDEO_DURATIONS,
                    publishInfo.getDropDurationOfVideoFrames() + DURATION_UNIT);
            logHandler.updateValue(LogInfoAdapter.LogItem.VIDEO_CAPTURE_TO_UPLOAD_DELAY,
                    publishInfo.getVideoDurationFromeCaptureToUpload() + DELAY_UNIT);
            logHandler.updateValue(LogInfoAdapter.LogItem.AUDIO_CAPTURE_TO_UPLOAD_DELAY,
                    publishInfo.getAudioDurationFromeCaptureToUpload() + DURATION_UNIT);
            logHandler.notifyUpdate();
        }
    }

    private MgrCallback mPublisherCallback = new MgrCallback() {
        @Override
        public void onEvent(int eventType, Bundle data) {
            switch (eventType) {
                case IPublisherMgr.TYPE_PLAYER_INTERNAL_ERROR:
                    //超时等状态需要提示连麦结束
//                    ToastUtils.showToast(getContext(), R.string.video_chatting_finished);
                    mView.showInterruptUI(R.string.video_chatting_finished, data.getInt(IPublisherMgr.DATA_KEY_PUBLISHER_ERROR_CODE));
                    break;
                case IPublisherMgr.TYPE_PUBLISHER_AUDIO_CAPTURE_FAILURE:
                    mView.showInterruptUI(R.string.no_audio, data.getInt(IPublisherMgr.DATA_KEY_PUBLISHER_ERROR_CODE));
                    break;
                case IPublisherMgr.TYPE_PUBLISHER_INTERNAL_ERROR:
                    mView.showInterruptUI(R.string.publish_error, data.getInt(IPublisherMgr.DATA_KEY_PUBLISHER_ERROR_CODE));
                    Log.d(TAG, "Publisher internal error!");
                    break;
                case IPublisherMgr.TYPE_PUBLISHER_NETWORK_GOOD:
                    ToastUtils.showToast(getContext(), R.string.good_network);
//                    mView.hideInterruptUI();
                    break;
                case IPublisherMgr.TYPE_PLAYER_NETWORK_POOR:
                    if (data != null) {
                        String url = data.getString(IPublisherMgr.DATA_KEY_PLAYER_ERROR_MSG);
                        ToastUtils.showToast(getContext(), "播放视频 " + url + " 网络差，可能造成延时");
                    }
//                    mView.hideInterruptUI();
                    break;
                case IPublisherMgr.TYPE_PUBLISHER_NETWORK_POOR:
                    ToastUtils.showToast(getContext(), R.string.poor_network);
//                    mView.showInterruptUI(R.string.no_network, data.getInt(IPublisherMgr.DATA_KEY_PUBLISHER_ERROR_CODE));
                    break;
                case IPublisherMgr.TYPE_PUBLISHER_RECONNECT_FAILURE:
                    mView.showToast(R.string.network_reconnect_failure);
                    break;
                case IPublisherMgr.TYPE_PUBLISHER_VIDEO_CAPTURE_FAILURE:
                    mView.showInterruptUI(R.string.error_video_capture, data.getInt(IPublisherMgr.DATA_KEY_PUBLISHER_INFO_CODE));
//                    mView.showCameraOpenFailureUI();
                    break;
                case IPublisherMgr.TYPE_PROCESS_INVITING_TIMEOUT:   //处理对方连麦邀请超时
                    ToastUtils.showToast(getContext(), R.string.inviting_process_timeout); //提醒超时未处理，已经自动拒绝对方的连麦邀
                    break;
                case IPublisherMgr.TYPE_PUBLISH_STREMA_SUCCESS:
                case IPublisherMgr.TYPE_START_CHATTING:
                    String inviteeUID = data.getString(IPublisherMgr.DATA_KEY_INVITEE_UID);
                    SurfaceView parterView = mView.showChattingUI(inviteeUID);      //显示连麦的UI
                    if (parterView != null) {
                        mPublisherMgr.launchChat(parterView, inviteeUID);
                    }
                    break;
                case IPublisherMgr.TYPE_SOMEONE_EXIT_CHATTING:
                    String playerUID = data.getString(IPublisherMgr.DATA_KEY_PLAYER_UID);
                    mView.showTerminateChattingUI(playerUID);
                    break;
                case IPublisherMgr.TYPE_OPERATION_CALLED_ERROR:
                    String msg = null;
                    if (data != null) {
                        msg = data.getString(IPublisherMgr.DATA_KEY_PLAYER_ERROR_MSG, null);
                    }
                    mView.showInfoDialog(msg);
                    break;
                case IPublisherMgr.TYPE_MIX_STREAM_ERROR:
                    mView.showToast(R.string.mix_internal_error);
                    break;
                case IPublisherMgr.TYPE_INVITE_TIMEOUT:     //邀请连麦，对方响应超时
                    ToastUtils.showToast(getContext(), R.string.invite_timeout_tip);   //提醒：对方长时间未响应，已取消连麦邀请
//                    mView.showInviteChattingTimeoutUI(data.getString(IPublisherMgr.DATA_KEY_INVITEE_UID));
                    break;
                case IPublisherMgr.TYPE_MIX_STREAM_NOT_EXIST:
                    mView.showToast(R.string.error_mix_stream_not_exist);
                    break;
                case IPublisherMgr.TYPE_MIX_STREAM_SUCCESS:
                    mView.showToast(R.string.error_mix_stream_success);
                    break;
                case IPublisherMgr.TYPE_MIX_STREAM_TIMEOUT:
                    mView.showToast(R.string.error_mix_stream_timeout);
                    break;
                case IPublisherMgr.TYPE_MAIN_STREAM_NOT_EXIST:
                    mView.showToast(R.string.error_main_stream_not_exist);
                    break;
                case IPublisherMgr.TYPE_PLAYER_INVALID_INPUTFILE:
                    mView.showToast(R.string.error_player_invalid_inputfile);
                    break;
                case IPublisherMgr.TYPE_PLAYER_OPEN_FAILED:
                    mView.showToast(R.string.error_player_open_failed);
                    break;
                case IPublisherMgr.TYPE_PLAYER_NO_NETWORK:
                    mView.showToast(R.string.error_player_no_network);
                    break;
                case IPublisherMgr.TYPE_PLAYER_TIMEOUT:
                    mView.showToast(R.string.error_player_timeout);
                    break;
                case IPublisherMgr.TYPE_PLAYER_READ_PACKET_TIMEOUT:
                    mView.showToast(R.string.error_player_read_packet_timeout);
                    break;
                case IPublisherMgr.TYPE_PUBLISHER_NETWORK_UNCONNECT:
                    mView.showInterruptUI(R.string.error_publisher_network_unconnect, -400);
                    break;
                case IPublisherMgr.TYPE_PUBLISHER_NETWORK_TIMEOUT:
                    mView.showInterruptUI(R.string.error_publisher_network_timeout, -406);
                    break;
                case IPublisherMgr.TYPE_PLAYER_AUDIO_PLAYER_ERROR:
                    mView.showInterruptUI(R.string.error_publisher_network_unconnect, 412);
            }
        }
    };
}
