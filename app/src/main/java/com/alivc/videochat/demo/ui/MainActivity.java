package com.alivc.videochat.demo.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.alivc.videochat.demo.R;
import com.alivc.videochat.demo.base.BaseActivity;
import com.alivc.videochat.demo.http.result.LiveItemResult;
import com.alivc.videochat.demo.http.result.WatcherResult;
import com.alivc.videochat.demo.presenter.MainPresenter;
import com.alivc.videochat.demo.ui.adapter.LiveListAdapter;
import com.alivc.videochat.demo.ui.view.MainView;
import com.alivc.videochat.demo.uitils.ToastUtils;

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
     * 变量的描述: 开启直播
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

        // PS: 本界面是在获取到了焦点时加载列表，失去焦点时停止正在加载的网络请求，如此，刚进本界面时先请求了网络，接着又弹出了权限对话框获得了焦点，使界面失去了焦点，网络请求停止
        // 没有权限才进行获取权限
        if (!permissionCheck()) {
            if (Build.VERSION.SDK_INT >= 23) {
                // 6.0 以上请求
                ActivityCompat.requestPermissions(this, permissionManifest, PERMISSION_REQUEST_CODE);
            } else {
                // 6.0 以下在清单文件中没有声明相关权限
                showNoPermissionTip("6.0以下: " + getString(noPermissionTip[mNoPermissionIndex]));
                finish();
            }
        }
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

                //  重新设置列表数据源，并通知更新
                mListAdapter.setDataList(dataList);
                mListAdapter.notifyDataSetChanged();
            } else {
                mRecyclerView.setAlpha(0);
                mNoDataView.setVisibility(View.VISIBLE);
                mRecordBtn.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void showWatcherList(List<WatcherResult> dataList) {

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
        // 失去焦点时停止正在请求的网络
        mMainPresenter.onStop();
    }

    // **************************************************** 权限请求 ****************************************************

    private final String[] permissionManifest = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    private final int[] noPermissionTip = {
            R.string.no_camera_permission,
            R.string.no_record_audio_permission,
            R.string.no_read_phone_state_permission,
            R.string.no_write_external_storage_permission,
            R.string.no_read_external_storage_permission
    };
    /**
     * 变量的描述: 没有权限的权限在权限组中的角标
     */
    private int mNoPermissionIndex = 0;
    /**
     * 变量的描述: 请求权限的Code
     */
    private final int PERMISSION_REQUEST_CODE = 1;

    /**
     * 权限检查（适配6.0以上手机）
     */
    private boolean permissionCheck() {
        int permissionCheck = PackageManager.PERMISSION_GRANTED;
        String permission;
        for (int i = 0; i < permissionManifest.length; i++) {
            permission = permissionManifest[i];
            mNoPermissionIndex = i;
            if (PermissionChecker.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionCheck = PackageManager.PERMISSION_DENIED;
            }
        }
        return permissionCheck == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        int toastTip = noPermissionTip[i];
                        mNoPermissionIndex = i;
                        if (toastTip != 0) {
                            ToastUtils.showToast(MainActivity.this, toastTip);
                            finish();
                        }
                    }
                }
                break;
        }
    }

    /**
     * 没有权限的提醒
     */
    private void showNoPermissionTip(String tip) {
        Toast.makeText(this, tip, Toast.LENGTH_LONG).show();
    }

    // --------------------------------------------------------------------------------------------------------
}
