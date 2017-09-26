package com.alivc.videochat.demo.bi;

import com.alivc.videochat.demo.http.form.SendCommentForm;
import com.alivc.videochat.demo.http.form.SendLikeForm;
import com.alivc.videochat.demo.http.result.HttpResponse;
import com.alivc.videochat.demo.http.service.NetworkServiceFactory;

import retrofit2.Call;

public class InteractionServiceBI extends ServiceBI {

    /**
     * 方法描述: 发送给业务服务器观众的评论信息
     */
    public Call sendComment(String uid, String roomID, String comment, Callback<Object> callback) {
        SendCommentForm form = new SendCommentForm(uid, roomID, comment);
        Call<HttpResponse<Object>> call = NetworkServiceFactory.getInteractionService().sendComment(form);
        processObservable(call, callback);
        return call;
    }

    /**
     * 方法描述: 发送给业务服务器观众的点赞信息
     */
    public Call sendLike(String roomID, String uid, Callback<Object> callback) {
        Call<HttpResponse<Object>> call;
        SendLikeForm form = new SendLikeForm(uid, roomID);
        call = NetworkServiceFactory.getInteractionService().sendLike(form);
        processObservable(call, callback);
        return call;
    }
}
