package com.alivc.videochat.demo.logic;

import android.view.SurfaceView;

import com.alivc.videochat.demo.base.AsyncCallback;
import com.alivc.videochat.demo.exception.ChatSessionException;

import java.util.Map;

public interface IPlayerManager {

    /**
     * 方法描述: 进入直播间,实质是请求网络获取播放url和播放信息等
     *
     * @param liveRoomID 直播列表Item的数据源LiveItemResult对象包含的roomId
     */
    void asyncEnterLiveRoom(String liveRoomID, AsyncCallback callback);

    /**
     * 方法描述: 如果成功通过asyncEnterLiveRoom方法获取了播放信息，则使用主播直播的播放url开始播放
     */
    void startPlay(SurfaceView playSurf);

    /**
     * 方法描述: 切换摄像头
     */
    void switchCamera();

    /**
     * 方法描述: 切换美颜
     */
    boolean switchBeauty();

    /**
     * 方法描述: 切换闪光灯
     */
    boolean switchFlash();

    /**
     * 方法描述: 请求网络向主播请求连麦，根据网络请求结果改变ChatSession状态为（邀请连麦成功，等待对方响应）或者（未连麦）
     * 接下的步骤是是通过MNS返回是否同意连麦，根据结果在进行接下来的操作
     */
    void asyncInviteChatting(AsyncCallback asyncCallback) throws ChatSessionException;

    /**
     * 方法描述: 真正开始连麦的方法，该方法中先把参数二的数据进行处理下，在正式调用开启连麦的核心方法startLaunchChat
     *
     * @param previewSurface 观众进行推流的SurfaceView
     * @param uidSurfaceMap  其他连麦观众用于播放短延迟的Surface和uid的集合
     */
    void launchChat(SurfaceView previewSurface, Map<String, SurfaceView> uidSurfaceMap);

    /**
     * 方法描述: 结束本观众和主播的连麦，只是结束本观众和主播的连麦，其他观众和主播连麦管不着，先请求网络，告诉业务服务器本观众要断开连麦，请求成功后，调用方法结束本观众的连麦
     */
    void asyncTerminateChatting(AsyncCallback callback);

    /**
     * 方法描述: 结束观看直播。调用该函数将关闭直播播放器，并销毁所有资源。其内部就是调用了 mSDKHelper.stopPlaying();
     */
    void asyncTerminatePlaying(AsyncCallback callback);

