package com.alivc.videochat.demo.bi;

import com.alivc.videochat.demo.http.form.SendCommentForm;
import com.alivc.videochat.demo.http.form.SendLikeForm;
import com.alivc.videochat.demo.http.service.ServiceFactory;

import retrofit2.Call;

/**
 * Created by liujianghao on 16-8-2.
 */
public class InteractionServiceBI extends ServiceBI {

    /**
     * 发送评论
     *
     * @param uid
     * @param roomID
     * @param comment
     * @param callback
     * @return
     */
    public Call sendComment(String uid, String roomID, String comment, Callback callback) {
        SendCommentForm form = new SendCommentForm(uid, roomID, comment);
        Call call = ServiceFactory.getInteractionService().sendComment(form);
        processObservable(call, callback);
        return call;
    }

    /**
     * 点赞
     *
     * @param roomID
     * @param uid
     * @param callback
     */
    public Call sendLike(String roomID, String uid, Callback callback) {
        Call call;
        SendLikeForm form = new SendLikeForm(uid, roomID);
        call = ServiceFactory.getInteractionService().sendLike(form);
        processObservable(call, callback);
        return call;
    }
}
