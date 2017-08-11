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

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by liujianghao on 16-7-29.
 */
public class LiveServiceBI extends ServiceBI {

    /**
     * @param uid
     * @param desc
     * @param callback
     */
    public Call createLive(String uid, String desc,
                           final Callback<LiveCreateResult> callback) {

        CreateLiveForm liveForm = new CreateLiveForm(uid, desc);
        Call createLiveCall;
        createLiveCall = ServiceFactory.getLiveService()
                .createLive(liveForm);
        final LiveCreateResult[] fResult = new LiveCreateResult[1];
        createLiveCall.enqueue(new retrofit2.Callback<HttpResponse<LiveCreateResult>>() {

            @Override
            public void onResponse(final Call<HttpResponse<LiveCreateResult>> call, Response<HttpResponse<LiveCreateResult>> response) {
                if (response.body().getCode() == HttpConstant.HTTP_OK) {
                    MNSConnectionInfoForm form = new MNSConnectionInfoForm(response.body().getData()
                            .getMNSModel().getTopic(), null);
                    fResult[0] = response.body().getData();
                    Call<HttpResponse<MNSConnectModel>> mnsCall = ServiceFactory.getAccountService().getMnsConnectionInfo(form);
                    processObservable(mnsCall,
                            new ServiceBI.Callback<MNSConnectModel>() {

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
                    onFailure(call, new APIException(response.body().getMessage(), response.body().getCode()));
                }
            }

            @Override
            public void onFailure(Call<HttpResponse<LiveCreateResult>> call, Throwable t) {
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
    public Call watchLive(String roomID, String uid,
                          final Callback<WatchLiveResult> callback) {
        Call watchLiveCall;
        final WatchLiveResult[] fResult = new WatchLiveResult[1];
        WatchLiveForm form = new WatchLiveForm(roomID, uid);
        watchLiveCall = ServiceFactory.getLiveService().watchLive(form);
        watchLiveCall.enqueue(new retrofit2.Callback<HttpResponse<WatchLiveResult>>() {

            @Override
            public void onResponse(final Call<HttpResponse<WatchLiveResult>> call, Response<HttpResponse<WatchLiveResult>> response) {
                fResult[0] = response.body().getData();
                if (fResult[0] != null && fResult[0].getMNSModel() != null) {
                    String topic = fResult[0].getMNSModel().getTopic();//subscriptionName = topic;
                    MNSConnectionInfoForm form = new MNSConnectionInfoForm(topic, topic);
                    Call<HttpResponse<MNSConnectModel>> mnsCall = ServiceFactory.getAccountService().getMnsConnectionInfo(form);
                    processObservable(mnsCall, new Callback<MNSConnectModel>() {
                        @Override
                        public void onResponse(int code, MNSConnectModel response) {
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
    public Call closeLive(String roomID, String uid,
                          Callback callback) {
        Call call;
        CloseLiveForm form = new CloseLiveForm(roomID, uid);
        call = ServiceFactory.getLiveService()
                .closeLive(form);
        processObservable(call, callback);
        return call;
    }

    /**
     * @param callback
     */
    public Call list(Callback<List<LiveItemResult>> callback) {
        Call<HttpResponse<List<LiveItemResult>>> call = ServiceFactory.getLiveService().list();
        processObservable(call, callback);
        return call;
    }


    /**
     * @param roomID
     * @param callback
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
