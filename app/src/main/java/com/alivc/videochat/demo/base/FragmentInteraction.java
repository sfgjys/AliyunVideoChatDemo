package com.alivc.videochat.demo.base;

import android.os.Bundle;

/**
 * 类的描述: 用于链接Activity与Fragment之间的数据交互
 */
public interface FragmentInteraction {
    /**
     * 方法描述: Activity与Fragment之间的数据交互
     *
     * @param actionType 用于区分
     * @param bundle     从Fragment传递给Activity的数据
     */
    void onPendingAction(int actionType, Bundle bundle);
}
