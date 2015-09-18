package cn.edu.zafu.easemob.activity;

import android.content.Intent;
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

import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMGroupManager;
import com.google.gson.Gson;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;

import cn.edu.zafu.easemob.R;
import cn.edu.zafu.easemob.model.RegisterModel;

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
                    RegisterModel bean= (RegisterModel) msg.obj;
                    if (bean.getStatus()==200){
                        Toast.makeText(getApplicationContext(),"注册成功！",Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(getApplicationContext(),"注册失败！"+bean.getMessage(),Toast.LENGTH_LONG).show();
                    }
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };
    private static final int REGISTER=0x01;
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

        RequestBody requestBody= new FormEncodingBuilder()
                .add("username",u)
                .add("password",p)
                .build();
        String url="http://10.0.0.24/huanxin/index.php";
        Request request=new Request.Builder().url(url).post(requestBody).build();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.e("TAG","Error,register failure.");
            }
            @Override
            public void onResponse(Response response) throws IOException {
                String result=response.body().string();
                RegisterModel bean=gson.fromJson(result,RegisterModel.class);
                Message message=Message.obtain();
                message.obj=bean;
                message.what=REGISTER;
                mHandler.sendMessage(message);
            }
        });
    }

    private void login() {
        String u=username.getText().toString();
        String p=password.getText().toString();
        if (TextUtils.isEmpty(u)||TextUtils.isEmpty(p)){
            Toast.makeText(getApplicationContext(),"账号或密码不能为空！",Toast.LENGTH_LONG).show();
            return ;
        }
        //这里先进行自己服务器的登录操作
        //自己服务器登录成功后再执行环信服务器的登录操作
        EMChatManager.getInstance().login(u, p, new EMCallBack() {//回调
            @Override
            public void onSuccess() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        EMGroupManager.getInstance().loadAllGroups();
                        EMChatManager.getInstance().loadAllConversations();
                        Toast.makeText(MainActivity.this, "登陆聊天服务器成功", Toast.LENGTH_SHORT).show();
                        Log.e("TAG", "登陆聊天服务器成功！");
                    }
                });
            }

            @Override
            public void onProgress(int progress, String status) {
                Log.e("TAG", "登陆聊天服务器中 " + "progress:" + progress + " status:" + status);
            }

            @Override
            public void onError(int code, String message) {
                Log.e("TAG", "登陆聊天服务器失败！");
            }
        });
    }

    private void logout() {
        //这里先进行自己服务器的退出操作
        //自己服务器登录成功后再执行环信服务器的退出操作

        //此方法为异步方法
        EMChatManager.getInstance().logout(new EMCallBack() {
            @Override
            public void onSuccess() {
                Log.e("TAG", "退出聊天服务器成功！");
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(MainActivity.this, "退出聊天服务器成功", Toast.LENGTH_SHORT).show();
                        Log.e("TAG", "退出聊天服务器成功！");
                    }
                });
            }

            @Override
            public void onProgress(int progress, String status) {
                Log.e("TAG", "退出聊天服务器中 " + " progress:" + progress + " status:" + status);

            }

            @Override
            public void onError(int code, String message) {
                Log.e("TAG", "退出聊天服务器失败！" + " code:" + code + " message:" + message);

            }
        });
    }


    private void voice() {
        if (!EMChatManager.getInstance().isConnected())
            Toast.makeText(this, "未连接到服务器", Toast.LENGTH_SHORT).show();
        else{
            String toUser=to.getText().toString();
            if (TextUtils.isEmpty(toUser)){
                Toast.makeText(MainActivity.this, "请填写接受方账号", Toast.LENGTH_SHORT).show();
                return ;
            }
            Intent intent = new Intent(MainActivity.this, VoiceCallActivity.class);
            intent.putExtra("username", toUser);
            intent.putExtra("isComingCall", false);
            startActivity(intent);
        }

    }

    private void video() {
        if (!EMChatManager.getInstance().isConnected()) {
            Toast.makeText(MainActivity.this, "未连接到服务器", Toast.LENGTH_SHORT).show();
        }
        else {
            String toUser=to.getText().toString();
            if (TextUtils.isEmpty(toUser)){
                Toast.makeText(MainActivity.this, "请填写接受方账号", Toast.LENGTH_SHORT).show();
                return ;
            }
            Intent intent = new Intent(MainActivity.this, VideoCallActivity.class);
            intent.putExtra("username", toUser);
            intent.putExtra("isComingCall", false);
            startActivity(intent);
        }
    }

}
