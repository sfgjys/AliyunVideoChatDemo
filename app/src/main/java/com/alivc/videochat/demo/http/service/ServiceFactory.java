package com.alivc.videochat.demo.http.service;

import com.alivc.videochat.demo.http.HttpConstant;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by liujianghao on 16-7-29.
 */
public class ServiceFactory {
    static {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();
        sRetrofit = new Retrofit.Builder()
                .baseUrl(HttpConstant.HTTP_BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    private static Retrofit sRetrofit;
    private static LiveService sLiveService = null;
    private static InviteService sInviteService = null;
    private static InteractionService sInteractionService = null;
    private static AccountService sAccountService = null;

    private static <T> T createServiceInstance(Class<T> clazz) {
        return sRetrofit.create(clazz);
    }

    public static LiveService getLiveService() {
        if (sLiveService == null) {
            sLiveService = createServiceInstance(LiveService.class);
        }
        return sLiveService;
    }

    public static InviteService getInviteService() {
        if (sInviteService == null) {
            sInviteService = createServiceInstance(InviteService.class);
        }
        return sInviteService;
    }

    public static InteractionService getInteractionService() {
        if (sInteractionService == null) {
            sInteractionService = createServiceInstance(InteractionService.class);
        }
        return sInteractionService;
    }

    public static AccountService getAccountService() {
        if (sAccountService == null) {
            sAccountService = createServiceInstance(AccountService.class);
        }
        return sAccountService;
    }
}
