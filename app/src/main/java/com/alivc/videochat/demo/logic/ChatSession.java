package com.alivc.videochat.demo.logic;

import android.os.Handler;
import android.os.Message;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.alivc.videochat.demo.im.model.MsgDataNotAgreeVideoCall;
import com.alivc.videochat.demo.ui.VideoChatStatus;

/**
 * Created by apple on 2017/1/7.
 */

public class ChatSession {
    public static final int RESULT_OK = 1;
    public static final int RESULT_INVALID_STATUS = -1;

    private static final long INVITE_CHAT_TIMEOUT_DELAY = 10 * 1000;   //连麦邀请等待响应超时时间——10秒
    private static final long MIX_STREAM_TIMEOUT = 15 * 1000;       //同意连麦后，等待混流成功的超时时间——30秒
    private static final long WAITING_FOR_MIX_SUCCESS_DELAY = 15 * 1000; //混流错误时等待重新混流成功的时间，超过这个时间会结束连麦


    /**
     * 自己发送邀请，对方超时未响应，则自己更新本地的连麦状态为未连麦
     */
    private static final int MSG_WHAT_INVITE_CHAT_TIMEOUT = 1;   //连麦邀请响应超时

    /**
     * 别人发送的邀请，自己超时未处理，自动回应不同意连麦，并且在自己的UI层给出提醒
     */
    private static final int MSG_WHAT_PROCESS_INVITING_TIMEOUT = 2;

    /**
     * 同意连麦后，等待混流成功超时
     */
    private static final int MSG_WHAT_MIX_STREAM_TIMEOUT = 3;

    /**
     * InternalError, MainStreamNotExist, MixStreamNotExist都认为是混流错误，
     * 超过{@link WatchLivePresenter#WAITING_FOR_MIX_SUCCESS_DELAY}时间还没收到Success的code，就会结束连麦
     */
    private static final int MSG_WHAT_MIX_STREAM_ERROR = 4;

    private static final int MSG_WHAT_MIX_STREAM_SUCCESS = 5;

    private static final int MSG_WHAT_MIX_STREAM_NOT_EXIST = 6;

    private static final int MSG_WHAT_MAIN_STREAM_NOT_EXIST = 7;

    /**
     * 变量的描述: 连麦的最大数
     */
    public static final int MAX_SESSION_NUM = 3;

    private VideoChatStatus mChatStatus = VideoChatStatus.UNCHAT;
    private SessionHandler mSessionHandler;
    private ChatSessionInfo mChatSessionInfo;
    private SurfaceHolder.Callback mSurfaceCallback;
    private SurfaceStatus mSurfaceStatus = SurfaceStatus.UNINITED;
    private SurfaceView mSurfaceView;

    private int mOperationStatus = 0; // 1 表示操作进行中,0 表示操作已经完成


    public ChatSession(SessionHandler handler) {
        this.mSessionHandler = handler;
    }

    public int invite(String publisherUID, String playerUID) {
        if (mChatStatus == VideoChatStatus.UNCHAT) {
            if (mChatSessionInfo != null) {
                mChatSessionInfo.setPublisherUID(publisherUID);
                mChatSessionInfo.setPlayerUID(playerUID);
            }
            mChatStatus = VideoChatStatus.INVITE_FOR_RES;
            return RESULT_OK;
        } else {
            return RESULT_INVALID_STATUS;
        }
    }

    public void feedbactInviting(boolean isAgree) {
        if (!isAgree) {
            mChatStatus = VideoChatStatus.UNCHAT;        //更新连麦状态为未连麦状态
        }
    }

    public void notifyFeedbackSuccess() {
        mChatStatus = VideoChatStatus.TRY_MIX;   //更新连麦状态为开始混流， 等待混流成功
        mHandler.removeMessages(MSG_WHAT_PROCESS_INVITING_TIMEOUT);
//        mHandler.sendEmptyMessageDelayed(MSG_WHAT_MIX_STREAM_TIMEOUT, MIX_STREAM_TIMEOUT);  //开始等待混流成功超时的倒计时
    }


