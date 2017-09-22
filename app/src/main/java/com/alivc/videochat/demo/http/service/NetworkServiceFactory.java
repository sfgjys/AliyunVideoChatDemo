package com.alivc.videochat.demo.http.service;

import com.alivc.videochat.demo.http.HttpConstant;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Service工厂类，根据不同的Service接口创建对应的Service对象实例，该实例对象在调用对应接口中获取Call的方法就可以获取到一个专属的Call对象
 */
public class NetworkServiceFactory {
    private static Retrofit sRetrofit;

    static {
        // 创建拦截器，并将拦截器添加进OkHttpClient，以便下面初始化Retrofit对象时用
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        // 设置拦截器打印  请求/响应行 + 头 + 体
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        // 初始化Retrofit对象
        sRetrofit = new Retrofit.Builder()
                .baseUrl(HttpConstant.HTTP_BASE_URL)
                .client(client)
                // 增加返回值为Gson的支持(以实体类返回)
                .addConverterFactory(GsonConverterFactory.create())// 注意：GsonConverterFactory需要converter-gson-2.1.0架包
                // 增加返回值为String的支持
                // .addConverterFactory(ScalarsConverterFactory.create())
                // 增加返回值为Oservable<T>的支持
                // .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
    }

    private static LiveNetworkService sLiveNetworkService = null;
    private static InviteNetworkService sInviteNetworkService = null;
    private static InteractionNetworkService sInteractionNetworkService = null;
    private static AccountNetworkService service = null;

    /**
     * 方法描述: 创建不同的网络请求接口
     *
     * @param clazz 接口类的class文件
     * @return 参数传递的是什么接口返回的就是对应接口的实例对象
     */
    private static <T> T createServiceInstance(Class<T> clazz) {
        return sRetrofit.create(clazz);
    }

    public static LiveNetworkService getLiveService() {
        if (sLiveNetworkService == null) {
            sLiveNetworkService = createServiceInstance(LiveNetworkService.class);
        }
        return sLiveNetworkService;
    }

    public static InviteNetworkService getInviteService() {
        if (sInviteNetworkService == null) {
            sInviteNetworkService = createServiceInstance(InviteNetworkService.class);
        }
        return sInviteNetworkService;
    }

    public static InteractionNetworkService getInteractionService() {
        if (sInteractionNetworkService == null) {
            sInteractionNetworkService = createServiceInstance(InteractionNetworkService.class);
        }
        return sInteractionNetworkService;
    }

    public static AccountNetworkService getAccountService() {
        if (service == null) {
            service = createServiceInstance(AccountNetworkService.class);
        }
        return service;
    }
}
