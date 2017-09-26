package com.alivc.videochat.demo.logic;

import android.os.Handler;
import android.os.Message;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.alivc.videochat.demo.im.model.MsgDataNotAgreeVideoCall;
import com.alivc.videochat.demo.ui.VideoChatStatus;

/**
 * 类的描述: 本类是包含了连麦的所有流程的一个会话
 * 1、未开始进行连麦链接流程
 * 2、向服务器发送邀请连麦的请求
 * 3、被邀请人收到服务器的邀请消息
 * 4、被邀请人同意还是拒绝发送给服务器
 * 5、服务器将被邀请人的结果发送给主动邀请人
 * 6、结果为拒绝则移除会话，结果为同意则继续会话
 * 7、因为同意，所以开始进行正式连麦
 * 8、结束连麦
 * 9、移除本会话
 */
public class ChatSession {
    /**
     * 变量的描述: 连麦的最大数
     */
    public static final int MAX_SESSION_NUM = 3;
    /**
     * 变量的描述: 连麦流程的结果进行回调
     */
    private ChatSessionCallback mChatSessionCallback;
    /**
     * 变量的描述: 连麦各个状态的表现，其值用VideoChatStatus枚举来赋值，初始是未连麦
     */
    private VideoChatStatus mChatStatus = VideoChatStatus.UNCHAT;
    /**
     * 变量的描述: 修改连麦流程状态成功
     */
    public static final int RESULT_OK = 1;
    /**
     * 变量的描述: 修改连麦流程状态失败，无效的状态
     */
    public static final int RESULT_INVALID_STATUS = -1;
    /**
     * 变量的描述: 用来存储推流地址推流用户的ID，以及播放地址播放用户的ID
     */
    private ChatSessionInfo mChatSessionInfo;
    /**
     * 变量的描述: 连麦流程会话所对应的播放或推流的SurfaceView控件
     */
    private SurfaceView mSurfaceView;
    /**
     * 变量的描述: mSurfaceView的生命周期监听
     */
    private SurfaceHolder.Callback mSurfaceCallback;
    /**
     * 变量的描述: mSurfaceView的生命周期的状态标识
     */
    private SurfaceStatus mSurfaceStatus = SurfaceStatus.UNINITED;
    // --------------------------------------------------------------------------------------------------------
    /**
     * 变量的描述: 等待被邀请人响应连麦的响应超时时间——10秒
     */
    private static final long INVITE_CHAT_TIMEOUT_DELAY = 10 * 1000;
    /**
     * 变量的描述: 同意连麦后，等待混流成功的超时时间——30秒
     */
    private static final long MIX_STREAM_TIMEOUT = 15 * 1000;
    /**
     * 变量的描述: 混流失败出现异常后会通过Handler发送一个延迟消息，这个就是延迟时间的大小
     */
    private static final long WAITING_FOR_MIX_SUCCESS_DELAY = 15 * 1000;
    // --------------------------------------------------------------------------------------------------------
    /**
     * 变量的描述: 自己发送邀请，对方超时未响应，则自己更新本地的连麦状态为未连麦
     */
    private static final int MSG_WHAT_INVITE_CHAT_TIMEOUT = 1;   //连麦邀请响应超时
    /**
     * 变量的描述: 被邀请人收到别人发送的邀请，自己超时未处理，自动回应不同意连麦，并且在自己的UI层给出提醒
     */
    private static final int MSG_WHAT_PROCESS_INVITING_TIMEOUT = 2;
    /**
     * 变量的描述: 同意连麦后，等待混流成功超时 暂时没人用
     */
    private static final int MSG_WHAT_MIX_STREAM_TIMEOUT = 3;
    /**
     * 变量的描述: InternalError, MainStreamNotExist, MixStreamNotExist都认为是混流错误的Handler消息识别标识
     */
    private static final int MSG_WHAT_MIX_STREAM_ERROR = 4;
    /**
     * 变量的描述: 混流成功的Handler消息识别标识
     */
    private static final int MSG_WHAT_MIX_STREAM_SUCCESS = 5;
    /**
     * 变量的描述: 混流(连麦观众流)不存在的Handler消息识别标识
     */
    private static final int MSG_WHAT_MIX_STREAM_NOT_EXIST = 6;
    /**
     * 变量的描述: 主播流不存在的Handler消息识别标识
     */
    private static final int MSG_WHAT_MAIN_STREAM_NOT_EXIST = 7;
    // --------------------------------------------------------------------------------------------------------

    /**
     * 方法描述: 本类的构造方法，在创建的时候传入已经实例化好的回调接口
     */
    public ChatSession(ChatSessionCallback handler) {
        this.mChatSessionCallback = handler;
    }
    // --------------------------------------------------------------------------------------------------------

