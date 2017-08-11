package com.alivc.videochat.demo.presenter;

import com.alivc.videochat.demo.R;
import com.alivc.videochat.demo.bi.ServiceBI;
import com.alivc.videochat.demo.bi.ServiceBIFactory;
import com.alivc.videochat.demo.http.model.LiveItemResult;
import com.alivc.videochat.demo.http.model.WatcherModel;
import com.alivc.videochat.demo.ui.view.MainView;

import java.util.List;

import retrofit2.Call;

/**
 * Created by liujianghao on 16-7-29.
 */
public class MainPresenter {
    private static final String TAG = "LiveRecordPresenter";
    private MainView mMainView;

    public MainPresenter(MainView view) {
        mMainView = view;
    }


    private Call mLoadLiveCall = null;
    private Call mLoadWatcherCall = null;

    /**
     * 加载直播列表
     */
    public void loadLiveList() {
        if(ServiceBI.isCalling(mLoadLiveCall)) {
            mLoadLiveCall.cancel();
        }
        mLoadLiveCall = ServiceBIFactory.getLiveServiceBI().list(mLoadLiveCallback);
    }

    private ServiceBI.Callback<List<LiveItemResult>> mLoadLiveCallback = new ServiceBI.Callback<List<LiveItemResult>>() {
        @Override
        public void onResponse(int code, List<LiveItemResult> results) {
            mMainView.showLiveList(results);
            mMainView.completeLoad();
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

    /**
     * 加载观众列表
     * @param roomID
     */
    public void loadWatcherList(String roomID) {
        if(ServiceBI.isCalling(mLoadWatcherCall)){
            mLoadWatcherCall.cancel();
        }

        mLoadWatcherCall = ServiceBIFactory.getLiveServiceBI().watcherList(roomID, mLoadWatcherCallback);
    }

    private ServiceBI.Callback<List<WatcherModel>> mLoadWatcherCallback = new ServiceBI.Callback<List<WatcherModel>>() {
        @Override
        public void onResponse(int code, List<WatcherModel> results) {
            mMainView.showWatcherList(results);
            mLoadWatcherCall = null;
            mMainView.completeLoad();
        }

        @Override
        public void onFailure(Throwable t) {
            mMainView.completeLoad();
            mLoadWatcherCall = null;
            t.printStackTrace();
            mMainView.completeLoad();
            mMainView.showToast(R.string.live_list_failed);
        }
    };

    public void onStop() {
        if(ServiceBI.isCalling(mLoadLiveCall)) {
            mLoadLiveCall.cancel();
        }

        if(ServiceBI.isCalling(mLoadWatcherCall)) {
            mLoadWatcherCall.cancel();
        }
    }

}
