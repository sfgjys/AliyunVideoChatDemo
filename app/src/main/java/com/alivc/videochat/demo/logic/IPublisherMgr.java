package com.alivc.videochat.demo.logic;

import android.view.SurfaceView;

import com.alivc.videochat.demo.base.AsyncCallback;

import java.util.List;

/**
 * Created by liujianghao on 2017/1/3.
 */

public interface IPublisherMgr {

    int TYPE_PUBLISHER_NETWORK_POOR = 0x0001;
    int TYPE_PUBLISHER_AUDIO_CAPTURE_FAILURE = 0x0002;   //音频采集失败，需要停止推流
    int TYPE_PUBLISHER_VIDEO_CAPTURE_FAILURE = 0x0003;   //摄像头采集失败
    int TYPE_PUBLISHER_INTERNAL_ERROR = 0x0004;          //推流器内部错误，如果向继续推流，需要重启推流器
    int TYPE_PUBLISHER_NETWORK_GOOD = 0x0005;            //推流器网络状况良好
    int TYPE_PUBLISHER_RECONNECT_FAILURE = 0x0006;       //推流器重连失败
    int TYPE_PUBLISHER_NETWORK_UNCONNECT = 0x0007;
    int TYPE_PUBLISHER_NETWORK_TIMEOUT = 0x0008;

    int TYPE_PROCESS_INVITING_TIMEOUT = 0x0010;         //处理对方连麦响应超时
    int TYPE_PUBLISH_STREMA_SUCCESS = 0x0011;           //某个连麦对方推流成功
    int TYPE_RECEIVED_CHAT_INVITING = 0x0012;           //收到连麦邀请
    int TYPE_LIVE_CREATED = 0x0013;                     //直播创建成功
    int TYPE_START_CHATTING = 0x0014;                   //开始连麦
    int TYPE_SOMEONE_EXIT_CHATTING = 0x0015;            //某人退出连麦


    int TYPE_PLAYER_INTERNAL_ERROR = 0x0101;             //连麦播放出错，如果想要继续播放，需要重启连麦播放器(推流器)
    int TYPE_PLAYER_NO_NETWORK = 0x0102;
    int TYPE_PLAYER_TIMEOUT = 0x0103;                    //连麦播放器超时，需要重连
    int TYPE_OPERATION_CALLED_ERROR = 0x0104;
    int TYPE_PLAYER_NETWORK_POOR = 0x0105;
    int TYPE_PLAYER_AUDIO_PLAYER_ERROR = 0X0106;

    int TYPE_INVITE_TIMEOUT = 0x0021;                   //连麦对方响应超时
    int TYPE_MIX_STREAM_ERROR = 0x0022;                 //混流错误
    int TYPE_MIX_STREAM_SUCCESS = 0x0027;
    int TYPE_MIX_STREAM_NOT_EXIST = 0x0028;
    int TYPE_MAIN_STREAM_NOT_EXIST = 0x0029;
    int TYPE_MIX_STREAM_TIMEOUT = 0x0030;

    int TYPE_PLAYER_INVALID_INPUTFILE = 0x0033;
    int TYPE_PLAYER_OPEN_FAILED = 0x0034;
    int TYPE_PLAYER_READ_PACKET_TIMEOUT = 0x0035;


    String DATA_KEY_PUBLISHER_INFO_CODE = "publisher_info_code";
    String DATA_KEY_PUBLISHER_ERROR_CODE = "publisher_error_code";
    String DATA_KEY_CREATE_LIVE_RESULT = "create_live_result";
    String DATA_KEY_PUBLISH_STREAM_SUCCESS_INFO = "publish_stream_success_info";
    String DATA_KEY_INVITEE_UID = "invitee_uid";        //被邀请人的UID
    String DATA_KEY_PLAYER_UID = "player_uid";          //观众的UID

    String DATA_KEY_PLAYER_ERROR_MSG = "player_error_msg";

    /**
     * 方法描述: 创建直播，其意思是请求服务器获取直播的推流地址
     */
    void asyncCreateLive(String desc, AsyncCallback callback);


    /**
     * 方法描述: 开始连麦
     */
    void launchChat(SurfaceView parterView, String playerUID);


    // --------------------------------------------------------------------------------------------------------

    /**
     * 方法描述: 开始预览
     */
    void asyncStartPreview(SurfaceView holder, AsyncCallback callback);

    /**
     * 方法描述: 邀请观众连麦（某一个观众）
     */
    void asyncInviteChatting(List<String> inviteeUIDs, AsyncCallback callback);

    /**
     * 方法描述: 切换摄像头
     */
    void switchCamera();

    /**
     * 方法描述: 开关美颜
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
    void asyncCloseLive(AsyncCallback callback);

    /**
     * 方法描述: 结束连麦(某一个观众)
     */
    void asyncTerminateChatting(String playerUID, AsyncCallback callback);

    /**
     * 方法描述: 结束连麦（所有观众）
     */
    void asyncTerminateAllChatting(AsyncCallback callback);

    //反馈邀请
//    void asyncFeedbackInviting(String playerUID, int status);
}
