package com.alivc.videochat.demo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.alivc.videochat.demo.R;
import com.alivc.videochat.demo.base.BaseActivity;
import com.alivc.videochat.demo.http.model.LiveItemResult;
import com.alivc.videochat.demo.http.model.WatcherModel;
import com.alivc.videochat.demo.presenter.MainPresenter;
import com.alivc.videochat.demo.ui.adapter.LiveListAdapter;
import com.alivc.videochat.demo.ui.view.MainView;

import java.util.List;

public class MainActivity extends BaseActivity
        implements View.OnClickListener,
        SwipeRefreshLayout.OnRefreshListener {

    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mRefreshLayout;
    private LinearLayout mNoDataView;
    private ImageView mRecordBtn;


    private LiveListAdapter mListAdapter;

    MainPresenter mMainPresenter = null;


    /**
     * init RecyclerView
     */
    private void initRecyclerView() {
        mListAdapter = new LiveListAdapter(this, getUid());
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setAdapter(mListAdapter);
        mRecyclerView.setLayoutManager(layoutManager);
    }


    private void initRefreshLayout() {
        mRefreshLayout.setOnRefreshListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_record:
            case R.id.btn_create_my:
                LiveActivity.startActivity(this);
                break;
            case R.id.iv_back:
                finish();
                break;
        }
    }

    MainView mMainView = new MainView() {
        @Override
        public void showToast(int resID) {
            Toast.makeText(MainActivity.this, getString(resID), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void showLiveList(List<LiveItemResult> dataList) {
            if (dataList != null && dataList.size() > 0) {
                mRecyclerView.setVisibility(View.VISIBLE);
                mRecyclerView.setAlpha(1);
                mNoDataView.setVisibility(View.INVISIBLE);
                mRecordBtn.setVisibility(View.VISIBLE);
                mListAdapter.setDataList(dataList);
                mListAdapter.notifyDataSetChanged();
            } else {
                mRecyclerView.setAlpha(0);
                mNoDataView.setVisibility(View.VISIBLE);
                mRecordBtn.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void showWatcherList(List<WatcherModel> dataList) {

        }


        @Override
        public void completeLoad() {
            mRefreshLayout.setRefreshing(false);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMainPresenter = new MainPresenter(mMainView);
        setContentView(R.layout.activity_main);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        mNoDataView = (LinearLayout) findViewById(R.id.no_data);
        mRecordBtn = (ImageView) findViewById(R.id.btn_record);

        initRecyclerView();

        initRefreshLayout();

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while (true) {
//                    long timeStamp = System.currentTimeMillis();
//                    Log.d("xiongbo06", "64 time stamp =  " + timeStamp + ", 32 time stamp = " + (timeStamp & 0xffffffffl) + ", 31 time stamp = " + (timeStamp & 0x7fffffff));
//                    try {
//                        Thread.sleep(2000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMainPresenter.loadLiveList();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMainPresenter.onStop();
    }


    @Override
    public void onRefresh() {
        mMainPresenter.loadLiveList();
    }
}