    /**
     * 方法描述: 请求网络告诉业务服务器本观众退出直播间了
     */
    void asyncExitRoom(AsyncCallback asyncCallback);
    // --------------------------------------------------------------------------------------------------------
    /**
     * 变量的描述: 区分连麦播放回调错误结果-->播放无效的输入流
     */
    int TYPE_PLAYER_INVALID_INPUTFILE = 0x0033;
    /**
     * 变量的描述: 区分连麦播放回调错误结果-->播放打开失败，流打开失败
     */
    int TYPE_PLAYER_OPEN_FAILED = 0x0034;
    /**
     * 变量的描述: 区分连麦播放回调错误结果-->播放器没有网络连接
     */
    int TYPE_PLAYER_NO_NETWORK = 0x0035;
    /**
     * 变量的描述: 区分连麦播放回调错误结果-->播放器网络超时
     */
    int TYPE_PLAYER_TIMEOUT = 0x0003;
    /**
     * 变量的描述: 区分连麦播放回调错误结果-->播放读取(下载)数据超时
     */
    int TYPE_PLAYER_READ_PACKET_TIMEOUT = 0x0036;
    // --------------------------------------------------------------------------------------------------------
    /**
     * 变量的描述: 区分连麦播放回调错误结果-->播放出错，如果想继续播放需要重启播放器，所以先请退出观看！
     */
    int TYPE_PLAYER_INTERNAL_ERROR = 0x0001;
    /**
     * 变量的描述: 区分连麦播放回调错误结果-->在播放出错时，退出观看的时候，也要结束正在进行中的连麦
     */
    int TYPE_CHATTING_FINISHED = 0x0002;
    /**
     * 变量的描述: 区分连麦播放回调错误结果-->观众进行连麦的时候采集音频出现错误，如果想进行连麦需要重启播放器，所以先请退出观看！
     */
    int TYPE_PUBLISHER_NO_AUDIO_DATA = 0x0026;
    /**
     * 变量的描述: 区分连麦播放回调错误结果-->观众进行连麦的时候采集视频出现错误，如果想进行连麦需要重启播放器，所以先请退出观看！
     */
    int TYPE_PUBLISHER_NO_VIDEO_DATA = 0x0126;
    /**
     * 变量的描述: 区分连麦播放回调错误结果-->音频播放错误,如果想播放主播的直播需要重启播放器，所以先请退出观看！
     */
    int TYPE_PLAYER_AUDIO_PLAYER_ERROR = 0x0038;
    // --------------------------------------------------------------------------------------------------------
    /**
     * 变量的描述: 区分连麦播放回调错误结果-->网络较慢
     */
    int TYPE_PUBLISHER_NETWORK_POOR = 0x0043;
    /**
     * 变量的描述: 区分连麦播放回调错误结果-->网络未连接，观众连麦推流无网络
     */
    int TYPE_PUBLISHER_NETWORK_UNCONNECT = 0x0041;
    /**
     * 变量的描述: 区分连麦播放回调错误结果-->推流网络超时，发送数据超时
     */
    int TYPE_PUBLISHER_NETWORK_TIMEOUT = 0x0042;
    // --------------------------------------------------------------------------------------------------------
    /**
     * 变量的描述: 区分连麦播放回调状态结果-->重连失败
     */
    int TYPE_PUBLISHER_RECONNECT_FAILURE = 0x0040;
    /**
     * 变量的描述: 区分连麦播放回调状态结果-->播放器网络差，不能及时下载数据包
     */
    int TYPE_PLAYER_NETWORK_POOR = 0x0037;
    // --------------------------------------------------------------------------------------------------------
    /**
     * 变量的描述: 当播放界面的连麦出现异常的时候，将异常结果进行回调，本变量就是回调识别
     */
    int TYPE_OPERATION_CALLED_ERROR = 0x0025;
    // --------------------------------------------------------------------------------------------------------
    /**
     * 变量的描述: 存储连麦播放错误码的Key
     */
    String DATA_KEY_PLAYER_ERROR_CODE = "player_error_code";
    /**
     * 变量的描述: 存储连麦播放状态信息码的Key
     */
    String DATA_KEY_PLAYER_INFO_CODE = "player_info_code";
    /**
     * 变量的描述: 播放器网络差时存储了URL的Key
     */
    String DATA_KEY_PLAYER_NETWORK_BAD = "player_info_network_bad";
    /**
     * 变量的描述: 存储连麦播放的异常信息的Key
     */
    String DATA_KEY_PLAYER_ERROR_MSG = "player_error_msg";
    /**
     * 变量的描述: 存储其他连麦观众的UID的KEY
     */
    String DATA_KEY_INVITEE_UID_LIST = "invitee_uid";
    /**
     * 变量的描述: 存储退出连麦的其他连麦观众的UID的KEY
     */
    String DATA_KEY_INVITEE_UID = "invitee_uid";    //被邀请者的UID
    // --------------------------------------------------------------------------------------------------------
    /**
     * 变量的描述: 通过MNS得知主播同意连麦后处理完连麦所需的数据，或者本观众自己同意连麦后处理完连麦所需的数据，这两种情况下，进行回调开始正式连麦
     */
    int TYPE_START_CHATTING = 0x0004;
    /**
     * 变量的描述: 自己退出连麦（或者时被主播踢出） 这时进行回调，让Presenter去调用UI更新(隐藏连麦界面)
     */
    int TYPE_SELF_EXIT_CHATTING = 0x0007;
    /**
     * 变量的描述: 其他连麦观众退出连麦后，调用SDK移除连麦成功， 这时进行回调，让Presenter去调用UI更新(隐藏对应的连麦界面)
     */
    int TYPE_OTHER_PEOPLE_EXIT_CHATTING = 0x0006;
    /**
     * 变量的描述: 主播结束所有的连麦。作为连麦的其中一员，需要做出相应的反应，结束所有连麦(其实质也就是本观众退出连麦，销毁所有连麦资源)，
     */
    int TYPE_PUBLISHER_TERMINATE_CHATTING = 0x0008;
    /**
     * 变量的描述: 主播要结束直播了，作为其中的一名观众，需要做出相应的反应-->弹出直播结束的界面
     */
    int TYPE_LIVE_CLOSE = 0x0009;
    /**
     * 变量的描述: 当本观众与主播连麦成功后，又有其他人加入连麦，进行回调的时候已经处理完连麦所需的数据。所以正式开启连麦(调用连麦的核心SDK)
     */
    int TYPE_OTHER_PEOPLE_JOIN_IN_CHATTING = 0x0005;
    // --------------------------------------------------------------------------------------------------------
    /**
     * 变量的描述: ChatSessionCallback的方法调用ManagerCallback进行回调的区分标识--->混流成功
     */
    int TYPE_MIX_STREAM_SUCCESS = 0x0027;
    /**
     * 变量的描述: ChatSessionCallback的方法调用ManagerCallback进行回调的区分标识--->混流失败，混流(连麦观众流)不存在
     */
    int TYPE_MIX_STREAM_NOT_EXIST = 0x0028;
    /**
     * 变量的描述: ChatSessionCallback的方法调用ManagerCallback进行回调的区分标识--->混流失败，主播流不存在
     */
    int TYPE_MAIN_STREAM_NOT_EXIST = 0x0029;
    /**
     * 变量的描述: ChatSessionCallback的方法调用ManagerCallback进行回调的区分标识--->连麦混流超时了
     */
    int TYPE_MIX_STREAM_TIMEOUT = 0x0030;
    /**
     * 变量的描述: ChatSessionCallback的方法调用ManagerCallback进行回调的区分标识--->混流出现异常
     */
    int TYPE_MIX_STREAM_ERROR = 0x0031;
    /**
     * 变量的描述: ChatSessionCallback的方法调用ManagerCallback进行回调的区分标识--->邀请主播进行连麦超时了
     */
    int TYPE_INVITE_CHAT_TIMEOUT = 0x0032;
    // --------------------------------------------------------------------------------------------------------
    // 没实际意义
    /**
     * 变量的描述: 区分连麦播放回调状态结果-->播放首帧渲染成功
     */
    int TYPE_PLAYER_FIRST_FRAME_RENDER_SUCCESS = 0x0010;
    /**
     * 变量的描述: 存储主播ID的KEY，但是没什么用，只有存储，没有取出
     */
    String DATA_KEY_INVITER_UID = "inviter_uid";    //邀请者的UID
    /**
     * 变量的描述: 是在核心SDK被调用的时候有几个回调，但是无意义，回调没有具体的作用
     */
    int TYPE_PARTER_OPT_START = 0x0021;
    /**
     * 变量的描述: 无意义，回调没有具体的作用
     */
    int TYPE_PARTER_OPT_END = 0x0022;
    /**
     * 变量的描述: 无意义，回调没有被调用这个标识
     */
    int TYPE_PUBLISHER_FIRST_FRAME_RENDER_SUCCESS = 0x0011; //连麦推流预览首帧渲染成功
    /**
     * 变量的描述: 无意义，回调没有被调用这个标识
     */
    int TYPE_PARTER_OPT_TIMEOUT = 0x0023;
}
