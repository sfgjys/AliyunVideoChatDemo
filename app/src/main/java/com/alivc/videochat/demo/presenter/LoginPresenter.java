package com.alivc.videochat.demo.presenter;

import com.alivc.videochat.demo.R;
import com.alivc.videochat.demo.bi.AccountServiceBI;
import com.alivc.videochat.demo.bi.ServiceBI;
import com.alivc.videochat.demo.http.result.LoginResult;
import com.alivc.videochat.demo.ui.view.LoginView;

import retrofit2.Call;

/**
 * 类的描述: 该类是用于分装具体登录方法和对登录返回结果进行具体操作的类,而登录返回结果具体的操作则是用一个接口放给用户具体去写
 */
public class LoginPresenter {

    private LoginView mLoginView;

    /**
     * 方法描述: 本类的构造函数
     *
     * @param mLoginView 这个参数用来在登录成功后进行操作
     */
    public LoginPresenter(LoginView mLoginView) {
        this.mLoginView = mLoginView;
    }

    // --------------------------------------------------------------------------------------------------------

    private AccountServiceBI mAccountServiceBI = new AccountServiceBI();

    private Call mLoginCall;

    /**
     * 变量的描述: 该对象是对一个网络请求结果监听接口的具体实现
     */
    private ServiceBI.Callback<LoginResult> mLoginCallback = new ServiceBI.Callback<LoginResult>() {
        @Override
        public void onResponse(int code, LoginResult response) {
            //登陆成功，使用SharedPreferences保存登陆信息
            mLoginView.saveLoginInfo(response.getId());

            // 初始化ImManager ，但是在具体实现initImManager方法时并没有内容
            mLoginView.initImManager(null);

            //跳转到主页
            mLoginView.gotoMainActivity();

            // 将登录时创建的Call任务释放
            mLoginCall = null;
        }

        @Override
        public void onFailure(Throwable e) {
            e.printStackTrace();
            // 显示错误
            mLoginView.showErrorInfo(R.string.login_failed);

            // 将登录时创建的Call任务释放
            mLoginCall = null;
        }
    };

    /**
     * 方法描述: 根据参数开启具体的登录操作
     *
     * @param username 登陆界面上输入框中的内容
     */
    public void login(String username) {
        // 判断Call是否还存在是否还在入队，执行  注意：虽然在请求结果的最后会释放Call，但是如果用户持续按登录，则login也会持续被调用
        if (ServiceBI.isCalling(mLoginCall)) {
            // 是的话，就取消Call
            mLoginCall.cancel();
        }
        // 重新创建一个Call任务
        mLoginCall = mAccountServiceBI.login(username, mLoginCallback);
    }
}
