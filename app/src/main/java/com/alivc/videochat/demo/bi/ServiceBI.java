package com.alivc.videochat.demo.bi;

import com.alivc.videochat.demo.exception.APIErrorCode;
import com.alivc.videochat.demo.exception.APIException;
import com.alivc.videochat.demo.http.HttpConstant;
import com.alivc.videochat.demo.http.model.HttpResponse;

import retrofit2.Call;
import retrofit2.Response;


/**
 * Created by liujianghao on 16-8-2.
 */
public class ServiceBI {
    public interface Callback<T> {
        void onResponse(int code, T response);

        void onFailure(Throwable t);
    }

    <T> void processObservable(Call<HttpResponse<T>> call, final ServiceBI.Callback<T> subscriber) {
        call.enqueue(new retrofit2.Callback<HttpResponse<T>>() {
            @Override
            public void onResponse(Call<HttpResponse<T>> call, Response<HttpResponse<T>> response) {
                HttpResponse<T> body = response.body();
                if (!response.isSuccessful() || body == null) {
                    onFailure(call, new APIException("UnKnown Exception", APIErrorCode.ERROR_UNKNOWN));
                    return;
                }
                if (body.getCode() == HttpConstant.HTTP_OK && subscriber != null) {
                    subscriber.onResponse(response.code(), response.body().getData());
                } else {
                    onFailure(call, new APIException(body.getMessage(), body.getCode()));
                }
            }

            @Override
            public void onFailure(Call<HttpResponse<T>> call, Throwable t) {
                if (subscriber != null)
                    subscriber.onFailure(t);
            }
        });
    }

    /**
     * 判断请求是否在执行
     *
     * @param call
     * @return
     */
    public static final boolean isCalling(Call call) {
        return (call != null && call.isExecuted() && !call.isCanceled());
    }
}
