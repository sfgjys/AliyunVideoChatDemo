package com.alivc.videochat.demo.base;

import android.content.Context;

import java.lang.ref.SoftReference;

/**
 * Created by apple on 2017/1/6.
 */

public class ContextBase {
    private SoftReference<Context> mContextRef = null;

    public ContextBase(Context context) {
        mContextRef = new SoftReference<>(context);
    }

    public Context getContext() {
        if(mContextRef != null) {
            return mContextRef.get();
        }
        return null;
    }

}
