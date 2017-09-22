package com.alivc.videochat.demo.ui.view;

import com.alivc.videochat.demo.http.result.LiveItemResult;
import com.alivc.videochat.demo.http.result.WatcherModel;

import java.util.List;

/**
 * 类的描述: 当从网络获取了展示list的数据后，使用本接口的实例去更新UI
 */
public interface MainView {

    /**
     * 方法描述: 根据参数获取资源中的String字符串，进行吐司展示
     */
    void showToast(int resID);

    /**
     * 方法描述: 根据直播参数进行list展示
     */
    void showLiveList(List<LiveItemResult> dataList);

    /**
     * 方法描述: 根据观众参数进行list展示
     */
    void showWatcherList(List<WatcherModel> dataList);

    /**
     * 方法描述: 展示完成后
     */
    void completeLoad();
}
