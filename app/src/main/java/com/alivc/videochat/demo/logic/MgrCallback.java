package com.alivc.videochat.demo.logic;

import android.os.Bundle;

/**
 * Created by apple on 2017/1/9.
 */

public interface MgrCallback {
    void onEvent(int eventType, Bundle data);
}
