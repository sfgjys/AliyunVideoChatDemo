package com.alivc.videochat.demo.logic;

/**
 * 类的描述: Surface的各种状态
 */
public enum SurfaceStatus {
    /**
     * 变量的描述: Surface未被初始化
     */
    UNINITED,
    /**
     * 变量的描述: Surface被创建
     */
    CREATED,
    /**
     * 变量的描述: Surface改变了
     */
    CHANGED,
    /**
     * 变量的描述: Surface销毁了
     */
    DESTROYED,
    /**
     * 变量的描述: Surface被重新创建
     */
    RECREATED
}
