package cn.edu.zafu.tencent.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.tencent.av.sdk.AVConstants;

import java.io.IOException;

import cn.edu.zafu.tencent.R;
import cn.edu.zafu.tencent.app.App;
import cn.edu.zafu.tencent.control.QavsdkControl;
import cn.edu.zafu.tencent.model.LoginModel;
import cn.edu.zafu.tencent.util.Util;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private static final OkHttpClient mOkHttpClient=new OkHttpClient();
    private static final Gson gson=new Gson();
    private EditText username,password;
    private Button register,login,logout;


    private QavsdkControl mQavsdkControl;
    private int mLoginErrorCode = AVConstants.AV_ERROR_OK;



    private String sign=null;
    private static final int LOGIN=0x01;
    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case LOGIN:
                    LoginModel bean= (LoginModel) msg.obj;
                    if (bean.getStatus()==200){
                        sign=bean.getMessage();
                        //保存sign
                        Log.e("TAG","sign:"+sign);
                        Toast.makeText(getApplicationContext(),"登录成功,请稍后！",Toast.LENGTH_LONG).show();
                        startTencentContext();
                    }else{
                        Toast.makeText(getApplicationContext(),"登录失败！"+bean.getMessage(),Toast.LENGTH_LONG).show();
                    }
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private void startTencentContext() {
        //发起音视频前需要向腾讯服务器验证。
        String u=username.getText().toString();
        String p=password.getText().toString();
        if (TextUtils.isEmpty(u)||TextUtils.isEmpty(p)){
            Toast.makeText(getApplicationContext(),"账号或密码不能为空！",Toast.LENGTH_LONG).show();
            return ;
        }
        if(TextUtils.isEmpty(sign)){
            Toast.makeText(MainActivity.this, "请先登录", Toast.LENGTH_SHORT).show();
            return ;
        }
        if (!mQavsdkControl.hasAVContext()) {
            if (!mQavsdkControl.isDefaultAppid()) {
                Toast.makeText(getApplicationContext(), getString(R.string.help_msg_appid_not_default), Toast.LENGTH_LONG).show();
            }
            if (!mQavsdkControl.isDefaultUid()) {
                Toast.makeText(getApplicationContext(), getString(R.string.help_msg_uid_not_default), Toast.LENGTH_LONG).show();
            }
            mLoginErrorCode = mQavsdkControl.startContext(u, sign);

            if (mLoginErrorCode != AVConstants.AV_ERROR_OK) {
                Toast.makeText(getApplicationContext(),"错误码:"+mLoginErrorCode, Toast.LENGTH_LONG).show();
            }
        }
    }

    private BroadcastReceiver mStartContextBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.e("TAG", "WL_DEBUG onReceive action = " + action);
            Log.e("TAG", "WL_DEBUG ANR StartContextActivity onReceive action = " + action + " In");
            if (action.equals(Util.ACTION_START_CONTEXT_COMPLETE)) {
                mLoginErrorCode = intent.getIntExtra( Util.EXTRA_AV_ERROR_RESULT, AVConstants.AV_ERROR_OK);
                if (mLoginErrorCode == AVConstants.AV_ERROR_OK) {
                    Intent i=new Intent(MainActivity.this,CallActivity.class);
                    Bundle bundle=new Bundle();
                    bundle.putString("from",username.getText().toString());
                    i.putExtras(bundle);
                    startActivity(i);
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.help_msg_login_error), Toast.LENGTH_LONG).show();

                }
            } else if (action.equals(Util.ACTION_CLOSE_CONTEXT_COMPLETE)) {

            }
            Log.e("TAG", "WL_DEBUG ANR StartContextActivity onReceive action = " + action + " Out");
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initStartContextBroadcast();
        mQavsdkControl = ((App) getApplication()).getQavsdkControl();
    }


    private void initStartContextBroadcast() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Util.ACTION_START_CONTEXT_COMPLETE);
        intentFilter.addAction(Util.ACTION_CLOSE_CONTEXT_COMPLETE);
        registerReceiver(mStartContextBroadcastReceiver, intentFilter);
    }

    private void initView() {
        username= (EditText) findViewById(R.id.username);
        password= (EditText) findViewById(R.id.password);
        register= (Button) findViewById(R.id.register);
        login= (Button) findViewById(R.id.login);
        logout= (Button) findViewById(R.id.logout);
        register.setOnClickListener(this);
        login.setOnClickListener(this);
        logout.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.register:
                register();
                break;
            case R.id.login:
                login();
                break;
            case R.id.logout:
                logout();
                break;
            default:
                break;
        }
    }


    private void register() {
        String u=username.getText().toString();
        String p=password.getText().toString();
        if (TextUtils.isEmpty(u)||TextUtils.isEmpty(p)){
            Toast.makeText(getApplicationContext(),"账号或密码不能为空！",Toast.LENGTH_LONG).show();
            return ;
        }
        //独立账号体系，这里由自己服务器进行注册
        Toast.makeText(getApplicationContext(),"注册逻辑由自己服务器实现，这里是空实现",Toast.LENGTH_LONG).show();

    }

    private void login() {
        String u=username.getText().toString();
        String p=password.getText().toString();
        if (TextUtils.isEmpty(u)||TextUtils.isEmpty(p)){
            Toast.makeText(getApplicationContext(),"账号或密码不能为空！",Toast.LENGTH_LONG).show();
            return ;
        }
        //首先登录自己的服务器
        //自己服务器登录成功后，服务器需要返回rsa加密后的串
        RequestBody requestBody= new FormEncodingBuilder()
                .add("username",u)
                .add("password",p)
                .build();
        String url="http://10.0.0.24/tencent/index.php";
        Request request=new Request.Builder().url(url).post(requestBody).build();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.e("TAG", "Error,register failure.");
            }

            @Override
            public void onResponse(Response response) throws IOException {
                String result = response.body().string();
                LoginModel bean = gson.fromJson(result, LoginModel.class);
                Message message = Message.obtain();
                message.obj = bean;
                message.what = LOGIN;
                mHandler.sendMessage(message);
            }
        });

        //获得rsa加密串后发起音视频前需要向腾讯服务器验证。

    }

    private void logout() {
        //这里进行自己服务器的退出操作
        Toast.makeText(getApplicationContext(),"退出逻辑由自己服务器实现，这里是空实现",Toast.LENGTH_LONG).show();
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mStartContextBroadcastReceiver != null) {
            unregisterReceiver(mStartContextBroadcastReceiver);
        }

    }


}
