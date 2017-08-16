package com.alivc.videochat.demo.logic;

import android.os.Bundle;

/**
 * 类的描述: 管理事件回调类
 */
public interface MgrCallback {
    /**
     * 方法描述: 将不同类型的事件和数据回调，根据类型分类更新UI
     *
     * @param eventType 用于区分事件类型的
     * @param data      回调过来的数据
     */
    void onEvent(int eventType, Bundle data);
}
