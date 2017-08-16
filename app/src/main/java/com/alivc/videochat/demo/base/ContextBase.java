package com.alivc.videochat.demo.base;

import android.content.Context;

import java.lang.ref.SoftReference;

/**
 * 类的描述: 使用软引用的方式将构造时传递进来的Context获取出来
 */
public class ContextBase {
    private SoftReference<Context> mContextRef = null;

    public ContextBase(Context context) {
        mContextRef = new SoftReference<>(context);
    }

    public Context getContext() {
        if (mContextRef != null) {
            return mContextRef.get();
        }
        return null;
    }
}
