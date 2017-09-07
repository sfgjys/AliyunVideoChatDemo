package com.alivc.videochat.demo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.alivc.videochat.demo.R;
import com.alivc.videochat.demo.base.BaseActivity;
import com.alivc.videochat.demo.http.model.IMUserInfo;
import com.alivc.videochat.demo.presenter.LoginPresenter;
import com.alivc.videochat.demo.ui.view.LoginView;
import com.alivc.videochat.demo.uitils.PreferenceUtil;

import java.util.regex.Pattern;

/**
 * 技巧:
 * 1、一个Button按钮要在EditText输入完成后才能运行点击事件中的代码，那么先让Button不可点击，当EditText的TextWatcher接口中的afterTextChanged方法被调用，
 * 且满足afterTextChanged方法中进行的判断，则Button可以点击
 * 2、Activity界面都是进行UI操作的，其他一些网络操作、开线程、读数据库等操作放到另一个类中实现，注意在创建这个操作类的实例对象时要传递一个回调接口实例，
 * 让这个回调接口实例调用方法接收操作类的操作结果，这样在Activity中实例化的该回调接口的方法就会接收到操作结果，以便Activity根据操作结果来更新UI
 */
public class LoginActivity extends BaseActivity {
    private static final String TAG = "LoginActivity";

    LoginPresenter mLoginPresenter;
    private EditText mInputUserName;
    private Button mLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 登录具体操作对象
        mLoginPresenter = new LoginPresenter(mLoginView);

        setContentView(R.layout.activity_login);
        mInputUserName = (EditText) findViewById(R.id.et_username);
        mLoginButton = (Button) findViewById(R.id.iv_login_phone_next);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLoginPresenter.login(mInputUserName.getText().toString());
            }
        });
        mInputUserName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Pattern mNamePattern = Pattern.compile("[\\w|\\d]+");

                // 使用正则表达式去约束输入的内容
                String text = s.toString();
                Log.d(TAG, "username : " + text);
                if (mNamePattern.matcher(text).matches()) {
                    Log.d(TAG, "text pattern true");
                    mLoginButton.setEnabled(true);
                } else {
                    mLoginButton.setEnabled(false);
                }
            }
        });
        // mInputUserName在没有输入文本前就不能用按钮点击
        mLoginButton.setEnabled(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLoginPresenter = null;
    }

    // **************************************************** 登录请求结果具体操作对象 ****************************************************
    private LoginView mLoginView = new LoginView() {
        /**
         * 方法描述: 跳转到主页(MainActivity)
         */
        @Override
        public void gotoMainActivity() {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
        }

        /**
         * 方法描述: 该方法吐司参数resID所代表的字符串。
         * @param   resID 填写字符串在string文件中的资源id，内容应该是错误信息字符串
         */
        @Override
        public void showErrorInfo(int resID) {
            Toast.makeText(LoginActivity.this, getString(resID), Toast.LENGTH_SHORT).show();
        }

        /**
         * 方法描述:       保存登录成功后的数据
         * @param uid     猜测是登录成功后返回的一个需要重复使用的数据
         */
        @Override
        public void saveLoginInfo(String uid) {
            PreferenceUtil preferenceUtil = getPreferenceUtil();
            if (preferenceUtil != null) {
                int ref = preferenceUtil.write(PreferenceUtil.REF_USER_INFO, PREFERENCE_KEY_UID, uid);
                if (ref < 0) {
                    Log.e(TAG, "User login info save failed");
                } else {
                    Log.d(TAG, "User login info save succeed, id = " + uid);
                }
            } else {
                Log.e(TAG, "PreferenceUtil not initialize, saving user info failed");
            }
        }

        /**
         * 方法描述: 暂时没有用处
         * @param   imUserInfo 暂时没有用处
         */
        @Override
        public void initImManager(IMUserInfo imUserInfo) {
//            if(imUserInfo != null) {
//                ((AlivcApplication)getApplication()).getImManager().init(LoginActivity.this, imUserInfo.getUsername());
//            }
        }
    };

    // --------------------------------------------------------------------------------------------------------
}
