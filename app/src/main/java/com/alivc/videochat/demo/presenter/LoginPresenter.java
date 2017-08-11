package com.alivc.videochat.demo.presenter;

import com.alivc.videochat.demo.R;
import com.alivc.videochat.demo.bi.AccountServiceBI;
import com.alivc.videochat.demo.bi.ServiceBI;
import com.alivc.videochat.demo.http.model.LoginResult;
import com.alivc.videochat.demo.ui.view.LoginView;

import retrofit2.Call;

/**
 * Created by liujianghao on 16-8-9.
 */
public class LoginPresenter {

    private AccountServiceBI mAccountServiceBI = new AccountServiceBI();

    private Call mLoginCall;

    private LoginView mLoginView;

    public LoginPresenter(LoginView mLoginView) {
        this.mLoginView = mLoginView;
    }

    /**
     * 登陆
     *
     * @param username
     */
    public void login(String username) {
        if (ServiceBI.isCalling(mLoginCall)) {
            mLoginCall.cancel();
        }

        mLoginCall = mAccountServiceBI.login(username, mLoginCallback);
    }

    private ServiceBI.Callback<LoginResult> mLoginCallback = new ServiceBI.Callback<LoginResult>() {
        @Override
        public void onResponse(int code, LoginResult response) {
            //登陆成功，保存登陆信息
            mLoginView.saveLoginInfo(response.getId());

            //初始化ImManager
            mLoginView.initImManager(response.getImUserInfo());

            //跳转到主页
            mLoginView.gotoMainActivity();
            mLoginCall = null;
        }

        @Override
        public void onFailure(Throwable e) {
            e.printStackTrace();
            mLoginView.showErrorInfo(R.string.login_failed);
            mLoginCall = null;
        }
    };


}
