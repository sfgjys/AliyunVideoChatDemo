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
import com.alivc.videochat.demo.http.result.WatcherResult;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * 类的描述: 获取推流地址；获取直播地址；主播告诉业务服务器将要关闭直播；获取正在直播的列表；获取某个直播间的观众列表；告诉业务服务器本观众退出直播间了
 */
public interface LiveNetworkService {

    @POST(HttpConstant.URL_CREATE_LIVE)
    Call<HttpResponse<LiveCreateResult>> createLive(@Body CreateLiveForm form);// 请求网络获取推流地址

    @POST(HttpConstant.URL_WATCH_LIVE)
    Call<HttpResponse<WatchLiveResult>> watchLive(@Body WatchLiveForm form);// 请求网络获取播放地址

    @POST(HttpConstant.URL_CLOSE_LIVE)
    Call<HttpResponse<Object>> closeLive(@Body CloseLiveForm form);// 请求网络，告诉业务服务器直播将要被关闭

    @POST(HttpConstant.URL_LIST_LIVE)
    Call<HttpResponse<List<LiveItemResult>>> list();// 请求网络获取正在直播的列表

    @POST(HttpConstant.URL_WATCHER_LIST)
    Call<HttpResponse<List<WatcherResult>>> watcherList(@Body WatcherListForm form);// 获取某个直播间的观众列表

    @POST(HttpConstant.URL_EXIT_WATCHING)
    Call<HttpResponse<Object>> exitWatching(@Body ExitWatchingForm form);// 请求网络告诉业务服务器本观众退出直播间了
}
