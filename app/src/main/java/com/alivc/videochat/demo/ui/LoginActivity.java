package com.alivc.videochat.demo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.alivc.videochat.demo.R;
import com.alivc.videochat.demo.base.BaseActivity;
import com.alivc.videochat.demo.http.model.IMUserInfo;
import com.alivc.videochat.demo.presenter.LoginPresenter;
import com.alivc.videochat.demo.ui.view.LoginView;
import com.alivc.videochat.demo.uitils.PreferenceUtil;

import java.util.regex.Pattern;

/**
 * Created by liujianghao on 16-8-9.
 */
public class LoginActivity extends BaseActivity implements View.OnClickListener, TextWatcher {
    private static final String TAG = "LoginActivity";

    LoginPresenter mLoginPresenter;
    private EditText mEtUsername;
    private Button mBtnLoginNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLoginPresenter = new LoginPresenter(mLoginView);
        setContentView(R.layout.activity_login);
        mEtUsername = (EditText) findViewById(R.id.et_username);
        mBtnLoginNext = (Button) findViewById(R.id.iv_login_phone_next);
        mEtUsername.addTextChangedListener(this);
        mBtnLoginNext.setEnabled(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_login_phone_next:
                mLoginPresenter.login(mEtUsername.getText().toString());
                break;
//            case R.id.iv_login_phone_back:
//                finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLoginPresenter = null;
    }


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

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    private Pattern mNamePattern = Pattern.compile("[\\w|\\d]+");

    @Override
    public void afterTextChanged(Editable s) {
        String text = s.toString();
        Log.d(TAG, "username : " + text);
        if (mNamePattern.matcher(text).matches()) {
            Log.d(TAG, "text pattern true");
            mBtnLoginNext.setEnabled(true);
        } else {
            mBtnLoginNext.setEnabled(false);
        }
    }
}
