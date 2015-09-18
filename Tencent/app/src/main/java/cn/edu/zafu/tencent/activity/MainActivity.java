package cn.edu.zafu.tencent.activity;

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

import java.io.IOException;

import cn.edu.zafu.tencent.R;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private static final OkHttpClient mOkHttpClient=new OkHttpClient();
    private static final Gson gson=new Gson();
    private EditText username,password,to;
    private Button register,login,logout,video,voice;
    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case REGISTER:
                    break;
                case LOGIN:
                    break;
                case VIDEO:
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };
    private static final int REGISTER=0x01;
    private static final int LOGIN=0x02;
    private static final int VIDEO=0x03;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView() {
        username= (EditText) findViewById(R.id.username);
        password= (EditText) findViewById(R.id.password);
        to= (EditText) findViewById(R.id.to);
        register= (Button) findViewById(R.id.register);
        login= (Button) findViewById(R.id.login);
        logout= (Button) findViewById(R.id.logout);
        video= (Button) findViewById(R.id.video);
        voice= (Button) findViewById(R.id.voice);
        register.setOnClickListener(this);
        login.setOnClickListener(this);
        logout.setOnClickListener(this);
        video.setOnClickListener(this);
        voice.setOnClickListener(this);
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
            case R.id.voice:
                voice();
                break;
            case R.id.video:
                video();
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
        String url="http://10.0.0.24/huanxin/index.php";
        Request request=new Request.Builder().url(url).post(requestBody).build();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.e("TAG", "Error,register failure.");
            }
            @Override
            public void onResponse(Response response) throws IOException {
                String result=response.body().string();

            }
        });

        //获得rsa加密串后向腾讯服务器验证。

    }

    private void logout() {
        //这里先进行自己服务器的退出操作
        //自己服务器登录成功后再执行环信服务器的退出操作


    }


    private void voice() {

        String toUser=to.getText().toString();
        if (TextUtils.isEmpty(toUser)){
            Toast.makeText(MainActivity.this, "请填写接受方账号", Toast.LENGTH_SHORT).show();
            return ;
        }


    }

    private void video() {
        String toUser=to.getText().toString();
        if (TextUtils.isEmpty(toUser)){
            Toast.makeText(MainActivity.this, "请填写接受方账号", Toast.LENGTH_SHORT).show();
            return ;
        }
    }

}