    public void notifyInviteSuccess() {
        mChatStatus = VideoChatStatus.INVITE_RES_SUCCESS;
        //开始响应倒计时
        mHandler.sendEmptyMessageDelayed(MSG_WHAT_INVITE_CHAT_TIMEOUT, INVITE_CHAT_TIMEOUT_DELAY);//倒计时，10s后未收到回复，自动认为对方决绝。
    }

    public void notifyInviteFailure() {
        mChatStatus = VideoChatStatus.UNCHAT;
    }

    public void launchChat() {
//        mChatStatus = VideoChatStatus.UNCHAT;
    }

    public void abortChat() {
        mChatStatus = VideoChatStatus.UNCHAT;
    }

    public int notifyAgreeInviting() {
        mHandler.removeMessages(MSG_WHAT_INVITE_CHAT_TIMEOUT);//TODO:移除邀请等待响应超时倒计时的消息
        if (mChatStatus == VideoChatStatus.INVITE_RES_SUCCESS) {        //如果当前是已经发送邀请，等待对方反馈的状态，则处理这个消息，否则视为无效的消息，不作处理
            mChatStatus = VideoChatStatus.TRY_MIX;   //更新当前状态为开始混流，等待混流成功
            return RESULT_OK;
        }
        return RESULT_INVALID_STATUS;
    }

    public int notifyParterAgreeInviting() {
        mHandler.removeMessages(MSG_WHAT_INVITE_CHAT_TIMEOUT);//TODO:移除邀请等待响应超时倒计时的消息
        if (mChatStatus == VideoChatStatus.INVITE_RES_SUCCESS) {        //如果当前是已经发送邀请，等待对方反馈的状态，则处理这个消息，否则视为无效的消息，不作处理
            mChatStatus = VideoChatStatus.MIX_SUCC;   //更新当前状态为开始混流，等待混流成功
            return RESULT_OK;
        }
        return RESULT_INVALID_STATUS;
    }

    public int notifyNotAgreeInviting(MsgDataNotAgreeVideoCall notAgreeVideoCall) {
        if (mChatStatus == VideoChatStatus.INVITE_RES_SUCCESS) {//如果当前是已经发送邀请，等待对方反馈的状态，则处理这个消息，否则视为无效的消息，不作处理
            mHandler.removeMessages(MSG_WHAT_INVITE_CHAT_TIMEOUT);//移除邀请等待响应超时倒计时的消息
            mChatStatus = VideoChatStatus.UNCHAT;    //更新当前状态为未混流
            return RESULT_OK;
        }
        return RESULT_INVALID_STATUS;
    }

    public void notifyReceivedInviting(String publisherUID, String playerUID) {
        if (mChatSessionInfo != null) {
            mChatSessionInfo.setPublisherUID(publisherUID);
            mChatSessionInfo.setPlayerUID(playerUID);
        }
        mChatStatus = VideoChatStatus.RECEIVED_INVITE;   //更新当前连麦状态为收到邀请，等待反馈的状态
        mHandler.sendEmptyMessageDelayed(MSG_WHAT_PROCESS_INVITING_TIMEOUT, INVITE_CHAT_TIMEOUT_DELAY); //超过10s自动拒绝连麦
    }

    public void notifyMixStreamSuccess() {
        //                    mView.showToast(R.string.mix_success);
        mChatStatus = VideoChatStatus.MIX_SUCC;
        mHandler.removeMessages(MSG_WHAT_MIX_STREAM_TIMEOUT);
        mHandler.removeMessages(MSG_WHAT_MIX_STREAM_ERROR);
        mHandler.sendEmptyMessage(MSG_WHAT_MIX_STREAM_SUCCESS);
    }

    public void notifyMixStreamNotExist() {
//                    mView.showToast(R.string.mix_stream_not_exist);
        mHandler.removeMessages(MSG_WHAT_MIX_STREAM_ERROR);
        mHandler.sendEmptyMessage(MSG_WHAT_MIX_STREAM_NOT_EXIST);
        mHandler.sendEmptyMessageDelayed(MSG_WHAT_MIX_STREAM_ERROR, WAITING_FOR_MIX_SUCCESS_DELAY);
    }

