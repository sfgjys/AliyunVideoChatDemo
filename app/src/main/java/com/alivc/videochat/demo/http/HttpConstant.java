package com.alivc.videochat.demo.http;

public class HttpConstant {
    /**
     * 变量的描述: 网络请求成功的Code
     */
    public static final int HTTP_OK_CODE = 200;
    /**
     * 变量的描述: 网络请求的网址的基础地址
     */
    public static final String HTTP_BASE_URL = "http://118.31.79.239:4000";

    // **************************************************** 登录网络请求 ****************************************************
    /**
     * 变量的描述: 登录网络请求---URL 返回的json数据:{"code":200,"message":"成功","data":{"id":"1","name":"est"}}
     */
    public static final String URL_LOGIN = "login";
    /**
     * 变量的描述: 登录返回的字段---登录人的id
     */
    public static final String KEY_ID = "id";
    /**
     * 变量的描述: 登录返回的字段,也是请求网络时的参数字段---登录人的名字
     */
    public static final String KEY_NAME = "name";
    // --------------------------------------------------------------------------------------------------------
    // **************************************************** 发送评论点赞网络请求 ****************************************************
    /**
     * 变量的描述: 发送观众写的评论给业务服务器---URL 返回的json的数据:{"code":200,"message":"成功"} 只返回了HttpResponse的内容，其子对象不需要，只要知道是否发送成功
     */
    public static final String URL_SEND_COMMEND = "live/comment";
    /**
     * 变量的描述: 请求网络时的参数字段---观众进行评论的内容
     */
    public static final String KEY_COMMENT = "comment";
    /**
     * 变量的描述: 将观众的点赞发送给业务服务器--URL 返回的json的数据:{"code":200,"message":"成功"} 只返回了HttpResponse的内容，其子对象不需要，只要知道是否发送成功
     */
    public static final String URL_SEND_LIKE = "live/like";
    // --------------------------------------------------------------------------------------------------------
    // **************************************************** 请求网络获取直播推流地址 ****************************************************
    /**
     * 变量的描述: 向业务服务器请求获取推流地址(创建直播)--URL 返回的json的数据: {"code":"200","message":"成功","data":{"uid":"1","name":"你好","roomId":"474c7658805b798814","rtmpUrl":"rtmp://videocall.push.danqoo.com/DemoApp/474c7658805b798814?auth_key=1475044685-0-0-3bef3969e641856d785dbeeb23f188a9","playUrl":"http://videocall.play.danqoo.com/DemoApp/474c7658805b798814_mix.flv","m3u8PlayUrl":"http://videocall.play.danqoo.com/DemoApp/474c7658805b798814_mix.m3u8","rtmpPlayUrl":"rtmp://videocall.play.danqoo.com/DemoApp/474c7658805b798814_mix","status":"1 --＃0:创建还未推流  1:在推流，2: 直播结束，  10: 连麦时系统创建的直播(主要是观众连麦时)","type":"2  --＃1:观众  2:主播","isMixReady":false,"isMixed":false,"mns":{"topic":"474c7658805b798814","topicLocation":"http://125277.mns.cn-hangzhou.aliyuncs.com/topics/229820386403942828","roomTag":"474c7658805b798814","userRoomTag":"474c7658805b798814_1  --#用于客户端过滤订阅消息给主播"},"description":"test"}}
     */
    public static final String URL_CREATE_LIVE = "live/create";
    /**
     * 变量的描述: 请求网络时的参数字段---创建直播获取推流地址的直播间标题，描述
     */
    public static final String KEY_DESC = "description";
    // --------------------------------------------------------------------------------------------------------
    // **************************************************** 请求网络获取MNS链接到服务器的所需要各种的参数 ****************************************************
    /**
     * 变量的描述: 请求网络获取MNS链接到服务器的所需要各种的参数，返回json数据:{"code":"200","message":"成功","data":{"authentication":"Fa91Q+YDqsa7CQOMHyYXE7OFw=","topicWebsocketServerAddress":"ws://125277.mns-websocket.cn-shanghai.aliyuncs.com/mns","accountId":"12277","accessId":"Q1dfW3pJSOJf6"}}
     */
    public static final String URL_WEBSOCKET_INFO = "mns/topic/websocket/info";
    /**
     * 变量的描述: 请求网络时的参数字段---订阅名字 默认和topic名字一样
     */
    public static final String KEY_SUBSCRIPTION_NAME = "subscriptionName";
    /**
     * 变量的描述: 请求网络时的参数字段---主题
     */
    public static final String KEY_TOPIC = "topic";
    /**
     * 变量的描述: 请求网络返回的json数据中的字段
     */
    public static final String KEY_AUTHENTICATION = "authentication";
    public static final String KEY_WS_SERVER_ADDRESS = "topicWebsocketServerAddress";
    public static final String KEY_ACCOUNT_ID = "accountId";
    public static final String KEY_ACCESS_ID = "accessId";
    public static final String KEY_DATE = "date";
    // --------------------------------------------------------------------------------------------------------
    // **************************************************** 请求网络获取播放地址 ****************************************************
    public static final String URL_WATCH_LIVE = "live/play";
    // --------------------------------------------------------------------------------------------------------
    // **************************************************** 请求网络，告诉业务服务器直播将要被关闭 ****************************************************
    public static final String URL_CLOSE_LIVE = "live/leave";
    // --------------------------------------------------------------------------------------------------------
    // **************************************************** 请求网络获取正在直播的列表 ****************************************************
    public static final String URL_LIST_LIVE = "live/list";
    // --------------------------------------------------------------------------------------------------------
    // **************************************************** 请求网络告诉业务服务器本观众退出直播间了 ****************************************************
    public static final String URL_EXIT_WATCHING = "live/audience/leave";
    // --------------------------------------------------------------------------------------------------------
    // **************************************************** 获取某个直播间的观众列表 ****************************************************
    public static final String URL_WATCHER_LIST = "im/room/users";
    // --------------------------------------------------------------------------------------------------------
    // **************************************************** 向服务器发送邀请用户进行连麦的请求,只是邀请并不是正式连麦 ****************************************************
    public static final String URL_INVITE_VIDEO = "videocall/invite";
    /**
     * 变量的描述: 请求网络时的参数字段 --- 被邀请连麦ID
     */
    public static final String KEY_INVITEE_UID_LIST = "inviteeUids";
    /**
     * 变量的描述: 请求网络时的参数字段 --- 直播间ID
     */
    public static final String KEY_LIVE_ROOM_ID = "liveRoomId";
    // --------------------------------------------------------------------------------------------------------
    // **************************************************** 发送被邀请人回答是否进行连麦 ****************************************************
    public static final String URL_INVITE_FEEDBACK = "videocall/feedback";
    /**
     * 变量的描述: 请求网络时的参数字段 --- 被邀请人是否为观众 #1是观众 2 主播
     */
    public static final String KEY_INVITEE_TYPE = "inviteeType";
    /**
     * 变量的描述: 网络请求返回大参数字段 --- 其他连麦观众的短延迟播放地址
     */
    public static final String KEY_URL = "url";
    // --------------------------------------------------------------------------------------------------------
    // **************************************************** 主播断开所有正在进行连麦 ****************************************************
    public static final String URL_CLOSE_VIDEO_CALL = "videocall/close";
    // --------------------------------------------------------------------------------------------------------
    // **************************************************** 某人准备退出连麦了（或者被强制退出）告诉下业务服务器 ****************************************************
    public static final String URL_LEAVE_VIDEO_CALL = "videocall/leave";
    // --------------------------------------------------------------------------------------------------------
    // **************************************************** 共用的字段 ****************************************************
    /**
     * 变量的描述: 请求网络时的参数字段 --- 邀请别人进行连麦，其中进行邀请的人的ID
     */
    public static final String KEY_INVITER_UID = "inviterUid";
    /**
     * 变量的描述: 请求网络时的参数字段 --- 邀请者是否为观众 #1是观众 2 主播
     */
    public static final String KEY_INVITER_TYPE = "inviterType";
    /**
     * 变量的描述: 请求网络时的参数字段 --- 并排还是画中画的类型 ---------------- 2, ＃1:观众  2:主播
     */
    public static final String KEY_TYPE = "type";
    /**
     * 变量的描述: 请求网络时的参数字段 --- 被邀请连麦ID
     */
    public static final String KEY_INVITEE_UID = "inviteeUid";
    /**
     * 变量的描述: 请求网络时的参数字段 --- 是否同意进行连麦  #1同意 2 不同意 1  -----  ＃0:创建还未推流  1:在推流，2: 直播结束，  10: 连麦时系统创建的直播（主要是观众连麦时）
     */
    public static final String KEY_STATUS = "status";
    /**
     * 变量的描述: 请求网络时的参数字段 --- 主播推流成功后的主流播放地址
     */
    public static final String KEY_MAIN_PLAY_URL = "mainPlayUrl";
    /**
     * 变量的描述: 请求网络时的参数字段 --- 观众进行连麦时的副流推流地址
     */
    public static final String KEY_RTMP_URL = "rtmpUrl";
    /**
     * 变量的描述: 包含其他连麦观众的副流短延时播放地址
     */
    public static final String KEY_PLAY_URLS = "playUrls";
    /**
     * 变量的描述: 请求网络时的参数字段---观众或者主播所在房间id
     */
    public static final String KEY_ROOM_ID = "roomId";
    /**
     * 变量的描述: 请求网络时的参数字段---登录的用户的id
     */
    public static final String KEY_UID = "uid";
    /**
     * 变量的描述: 请求网络时的参数字段---包含了MNS链接所需要的参数
     */
    public static final String KEY_MNS = "mns";
    /**
     * 变量的描述: 请求网络时的参数字段---混流是否准备好了
     */
    public static final String KEY_IS_MIX_READY = "isMixReady";
    /**
     * 变量的描述: 请求网络时的参数字段---是否已经混流成功
     */
    public static final String KEY_IS_MIXED = "isMixed";
    /**
     * 变量的描述: 请求网络时的参数字段---推流地址对应的flv格式的播放地址
     */
    public static final String KEY_PLAY_URL = "playUrl";
    /**
     * 变量的描述: 请求网络时的参数字段---推流地址对应的rtmp格式的播放地址
     */
    public static final String KEY_RTMP_PLAY_URL = "rtmpPlayUrl";
    /**
     * 变量的描述: 请求网络时的参数字段---推流地址对应的m3u8格式的播放地址
     */
    public static final String KEY_M3U8_PLAY_URL = "m3u8PlayUrl";

