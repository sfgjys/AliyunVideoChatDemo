package com.alivc.videochat.demo.bi;

import com.alivc.videochat.demo.exception.APIErrorCode;
import com.alivc.videochat.demo.exception.APIException;
import com.alivc.videochat.demo.http.HttpConstant;
import com.alivc.videochat.demo.http.result.HttpResponse;

import retrofit2.Call;
import retrofit2.Response;

/**
 * 类的描述: 该类包含请求结果回调接口，判断Call是否在队列中或已经执行，正式使用Call和结果回调接口实例开启请求的方法processObservable
 */
public class ServiceBI {

    /**
     * 接口的描述: 该类是网络请求后的结果监听回调
     */
    public interface Callback<T> {
        /**
         * 方法描述: 网络请求成功回调接口
         *
         * @param code     Response的code
         * @param response HttpResponse的data成员变量
         */
        void onResponse(int code, T response);

        void onFailure(Throwable t);
    }

    /**
     * 方法描述: 使用参数一Call对象去进行正式的网络请求，并调用参数二接口去回调请求结果
     */
    <T> void processObservable(Call<HttpResponse<T>> call, final ServiceBI.Callback<T> subscriber) {
        call.enqueue(new retrofit2.Callback<HttpResponse<T>>() {
            @Override
            public void onResponse(Call<HttpResponse<T>> call, Response<HttpResponse<T>> response) {
                HttpResponse<T> body = response.body();
                if (!response.isSuccessful() || body == null) {
                    // 请求失败，转到onFailure方法
                    onFailure(call, new APIException("UnKnown Exception", APIErrorCode.ERROR_UNKNOWN));
                    return;
                }
                if (body.getCode() == HttpConstant.HTTP_OK_CODE && subscriber != null) {
                    // 调用回调接口的方法
                    subscriber.onResponse(response.code(), response.body().getData());
                } else {
                    onFailure(call, new APIException(body.getMessage(), body.getCode()));
                }
            }

            @Override
            public void onFailure(Call<HttpResponse<T>> call, Throwable t) {
                // 调用回调接口的方法
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
