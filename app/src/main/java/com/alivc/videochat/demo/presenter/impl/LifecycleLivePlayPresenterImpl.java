package com.alivc.videochat.demo.presenter.impl;

import android.content.Context;
import android.os.Bundle;

import com.alivc.videochat.publisher.AlivcPublisherPerformanceInfo;
import com.alivc.videochat.demo.R;
import com.alivc.videochat.demo.base.AsyncCallback;
import com.alivc.videochat.demo.base.ContextBase;
import com.alivc.videochat.demo.exception.ChatSessionException;
import com.alivc.videochat.demo.im.ImManager;
import com.alivc.videochat.demo.logic.IPlayerMgr;
import com.alivc.videochat.demo.logic.LifecycledPlayerMgr;
import com.alivc.videochat.demo.logic.MgrCallback;
import com.alivc.videochat.demo.presenter.ILifecycleLivePlayPresenter;
import com.alivc.videochat.demo.presenter.view.ILivePlayView;
import com.alivc.videochat.demo.ui.LogInfoFragment;
import com.alivc.videochat.demo.ui.adapter.LogInfoAdapter;
import com.alivc.videochat.demo.uitils.ToastUtils;

import java.util.ArrayList;

import static com.alivc.videochat.demo.logic.IPlayerMgr.TYPE_INVITE_CHAT_TIMEOUT;
import static com.alivc.videochat.demo.logic.IPlayerMgr.TYPE_MAIN_STREAM_NOT_EXIST;
import static com.alivc.videochat.demo.logic.IPlayerMgr.TYPE_MIX_STREAM_ERROR;
import static com.alivc.videochat.demo.logic.IPlayerMgr.TYPE_MIX_STREAM_NOT_EXIST;
import static com.alivc.videochat.demo.logic.IPlayerMgr.TYPE_MIX_STREAM_SUCCESS;
import static com.alivc.videochat.demo.logic.IPlayerMgr.TYPE_MIX_STREAM_TIMEOUT;
import static com.alivc.videochat.demo.logic.IPlayerMgr.TYPE_PLAYER_AUDIO_PLAYER_ERROR;
import static com.alivc.videochat.demo.logic.IPlayerMgr.TYPE_PLAYER_INVALID_INPUTFILE;
import static com.alivc.videochat.demo.logic.IPlayerMgr.TYPE_PLAYER_NETWORK_POOR;
import static com.alivc.videochat.demo.logic.IPlayerMgr.TYPE_PLAYER_NO_NETWORK;
import static com.alivc.videochat.demo.logic.IPlayerMgr.TYPE_PLAYER_OPEN_FAILED;
import static com.alivc.videochat.demo.logic.IPlayerMgr.TYPE_PLAYER_READ_PACKET_TIMEOUT;
import static com.alivc.videochat.demo.logic.IPlayerMgr.TYPE_PLAYER_TIMEOUT;
import static com.alivc.videochat.demo.logic.IPlayerMgr.TYPE_PUBLISHER_NETWORK_POOR;
import static com.alivc.videochat.demo.logic.IPlayerMgr.TYPE_PUBLISHER_NETWORK_TIMEOUT;
import static com.alivc.videochat.demo.logic.IPlayerMgr.TYPE_PUBLISHER_NETWORK_UNCONNECT;
import static com.alivc.videochat.demo.logic.IPlayerMgr.TYPE_PUBLISHER_RECONNECT_FAILURE;

/**
 * Created by apple on 2017/1/9.
 */

public class LifecycleLivePlayPresenterImpl extends ContextBase implements ILifecycleLivePlayPresenter {
    private static final String TAG = LifecycleLivePlayPresenterImpl.class.getName();
    private ILivePlayView mView;
    private LifecycledPlayerMgr mPlayerMgr;


    public LifecycleLivePlayPresenterImpl(Context context,
                                          ILivePlayView view,
                                          ImManager imManager,
                                          String mUID) {
        super(context);
        this.mView = view;
        this.mPlayerMgr = new LifecycledPlayerMgr(context, imManager, mUID, mCallback);
    }

    @Override
    public void onCreate() {
        mPlayerMgr.onCreate();
    }

    @Override
    public void onStart() {
        mPlayerMgr.onStart();
    }

    @Override
    public void onResume() {
        mPlayerMgr.onResume();
    }

    @Override
    public void onPause() {
        mPlayerMgr.onPause();
    }

    @Override
    public void onStop() {
        mPlayerMgr.onStop();
    }

    @Override
    public void onDestroy() {
        mPlayerMgr.onDestroy();
    }

    @Override
    public void enterLiveRoom(String liveRoomID) {
        mPlayerMgr.asyncEnterLiveRoom(liveRoomID, new AsyncCallback() {
            @Override
            public void onSuccess(Bundle bundle) {
                //成功则开始播放
                mPlayerMgr.startPlay(mView.getPlaySurfaceView());
            }

            @Override
            public void onFailure(Bundle bundle, Throwable e) {
                //显示进入直播间失败的UI
                mView.showEnterLiveRoomFailure();
            }
        });
    }

