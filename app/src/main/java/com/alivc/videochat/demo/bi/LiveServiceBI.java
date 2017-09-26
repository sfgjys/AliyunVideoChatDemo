package com.alivc.videochat.demo.bi;

import com.alivc.videochat.demo.exception.APIException;
import com.alivc.videochat.demo.http.HttpConstant;
import com.alivc.videochat.demo.http.form.CloseLiveForm;
import com.alivc.videochat.demo.http.form.CreateLiveForm;
import com.alivc.videochat.demo.http.form.ExitWatchingForm;
import com.alivc.videochat.demo.http.form.MNSConnectionInfoForm;
import com.alivc.videochat.demo.http.form.WatchLiveForm;
import com.alivc.videochat.demo.http.form.WatcherListForm;
import com.alivc.videochat.demo.http.result.HttpResponse;
import com.alivc.videochat.demo.http.result.LiveCreateResult;
import com.alivc.videochat.demo.http.result.LiveItemResult;
import com.alivc.videochat.demo.http.result.MNSConnectModel;
import com.alivc.videochat.demo.http.result.WatchLiveResult;
import com.alivc.videochat.demo.http.result.WatcherResult;
import com.alivc.videochat.demo.http.service.NetworkServiceFactory;

import java.util.List;

import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;

public class LiveServiceBI extends ServiceBI {

    /**
     * 方法描述: 请求网络获取推流地址
     *
     * @param uid      登录成功时返回的信息
     * @param desc     开启请求网络时填写的对这个推流地址的描述直播标题
     * @param callback 请求结果的回调接口实例
     */
    public Call createLive(String uid, String desc, final Callback<LiveCreateResult> callback) {
        // 获取请求推流地址的Call任务
        CreateLiveForm liveForm = new CreateLiveForm(uid, desc);
        Call<HttpResponse<LiveCreateResult>> createLiveCall;
        createLiveCall = NetworkServiceFactory.getLiveService().createLive(liveForm);

        // 用于存储网络请求结果的数组
        final LiveCreateResult[] fResult = new LiveCreateResult[1];

        Request request = createLiveCall.request();
        RequestBody body = request.body();
        HttpUrl url = request.url();

        // 使用Call任务开启请求
        createLiveCall.enqueue(new retrofit2.Callback<HttpResponse<LiveCreateResult>>() {
            @Override
            public void onResponse(final Call<HttpResponse<LiveCreateResult>> call, Response<HttpResponse<LiveCreateResult>> response) {

                int code = response.body().getCode();
                if (code == HttpConstant.HTTP_OK_CODE) {
                    MNSConnectionInfoForm form = new MNSConnectionInfoForm(response.body().getData().getMNSModel().getTopic(), null);
                    // 存储网络请求结果
                    fResult[0] = response.body().getData();

                    // 阿里MNS管理 获取MNS链接所需要的参数
                    Call<HttpResponse<MNSConnectModel>> mnsCall = NetworkServiceFactory.getAccountService().getMnsConnectionInfo(form);

                    processObservable(mnsCall, new ServiceBI.Callback<MNSConnectModel>() {
                        @Override
                        public void onResponse(int code, MNSConnectModel response) {
                            fResult[0].setMnsConnectModel(response);
                            if (callback != null) {
                                callback.onResponse(code, fResult[0]);
                            }
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            if (callback != null) {
                                callback.onFailure(t);
                            }
                        }
                    });
                } else {
                    // 失败
                    onFailure(call, new APIException(response.body().getMessage(), response.body().getCode()));
                }
            }

            @Override
            public void onFailure(Call<HttpResponse<LiveCreateResult>> call, Throwable t) {
                // 失败
                if (callback != null) {
                    callback.onFailure(t);
                }
            }
        });
        return createLiveCall;
    }

