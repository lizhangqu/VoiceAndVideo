package cn.edu.zafu.easemob.app;

import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;

import com.easemob.chat.EMChat;
import com.easemob.chat.EMChatManager;

import cn.edu.zafu.easemob.receiver.CallReceiver;

/**
 * User:lizhangqu(513163535@qq.com)
 * Date:2015-09-18
 * Time: 11:31
 */
public class App extends Application{
    public static Context applicationContext;
    private CallReceiver callReceiver;
    @Override
    public void onCreate() {
        super.onCreate();
        applicationContext = this;
        EMChat.getInstance().init(this);
        EMChat.getInstance().setDebugMode(true);
        IntentFilter callFilter = new IntentFilter(EMChatManager.getInstance().getIncomingCallBroadcastAction());
        if(callReceiver == null){
            callReceiver = new CallReceiver();
        }
        //注册通话广播接收者
        this.registerReceiver(callReceiver, callFilter);
    }

}
