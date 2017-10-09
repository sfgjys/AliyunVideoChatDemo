package com.alivc.videochat.demo.logic;

import android.view.SurfaceView;

import com.alivc.videochat.demo.base.AsyncCallback;

import java.util.List;

public interface IPublisherManager {
    /**
     * 方法描述: 开始预览
     */
    void asyncStartPreview(SurfaceView holder, AsyncCallback callback);

    /**
     * 方法描述: 创建直播，其意思是请求服务器获取直播的推流地址 请求网络获取推流地址，如此asyncStartPreview方法开启的预览才能通过推流地址直播出去
     */
    void asyncCreateLive(String desc, AsyncCallback callback);

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

    // --------------------------------------------------------------------------------------------------------

    /**
     * 方法描述: 当用户点击连麦时，请求业务服务器，发送邀请连麦的请求
     */
    void asyncInviteChatting(List<String> inviteeUIDs, AsyncCallback callback);

    /**
     * 方法描述: 开始连麦 正式的开启连麦，因为时主播界面的连麦，所以其内部主要是播放连麦人的推流视频
     */
    void launchChat(SurfaceView parterView, String playerUID);

    /**
     * 方法描述: 结束连麦(某一个观众) 结束指定uid的连麦
     */
    void asyncTerminateChatting(String playerUID, AsyncCallback callback);

    /**
     * 方法描述: 结束连麦（所有观众） 在直播界面被销毁时，如果还有正在连麦的观众，那么就结束所有连麦
     */
    void asyncTerminateAllChatting(AsyncCallback callback);

    /**
     * 方法描述: 结束直播 请求网络，告诉业务服务器直播将要被关闭
     */
    void asyncCloseLive(AsyncCallback callback);