    // MNS中使用的
    public static final String KEY_INVITER_NAME = "inviterName";
    public static final String KEY_INVITEE_NAME = "inviteeName";
    public static final String KEY_INVITEE_ROOM_ID = "inviteeRoomId";
    public static final String KEY_INVITER_ROOM_ID = "inviterRoomId";
    public static final String KEY_TOPIC_LOCATION = "topicLocation";
    public static final String KEY_ROOM_TAG = "roomTag";
    public static final String KEY_USER_ROOM_TAG = "userRoomTag";
    public static final String KEY_MIX_UID = "mixUid";
    public static final String KEY_MAIN_ROOM_ID = "mainRoomId";
    public static final String KEY_MAIN_MIX_ROOM_ID = "mainMixRoomId";                          //主流房间ID
    public static final String KEY_MIX_ROOM_ID = "mixRoomId";                                   //副流房间ID
    public static final String KEY_MIX_TYPE = "mixType";                                        //混流类型
    public static final String KEY_MIX_TEMPLATE = "mixTemplate";                                //混流模板
    public static final String KEY_MIX_MESSAGE = "message";                                     //错误描述，成功时为空
    public static final String KEY_MIX_CODE = "code";                                           //错误码

    // 没人用
    public static final String KEY_CLOSE_ROOM_ID = "closeRoomId";
    public static final String KEY_NOTIFIED_ROOM_ID = "notifiedRoomId";
    public static final String KEY_NOTIFIED_NAME = "notifiedName";
    public static final String KEY_NOTIFIED_UID = "notifiedUid";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_INVITEE_PLAY_URL = "inviteePlayUrl";
    public static final String KEY_INVITER_PLAY_URL = "inviterPlayUrl";
    public static final String KEY_WS_SERVER_IP = "topicWebsocketServerIp";
    public static final String KEY_OTHER_PLAY_URLS = "otherPlayUrls";
    public static final int ERR_NO_PRIMARY_STREAM_PUBLISH = 3404;
}