    /**
     * 被调用的时候: 邀请人准备请求网络，告诉业务服务器，邀请人打算邀请谁来进行连麦
     * <p>
     * 方法描述: 修改连麦流程会话的状态为请求网络邀请对方连麦
     *
     * @param publisherUID 主播的UID
     * @param playerUID    观众自己的UID
     */
    public int invite(String publisherUID, String playerUID) {
        if (mChatStatus == VideoChatStatus.UNCHAT) {

            // 在观众界面和主播界面中调用本方法的时候，本对象先是重新new出来的，且没有设置mChatSessionInfo对象，所以mChatSessionInfo为null
            // 所以下面的判断应该是进不去了
            if (mChatSessionInfo != null) {
                mChatSessionInfo.setPublisherUID(publisherUID);
                mChatSessionInfo.setPlayerUID(playerUID);
            }

            // 改变连麦流程状态
            mChatStatus = VideoChatStatus.INVITE_FOR_RES;
            return RESULT_OK;
        } else {
            return RESULT_INVALID_STATUS;
        }
    }
    // --------------------------------------------------------------------------------------------------------

    /**
     * 被调用的时候: 邀请人发送邀请给业务服务器的网络请求成功了
     * <p>
     * 方法描述: 发送邀请对方进行连麦的消息的网络请求成功，修改连麦流程状态为等待被邀请连麦的人的响应，并开启Handler的定时发送消息(如果10秒后没有接收到从服务器发给MNS的消息，则自动认为对方拒绝)
     */
    public void notifyInviteSuccess() {
        mChatStatus = VideoChatStatus.INVITE_RES_SUCCESS;
        // 开始响应倒计时，等待对方是否同意连麦，10秒中有回应，会移除该消息，否则10秒后发送自动认为对方拒绝的消息
        mHandler.sendEmptyMessageDelayed(MSG_WHAT_INVITE_CHAT_TIMEOUT, INVITE_CHAT_TIMEOUT_DELAY);//倒计时，10s后未收到回复，自动认为对方拒绝。
    }

    /**
     * 被调用的时候: 邀请人发送邀请给业务服务器的网络请求失败了
     * <p>
     * 方法描述: 请求网络邀请对方进行连麦，请求网络失败
     */
    public void notifyInviteFailure() {
        mChatStatus = VideoChatStatus.UNCHAT;
    }
    // --------------------------------------------------------------------------------------------------------

    /**
     * 被调用的时候: 被邀请人通过MNS收到了邀请人发起连麦的请求
     * <p>
     * 方法描述: 主播端或者观众端被邀请时调用，改变状态为收到邀请，等待被邀请人反馈的状态
     */
    public void notifyReceivedInviting(String publisherUID, String playerUID) {
        // 在观众界面和主播界面中调用本方法的时候，本对象先是重新new出来的，且没有设置mChatSessionInfo对象，所以mChatSessionInfo为null
        // 所以下面的判断应该是进不去了
        if (mChatSessionInfo != null) {
            mChatSessionInfo.setPublisherUID(publisherUID);
            mChatSessionInfo.setPlayerUID(playerUID);
        }
        mChatStatus = VideoChatStatus.RECEIVED_INVITE;   // 更新当前连麦状态为收到邀请，等待反馈的状态
        mHandler.sendEmptyMessageDelayed(MSG_WHAT_PROCESS_INVITING_TIMEOUT, INVITE_CHAT_TIMEOUT_DELAY); //超过10s自动拒绝连麦
    }
    // --------------------------------------------------------------------------------------------------------

    /**
     * 被调用的时候: 当被邀请人请求网络告诉业务服务器同意邀请，并且网络请求成功的时候
     * <p>
     * 方法描述: 同意了别人的连麦邀请时调用，并修改 连麦状态为开始混流， 等待混流成功
     */
    public void notifyFeedbackSuccess() {
        mChatStatus = VideoChatStatus.TRY_MIX;   // 更新连麦状态为开始混流， 等待混流成功
        mHandler.removeMessages(MSG_WHAT_PROCESS_INVITING_TIMEOUT);// 移除  别人发送的邀请，自己超时未处理，自动回应不同意连麦
//        mHandler.sendEmptyMessageDelayed(MSG_WHAT_MIX_STREAM_TIMEOUT, MIX_STREAM_TIMEOUT);  //开始等待混流成功超时的倒计时
    }

    /**
     * TODO 这个方法可以改为不同意别人的邀请的时候具体做什么
     * <p>
     * 方法描述: 对于别人的邀请连麦是否同意，根据是否同意来修改连麦流程会话的状态
     */
    public void feedbactInviting(boolean isAgree) {
        if (!isAgree) {
            mChatStatus = VideoChatStatus.UNCHAT;        //
        }
    }
    // --------------------------------------------------------------------------------------------------------

