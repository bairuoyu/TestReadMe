package com.uneed.www.uneedgroups.ui.activity;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.uneed.sdk.agent.UNHttpAgent;
import com.uneed.sdk.constants.UNConstants;
import com.uneed.sdk.model.common.DeviceInfo;
import com.uneed.sdk.network.common.VerifyDataBean;
import com.uneed.sdk.network.http.response.UNHttpResponse;
import com.uneed.sdk.utils.StatusBarUtils;
import com.uneed.sdk.utils.ValueUtils;
import com.uneed.www.uneedgroups.R;
import com.uneed.www.uneedgroups.model.api.request.UNHttpPostLoginRequest;
import com.uneed.www.uneedgroups.model.api.request.UNHttpPostVerifyRequest;
import com.uneed.www.uneedgroups.model.api.response.ApplyCodeRespDataBean;
import com.uneed.www.uneedgroups.utils.CountDownTimerUtils;
import com.uneed.www.uneedgroups.utils.SoftKeyboardUtil;

public class LoginActivity extends AppCompatActivity implements View.OnLayoutChangeListener{
    private int keyHeight,screenHeight;
    private LinearLayout mLinearLayout , mLinearLayoutBackground;
    private LinearLayout mLinearLayoutWeChat,mLinearLayoutCountDown;
    private EditText mEditText;
    private TextView mTextViewCountryCode;
    private TextView mTextViewVerifyCode;
    private TextView mTextViewCountDown;
    private ImageView mNextImageView;
    private ProgressBar mProgressBar;
    private LoginActivity mContext;
    private String mPhone,mCountryCode;
    private String mApply_token;
    private boolean isSuccessCheckMobile;
    private static final String TAG = "LoginActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //状态栏透明
        //测试一下SourceTree
        //本地仓库测试一下sourceTree
        //develop branch add a line
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            StatusBarUtils.transparencyBar(this);
            StatusBarUtils.StatusBarLightMode(this);
            getWindow().setNavigationBarColor(Color.BLACK);
        }
        setContentView(R.layout.activity_login);
        mContext = this;
        SoftKeyboardUtil.assistActivity(this);
        initView();
        initListener();
    }

    private void initView() {
        mLinearLayout = (LinearLayout) findViewById(R.id.ll_login_root);
        mLinearLayoutWeChat = (LinearLayout) findViewById(R.id.ll_wechat_login);
        mLinearLayoutCountDown = (LinearLayout) findViewById(R.id.ll_count_down);
        mLinearLayoutBackground = (LinearLayout) findViewById(R.id.ll_login_logo_bg);
        mEditText = (EditText) findViewById(R.id.edit_mobile);
        mTextViewCountryCode = (TextView) findViewById(R.id.country_code);
        mTextViewVerifyCode = (TextView) findViewById(R.id.tv_verify_code);
        mTextViewCountDown = (TextView) findViewById(R.id.tv_count_down);
        mNextImageView = (ImageView) findViewById(R.id.iv_login_next);
        mProgressBar = (ProgressBar) findViewById(R.id.bar_login_progress);
        screenHeight = this.getWindowManager().getDefaultDisplay().getHeight(); //获取屏幕高度
        keyHeight = screenHeight / 3;//弹起高度为屏幕高度的1/3
    }


    private void initListener() {
        mNextImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isSuccessCheckMobile){
                    checkVerifyCode();
                }else{
                    login();
                }
            }
        });
        mTextViewCountryCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, PickCountryActivity.class);
