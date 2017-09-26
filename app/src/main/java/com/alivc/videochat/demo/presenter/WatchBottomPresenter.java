package com.alivc.videochat.demo.presenter;

import android.content.Context;
import android.util.Log;

import com.alivc.videochat.demo.R;
import com.alivc.videochat.demo.base.ContextBase;
import com.alivc.videochat.demo.bi.ServiceBI;
import com.alivc.videochat.demo.bi.ServiceBIFactory;
import com.alivc.videochat.demo.uitils.ToastUtils;

/**
 * 类的描述: 发送评论和进行点赞的网络请求
 */
public class WatchBottomPresenter extends ContextBase {
    private static final String TAG = "WatchBottomPresenter";


    public WatchBottomPresenter(Context context) {
        super(context);
    }

    /**
     * 方法描述: 发送给业务服务器观众的评论信息
     */
    public void sendComment(String uid, String roomID, String comment) {
        ServiceBIFactory.getInteractionServiceBI().sendComment(uid, roomID, comment, mCommentCallback);
    }

    private ServiceBI.Callback<Object> mCommentCallback = new ServiceBI.Callback<Object>() {

        @Override
        public void onResponse(int code, Object response) {
            // 发送评论成功的话，服务器会通过MNS发送消息给聊天控件，让其显示评论
            Log.d(TAG, "send comment succeed");
        }

        @Override
        public void onFailure(Throwable t) {
            t.printStackTrace();
            Log.e(TAG, "send comment failed");
            Context context = getContext();
            if (context != null) {
                ToastUtils.showToast(context, R.string.send_comment_failed);
            }
        }
    };


    /**
     * 方法描述: 发送给业务服务器观众的点赞信息
     */
    public void sendLike(String roomID, String uid) {
        ServiceBIFactory.getInteractionServiceBI().sendLike(roomID, uid, mLikeCallback);
    }

    private ServiceBI.Callback<Object> mLikeCallback = new ServiceBI.Callback<Object>() {
        @Override
        public void onResponse(int code, Object response) {
            Log.d(TAG, "send like succeed");
        }

        @Override
        public void onFailure(Throwable t) {
            t.printStackTrace();
            Log.e(TAG, "send like failed");
            Context context = getContext();
            if (context != null) {
                ToastUtils.showToast(context, R.string.send_like_failed);
            }
        }
    };

}
