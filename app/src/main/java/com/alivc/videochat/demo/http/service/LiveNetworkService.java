package com.alivc.videochat.demo.http.service;

import com.alivc.videochat.demo.http.HttpConstant;
import com.alivc.videochat.demo.http.form.CloseLiveForm;
import com.alivc.videochat.demo.http.form.CreateLiveForm;
import com.alivc.videochat.demo.http.form.ExitWatchingForm;
import com.alivc.videochat.demo.http.form.WatchLiveForm;
import com.alivc.videochat.demo.http.form.WatcherListForm;
import com.alivc.videochat.demo.http.result.HttpResponse;
import com.alivc.videochat.demo.http.result.LiveCreateResult;
import com.alivc.videochat.demo.http.result.LiveItemResult;
import com.alivc.videochat.demo.http.result.WatchLiveResult;
import com.alivc.videochat.demo.http.result.WatcherModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * 类的描述: 
 */
public interface LiveNetworkService {

    @POST(HttpConstant.URL_CREATE_LIVE)
    Call<HttpResponse<LiveCreateResult>> createLive(@Body CreateLiveForm form);// 获取推流地址

    @POST(HttpConstant.URL_WATCH_LIVE)
    Call<HttpResponse<WatchLiveResult>> watchLive(@Body WatchLiveForm form);// 请求直播播放地址

    @POST(HttpConstant.URL_CLOSE_LIVE)
    Call<HttpResponse<Object>> closeLive(@Body CloseLiveForm form);

    @POST(HttpConstant.URL_LIST_LIVE)
    Call<HttpResponse<List<LiveItemResult>>> list();

    @POST(HttpConstant.URL_WATCHER_LIST)
    Call<HttpResponse<List<WatcherModel>>> watcherList(@Body WatcherListForm form);

    @POST(HttpConstant.URL_EXIT_WATCHING)
    Call<HttpResponse<Object>> exitWatching(@Body ExitWatchingForm form);// 结束观看直播
}
