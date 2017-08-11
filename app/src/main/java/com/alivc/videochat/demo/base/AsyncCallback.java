package com.alivc.videochat.demo.base;

import android.os.Bundle;

/**
 * Created by apple on 2017/1/6.
 */

public interface AsyncCallback {
    void onSuccess(Bundle bundle);

    void onFailure(Bundle bundle, Throwable e);
}
