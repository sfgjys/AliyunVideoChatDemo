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
     * 发送评论
     *
     * @param uid
     * @param roomID
     * @param comment
     */
    public void sendComment(String uid, String roomID, String comment) {
        ServiceBIFactory.getInteractionServiceBI().sendComment(uid, roomID, comment, mCommentCallback);
    }

    private ServiceBI.Callback mCommentCallback = new ServiceBI.Callback() {

        @Override
        public void onResponse(int code, Object response) {
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
     * 发送赞
     *
     * @param roomID
     * @param uid
     */
    public void sendLike(String roomID, String uid) {
        ServiceBIFactory.getInteractionServiceBI().sendLike(roomID, uid, mLikeCallback);
    }

    private ServiceBI.Callback mLikeCallback = new ServiceBI.Callback() {
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