    public void notifyMainStreamNotExist() {
//                    mView.showToast(R.string.main_stream_not_exist);
        mHandler.removeMessages(MSG_WHAT_MIX_STREAM_ERROR);
        mHandler.sendEmptyMessage(MSG_WHAT_MAIN_STREAM_NOT_EXIST);
        mHandler.sendEmptyMessageDelayed(MSG_WHAT_MIX_STREAM_ERROR, WAITING_FOR_MIX_SUCCESS_DELAY);
    }

    public void notifyInternalError() {
//        mView.showToast(R.string.mix_internal_error);
        mHandler.removeMessages(MSG_WHAT_MIX_STREAM_ERROR);
        mHandler.sendEmptyMessage(MSG_WHAT_MIX_STREAM_ERROR);
//        mHandler.sendEmptyMessageDelayed(MSG_WHAT_MIX_STREAM_ERROR, WAITING_FOR_MIX_SUCCESS_DELAY);
    }

    boolean isMixing() {
        return mChatStatus == VideoChatStatus.MIX_SUCC;
    }

    boolean isActive() {
        return mChatStatus != VideoChatStatus.UNCHAT;
    }

    public VideoChatStatus getChatStatus() {
        return mChatStatus;
    }

    public void setChatStatus(VideoChatStatus mChatStatus) {
        this.mChatStatus = mChatStatus;
    }

    boolean isTryMix() {
        return mChatStatus == VideoChatStatus.TRY_MIX;
    }

//    boolean isOperationCompleted() {
//        return mOperationStatus == 0;
//    }
//
//    void setOperationStarted() {
//        mOperationStatus = 1;
//    }
//
//    void setOperationCompleted() {
//        mOperationStatus = 0;
//    }


    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                /**
                 * 自己发送邀请，对方超时未响应，则自己更新本地的连麦状态为未连麦
                 */
                case MSG_WHAT_INVITE_CHAT_TIMEOUT://连麦响应超时
                    if (mSessionHandler != null) {
                        mSessionHandler.onInviteChatTimeout();
                    }
                    break;
                case MSG_WHAT_PROCESS_INVITING_TIMEOUT:
                    if (mSessionHandler != null) {
                        mSessionHandler.onProcessInvitingTimeout();
                    }
                    break;
                case MSG_WHAT_MIX_STREAM_TIMEOUT:
                    if (mSessionHandler != null) {
                        mSessionHandler.onMixStreamTimeout();
                    }
                    break;
                case MSG_WHAT_MIX_STREAM_ERROR:
                    if (mSessionHandler != null) {
                        mSessionHandler.onMixStreamError();
                    }
                    break;
                case MSG_WHAT_MIX_STREAM_SUCCESS:
                    if (mSessionHandler != null) {
                        mSessionHandler.onMixStreamSuccess();
                    }
                    break;
                case MSG_WHAT_MIX_STREAM_NOT_EXIST:
                    if (mSessionHandler != null) {
                        mSessionHandler.onMixStreamNotExist();
                    }
                    break;
                case MSG_WHAT_MAIN_STREAM_NOT_EXIST:
                    if (mSessionHandler != null) {
                        mSessionHandler.onMainStreamNotExist();
                    }
                    break;
            }
        }
    };

    public ChatSessionInfo getChatSessionInfo() {
        return mChatSessionInfo;
    }

    public void setChatSessionInfo(ChatSessionInfo chatSessionInfo) {
        mChatSessionInfo = chatSessionInfo;
    }

    public SurfaceHolder.Callback getSurfaceCallback() {
        return mSurfaceCallback;
    }

    public void setSurfaceCallback(SurfaceHolder.Callback surfaceCallback) {
        mSurfaceCallback = surfaceCallback;
    }

    public SurfaceStatus getSurfaceStatus() {
        return mSurfaceStatus;
    }

    public void setSurfaceStatus(SurfaceStatus surfaceStatus) {
        mSurfaceStatus = surfaceStatus;
    }

    public SurfaceView getSurfaceView() {
        return mSurfaceView;
    }

    public void setSurfaceView(SurfaceView surfaceView) {
        mSurfaceView = surfaceView;
    }
}
