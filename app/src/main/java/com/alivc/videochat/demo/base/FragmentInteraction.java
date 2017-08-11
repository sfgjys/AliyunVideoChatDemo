package com.alivc.videochat.demo.base;

import android.os.Bundle;

/**
 * Created by liujianghao on 16-9-28.
 */

public interface FragmentInteraction {
    void onPendingAction(int actionType, Bundle bundle);
}
