package cn.edu.zafu.tencent.activity;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.tencent.av.sdk.AVConstants;

import cn.edu.zafu.tencent.R;
import cn.edu.zafu.tencent.app.App;
import cn.edu.zafu.tencent.control.QavsdkControl;
import cn.edu.zafu.tencent.util.Util;


public class CallActivity extends AppCompatActivity implements View.OnClickListener{
    private String from;
    private EditText to;
    private Button video,voice;

    private QavsdkControl mQavsdkControl;

    private int mCreateRoomErrorCode = AVConstants.AV_ERROR_OK;
    private int mCloseRoomErrorCode = AVConstants.AV_ERROR_OK;
    private int mAcceptErrorCode = AVConstants.AV_ERROR_OK;
    private int mInviteErrorCode = AVConstants.AV_ERROR_OK;
    private int mRefuseErrorCode = AVConstants.AV_ERROR_OK;
    private int mJoinRoomErrorCode = AVConstants.AV_ERROR_OK;


    private String mReceiveIdentifier = "";
    private String mSelfIdentifier = "";
    private boolean mIsVideo = false;
    private boolean isSender = false;
    private boolean isReceiver = false;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.e("TAG", "WL_DEBUG onReceive action = " + action);
            Log.e("TAG", "WL_DEBUG ANR CreateRoomActivity onReceive action = " + action + " In");
            if (action.equals(Util.ACTION_ACCEPT_COMPLETE)) {
                String identifier = intent.getStringExtra(Util.EXTRA_IDENTIFIER);
                mSelfIdentifier = intent.getStringExtra(Util.EXTRA_SELF_IDENTIFIER);
                mReceiveIdentifier = identifier;
                long roomId = intent.getLongExtra(Util.EXTRA_ROOM_ID, -1);
                mAcceptErrorCode = intent.getIntExtra(
                        Util.EXTRA_AV_ERROR_RESULT, AVConstants.AV_ERROR_OK);

                if (mAcceptErrorCode == AVConstants.AV_ERROR_OK) {
                    mQavsdkControl.enterRoom(roomId, identifier, mIsVideo);
                } else {
                    Toast.makeText(CallActivity.this,R.string.accept_failed,Toast.LENGTH_SHORT).show();
                }
            } else if (action.equals(Util.ACTION_CLOSE_ROOM_COMPLETE)) {
            } else if (action.equals(Util.ACTION_INVITE_ACCEPTED)) {

                startActivity(mReceiveIdentifier,mSelfIdentifier);
            } else if (action.equals(Util.ACTION_INVITE_CANCELED)) {
                    Toast.makeText(getApplicationContext(), R.string.invite_canceled_toast,
                            Toast.LENGTH_LONG).show();
            } else if (action.equals(Util.ACTION_INVITE_COMPLETE)) {
                if (isReceiver) {
                    Toast.makeText(getApplicationContext(), R.string.notify_conflict,
                            Toast.LENGTH_LONG).show();
                }

                mInviteErrorCode = intent.getIntExtra(
                        Util.EXTRA_AV_ERROR_RESULT, AVConstants.AV_ERROR_OK);

                if (mInviteErrorCode == AVConstants.AV_ERROR_OK) {
                    //等待对方接受邀请
                    Toast.makeText(getApplicationContext(), R.string.dialog_waitting_title,
                            Toast.LENGTH_LONG).show();

                } else {
                    //邀请失败
                    Toast.makeText(getApplicationContext(), R.string.invite_failed,
                            Toast.LENGTH_LONG).show();
                    closeRoom();
                }

            } else if (action.equals(Util.ACTION_INVITE_REFUSED)) {
                    Toast.makeText(getApplicationContext(), R.string.invite_refused_toast,
                            Toast.LENGTH_LONG).show();
            } else if (action.equals(Util.ACTION_RECV_INVITE)) {
                if (isSender) {
                    Toast.makeText(getApplicationContext(), R.string.notify_conflict,
                            Toast.LENGTH_LONG).show();
                }

                isReceiver = true;
                mIsVideo = intent.getBooleanExtra(Util.EXTRA_IS_VIDEO, false);
                new AlertDialog.Builder(CallActivity.this)
                        .setTitle(R.string.invite_title)
                        .setPositiveButton(android.R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        mQavsdkControl.accept();
                                    }
                                })
                        .setNegativeButton(android.R.string.cancel,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        mQavsdkControl.refuse();
                                        isSender = isReceiver = false;
                                    }
                                })
                        .setOnCancelListener(
                                new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialog) {
                                        Log.e("TAG", "WL_DEBUG onCancel");
                                        mQavsdkControl.refuse();
                                        isSender = isReceiver = false;
                                    }
                                }).create().show();




            } else if (action.equals(Util.ACTION_REFUSE_COMPLETE)) {
                mRefuseErrorCode = intent.getIntExtra(
                        Util.EXTRA_AV_ERROR_RESULT, AVConstants.AV_ERROR_OK);

                if (mRefuseErrorCode != AVConstants.AV_ERROR_OK) {
                    Toast.makeText(getApplicationContext(), R.string.refuse_failed,
                            Toast.LENGTH_LONG).show();
                }

            } else if (action.equals(Util.ACTION_ROOM_CREATE_COMPLETE)) {
                mCreateRoomErrorCode = intent.getIntExtra(
                        Util.EXTRA_AV_ERROR_RESULT, AVConstants.AV_ERROR_OK);
                if (mCreateRoomErrorCode != AVConstants.AV_ERROR_OK) {
                    Toast.makeText(getApplicationContext(), R.string.create_room_failed,
                            Toast.LENGTH_LONG).show();
                }
            } else if (action.equals(Util.ACTION_ROOM_JOIN_COMPLETE)) {
                mJoinRoomErrorCode = intent.getIntExtra(
                        Util.EXTRA_AV_ERROR_RESULT, AVConstants.AV_ERROR_OK);
                if (mJoinRoomErrorCode != AVConstants.AV_ERROR_OK) {
                    Toast.makeText(getApplicationContext(), R.string.join_room_failed,
                            Toast.LENGTH_LONG).show();
                } else {
                    startActivity(mReceiveIdentifier,mSelfIdentifier);
                }
            }
            Log.e("TAG", "WL_DEBUG ANR CreateRoomActivity onReceive action = " + action + " Out");
        }
    };

    private void closeRoom() {
        mCloseRoomErrorCode = mQavsdkControl.exitRoom();
        if (mCloseRoomErrorCode != AVConstants.AV_ERROR_OK) {
            Toast.makeText(CallActivity.this,R.string.close_room_failed,Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);
        initIntent();
        initView();
        initInviteBroadcast();
        mQavsdkControl = ((App) getApplication()).getQavsdkControl();
    }

    private void initIntent() {
        Intent intent=getIntent();
        Bundle bundle=intent.getExtras();
        from=bundle.getString("from");
        Log.e("TAG","from:"+from);
    }

    private void initInviteBroadcast() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Util.ACTION_ACCEPT_COMPLETE);
        intentFilter.addAction(Util.ACTION_CLOSE_ROOM_COMPLETE);
        intentFilter.addAction(Util.ACTION_INVITE_ACCEPTED);
        intentFilter.addAction(Util.ACTION_INVITE_CANCELED);
        intentFilter.addAction(Util.ACTION_INVITE_COMPLETE);
        intentFilter.addAction(Util.ACTION_INVITE_REFUSED);
        intentFilter.addAction(Util.ACTION_RECV_INVITE);
        intentFilter.addAction(Util.ACTION_REFUSE_COMPLETE);
        intentFilter.addAction(Util.ACTION_ROOM_CREATE_COMPLETE);
        intentFilter.addAction(Util.ACTION_ROOM_JOIN_COMPLETE);
        registerReceiver(mBroadcastReceiver, intentFilter);
    }



    private void initView() {
        to= (EditText) findViewById(R.id.to);
        video= (Button) findViewById(R.id.video);
        voice= (Button) findViewById(R.id.voice);
        video.setOnClickListener(this);
        voice.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
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






    private void voice() {
        mSelfIdentifier=from;
        mReceiveIdentifier=to.getText().toString();
        mIsVideo=false;
        if (Util.isNetworkAvailable(getApplicationContext())) {
            if (TextUtils.isEmpty(mSelfIdentifier)) {
                Toast.makeText(getApplicationContext(), getString(R.string.help_msg_send_account_error), Toast.LENGTH_SHORT).show();
            } else if (TextUtils.isEmpty(mReceiveIdentifier)) {
                Toast.makeText(getApplicationContext(), getString(R.string.help_msg_recv_account_error), Toast.LENGTH_SHORT).show();
            } else {
                if (mSelfIdentifier.equals(mReceiveIdentifier)) {
                    Toast.makeText(getApplicationContext(), getString(R.string.help_msg_account_send_equal_recv), Toast.LENGTH_SHORT).show();
                } else {
                    invite(mIsVideo);
                    isSender = true;
                }
            }
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.notify_no_network), Toast.LENGTH_SHORT).show();
        }

    }

    private void video() {
        mSelfIdentifier=from;
        mReceiveIdentifier=to.getText().toString();
        mIsVideo=true;
        if (Util.isNetworkAvailable(getApplicationContext())) {
            if (TextUtils.isEmpty(mSelfIdentifier)) {
                Toast.makeText(getApplicationContext(), getString(R.string.help_msg_send_account_error), Toast.LENGTH_SHORT).show();
            } else if (TextUtils.isEmpty(mReceiveIdentifier)) {
                Toast.makeText(getApplicationContext(), getString(R.string.help_msg_recv_account_error), Toast.LENGTH_SHORT).show();
            } else {
                if (mSelfIdentifier.equals(mReceiveIdentifier)) {
                    Toast.makeText(getApplicationContext(), getString(R.string.help_msg_account_send_equal_recv), Toast.LENGTH_SHORT).show();
                } else {
                    invite(mIsVideo);
                    isSender = true;
                }
            }
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.notify_no_network), Toast.LENGTH_SHORT).show();
        }

    }



    private void startActivity(String mReceiveIdentifier,String mSelfIdentifier) {
        Log.e("TAG", "WL_DEBUG startActivity");
        if ((mQavsdkControl != null) && (mQavsdkControl.getAVContext() != null) && (mQavsdkControl.getAVContext().getAudioCtrl() != null)) {
            mQavsdkControl.getAVContext().getAudioCtrl().startTRAEService();
        }
        startActivityForResult(
                new Intent(Intent.ACTION_MAIN)
                        .putExtra(Util.EXTRA_IDENTIFIER, mReceiveIdentifier)
                        .putExtra(Util.EXTRA_SELF_IDENTIFIER, mSelfIdentifier)
                        .setClass(this, AvActivity.class),
                0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e("TAG", "WL_DEBUG onActivityResult");
        isSender = isReceiver = false;
        if ((mQavsdkControl != null) && (mQavsdkControl.getAVContext() != null) && (mQavsdkControl.getAVContext().getAudioCtrl() != null)) {
            mQavsdkControl.getAVContext().getAudioCtrl().stopTRAEService();
        }
        closeRoom();
    }


    private void invite(boolean isVideo) {
        if (TextUtils.isEmpty(mReceiveIdentifier))
            return;
        mQavsdkControl.invite(mReceiveIdentifier, isVideo);

    }

}