    @Override
    public void invite() {
        try {
            mPlayerMgr.asyncInviteChatting(new AsyncCallback() {
                @Override
                public void onSuccess(Bundle bundle) {

                }

                @Override
                public void onFailure(Bundle bundle, Throwable e) {
                    // 显示exception的提示
                    mView.showInviteRequestFailedUI(e);
                }
            });
        } catch (ChatSessionException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void switchCamera() {
        mPlayerMgr.switchCamera();
    }

    @Override
    public boolean switchBeauty() {
        return mPlayerMgr.switchBeauty();
    }

    @Override
    public boolean switchFlash() {
        return mPlayerMgr.switchFlash();
    }

    @Override
    public void exitChatting() {
        mPlayerMgr.asyncTerminateChatting(new AsyncCallback() {
            @Override
            public void onSuccess(Bundle bundle) {
                mView.showSelfExitChattingUI();
            }

            @Override
            public void onFailure(Bundle bundle, Throwable e) {

            }
        });
    }

    @Override
    public void exitLiveRoom() {
        mPlayerMgr.asyncExitRoom(new AsyncCallback() {
            @Override
            public void onSuccess(Bundle bundle) {

            }

            @Override
            public void onFailure(Bundle bundle, Throwable e) {

            }
        });
    }

    private MgrCallback mCallback = new MgrCallback() {
        @Override
        public void onEvent(int eventType, Bundle data) {
            switch (eventType) {
                case IPlayerMgr.TYPE_PLAYER_INTERNAL_ERROR:
                    mView.showLiveInterruptUI(R.string.error_stop_playing, data.getInt(IPlayerMgr.DATA_KEY_PLAYER_ERROR_CODE));
                    break;
                case IPlayerMgr.TYPE_CHATTING_FINISHED:
//                    mView.showChattingFinishedUI();
                    break;
                case IPlayerMgr.TYPE_START_CHATTING:
                case IPlayerMgr.TYPE_OTHER_PEOPLE_JOIN_IN_CHATTING:
                    //开始连麦
                    ArrayList<String> inviteeUIDList = data.getStringArrayList(IPlayerMgr.DATA_KEY_INVITEE_UID_LIST);
                    if (inviteeUIDList == null) {
                        inviteeUIDList = new ArrayList<>();
                    }
                    mPlayerMgr.launchChat(mView.showLaunchChatUI(), mView.getOtherParterViews(inviteeUIDList));
                    break;
                case IPlayerMgr.TYPE_OTHER_PEOPLE_EXIT_CHATTING:    //其他人退出连麦
                    String inviteeUID = data.getString(IPlayerMgr.DATA_KEY_INVITEE_UID);
                    mView.showExitChattingUI(inviteeUID);
                    break;
                case IPlayerMgr.TYPE_SELF_EXIT_CHATTING:
                case IPlayerMgr.TYPE_PUBLISHER_TERMINATE_CHATTING:
                    mView.showSelfExitChattingUI();
                    break;
                case IPlayerMgr.TYPE_LIVE_CLOSE:
                    mView.showLiveCloseUI();
                    break;
                case IPlayerMgr.TYPE_PLAYER_FIRST_FRAME_RENDER_SUCCESS:
                    mView.hideLoading();
                    mView.hideLiveInterruptUI();
                    break;
                case IPlayerMgr.TYPE_PUBLISHER_FIRST_FRAME_RENDER_SUCCESS:
                    mView.hideChattingView();
                    break;
                case IPlayerMgr.TYPE_OFFLINE_CHAT_SUCCESS:
                    // 显示online按钮
//                    mView.showOnlineChatBtn();
                    break;
                case IPlayerMgr.TYPE_ONLINE_CHAT_SUCCESS:
                    // 显示offline按钮
//                    mView.showOfflineChatBtn();
                    break;
                case IPlayerMgr.TYPE_ADD_CHAT_SUCCESS:
//                    mView.showOfflineChatBtn();
                    break;
                case IPlayerMgr.TYPE_PARTER_OPT_START:
                    // 按钮不可点击
                    // 显示进度条
//                    mView.showLoading();
                    break;
                case IPlayerMgr.TYPE_PARTER_OPT_END:
                    // 按钮可以点击
                    // 隐藏进度条
//                    mView.hideLoading();
                    break;
                case IPlayerMgr.TYPE_PARTER_OPT_TIMEOUT:
                    mView.hideLoading();
                    mView.showLiveInterruptUI(R.string.error_video_chat_timeout, 0);
                    break;
                case IPlayerMgr.TYPE_OPERATION_CALLED_ERROR:
                    String msg = null;
                    if (data != null) {
                        msg = data.getString(IPlayerMgr.DATA_KEY_PLAYER_ERROR_MSG, null);
                    }
                    mView.showInfoDialog(msg);
                    break;
                case IPlayerMgr.TYPE_PUBLISHER_NO_AUDIO_DATA:
                    mView.hideLoading();
                    mView.showLiveInterruptUI(R.string.error_video_chat_no_audio_data, data.getInt(IPlayerMgr.DATA_KEY_PLAYER_ERROR_CODE));
                    break;
                case IPlayerMgr.TYPE_PUBLISHER_NO_VIDEO_DATA:
                    mView.hideLoading();
                    mView.showLiveInterruptUI(R.string.error_video_chat_no_video_data, data.getInt(IPlayerMgr.DATA_KEY_PLAYER_ERROR_CODE));
                    break;
                case TYPE_INVITE_CHAT_TIMEOUT:
                    mView.showToast(R.string.error_invite_timeout);
                    break;
                case TYPE_MIX_STREAM_ERROR:
                    mView.showToast(R.string.error_mix_stream_error);
                    break;
                case TYPE_MIX_STREAM_NOT_EXIST:
                    mView.showToast(R.string.error_mix_stream_not_exist);
                    break;
                case TYPE_MIX_STREAM_SUCCESS:
                    mView.showToast(R.string.error_mix_stream_success);
                    break;
                case TYPE_MIX_STREAM_TIMEOUT:
                    mView.showToast(R.string.error_mix_stream_timeout);
                    break;
                case TYPE_MAIN_STREAM_NOT_EXIST:
                    mView.showToast(R.string.error_main_stream_not_exist);
                    break;
                case TYPE_PLAYER_INVALID_INPUTFILE:
                    mView.showToast(R.string.error_player_invalid_inputfile);
                    break;
                case TYPE_PLAYER_OPEN_FAILED:
                    mView.showToast(R.string.error_player_open_failed);
                    break;
                case TYPE_PLAYER_NO_NETWORK:
                    mView.showToast(R.string.error_player_no_network);
                    break;
                case TYPE_PLAYER_TIMEOUT:
                    mView.showToast(R.string.error_player_timeout);
                    break;
                case TYPE_PLAYER_READ_PACKET_TIMEOUT:
                    mView.showToast(R.string.error_player_read_packet_timeout);
                    break;
                case TYPE_PUBLISHER_NETWORK_POOR:
                    mView.showToast(R.string.poor_network);
                    break;
                case TYPE_PUBLISHER_NETWORK_UNCONNECT:
                    mView.showLiveInterruptUI(R.string.error_publisher_network_unconnect, -400);
                    break;
                case TYPE_PUBLISHER_NETWORK_TIMEOUT:
                    mView.showLiveInterruptUI(R.string.error_publisher_network_timeout, -406);
                    break;
                case TYPE_PLAYER_AUDIO_PLAYER_ERROR:
                    mView.showLiveInterruptUI(R.string.error_audio_player, data.getInt(IPlayerMgr.DATA_KEY_PLAYER_ERROR_CODE));
                    break;
                case TYPE_PUBLISHER_RECONNECT_FAILURE:
                    mView.showToast(R.string.network_reconnect_failure);
                    break;
                case TYPE_PLAYER_NETWORK_POOR:
                    if (data != null) {
                        String url = data.getString(IPlayerMgr.DATA_KEY_PLAYER_ERROR_MSG);
                        ToastUtils.showToast(getContext(), "播放视频 " + url + " 网络差，可能造成延时");
                    }
                    break;

            }
        }
    };

    public void updateLog(LogInfoFragment.LogHandler logHandler) {
        if (mPlayerMgr != null) {
            AlivcPublisherPerformanceInfo publishInfo = mPlayerMgr.getPublisherPerformanceInfo();
//            AlivcPlayerPerformanceInfo playInfo = mPlayerMgr.getPlayerPerformanceInfo();
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
//            logHandler.updateValue(LogInfoAdapter.LogItem.CACHE_VIDEO_FRAME_COUNT,
//                    String.valueOf(playInfo.getVideoPacketsInBuffer()));
//            logHandler.updateValue(LogInfoAdapter.LogItem.CACHE_AUDIO_FRAME_COUNT,
//                    String.valueOf(playInfo.getAudioPacketsInBuffer()));
//            logHandler.updateValue(LogInfoAdapter.LogItem.VIDEO_DOWNLOAD_TO_PLAY_DELAY,
//                    playInfo.getVideoDurationFromDownloadToRender() + DELAY_UNIT);
//            logHandler.updateValue(LogInfoAdapter.LogItem.AUDIO_DOWNLOAD_TO_PLAY_DELAY,
//                    playInfo.getVideoDurationFromDownloadToRender() + DELAY_UNIT);
//            logHandler.updateValue(LogInfoAdapter.LogItem.CACHING_LAST_VIDEO_FRAME_PTS,
//                    playInfo.getVideoPtsOfLastPacketInBuffer() + PTS_UNIT);
//            logHandler.updateValue(LogInfoAdapter.LogItem.CACHING_LAST_AUDIO_FRAME_PTS,
//                    playInfo.getAudioPtsOfLastPacketInBuffer() + PTS_UNIT);
            logHandler.notifyUpdate();
        }
    }

}

