package com.alivc.videochat.demo.presenter;

import com.alivc.videochat.demo.R;
import com.alivc.videochat.demo.bi.ServiceBI;
import com.alivc.videochat.demo.bi.ServiceBIFactory;
import com.alivc.videochat.demo.http.result.LiveItemResult;
import com.alivc.videochat.demo.http.result.WatcherModel;
import com.alivc.videochat.demo.ui.view.MainView;

import java.util.List;

import retrofit2.Call;

/**
 * 类的描述: 用于开启网络请求和设置网络请求结果回调，本类有两种请求：请求获取直播列表，请求获取观众列表
 */
public class MainPresenter {
    private static final String TAG = "LiveRecordPresenter";

    private MainView mMainView;

    public MainPresenter(MainView view) {
        mMainView = view;
    }

    // --------------------------------------------------------------------------------------------------------

    private Call mLoadLiveCall = null;

    /**
     * 方法描述: 请求网络获取直播列表，首先判断是否存在正在队列中或者正在执行Call，有就取消掉，重新创建
     * 这个方法也可以获取正在直播的主播名单列表
     */
    public void loadLiveList() {
        if (ServiceBI.isCalling(mLoadLiveCall)) {
            mLoadLiveCall.cancel();
        }
        mLoadLiveCall = ServiceBIFactory.getLiveServiceBI().list(mLoadLiveCallback);
    }

    /**
     * 变量的描述: 请求网络获取直播列表的结果监听回调
     */
    private ServiceBI.Callback<List<LiveItemResult>> mLoadLiveCallback = new ServiceBI.Callback<List<LiveItemResult>>() {
        @Override
        public void onResponse(int code, List<LiveItemResult> results) {
            // 调用回调接口MainView，让使用者自己去具体操作
            mMainView.showLiveList(results);
            mMainView.completeLoad();
            // 释放Call
            mLoadLiveCall = null;
        }

        @Override
        public void onFailure(Throwable t) {
            t.printStackTrace();
            mMainView.completeLoad();
            mMainView.showToast(R.string.live_list_failed);
            mLoadLiveCall = null;
        }
    };

    // --------------------------------------------------------------------------------------------------------

    private Call mLoadWatcherCall = null;

    /**
     * 方法描述: 用参数一来开启网络请求获取观众列表
     *
     * @param roomID 房间id？？？？？？？？？？？？？？？？？
     */
    public void loadWatcherList(String roomID) {
        if (ServiceBI.isCalling(mLoadWatcherCall)) {
            mLoadWatcherCall.cancel();
        }
        mLoadWatcherCall = ServiceBIFactory.getLiveServiceBI().watcherList(roomID, mLoadWatcherCallback);
    }

    /**
     * 变量的描述: 请求观众列表的网络结果回调接口实例
     */
    private ServiceBI.Callback<List<WatcherModel>> mLoadWatcherCallback = new ServiceBI.Callback<List<WatcherModel>>() {
        @Override
        public void onResponse(int code, List<WatcherModel> results) {
            // 根据数据源加载列表，并停止刷新
            mMainView.showWatcherList(results);
            mLoadWatcherCall = null;
            mMainView.completeLoad();
        }

        @Override
        public void onFailure(Throwable t) {
            // 停止刷新
            mMainView.completeLoad();
            mLoadWatcherCall = null;
            t.printStackTrace();
            mMainView.completeLoad();
            mMainView.showToast(R.string.live_list_failed);
        }
    };

    // --------------------------------------------------------------------------------------------------------

    /**
     * 方法描述: 不再需要操作是，取消Call
     */
    public void onStop() {
        if (ServiceBI.isCalling(mLoadLiveCall)) {
            mLoadLiveCall.cancel();
        }

        if (ServiceBI.isCalling(mLoadWatcherCall)) {
            mLoadWatcherCall.cancel();
        }
    }

}