    /**
     * 被调用的时候: 主播通过MNS收到被邀请观众同意连麦的消息
     * <p>
     * 方法描述:  邀请对发进行连麦，MNS接收到了对方同意进行连麦，修改连麦流程的状态为尝试混流，移除邀请等待响应超时倒计时的消息
     */
    public int notifyAgreeInviting() {
        mHandler.removeMessages(MSG_WHAT_INVITE_CHAT_TIMEOUT);// 移除邀请等待响应超时倒计时的消息
        if (mChatStatus == VideoChatStatus.INVITE_RES_SUCCESS) {  // 如果当前是已经发送连麦邀请，等待对方是否同意的状态，则处理这个消息，否则视为无效的消息，不作处理
            mChatStatus = VideoChatStatus.TRY_MIX;   // 更新当前状态为尝试混流，等待混流成功
            return RESULT_OK;
        }
        return RESULT_INVALID_STATUS;
    }

    /**
     * 被调用的时候: 观众通过MNS收到被邀请的主播同意连麦的消息
     * <p>
     * 方法描述: 观众发起连麦，主播同意了，观众这里才更新当前状态为开始混流，等待混流成功
     */
    public int notifyParterAgreeInviting() {
        mHandler.removeMessages(MSG_WHAT_INVITE_CHAT_TIMEOUT);// 移除邀请等待响应超时倒计时的消息
        if (mChatStatus == VideoChatStatus.INVITE_RES_SUCCESS) {        //如果当前是已经发送邀请，等待对方反馈的状态，则处理这个消息，否则视为无效的消息，不作处理
            // TODO 这里的状态改为 VideoChatStatus.TRY_MIX 更合理些
            mChatStatus = VideoChatStatus.MIX_SUCC;   // 更新当前状态为开始混流，等待混流成功
            return RESULT_OK;
        }
        return RESULT_INVALID_STATUS;
    }

    /**
     * 被调用的时候: 邀请人通过MNS收到被邀请人不同意连麦的消息
     * <p>
     * 方法描述: 对方不同意进行连麦，所以调用此方法进行处理，此处的不同意可以是超时的，也可以是对方认定的
     */
    public int notifyNotAgreeInviting(MsgDataNotAgreeVideoCall notAgreeVideoCall) {
        if (mChatStatus == VideoChatStatus.INVITE_RES_SUCCESS) {// 如果当前是已经发送邀请，等待对方反馈的状态，则处理这个消息，否则视为无效的消息，不作处理
            mHandler.removeMessages(MSG_WHAT_INVITE_CHAT_TIMEOUT);// 移除邀请等待响应超时倒计时的消息
            mChatStatus = VideoChatStatus.UNCHAT;    // 更新当前状态为未进行连麦流程
            return RESULT_OK;
        }
        return RESULT_INVALID_STATUS;
    }
    // --------------------------------------------------------------------------------------------------------

    /**
     * 方法描述: 中止连麦，将mChatStatus状态改为未连麦，连麦流程走到了终点
     */
    public void abortChat() {
        mChatStatus = VideoChatStatus.UNCHAT;
    }
    // --------------------------------------------------------------------------------------------------------

    // 获取连麦流程状态
    public VideoChatStatus getChatStatus() {
        return mChatStatus;
    }

    // 修改连麦流程状态
    public void setChatStatus(VideoChatStatus mChatStatus) {
        this.mChatStatus = mChatStatus;
    }
    // --------------------------------------------------------------------------------------------------------

    /**
     * 方法描述: 判断本连麦流程的状态是否为成功混流
     */
    boolean isMixing() {
        return mChatStatus == VideoChatStatus.MIX_SUCC;
    }

    /**
     * 方法描述: 判断本连麦流程的状态是否为尝试混流
     */
    boolean isTryMix() {
        return mChatStatus == VideoChatStatus.TRY_MIX;
    }
    // --------------------------------------------------------------------------------------------------------

    // ChatSessionInfo的Get和Set
    public ChatSessionInfo getChatSessionInfo() {
        return mChatSessionInfo;
    }

    public void setChatSessionInfo(ChatSessionInfo chatSessionInfo) {
        mChatSessionInfo = chatSessionInfo;
    }
    // --------------------------------------------------------------------------------------------------------

    // SurfaceCallback的Get和Set
    public SurfaceHolder.Callback getSurfaceCallback() {
        return mSurfaceCallback;
    }

    public void setSurfaceCallback(SurfaceHolder.Callback surfaceCallback) {
        mSurfaceCallback = surfaceCallback;
    }

    // --------------------------------------------------------------------------------------------------------
    // SurfaceStatus的Get和Set
    public SurfaceStatus getSurfaceStatus() {
        return mSurfaceStatus;
    }

    public void setSurfaceStatus(SurfaceStatus surfaceStatus) {
        mSurfaceStatus = surfaceStatus;
    }

