package com.alivc.videochat.demo.presenter.impl;

import android.content.Context;
import android.os.Bundle;

import com.alivc.videochat.demo.R;
import com.alivc.videochat.demo.http.model.LiveCreateResult;
import com.alivc.videochat.demo.base.AsyncCallback;
import com.alivc.videochat.demo.base.ContextBase;
import com.alivc.videochat.demo.logic.IPublisherManager;
import com.alivc.videochat.demo.logic.LifecyclePublisherManager;
import com.alivc.videochat.demo.presenter.ILifecycleCreateLivePresenter;
import com.alivc.videochat.demo.presenter.view.ICreateLiveView;
import com.alivc.videochat.demo.uitils.ToastUtils;

/**
 * 类的描述: 该接口实例是用于在LifecyclePublisherMgr中进行获取推流地址相关
 */
public class LifecycleCreateLivePresenterImpl extends ContextBase implements ILifecycleCreateLivePresenter {
    private LifecyclePublisherManager mPublisherMgr;
    private ICreateLiveView mCreateLiveView;

    public LifecycleCreateLivePresenterImpl(Context context, LifecyclePublisherManager mgr, ICreateLiveView view) {
        super(context);
        this.mPublisherMgr = mgr;
        this.mCreateLiveView = view;
    }

    @Override
    public void createLive(String description) {
        mPublisherMgr.asyncCreateLive(description, new AsyncCallback() {
            @Override
            public void onSuccess(Bundle bundle) {
                LiveCreateResult result = (LiveCreateResult) bundle.getSerializable(IPublisherManager.DATA_KEY_CREATE_LIVE_RESULT);
                if (result != null) {
                    mCreateLiveView.showPublishStreamUI(result.getRoomID(), result.getName(), result.getUid());   // 显示创建直播并推流成功的UI
                } else {
                    ToastUtils.showToast(getContext(), R.string.create_live_failed);
                }
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

    // --------------------------------------------------------------------------------------------------------

    // 下面的这些生命周期需要与Activity或者Fragment的生命周期联动才有效果，在本APP中暂时没有使用到
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
