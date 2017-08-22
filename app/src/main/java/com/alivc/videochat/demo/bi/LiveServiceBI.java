package com.alivc.videochat.demo.bi;

import com.alivc.videochat.demo.exception.APIException;
import com.alivc.videochat.demo.http.HttpConstant;
import com.alivc.videochat.demo.http.form.CloseLiveForm;
import com.alivc.videochat.demo.http.form.CreateLiveForm;
import com.alivc.videochat.demo.http.form.ExitWatchingForm;
import com.alivc.videochat.demo.http.form.MNSConnectionInfoForm;
import com.alivc.videochat.demo.http.form.WatchLiveForm;
import com.alivc.videochat.demo.http.form.WatcherListForm;
import com.alivc.videochat.demo.http.model.HttpResponse;
import com.alivc.videochat.demo.http.model.LiveCreateResult;
import com.alivc.videochat.demo.http.model.LiveItemResult;
import com.alivc.videochat.demo.http.model.MNSConnectModel;
import com.alivc.videochat.demo.http.model.WatchLiveResult;
import com.alivc.videochat.demo.http.model.WatcherModel;
import com.alivc.videochat.demo.http.service.ServiceFactory;

import java.util.List;

import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by liujianghao on 16-7-29.
 */
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
        createLiveCall = ServiceFactory.getLiveService().createLive(liveForm);

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
                if (code == HttpConstant.HTTP_OK) {
                    MNSConnectionInfoForm form = new MNSConnectionInfoForm(response.body().getData().getMNSModel().getTopic(), null);
                    // 存储网络请求结果
                    fResult[0] = response.body().getData();

                    // TODO MNS请求
                    Call<HttpResponse<MNSConnectModel>> mnsCall = ServiceFactory.getAccountService().getMnsConnectionInfo(form);

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
     * @param roomID
     * @param uid
     * @param callback
     */
    public Call watchLive(String roomID, String uid, final Callback<WatchLiveResult> callback) {
        final WatchLiveResult[] fResult = new WatchLiveResult[1];

        Call<HttpResponse<WatchLiveResult>> watchLiveCall;
        // 将房间id和登录成功后保存的信息分装进WatchLiveForm对象
        WatchLiveForm form = new WatchLiveForm(roomID, uid);
        // 获取LiveService专属的Call任务
        watchLiveCall = ServiceFactory.getLiveService().watchLive(form);

        RequestBody body = watchLiveCall.request().body();
        HttpUrl url = watchLiveCall.request().url();

        // 使用Call开启网络请求
        watchLiveCall.enqueue(new retrofit2.Callback<HttpResponse<WatchLiveResult>>() {
            @Override
            public void onResponse(final Call<HttpResponse<WatchLiveResult>> call, Response<HttpResponse<WatchLiveResult>> response) {
                fResult[0] = response.body().getData();

                // 这里是获取topic和subscriptionName参数分装成MNSConnectionInfoForm对象，然后去请求获取MNS链接信息
                if (fResult[0] != null && fResult[0].getMNSModel() != null) {
                    String topic = fResult[0].getMNSModel().getTopic();//subscriptionName = topic;
                    MNSConnectionInfoForm form = new MNSConnectionInfoForm(topic, topic);
                    Call<HttpResponse<MNSConnectModel>> mnsCall = ServiceFactory.getAccountService().getMnsConnectionInfo(form);
                    processObservable(mnsCall, new Callback<MNSConnectModel>() {
                        @Override
                        public void onResponse(int code, MNSConnectModel response) {
                            // 将包含MNS链接信息的MNSConnectModel对象设置进WatchLiveResult回调给接口
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
     * @param roomID
     * @param uid
     * @param callback
     */
    public Call closeLive(String roomID, String uid, Callback callback) {
        Call call;
        CloseLiveForm form = new CloseLiveForm(roomID, uid);
        call = ServiceFactory.getLiveService()
                .closeLive(form);
        processObservable(call, callback);
        return call;
    }

    /**
     * 方法描述: 获取直播列表网络请求的Call，并使用Call和结果回调接口开启请求
     */
    public Call list(Callback<List<LiveItemResult>> callback) {
        Call<HttpResponse<List<LiveItemResult>>> call = ServiceFactory.getLiveService().list();
        processObservable(call, callback);
        return call;
    }


    /**
     * 方法描述: 使用roomId去开启网络请求，获取观众列表
     */
    public Call watcherList(String roomID, Callback<List<WatcherModel>> callback) {
        Call<HttpResponse<List<WatcherModel>>> call;
        WatcherListForm form = new WatcherListForm(roomID);
        call = ServiceFactory.getLiveService().watcherList(form);
        processObservable(call, callback);
        return call;
    }

    /**
     * 退出观看直播
     *
     * @param roomID
     * @param uid
     */
    public Call exitWatching(String roomID, String uid, Callback callback) {
        Call<HttpResponse<Object>> call;
        ExitWatchingForm form = new ExitWatchingForm(roomID, uid);
        call = ServiceFactory.getLiveService().exitWatching(form);
        processObservable(call, callback);
        return call;
    }
}
