package com.alivc.videochat.demo.http;

public class HttpConstant {
    /**
     * 变量的描述: 网络请求成功的Code
     */
    public static final int HTTP_OK_CODE = 200;
    /**
     * 变量的描述: 网络请求的网址的基础地址
     */
    public static final String HTTP_BASE_URL = "http://116.62.236.244:4000";

    // --------------------------------------------------------------------------------------------------------
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
    /**
     * 变量的描述: 发送观众写的评论给业务服务器---URL 返回的json的数据:{"code":200,"message":"成功"} 只返回了HttpResponse的内容，其子对象不需要，只要知道是否发送成功
     */
    public static final String URL_SEND_COMMEND = "live/comment";
    /**
     * 变量的描述: 请求网络时的参数字段---观众进行评论的内容
     */
    public static final String KEY_COMMENT = "comment";
    // 评论和点赞共用的请求网络时的参数字段
    /**
     * 变量的描述: 请求网络时的参数字段---评论点赞的观众的id，创建直播获取推流地址的用户的id
     */
    public static final String KEY_UID = "uid";
    /**
     * 变量的描述: 请求网络时的参数字段---评论点赞的观众所在房间id
     */
    public static final String KEY_ROOM_ID = "roomId";
    // 评论和点赞共用的请求网络时的参数字段
    /**
     * 变量的描述: 将观众的点赞发送给业务服务器--URL 返回的json的数据:{"code":200,"message":"成功"} 只返回了HttpResponse的内容，其子对象不需要，只要知道是否发送成功
     */
    public static final String URL_SEND_LIKE = "live/like";
    // --------------------------------------------------------------------------------------------------------
    /**
     * 变量的描述: 向业务服务器请求获取推流地址(创建直播)--URL 返回的json的数据: {"code":"200","message":"成功","data":{"uid":"1","name":"你好","roomId":"474c7658805b798814","rtmpUrl":"rtmp://videocall.push.danqoo.com/DemoApp/474c7658805b798814?auth_key=1475044685-0-0-3bef3969e641856d785dbeeb23f188a9","playUrl":"http://videocall.play.danqoo.com/DemoApp/474c7658805b798814_mix.flv","m3u8PlayUrl":"http://videocall.play.danqoo.com/DemoApp/474c7658805b798814_mix.m3u8","rtmpPlayUrl":"rtmp://videocall.play.danqoo.com/DemoApp/474c7658805b798814_mix","status":"1 --＃0:创建还未推流  1:在推流，2: 直播结束，  10: 连麦时系统创建的直播(主要是观众连麦时)","type":"2  --＃1:观众  2:主播","isMixReady":false,"isMixed":false,"mns":{"topic":"474c7658805b798814","topicLocation":"http://125277.mns.cn-hangzhou.aliyuncs.com/topics/229820386403942828","roomTag":"474c7658805b798814","userRoomTag":"474c7658805b798814_1  --#用于客户端过滤订阅消息给主播"},"description":"test"}}
     */
    public static final String URL_CREATE_LIVE = "live/create";
    /**
     * 变量的描述: 请求网络时的参数字段---创建直播获取推流地址的直播间标题，描述
     */
    public static final String KEY_DESC = "description";
    // --------------------------------------------------------------------------------------------------------


    public static final String URL_WATCH_LIVE = "live/play";
    public static final String URL_CLOSE_LIVE = "live/leave";
    public static final String URL_LIST_LIVE = "live/list";
    public static final String URL_EXIT_WATCHING = "live/audience/leave";
    public static final String URL_WATCHER_LIST = "im/room/users";
    public static final String URL_INVITE_VIDEO = "videocall/invite";
    public static final String URL_INVITE_FEEDBACK = "videocall/feedback";
    public static final String URL_CLOSE_VIDEO_CALL = "videocall/close";
    public static final String URL_LEAVE_VIDEO_CALL = "videocall/leave";

    public static final String URL_WEBSOCKET_INFO = "mns/topic/websocket/info";


    public static final String KEY_INVITER_UID = "inviterUid";
    public static final String KEY_INVITER_NAME = "inviterName";
    public static final String KEY_INVITEE_UID = "inviteeUid";
    public static final String KEY_INVITEE_UID_LIST = "inviteeUids";
    public static final String KEY_INVITEE_NAME = "inviteeName";
    public static final String KEY_CLOSE_ROOM_ID = "closeRoomId";
    public static final String KEY_NOTIFIED_ROOM_ID = "notifiedRoomId";
    public static final String KEY_NOTIFIED_NAME = "notifiedName";
    public static final String KEY_NOTIFIED_UID = "notifiedUid";
    public static final String KEY_PASSWORD = "password";

    public static final String KEY_TYPE = "type";
    public static final String KEY_STATUS = "status";
    public static final String KEY_INVITEE_PLAY_URL = "inviteePlayUrl";
    public static final String KEY_RTMP_URL = "rtmpUrl";
    public static final String KEY_INVITEE_ROOM_ID = "inviteeRoomId";
    public static final String KEY_INVITER_ROOM_ID = "inviterRoomId";
    public static final String KEY_INVITER_TYPE = "inviterType";
    public static final String KEY_INVITEE_TYPE = "inviteeType";
    public static final String KEY_INVITER_PLAY_URL = "inviterPlayUrl";
    public static final String KEY_TOPIC = "topic";
    public static final String KEY_TOPIC_LOCATION = "topicLocation";
    public static final String KEY_ROOM_TAG = "roomTag";
    public static final String KEY_USER_ROOM_TAG = "userRoomTag";
    public static final String KEY_IS_MIX_READY = "isMixReady";
    public static final String KEY_IS_MIXED = "isMixed";
    public static final String KEY_MNS = "mns";
    public static final String KEY_PLAY_URL = "playUrl";
    public static final String KEY_MIX_UID = "mixUid";
    public static final String KEY_PLAY_URLS = "playUrls";
    public static final String KEY_RTMP_PLAY_URL = "rtmpPlayUrl";
    public static final String KEY_M3U8_PLAY_URL = "m3u8PlayUrl";
    public static final String KEY_AUTHENTICATION = "authentication";
    public static final String KEY_WS_SERVER_IP = "topicWebsocketServerIp";
    public static final String KEY_WS_SERVER_ADDRESS = "topicWebsocketServerAddress";
    public static final String KEY_ACCOUNT_ID = "accountId";
    public static final String KEY_ACCESS_ID = "accessId";
    public static final String KEY_SUBSCRIPTION_NAME = "subscriptionName";
    public static final String KEY_DATE = "date";
    public static final String KEY_LIVE_ROOM_ID = "liveRoomId";
    public static final String KEY_URL = "url";

    public static final String KEY_MAIN_ROOM_ID = "mainRoomId";
    public static final String KEY_MAIN_MIX_ROOM_ID = "mainMixRoomId";                          //主流房间ID
    public static final String KEY_MIX_ROOM_ID = "mixRoomId";                                   //副流房间ID
    public static final String KEY_MIX_TYPE = "mixType";                                        //混流类型
    public static final String KEY_MIX_TEMPLATE = "mixTemplate";                                //混流模板
    public static final String KEY_MIX_MESSAGE = "message";                                     //错误描述，成功时为空
    public static final String KEY_MIX_CODE = "code";                                           //错误码

    public static final String KEY_MAIN_PLAY_URL = "mainPlayUrl";
    public static final String KEY_OTHER_PLAY_URLS = "otherPlayUrls";

    public static final int ERR_NO_PRIMARY_STREAM_PUBLISH = 3404;
}
