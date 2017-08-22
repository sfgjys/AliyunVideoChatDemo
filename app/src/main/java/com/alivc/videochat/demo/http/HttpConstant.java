package com.alivc.videochat.demo.http;

/**
 * Created by liujianghao on 16-7-28.
 */
public class HttpConstant {
    public static final int HTTP_OK = 200;

    public static final String HTTP_BASE_URL = "http://116.62.236.244:4000";

    public static final String URL_CREATE_LIVE = "live/create";
    public static final String URL_WATCH_LIVE = "live/play";
    public static final String URL_CLOSE_LIVE = "live/leave";
    public static final String URL_LIST_LIVE = "live/list";
    public static final String URL_EXIT_WATCHING = "live/audience/leave";
    public static final String URL_WATCHER_LIST = "im/room/users";

    public static final String URL_INVITE_VIDEO = "videocall/invite";
    public static final String URL_INVITE_FEEDBACK = "videocall/feedback";
    public static final String URL_CLOSE_VIDEO_CALL = "videocall/close";
    public static final String URL_LEAVE_VIDEO_CALL = "videocall/leave";

    public static final String URL_LOGIN = "login";

    public static final String URL_SEND_COMMEND = "live/comment";
    public static final String URL_SEND_LIKE = "live/like";

    public static final String URL_WEBSOCKET_INFO = "mns/topic/websocket/info";

    public static final String KEY_ROOM_ID = "roomId";
    public static final String KEY_NAME = "name";
    public static final String KEY_UID = "uid";
    public static final String KEY_INVITER_UID = "inviterUid";
    public static final String KEY_INVITER_NAME = "inviterName";
    public static final String KEY_INVITEE_UID = "inviteeUid";
    public static final String KEY_INVITEE_UID_LIST = "inviteeUids";
    public static final String KEY_INVITEE_NAME = "inviteeName";
    public static final String KEY_CLOSE_ROOM_ID = "closeRoomId";
    public static final String KEY_NOTIFIED_ROOM_ID = "notifiedRoomId";
    public static final String KEY_NOTIFIED_NAME = "notifiedName";
    public static final String KEY_NOTIFIED_UID = "notifiedUid";
    public static final String KEY_COMMENT = "comment";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_DESC = "description";
    public static final String KEY_ID = "id";
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
