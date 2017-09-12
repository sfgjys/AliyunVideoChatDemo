package com.alivc.videochat.demo.ui;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.alivc.videochat.demo.R;
import com.alivc.videochat.demo.ui.adapter.LogInfoAdapter;

/**
 * 类的描述: 展示性能的Fragment
 * 技巧:
 * 本类需要实时从新的AlivcPublisherPerformanceInfo对象实例中获取性能参数数据，但是AlivcPublisherPerformanceInfo获取的方法按照功能模块分只能在在LifecyclePublisherMgr类中，
 * 并且这个方法按照功能用来来分应该在LifecycleLiveRecordPresenterImpl中才能被调用后进行性能参数获取，在又因为本界面中是以List形式展示数据的，
 * 以根据AlivcPublisherPerformanceInfo对象实时更新数据的应该通过list的适配器，但是根据隐私安全规则，不能将适配器的对象传递给LifecycleLiveRecordPresenterImpl，
 * 让其直接使用，所以我们需要一个中间类LogHandler来实现调用适配器更新UI。接下来我只要不断循环调用LifecycleLiveRecordPresenterImpl的refreshLogInfo()方法就可以了。
 * 而循环调用是耗时操作，所以另开线程，使用Handler的循环操作方法，但是我们需要考虑到Handler的生命周期（什么时候开始循环，什么时候结束循环）。所以Handler对象应该放在本类中，
 * 根据本类的生命周期来控制Handler的开闭，如此在Handler的run()方法中我们需要使用LifecycleLiveRecordPresenterImpl对象调用refreshLogInfo()方法，
 * 但是将LifecycleLiveRecordPresenterImpl对象传入到本类中也不太好，所以使用第三方回调接口，来实现，在Handler的run()中调用LogRefreshListener接口的onPendingRefresh方法
 * 而在LogRefreshListener接口的实例化中使用LifecycleLiveRecordPresenterImpl对象调用refreshLogInfo()方法
 */
public class LogInfoFragment extends Fragment implements Runnable {
    private final long REFRESH_INTERVAL = 1000;

    private LogHandler mLogHandler;
    private LogRefreshListener mRefreshListener;
    private RecyclerView mLogRecyclerView;
    private LogInfoAdapter mAdapter;
    private Handler mHandler = new Handler();

    public class LogHandler {
        public void updateValue(int key, String value) {
            if (mAdapter != null) {
                // 更新数据源
                mAdapter.updateValue(key, value);
            }
        }

        public void notifyUpdate() {
            if (mAdapter != null) {
                // 通知列表更新UI
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLogHandler = new LogHandler();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_log_info, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mLogRecyclerView = (RecyclerView) view.findViewById(R.id.log_recycler);

        mAdapter = new LogInfoAdapter(getActivity());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mLogRecyclerView.setLayoutManager(layoutManager);
        mAdapter.setHasStableIds(true);
        mLogRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        mHandler.post(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeCallbacks(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLogHandler = null;
        mRefreshListener = null;
    }

    /**
     * 方法描述: Handler post()和removeCallbacks()的内容
     */
    @Override
    public void run() {
        if (mRefreshListener != null) {
            mRefreshListener.onPendingRefresh();
        }
        // 1秒后在post的本run()方法，不断循环
        mHandler.postDelayed(this, REFRESH_INTERVAL);
    }


    public void setRefreshListener(LogRefreshListener listener) {
        this.mRefreshListener = listener;
    }

    public interface LogRefreshListener {
        void onPendingRefresh();
    }

    public LogHandler getLogHandler() {
        return mLogHandler;
    }
}