    // **************************************************** 下面的时连麦推流状态信息的范畴 ****************************************************
    /**
     * 变量的描述: 区分连麦推流回调状态结果-->一般时从差的网络恢复到网络较好，推流器网络状况良好
     */
    int TYPE_PUBLISHER_NETWORK_GOOD = 0x0005;
    /**
     * 变量的描述: 区分连麦推流回调状态结果-->推流器重连失败
     */
    int TYPE_PUBLISHER_RECONNECT_FAILURE = 0x0006;
    /**
     * 变量的描述: 区分连麦推流回调状态结果-->连麦观众的播放有网络延迟
     */
    int TYPE_PLAYER_NETWORK_POOR = 0x0105;
    // **************************************************** 下面的时连麦推流错误的范畴 ****************************************************
    /**
     * 变量的描述: 区分连麦推流回调错误结果-->连麦播放无效的输入流
     */
    int TYPE_PLAYER_INVALID_INPUTFILE = 0x0033;
    /**
     * 变量的描述: 区分连麦推流回调错误结果-->连麦播放打开失败
     */
    int TYPE_PLAYER_OPEN_FAILED = 0x0034;
    /**
     * 变量的描述: 区分连麦推流回调错误结果-->连麦播放没有网络
     */
    int TYPE_PLAYER_NO_NETWORK = 0x0102;
    /**
     * 变量的描述: 区分连麦推流回调错误结果-->连麦播放器超时，需要重连
     */
    int TYPE_PLAYER_TIMEOUT = 0x0103;
    /**
     * 变量的描述: 区分连麦推流回调错误结果-->连麦播放读取数据超时
     */
    int TYPE_PLAYER_READ_PACKET_TIMEOUT = 0x0035;
    // --------------------------------------------------------------------------------------------------------
    /**
     * 变量的描述: 区分连麦推流回调错误结果-->连麦播放出错(播放无足够内存,播放不支持的解码格式,播放没有设置显示窗口)，
     * 如果想要继续播放，需要重启连麦播放器(推流器),其实就是弹出个对话框显示错误，点击确定时会关闭直播界面，让用户重新进入
     */
    int TYPE_PLAYER_INTERNAL_ERROR = 0x0101;
    /**
     * 变量的描述: 区分连麦推流回调错误结果-->音频采集失败，需要停止推流,其实就是弹出个对话框显示错误，点击确定时会关闭直播界面，让用户重新进入
     */
    int TYPE_PUBLISHER_AUDIO_CAPTURE_FAILURE = 0x0002;
    /**
     * 变量的描述: 区分连麦推流回调错误结果-->摄像头采集失败，摄像头开启失败 视频被禁止，
     * 需要停止推流,其实就是弹出个对话框显示错误，点击确定时会关闭直播界面，让用户重新进入
     */
    int TYPE_PUBLISHER_VIDEO_CAPTURE_FAILURE = 0x0003;
    /**
     * 变量的描述: 区分连麦推流回调错误结果-->网络未链接，推流无网络，需要停止推流,其实就是弹出个对话框显示错误，点击确定时会关闭直播界面，让用户重新进入
     */
    int TYPE_PUBLISHER_NETWORK_UNCONNECT = 0x0007;
    /**
     * 变量的描述: 区分连麦推流回调错误结果-->发送数据超时，推流网络超时，需要停止推流,其实就是弹出个对话框显示错误，点击确定时会关闭直播界面，让用户重新进入
     */
    int TYPE_PUBLISHER_NETWORK_TIMEOUT = 0x0008;
    /**
     * 变量的描述: 区分连麦推流回调错误结果-->音频播放错误，需要停止推流,其实就是弹出个对话框显示错误，点击确定时会关闭直播界面，让用户重新进入
     */
    int TYPE_PLAYER_AUDIO_PLAYER_ERROR = 0X0106;
    /**
     * 变量的描述: 区分连麦推流回调错误结果-->推流器内部错误，如果向继续推流，需要重启推流器
     */
    int TYPE_PUBLISHER_INTERNAL_ERROR = 0x0004;
    // --------------------------------------------------------------------------------------------------------
    /**
     * 变量的描述: 区分连麦推流回调错误结果-->网络较差
     */
    int TYPE_PUBLISHER_NETWORK_POOR = 0x0001;
    // --------------------------------------------------------------------------------------------------------
    /**
     * 变量的描述: 区分回调结果-->获取直播推流地址成功
     */
    int TYPE_LIVE_CREATED = 0x0013;
    /**
     * 变量的描述: 区分回调结果-->在进行连麦的时候出现操作错误
     */
    int TYPE_OPERATION_CALLED_ERROR = 0x0104;
    /**
     * 变量的描述: 区分回调结果-->某个连麦观众推流成功，服务端获取了推流对应的播放地址，并通过MNS发送给了主播
     */
    int TYPE_PUBLISH_STREMA_SUCCESS = 0x0011;
    /**
     * 变量的描述: 区分回调结果-->某人退出连麦
     */
    int TYPE_SOMEONE_EXIT_CHATTING = 0x0015;
    /**
     * 变量的描述: 没人用
     */
    int TYPE_RECEIVED_CHAT_INVITING = 0x0012;           //收到连麦邀请
    // --------------------------------------------------------------------------------------------------------
    /**
     * 变量的描述: 区分回调结果-->处理对方连麦响应超时，主播收到观众的连麦邀请，但是一直没有处理，超时了
     */
    int TYPE_PROCESS_INVITING_TIMEOUT = 0x0010;         //
    /**
     * 变量的描述: 区分回调结果-->邀请观众进行连麦，但是观众一直没有进行响应，超时了
     */
    int TYPE_INVITE_TIMEOUT = 0x0021;                   //连麦对方响应超时
    /**
     * 变量的描述: 区分回调结果-->连麦混流错误(超时、CDN internal error code)
     */
    int TYPE_MIX_STREAM_ERROR = 0x0022;                 //混流错误
    /**
     * 变量的描述: 区分回调结果-->混流成功
     */
    int TYPE_MIX_STREAM_SUCCESS = 0x0027;
    /**
     * 变量的描述: 区分回调结果-->混流失败，混流(观众流)不存在
     */
    int TYPE_MIX_STREAM_NOT_EXIST = 0x0028;
    /**
     * 变量的描述: 区分回调结果-->混流失败，主播流不存在
     */
    int TYPE_MAIN_STREAM_NOT_EXIST = 0x0029;
    /**
     * 变量的描述: 区分回调结果-->混流超时，但是暂时没什么用
     */
    int TYPE_MIX_STREAM_TIMEOUT = 0x0030;
    // --------------------------------------------------------------------------------------------------------
    /**
     * 变量的描述: 存储连麦推流状态码的Key
     */
    String DATA_KEY_PUBLISHER_INFO_CODE = "publisher_info_code";
    /**
     * 变量的描述: 存储连麦推流错误码的Key
     */
    String DATA_KEY_PUBLISHER_ERROR_CODE = "publisher_error_code";
    /**
     * 变量的描述: 存储 连麦时出现的操作错误 的Key
     */
    String DATA_KEY_CHATTING_ERROR_MSG = "chatting_error_msg";
    /**
     * 变量的描述: 存储连麦推流状态的播放地址的Key
     */
    String DATA_KEY_PLAYER_URL = "player_url";
    /**
     * 变量的描述: 存储 请求网络获取推流地址信息结果对象 的Key
     */
    String DATA_KEY_CREATE_LIVE_RESULT = "create_live_result";
    /**
     * 变量的描述: 存储 连麦推流成功的观众的uid 的Key
     */
    String DATA_KEY_INVITEE_UID = "invitee_uid";        //被邀请人的UID
    /**
     * 变量的描述: 存储 某个退出连麦的观众的uid 的Key
     */
    String DATA_KEY_PLAYER_UID = "player_uid";          //观众的UID
    /**
     * 变量的描述: 没人用
     */
    String DATA_KEY_PUBLISH_STREAM_SUCCESS_INFO = "publish_stream_success_info";
}
