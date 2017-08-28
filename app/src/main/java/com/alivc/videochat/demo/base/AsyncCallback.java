package com.alivc.videochat.demo.base;

import android.os.Bundle;

/**
 * 类的描述: 成功与失败接口
 */
public interface AsyncCallback {
    void onSuccess(Bundle bundle);

    void onFailure(Bundle bundle, Throwable e);
}
