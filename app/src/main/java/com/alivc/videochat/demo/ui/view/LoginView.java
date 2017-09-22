package com.alivc.videochat.demo.ui.view;

import com.alivc.videochat.demo.http.result.IMUserInfo;

/**
 * 类的描述: 有着自定义 跳转Activity，展示错误信息，保存登录信息，初始化ImManager 方法的具体内容的接口
 */
public interface LoginView {
    void gotoMainActivity();

    void showErrorInfo(int resID);

    void saveLoginInfo(String uid);

    void initImManager(IMUserInfo imUserInfo);
}
