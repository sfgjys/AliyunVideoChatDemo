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

    /**
     * 接口的描述: 该类是网络请求后的结果监听回调
     */
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
     * 方法描述: 判断参数Call任务是否还存在
     *
     * @param call Call任务请求
     * @return true代表Call任务请求还存在
     */
    public static boolean isCalling(Call call) {
        // isExecuted返回true 代表call已经入队或者已经执行
        return (call != null && call.isExecuted() && !call.isCanceled());
    }
}
