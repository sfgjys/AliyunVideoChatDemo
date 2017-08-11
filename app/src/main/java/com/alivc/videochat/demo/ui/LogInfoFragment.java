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
 * Created by liujianghao on 16-9-22.
 */
public class LogInfoFragment extends Fragment implements Runnable{
    private final long REFRESH_INTERVAL = 1000;

    private LogHandler mLogHandler;
    private LogRefreshListener mRefreshListener;

    private RecyclerView mLogRecyclerView;
    private LogInfoAdapter mAdapter;
    private Handler mHandler = new Handler();


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

    @Override
    public void run() {
        if(mRefreshListener != null) {
            mRefreshListener.onPendingRefresh();
        }
        mHandler.postDelayed(this, REFRESH_INTERVAL);
    }

    public void setRefreshListener(LogRefreshListener listener) {
        this.mRefreshListener = listener;
    }

    public class LogHandler{
        public void updateValue(int key, String value){
            if(mAdapter != null){
                mAdapter.updateValue(key, value);
            }
        }

        public void notifyUpdate() {
            if(mAdapter != null) {
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    public interface LogRefreshListener{
        void onPendingRefresh();
    }


    public LogHandler getLogHandler() {
        return mLogHandler;
    }


}