//                intent.putExtra("position", mposition);
                mContext.startActivityForResult(intent,UNConstants.MORE_COUNTRY);
            }
        });
    }

    private void checkVerifyCode() {
        String mVerifyCode = mEditText.getText().toString();
        if(mVerifyCode.length() == 6){
            String deviceType = UNConstants.ANDROID;
            String deviceId = ValueUtils.getDeviceId();
            DeviceInfo deviceInfo = new DeviceInfo();
            deviceInfo.setDeviceid(deviceId);
            deviceInfo.setDevice_Type(deviceType);
            deviceInfo.setTerminal_Type(ValueUtils.isPad(mContext) ? "PAD" : "MOBILE");
            deviceInfo.setDevice_name(Build.MODEL);
            deviceInfo.setOs_version(Build.VERSION.SDK_INT + "");
            deviceInfo.setPush_token(mPhone);
            UNHttpPostVerifyRequest req = new UNHttpPostVerifyRequest();
            req.setCountryCode(mCountryCode);
            req.setDeviceInfo(deviceInfo);
            req.setMobile(mPhone);
            req.setApply_token(mApply_token);
            req.setVerify_code(mVerifyCode);
            Log.e(TAG, "toVerify: " + req);
            UNHttpAgent.shared.send(req, new UNHttpAgent.UNHttpAgentListener<UNHttpResponse<VerifyDataBean>>() {

                @Override
                public void onRequestFinished(boolean isSuccess, UNHttpResponse<VerifyDataBean> resp) {
                    Log.e(TAG, "onRequestFinished: =-" + resp);
                    if (isSuccess && resp.err.code == 200) {
                        Toast.makeText(mContext, "通过验证", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e(TAG, "onRequestFinished: --" + resp.err.code + "---" + isSuccess);
                    }
                }
            });
        }else{
            Toast.makeText(mContext, "验证码位数不对", Toast.LENGTH_SHORT).show();
        }


    }

    private void login() {

        mPhone = mEditText.getText().toString();
        mCountryCode = mTextViewCountryCode.getText().toString();
        if (mPhone == null || (mPhone != null && (mPhone.length() != 11) || mPhone.length() < 5)) {
            Toast.makeText(mContext, "请输入正确的手机号码", Toast.LENGTH_SHORT).show();
            return;
        }
        //输入框不可操作
        mEditText.setEnabled(false);
        mNextImageView.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);
        String androidId = ValueUtils.getDeviceId();
        DeviceInfo mDeviceInfo = new DeviceInfo();
        String deviceType = UNConstants.ANDROID;
        mDeviceInfo.setDevice_Type(deviceType);
        mDeviceInfo.setDeviceid(androidId);
        mDeviceInfo.setTerminal_Type(ValueUtils.isPad(mContext) ? "PAD" : "MOBILE");

        final UNHttpPostLoginRequest req = new UNHttpPostLoginRequest();
        req.setCountryCode(mCountryCode);
        req.setDeviceInfo(mDeviceInfo);
        req.setMobile(mPhone);
        UNHttpAgent.shared.send(req, new UNHttpAgent.UNHttpAgentListener<UNHttpResponse<ApplyCodeRespDataBean>>() {

            @Override
            public void onRequestFinished(boolean isSuccess, UNHttpResponse<ApplyCodeRespDataBean> resp) {
                if (isSuccess && resp.err.code == 200) {
                    mApply_token = resp.getData().getApply_token();
                    Log.e(TAG, "onRequestFinished: --" + resp);
                    handleVerifyCode();
                } else {
                    mEditText.setEnabled(true);
                }
            }
        });

    }

    private void handleVerifyCode() {
        isSuccessCheckMobile = true;
        mProgressBar.setVisibility(View.INVISIBLE);
        mNextImageView.setVisibility(View.VISIBLE);
        mEditText.setEnabled(true);
        mTextViewCountryCode.setVisibility(View.GONE);
        mEditText.setText("");
        mEditText.setHint("请输入短信验证码");
        mLinearLayoutWeChat.setVisibility(View.GONE);
        mLinearLayoutCountDown.setVisibility(View.VISIBLE);
        CountDownTimerUtils countDownTimerUtils = new CountDownTimerUtils(mTextViewCountDown,45000,1000);
        countDownTimerUtils.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLinearLayout.addOnLayoutChangeListener(this);
    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        Log.e(TAG,"screenHeight: "+screenHeight+" paramsHeight: "+mLinearLayoutBackground.getHeight()+" bottom: "+bottom+" oldBottom: "+oldBottom);
        //键盘弹出
        if (oldBottom != 0 && bottom != 0 && (oldBottom - bottom > keyHeight)) {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(mLinearLayoutBackground.getLayoutParams());
            lp.setMargins(0, 0, 0, 0);//设置包含logo的布局的位置
            mLinearLayoutBackground.setLayoutParams(lp);
            setLittleAnimation();
            Log.e(TAG,"screenHeight: "+screenHeight+" paramsHeight: "+mLinearLayoutBackground.getHeight()+" bottom: "+bottom+" oldBottom: "+oldBottom);
        } else if (oldBottom != 0 && bottom != 0 && (bottom - oldBottom > keyHeight)) {//键盘收回后，logo恢复原来大小，位置同样回到初始位置
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(mLinearLayoutBackground.getLayoutParams());
            lp.setMargins(0, 180, 0, 100);
            mLinearLayoutBackground.setLayoutParams(lp);
            setBigAnimation();
            Log.e(TAG,"screenHeight: "+screenHeight+" paramsHeight: "+mLinearLayoutBackground.getHeight()+" bottom: "+bottom+" oldBottom: "+oldBottom);
        }

    }

    protected void setLittleAnimation() {
        ValueAnimator animator = ValueAnimator.ofFloat(1.0f, 0.8f);
        animator.setTarget(mLinearLayoutBackground);
        animator.setDuration(1000).start();
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
        {
            @Override
            public void onAnimationUpdate(ValueAnimator animation)
            {
                mLinearLayoutBackground.setScaleX((Float) animation.getAnimatedValue());
                mLinearLayoutBackground.setScaleY((Float) animation.getAnimatedValue());
            }
        });
    }

    protected void setBigAnimation() {
        ValueAnimator animator = ValueAnimator.ofFloat(0.8f, 1.0f);
        animator.setTarget(mLinearLayoutBackground);
        animator.setDuration(1000).start();
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
        {
            @Override
            public void onAnimationUpdate(ValueAnimator animation)
            {
                mLinearLayoutBackground.setScaleX((Float) animation.getAnimatedValue());
                mLinearLayoutBackground.setScaleY((Float) animation.getAnimatedValue());
            }
        });
    }

}
