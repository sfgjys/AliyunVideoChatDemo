package com.alivc.videochat.demo.uitils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

public class ToastUtils {

    public static Toast toast;
    public ToastUtils() {
    }

    public static void showToast(final Context context, final String text) {
        if(context != null) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public static void showToast(Context context, int resID) {
        if(context != null) {
            showToast(context, context.getString(resID));
        }
    }


}
