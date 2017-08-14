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

public class MainActivity extends BaseActivity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    /**
     * 变量的描述: 用于展示直播列表的控件
     */
    private RecyclerView mRecyclerView;
    /**
     * 变量的描述: 下拉刷新动画控件
     */
    private SwipeRefreshLayout mRefreshLayout;
    /**
     * 变量的描述: 没有正在直播的列表时展示的控件页面，该页面可以发起自己的直播
     */
    private LinearLayout mNoDataView;

    /**
     * 变量的描述: 此控件暂时没什么用
     */
    private ImageView mRecordBtn;

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

    }

    // --------------------------------------------------------------------------------------------------------

    MainPresenter mMainPresenter = null;

    MainView mMainView = new MainView() {

        /**
         * 方法描述: 显示参数所代表字符串的吐司
         * @param   resID 字符串 id
         */
        @Override
        public void showToast(int resID) {
            Toast.makeText(MainActivity.this, getString(resID), Toast.LENGTH_SHORT).show();
        }

        /**
         * 方法描述: 展示直播列表
         * @param   dataList 直播列表的数据源
         */
        @Override
        public void showLiveList(List<LiveItemResult> dataList) {
            // 根据数据源来决定是展示列表还是创建直播
            if (dataList != null && dataList.size() > 0) {
                mRecyclerView.setVisibility(View.VISIBLE);
                mRecyclerView.setAlpha(1);
                mNoDataView.setVisibility(View.INVISIBLE);
                mRecordBtn.setVisibility(View.VISIBLE);

                // 重新设置列表数据源，并通知更新
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

        /**
         * 方法描述: 直播列表加载完成
         */
        @Override
        public void completeLoad() {
            // 停止刷新动画
            mRefreshLayout.setRefreshing(false);
        }
    };

    // --------------------------------------------------------------------------------------------------------

    private LiveListAdapter mListAdapter;

    /**
     * 初始化RecyclerView控件列表
     */
    private void initRecyclerView() {
        mListAdapter = new LiveListAdapter(this, getUid());
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setAdapter(mListAdapter);
        mRecyclerView.setLayoutManager(layoutManager);
    }

    // --------------------------------------------------------------------------------------------------------

    /**
     * 方法描述: 设置下拉刷新控件的下拉刷新监听
     */
    private void initRefreshLayout() {
        mRefreshLayout.setOnRefreshListener(this);
    }

    @Override
    public void onRefresh() {
        // 下拉刷新时重新请求网络刷新数据
        mMainPresenter.loadLiveList();
    }

    // --------------------------------------------------------------------------------------------------------

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_record:
            case R.id.btn_create_my: // 开启直播页面
                LiveActivity.startActivity(this);
                break;
            case R.id.iv_back: // 关闭
                finish();
                break;
        }
    }

    // --------------------------------------------------------------------------------------------------------

    @Override
    protected void onResume() {
        super.onResume();
        // 具有焦点时时重新请求网络刷新数据
        mMainPresenter.loadLiveList();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMainPresenter.onStop();
    }
}