    // --------------------------------------------------------------------------------------------------------
    // SurfaceView的Get和Set
    public SurfaceView getSurfaceView() {
        return mSurfaceView;
    }

    public void setSurfaceView(SurfaceView surfaceView) {
        mSurfaceView = surfaceView;
    }
    // --------------------------------------------------------------------------------------------------------

    /**
     * 方法描述: 判断本 连麦流程会话 是否处于连麦流程中
     */
    boolean isActive() {
        return mChatStatus != VideoChatStatus.UNCHAT;
    }

    /**
     * 方法描述: ？？？？？？？？？？？？？？？？？？？无内容
     */
    public void launchChat() {
//        mChatStatus = VideoChatStatus.UNCHAT;
    }

    /**
     * 方法描述: 通知混流成功
     */
    public void notifyMixStreamSuccess() {
        mChatStatus = VideoChatStatus.MIX_SUCC;
        mHandler.removeMessages(MSG_WHAT_MIX_STREAM_TIMEOUT);
        mHandler.removeMessages(MSG_WHAT_MIX_STREAM_ERROR);
        mHandler.sendEmptyMessage(MSG_WHAT_MIX_STREAM_SUCCESS);
    }

    /**
     * 方法描述: 通知混流失败，失败原因为混流(连麦观众流)不存在
     */
    public void notifyMixStreamNotExist() {
        mHandler.removeMessages(MSG_WHAT_MIX_STREAM_ERROR);
        mHandler.sendEmptyMessage(MSG_WHAT_MIX_STREAM_NOT_EXIST);
        mHandler.sendEmptyMessageDelayed(MSG_WHAT_MIX_STREAM_ERROR, WAITING_FOR_MIX_SUCCESS_DELAY);
    }

    /**
     * 方法描述: 通知混流失败，失败原因为主播流不存在
     */
    public void notifyMainStreamNotExist() {
        mHandler.removeMessages(MSG_WHAT_MIX_STREAM_ERROR);
        mHandler.sendEmptyMessage(MSG_WHAT_MAIN_STREAM_NOT_EXIST);
        mHandler.sendEmptyMessageDelayed(MSG_WHAT_MIX_STREAM_ERROR, WAITING_FOR_MIX_SUCCESS_DELAY);
    }

    /**
     * 方法描述: 通知混流失败，失败原因为网络异常
     */
    public void notifyInternalError() {
        mHandler.removeMessages(MSG_WHAT_MIX_STREAM_ERROR);
        mHandler.sendEmptyMessage(MSG_WHAT_MIX_STREAM_ERROR);
//        mHandler.sendEmptyMessageDelayed(MSG_WHAT_MIX_STREAM_ERROR, WAITING_FOR_MIX_SUCCESS_DELAY);
    }
    // --------------------------------------------------------------------------------------------------------

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_WHAT_INVITE_CHAT_TIMEOUT:
                    // 邀请人发送邀请给业务服务器的网络请求成功，被邀请人收到邀请，但是一直不处理，导致超时(该消息是有延迟的)
                    if (mChatSessionCallback != null) {
                        // 使用ChatSessionCallback进行超时UI处理
                        mChatSessionCallback.onInviteChatTimeout();
                    }
                    break;
                case MSG_WHAT_PROCESS_INVITING_TIMEOUT:
                    // 别人发送的邀请，自己超时未处理，自动回应不同意连麦，并且在自己的UI层给出提醒(该消息是有延迟的)
                    if (mChatSessionCallback != null) {
                        mChatSessionCallback.onProcessInvitingTimeout();
                    }
                    break;
                case MSG_WHAT_MIX_STREAM_TIMEOUT:
                    // 暂时没有用(该消息是有延迟的)
                    if (mChatSessionCallback != null) {
                        mChatSessionCallback.onMixStreamTimeout();
                    }
                    break;
                case MSG_WHAT_MIX_STREAM_ERROR:
                    // 混流出现异常(该消息是有延迟的)
                    if (mChatSessionCallback != null) {
                        mChatSessionCallback.onMixStreamError();
                    }
                    break;
                case MSG_WHAT_MIX_STREAM_SUCCESS:
                    // 混流成功
                    if (mChatSessionCallback != null) {
                        mChatSessionCallback.onMixStreamSuccess();
                    }
                    break;
                case MSG_WHAT_MIX_STREAM_NOT_EXIST:
                    // 连麦观众流不存在
                    if (mChatSessionCallback != null) {
                        mChatSessionCallback.onMixStreamNotExist();
                    }
                    break;
                case MSG_WHAT_MAIN_STREAM_NOT_EXIST:
                    // 主播推流不存在
                    if (mChatSessionCallback != null) {
                        mChatSessionCallback.onMainStreamNotExist();
                    }
                    break;
            }
        }
    };
}