    /**
     * 方法描述: 请求网络获取播放地址
     */
    public Call watchLive(String roomID, String uid, final Callback<WatchLiveResult> callback) {
        final WatchLiveResult[] fResult = new WatchLiveResult[1];

        Call<HttpResponse<WatchLiveResult>> watchLiveCall;
        // 将房间id和登录成功后保存的信息分装进WatchLiveForm对象
        WatchLiveForm form = new WatchLiveForm(roomID, uid);
        // 获取LiveService专属的Call任务
        watchLiveCall = NetworkServiceFactory.getLiveService().watchLive(form);

        RequestBody body = watchLiveCall.request().body();
        HttpUrl url = watchLiveCall.request().url();

        // 使用Call开启网络请求
        watchLiveCall.enqueue(new retrofit2.Callback<HttpResponse<WatchLiveResult>>() {
            @Override
            public void onResponse(final Call<HttpResponse<WatchLiveResult>> call, Response<HttpResponse<WatchLiveResult>> response) {
                /*
                返回的json数据
                {
                    "code": 200,        HttpResponse的code
                    "message": "成功",  HttpResponse的message
                    "data": {           HttpResponse的data
                            "uid": "2",                         WatchLiveResult的uid
                            "name": "test5",                    WatchLiveResult的name
                            "roomId": 230178951253721540,       WatchLiveResult的roomid
                            "playUrl": "http://videocall.play.danqoo.com/DemoApp/230178951253721540.flv",  WatchLiveResult的播放地址
                            "mns":{             WatchLiveResult的MNSModel
                                "topic":'474c7658805b798814',
                                "topicLocation": "http://125277.mns.cn-hangzhou.aliyuncs.com/topics/229820386403942828",
                                "roomTag": "474c7658805b798814",
                                "userRoomTag": "474c7658805b798814_2" #当前观众过滤消息的tag
                            }
                    }
                }
                */
                fResult[0] = response.body().getData();// 获取存入的是WatchLiveResult对象

                // 这里是获取topic和subscriptionName参数分装成MNSConnectionInfoForm对象，然后去请求获取MNS链接信息
                if (fResult[0] != null && fResult[0].getMNSModel() != null) {

                    String topic = fResult[0].getMNSModel().getTopic();//subscriptionName = topic;

                    MNSConnectionInfoForm form = new MNSConnectionInfoForm(topic, topic);

                    // 阿里MNS管理 获取MNS链接所需要的参数
                    Call<HttpResponse<MNSConnectModel>> mnsCall = NetworkServiceFactory.getAccountService().getMnsConnectionInfo(form);

                    processObservable(mnsCall, new Callback<MNSConnectModel>() {
                        @Override
                        public void onResponse(int code, MNSConnectModel response) {
                            /*
                            返回的json数据
                            {
                                "code":200,
                                "message":"成功",
                                "data":{     HttpResponse的data
                                    "authentication": "Fa91Q+YDqsa7CQOMHyYXE7OFw=",      // 授权
                                    "topicWebsocketServerAddress": "ws://125277.mns-websocket.cn-shanghai.aliyuncs.com/mns", // 主题ws服务地址
                                    "accountId":"12277",
                                    "accessId":"Q1dfW3pJSOJf6"
                                }
                             }
                            */
                            // 将包含MNS链接信息的MNSConnectModel对象设置进WatchLiveResult对象中
                            fResult[0].setConnectModel(response);
                            if (callback != null) {
                                callback.onResponse(code, fResult[0]);
                            }
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            if (callback != null) {
                                callback.onFailure(t);
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<HttpResponse<WatchLiveResult>> call, Throwable t) {
                if (callback != null) {
                    callback.onFailure(t);
                }
            }
        });
        return watchLiveCall;
    }

    /**
     * 方法描述: 请求网络，告诉业务服务器直播将要被关闭
     */
    public Call closeLive(String roomID, String uid, Callback<Object> callback) {
        Call<HttpResponse<Object>> call;
        CloseLiveForm form = new CloseLiveForm(roomID, uid);
        call = NetworkServiceFactory.getLiveService().closeLive(form);
        processObservable(call, callback);
        return call;
    }

    /**
     * 方法描述: 获取直播列表网络请求的Call，并使用Call和结果回调接口开启请求
     */
    public Call list(Callback<List<LiveItemResult>> callback) {
        Call<HttpResponse<List<LiveItemResult>>> call = NetworkServiceFactory.getLiveService().list();
        processObservable(call, callback);
        return call;
    }

    /**
     * 方法描述: 使用roomId去开启网络请求，获取观众列表
     */
    public Call watcherList(String roomID, Callback<List<WatcherResult>> callback) {
        Call<HttpResponse<List<WatcherResult>>> call;
        WatcherListForm form = new WatcherListForm(roomID);
        call = NetworkServiceFactory.getLiveService().watcherList(form);
        processObservable(call, callback);
        return call;
    }

    /**
     * 方法描述: 请求网络告诉业务服务器本观众退出直播间了
     *
     * @param roomID 直播间ID
     * @param uid    用户ID
     */
    public Call exitWatching(String roomID, String uid, Callback<Object> callback) {
        Call<HttpResponse<Object>> call;
        ExitWatchingForm form = new ExitWatchingForm(roomID, uid);
        call = NetworkServiceFactory.getLiveService().exitWatching(form);
        processObservable(call, callback);
        return call;
    }
}
