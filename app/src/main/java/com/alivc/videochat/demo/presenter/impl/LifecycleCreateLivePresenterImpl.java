package com.alivc.videochat.demo.presenter.impl;

import android.content.Context;
import android.os.Bundle;

import com.alivc.videochat.demo.R;
import com.alivc.videochat.demo.http.model.LiveCreateResult;
import com.alivc.videochat.demo.base.AsyncCallback;
import com.alivc.videochat.demo.base.ContextBase;
import com.alivc.videochat.demo.logic.IPublisherMgr;
import com.alivc.videochat.demo.logic.LifecyclePublisherMgr;
import com.alivc.videochat.demo.presenter.ILifecycleCreateLivePresenter;
import com.alivc.videochat.demo.presenter.view.ICreateLiveView;
import com.alivc.videochat.demo.uitils.ToastUtils;

/**
 * Created by apple on 2017/1/9.
 */

public class LifecycleCreateLivePresenterImpl extends ContextBase implements ILifecycleCreateLivePresenter {
    private LifecyclePublisherMgr mPublisherMgr;
    private ICreateLiveView mView;

    public LifecycleCreateLivePresenterImpl(Context context, LifecyclePublisherMgr mgr, ICreateLiveView view) {
        super(context);
        this.mPublisherMgr = mgr;
        this.mView = view;
    }

    @Override
    public void createLive(String description) {
        mPublisherMgr.asyncCreateLive(description, new AsyncCallback() {
            @Override
            public void onSuccess(Bundle bundle) {
                LiveCreateResult result = (LiveCreateResult) bundle.getSerializable(IPublisherMgr.DATA_KEY_CREATE_LIVE_RESULT);
                mView.showPublishStreamUI(result.getRoomID(), result.getName(), result.getUid());   //显示创建直播成功的UI
            }

            @Override
            public void onFailure(Bundle bundle, Throwable e) {
                ToastUtils.showToast(getContext(), R.string.create_live_failed);
            }
        });
    }

    @Override
    public void switchBeauty() {
        mPublisherMgr.switchBeauty();
    }

    @Override
    public void switchCamera() {
        mPublisherMgr.switchCamera();
    }

    @Override
    public void onCreate() {
        mPublisherMgr.onCreate();
    }

    @Override
    public void onStart() {
        mPublisherMgr.onStart();
    }

    @Override
    public void onResume() {
        mPublisherMgr.onResume();
    }

    @Override
    public void onPause() {
        mPublisherMgr.onPause();
    }

    @Override
    public void onStop() {
        mPublisherMgr.onStop();
    }

    @Override
    public void onDestroy() {
        mPublisherMgr.onDestroy();
    }
}
