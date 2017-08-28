package com.alivc.videochat.demo.logic;

import android.view.SurfaceView;

import com.alivc.videochat.demo.base.AsyncCallback;
import com.alivc.videochat.demo.exception.ChatSessionException;

import java.util.Map;

/**
 * Created by apple on 2017/1/3.
 */

public interface IPlayerMgr {

    int TYPE_PLAYER_INTERNAL_ERROR = 0x0001;    //播放出错，如果想继续播放需要重启播放器
    int TYPE_CHATTING_FINISHED = 0x0002;        //连麦结束
    int TYPE_PLAYER_TIMEOUT = 0x0003;           //播放超时
    int TYPE_START_CHATTING = 0x0004;           //开始连麦
    int TYPE_OTHER_PEOPLE_JOIN_IN_CHATTING = 0x0005;  //其他人加入连麦
    int TYPE_OTHER_PEOPLE_EXIT_CHATTING = 0x0006;      //其他人退出连麦
    int TYPE_SELF_EXIT_CHATTING = 0x0007;       //自己退出连麦（被主播踢出）
    int TYPE_PUBLISHER_TERMINATE_CHATTING = 0x0008; //主播结束连麦
    int TYPE_LIVE_CLOSE = 0x0009;                //直播结束
    int TYPE_PLAYER_FIRST_FRAME_RENDER_SUCCESS = 0x0010;   //播放首帧渲染成功
    int TYPE_PUBLISHER_FIRST_FRAME_RENDER_SUCCESS = 0x0011; //连麦推流预览首帧渲染成功

    int TYPE_ONLINE_CHAT_START = 0x0012;
    int TYPE_ONLINE_CHAT_SUCCESS = 0x0013;
    int TYPE_OFFLINE_CHAT_START = 0x0014;
    int TYPE_OFFLINE_CHAT_SUCCESS = 0x0015;
    int TYPE_ADD_CHAT_START = 0x0016;
    int TYPE_ADD_CHAT_SUCCESS = 0x0017;
    int TYPE_REMOVE_CHAT_START = 0x0018;
    int TYPE_REMOVE_CHAT_SUCCESS = 0x0019;
    int TYPE_PARTER_OPT_START = 0x0021;
    int TYPE_PARTER_OPT_END = 0x0022;
    int TYPE_PARTER_OPT_TIMEOUT = 0x0023;
    int TYPE_BACK_TO_LIST = 0x0024;

    int TYPE_OPERATION_CALLED_ERROR = 0x0025;
    int TYPE_PUBLISHER_NO_AUDIO_DATA = 0x0026;
    int TYPE_PUBLISHER_NO_VIDEO_DATA = 0x0126;

    int TYPE_MIX_STREAM_SUCCESS = 0x0027;
    int TYPE_MIX_STREAM_NOT_EXIST = 0x0028;
    int TYPE_MAIN_STREAM_NOT_EXIST = 0x0029;
    int TYPE_MIX_STREAM_TIMEOUT = 0x0030;
    int TYPE_MIX_STREAM_ERROR = 0x0031;
    int TYPE_INVITE_CHAT_TIMEOUT = 0x0032;

    int TYPE_PLAYER_INVALID_INPUTFILE = 0x0033;
    int TYPE_PLAYER_OPEN_FAILED = 0x0034;
    int TYPE_PLAYER_NO_NETWORK = 0x0035;
    int TYPE_PLAYER_READ_PACKET_TIMEOUT = 0x0036;
    int TYPE_PLAYER_NETWORK_POOR = 0x0037;
    int TYPE_PLAYER_AUDIO_PLAYER_ERROR = 0x0038;

    int TYPE_PUBLISHER_RECONNECT_FAILURE = 0x0040;
    int TYPE_PUBLISHER_NETWORK_UNCONNECT = 0x0041;
    int TYPE_PUBLISHER_NETWORK_TIMEOUT = 0x0042;
    int TYPE_PUBLISHER_NETWORK_POOR = 0x0043;


    String DATA_KEY_PLAYER_ERROR_CODE = "player_error_code";
    String DATA_KEY_PLAYER_ERROR_MSG = "player_error_msg";
    String DATA_KEY_PUBLISHER_INFO_CODE = "publisher_info_code";
    String DATA_KEY_INVITER_UID = "inviter_uid";    //邀请者的UID
    String DATA_KEY_INVITEE_UID = "invitee_uid";    //被邀请者的UID
    String DATA_KEY_INVITEE_UID_LIST = "invitee_uid";

    /**
     * 方法描述: 进入直播间,实质是请求网络获取播放url和播放信息等
     *
     * @param liveRoomID 直播列表Item的数据源LiveItemResult对象包含的roomId
     */
    void asyncEnterLiveRoom(String liveRoomID, AsyncCallback callback);

    /**
     * 方法描述: 如果成功通过asyncEnterLiveRoom方法获取了播放信息，则使用主播播放url开始播放
     */
    void startPlay(SurfaceView playSurf);

    /**
     * 方法描述: 请求网络向主播请求连麦，根据网络请求结果改变ChatSession状态为（邀请连麦成功，等待对方响应）或者（未连麦）
     * 剩下的是通过MNS返回是否同意连麦，根据结果在进行接下来的操作
     */
    void asyncInviteChatting(AsyncCallback asyncCallback) throws ChatSessionException;

    /**
     * 方法描述: 真正开始连麦的方法，该方法中先把参数二的数据进行处理下，在正式调用开启连麦的核心方法startLaunchChat
     *
     * @param previewSurface 观众进行推流的SurfaceView
     * @param uidSurfaceMap  其他连麦观众用于播放短延迟的Surface和uid的集合
     */
    void launchChat(SurfaceView previewSurface, Map<String, SurfaceView> uidSurfaceMap);

    //切换摄像头
    void switchCamera();

    //切换美颜
    boolean switchBeauty();

    //切换闪光灯
    boolean switchFlash();

    //结束连麦
    void asyncTerminateChatting(AsyncCallback callback);

    /**
     * 方法描述: 结束观看直播。调用该函数将关闭直播播放器，并销毁所有资源。
     * 备注: 若当前处在连麦状态下，需要先调用PlayerSDKHelper的offlineChat函数结束连麦，然后在调用该函数结束观看直播
     */
    void asyncTerminatePlaying(AsyncCallback callback);

    //退出观看
    void asyncExitRoom(AsyncCallback asyncCallback);
}
