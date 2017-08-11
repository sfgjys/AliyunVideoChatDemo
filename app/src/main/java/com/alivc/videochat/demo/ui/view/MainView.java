package com.alivc.videochat.demo.ui.view;

import com.alivc.videochat.demo.http.model.LiveItemResult;
import com.alivc.videochat.demo.http.model.WatcherModel;

import java.util.List;

/**
 * Created by liujianghao on 16-7-29.
 */
public interface MainView {
    void showToast(int resID);

    void showLiveList(List<LiveItemResult> dataList);

    void showWatcherList(List<WatcherModel> dataList);

    void completeLoad();

}
