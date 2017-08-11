package com.alivc.videochat.demo.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.alivc.videochat.demo.R;
import com.alivc.videochat.demo.base.ActionFragment;
import com.alivc.videochat.demo.base.BaseActivity;
import com.alivc.videochat.demo.http.form.FeedbackForm;
import com.alivc.videochat.demo.http.model.LiveItemResult;
import com.alivc.videochat.demo.http.model.WatcherModel;
import com.alivc.videochat.demo.presenter.MainPresenter;
import com.alivc.videochat.demo.ui.adapter.AnchorListAdapter;
import com.alivc.videochat.demo.ui.view.MainView;
import com.alivc.videochat.demo.uitils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liujianghao on 16-9-13.
 */
public class AnchorListFragment extends ActionFragment
        implements SwipeRefreshLayout.OnRefreshListener,
        AnchorListAdapter.OnItemClickListener {
    public static final int FLAG_ANCHOR = 1;
    public static final int FLAG_WATCHER = 2;

    private static final String EXTRA_FLAG = "flag";
    public static final String EXTRA_INVITEE_UID = "extra-invitee-uid";
    public static final String KEY_LIVE_ITEM_DATA = "key-live-item-data";

    private SwipeRefreshLayout mRefreshLayout;
    private RecyclerView mRecyclerView;
    private LinearLayout mNoDataView;


    private AnchorListAdapter mAdapter;
    private String mUid = null;

    private MainPresenter mPresenter;
    private int mFlag;

    private String mRoomID;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        mFlag = bundle.getInt(EXTRA_FLAG, FLAG_ANCHOR);
        mRoomID = bundle.getString(ExtraConstant.EXTRA_ROOM_ID);

        mPresenter = new MainPresenter(mView);
        mUid = ((BaseActivity) getActivity()).getUid();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_anchor_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refresh_layout);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mNoDataView = (LinearLayout) view.findViewById(R.id.no_data);


        initRecyclerView();
        initRefreshLayout();
    }

    @Override
    public void onResume() {
        super.onResume();
        switch (mFlag) {
            case FLAG_ANCHOR:
                mPresenter.loadLiveList();
                break;
            case FLAG_WATCHER:
                mPresenter.loadWatcherList(mRoomID);
                break;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mPresenter.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter = null;
    }


    private MainView mView = new MainView() {
        @Override
        public void showToast(int resID) {
            Toast.makeText(getActivity(), getString(resID), Toast.LENGTH_SHORT).show();
        }


        @Override
        public void showLiveList(List<LiveItemResult> dataList) {
            if (dataList != null && dataList.size() > 0) {
                mRecyclerView.setVisibility(View.VISIBLE);
                mRecyclerView.setAlpha(1);
                mNoDataView.setVisibility(View.INVISIBLE);
                mAdapter.setDataList(dataList);
                mAdapter.notifyDataSetChanged();
            } else {
                mRecyclerView.setAlpha(0);
                mNoDataView.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void showWatcherList(List<WatcherModel> dataList) {
            LiveItemResult item;
            WatcherModel watcherModel;
            List<LiveItemResult> watcherList;
            if(dataList != null) {
                int count = dataList.size();
                watcherList = new ArrayList<>(count);

                for(int i=0;i<count;i++) {
                    watcherModel = dataList.get(i);
                    item = new LiveItemResult();
                    item.setUid(watcherModel.getUid());
                    item.setName(watcherModel.getName());
                    watcherList.add(item);
                }
                showLiveList(watcherList);
            }
        }

        @Override
        public void completeLoad() {
            mRefreshLayout.setRefreshing(false);
        }
    };

    /**
     * 初始化RecyclerView
     */
    private void initRecyclerView() {
        mAdapter = new AnchorListAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter.setItemClickListener(this);
    }

    /**
     * 初始化SwipeRefreshLayout
     */
    private void initRefreshLayout() {
        mRefreshLayout.setOnRefreshListener(this);
    }

    @Override
    public void onRefresh() {
        switch (mFlag) {
            case FLAG_ANCHOR:
                mPresenter.loadLiveList();
                break;
            case FLAG_WATCHER:
                mPresenter.loadWatcherList(mRoomID);
                break;
        }
    }

    @Override
    public void onItemClick(int position, LiveItemResult itemData) {
        if (itemData.getUid().equals(mUid)) {
            ToastUtils.showToast(getActivity(), R.string.not_allow_call_self);
        } else if(mActionListener != null) {
            switch (mFlag) {
                case FLAG_ANCHOR:
                    itemData.setUserType(FeedbackForm.INVITE_TYPE_ANCHOR);
                    break;
                default:
                    itemData.setUserType(FeedbackForm.INVITE_TYPE_WATCHER);

            }
            Bundle bundle = new Bundle();
            bundle.putSerializable(KEY_LIVE_ITEM_DATA, itemData);
            if(mActionListener != null) {
                mActionListener.onPendingAction(LiveActivity.INTERACTION_TYPE_INVITE, bundle);
            }
            //发起连麦邀请
//            mInvitePresenter.inviteVideoCall(mUid, itemData.getUid(), FeedbackForm.INVITE_TYPE_ANCHOR);
        }
    }


    public static final AnchorListFragment newInstance(int flag, String roomID) {
        AnchorListFragment fragment = new AnchorListFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_FLAG, flag);
        bundle.putString(ExtraConstant.EXTRA_ROOM_ID, roomID);
        fragment.setArguments(bundle);
        return fragment;
    }


}
