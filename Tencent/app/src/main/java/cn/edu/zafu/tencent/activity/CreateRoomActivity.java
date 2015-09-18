package cn.edu.zafu.tencent.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.av.sdk.AVConstants;

import cn.edu.zafu.tencent.QavsdkApplication;
import cn.edu.zafu.tencent.R;
import cn.edu.zafu.tencent.control.QavsdkControl;
import cn.edu.zafu.tencent.util.DialogWaitingAsyncTask;
import cn.edu.zafu.tencent.util.Util;

public class CreateRoomActivity extends Activity implements OnClickListener {
	private static final String TAG = "CreateRoomActivity";
	private static final int DIALOG_QQ_LIST = 0;
	private static final int DIALOG_WAITING = DIALOG_QQ_LIST + 1;
	private static final int DIALOG_INVITE = DIALOG_WAITING + 1;
	private static final int DIALOG_CREATE_ROOM = DIALOG_INVITE + 1;
	private static final int DIALOG_CLOSE_ROOM = DIALOG_CREATE_ROOM + 1;
	private static final int DIALOG_CREATE_ROOM_ERROR = DIALOG_CLOSE_ROOM + 1;
	private static final int DIALOG_CLOSE_ROOM_ERROR = DIALOG_CREATE_ROOM_ERROR + 1;
	private static final int DIALOG_AT_ACCEPT = DIALOG_CLOSE_ROOM_ERROR + 1;
	private static final int DIALOG_ACCEPT_ERROR = DIALOG_AT_ACCEPT + 1;
	private static final int DIALOG_AT_INVITE = DIALOG_ACCEPT_ERROR + 1;
	private static final int DIALOG_INVITE_ERROR = DIALOG_AT_INVITE + 1;
	private static final int DIALOG_AT_REFUSE = DIALOG_INVITE_ERROR + 1;
	private static final int DIALOG_REFUSE_ERROR = DIALOG_AT_REFUSE + 1;
	private static final int DIALOG_AT_JOIN_ROOM = DIALOG_REFUSE_ERROR + 1;
	private static final int DIALOG_JOIN_ROOM_ERROR = DIALOG_AT_JOIN_ROOM + 1;
	private static final int MAX_PROGRESS = 100;
	private static final int MAX_SENCONDS = 30;

	private boolean mIsVideo = false;

	private int mReceiveQQListIndex = -1;
	private String mReceiveIdentifier = "";
	private String mSelfIdentifier = "";
	private int mCreateRoomErrorCode = AVConstants.AV_ERROR_OK;
	private int mCloseRoomErrorCode = AVConstants.AV_ERROR_OK;
	private int mAcceptErrorCode = AVConstants.AV_ERROR_OK;
	private int mInviteErrorCode = AVConstants.AV_ERROR_OK;
	private int mRefuseErrorCode = AVConstants.AV_ERROR_OK;
	private int mJoinRoomErrorCode = AVConstants.AV_ERROR_OK;
	private AlertDialog mDialogInvite = null;
	private ProgressDialog mDialogWaiting = null;
	private ProgressDialog mDialogCreateRoom = null;
	private ProgressDialog mDialogCloseRoom = null;
	private ProgressDialog mDialogAtAccept = null;
	private ProgressDialog mDialogAtInvite = null;
	private ProgressDialog mDialogAtRefuse = null;
	private ProgressDialog mDialogAtJoinRoom = null;
	private QavsdkControl mQavsdkControl;
	private Context ctx = null;
	private boolean isSender = false;
	private boolean isReceiver = false;
	private EditText peerIdentifierEditior;
	
	
	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.e(TAG, "WL_DEBUG onReceive action = " + action);
			Log.e(TAG, "WL_DEBUG ANR CreateRoomActivity onReceive action = " + action + " In");					
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
					showDialog(DIALOG_ACCEPT_ERROR);
				}

				refreshWaitingDialog();
			} else if (action.equals(Util.ACTION_CLOSE_ROOM_COMPLETE)) {
				refreshWaitingDialog();
			} else if (action.equals(Util.ACTION_INVITE_ACCEPTED)) {
				if (mDialogWaiting != null && mDialogWaiting.isShowing()) {
					mDialogWaiting.dismiss();
				}
				dismissAllWaitingDialog();
				startActivity();
			} else if (action.equals(Util.ACTION_INVITE_CANCELED)) {
				if (mDialogInvite != null && mDialogInvite.isShowing()) {
					Toast.makeText(getApplicationContext(), R.string.invite_canceled_toast,
							Toast.LENGTH_LONG).show();
					mDialogInvite.dismiss();
				}
			} else if (action.equals(Util.ACTION_INVITE_COMPLETE)) {
				if (isReceiver) {
					Toast.makeText(getApplicationContext(), R.string.notify_conflict,
							Toast.LENGTH_LONG).show();	
				} 

				mInviteErrorCode = intent.getIntExtra(
						Util.EXTRA_AV_ERROR_RESULT, AVConstants.AV_ERROR_OK);

				if (mInviteErrorCode == AVConstants.AV_ERROR_OK) {
					showDialog(DIALOG_WAITING);
					new DialogWaitingAsyncTask(mDialogWaiting)
							.execute(MAX_SENCONDS);
				} else {
					showDialog(DIALOG_INVITE_ERROR);
					closeRoom();
				}

				refreshWaitingDialog();				
				
			} else if (action.equals(Util.ACTION_INVITE_REFUSED)) {
				if (mDialogWaiting != null && mDialogWaiting.isShowing()) {
					Toast.makeText(getApplicationContext(), R.string.invite_refused_toast,
							Toast.LENGTH_LONG).show();
					mDialogWaiting.cancel();
				}
			} else if (action.equals(Util.ACTION_RECV_INVITE)) {
				if (isSender) {
					Toast.makeText(getApplicationContext(), R.string.notify_conflict,
							Toast.LENGTH_LONG).show();	
				} 
				
				isReceiver = true;
				mIsVideo = intent.getBooleanExtra(Util.EXTRA_IS_VIDEO, false);
				showDialog(DIALOG_INVITE);					

			} else if (action.equals(Util.ACTION_REFUSE_COMPLETE)) {
				mRefuseErrorCode = intent.getIntExtra(
						Util.EXTRA_AV_ERROR_RESULT, AVConstants.AV_ERROR_OK);

				if (mRefuseErrorCode != AVConstants.AV_ERROR_OK) {
					showDialog(DIALOG_REFUSE_ERROR);
				}

				refreshWaitingDialog();
			} else if (action.equals(Util.ACTION_ROOM_CREATE_COMPLETE)) {
				mCreateRoomErrorCode = intent.getIntExtra(
						Util.EXTRA_AV_ERROR_RESULT, AVConstants.AV_ERROR_OK);

				if (mCreateRoomErrorCode != AVConstants.AV_ERROR_OK) {
					showDialog(DIALOG_CREATE_ROOM_ERROR);
				} 

				refreshWaitingDialog();
			} else if (action.equals(Util.ACTION_ROOM_JOIN_COMPLETE)) {
				mJoinRoomErrorCode = intent.getIntExtra(
						Util.EXTRA_AV_ERROR_RESULT, AVConstants.AV_ERROR_OK);

				dismissAllWaitingDialog();
				if (mJoinRoomErrorCode != AVConstants.AV_ERROR_OK) {
					showDialog(DIALOG_JOIN_ROOM_ERROR);
				} else {				
					startActivity();
				}

				refreshWaitingDialog();
			}
			Log.e(TAG, "WL_DEBUG ANR CreateRoomActivity onReceive action = " + action + " Out");				
		}
	};
	
	private TextWatcher mTextWatcher = new TextWatcher() {
		@Override
		public void afterTextChanged(Editable s) {
			mReceiveIdentifier = peerIdentifierEditior.getText().toString();
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			// TODO Auto-generated method stub

		}	
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_room_activity);
		ctx = this;

		peerIdentifierEditior = (EditText)findViewById(R.id.receiveTextView);
		peerIdentifierEditior.addTextChangedListener(mTextWatcher);
		findViewById(R.id.receiveButton).setOnClickListener(this);
		findViewById(R.id.accept_audio).setOnClickListener(this);
		findViewById(R.id.accept_video).setOnClickListener(this);		
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

		int QQIdentifierIndex = getIntent()
				.getIntExtra(Util.EXTRA_IDENTIFIER_LIST_INDEX, -1);
		mQavsdkControl = ((QavsdkApplication) getApplication())
				.getQavsdkControl();

		if (QQIdentifierIndex != -1 && mQavsdkControl.getAVContext() != null) {
			TextView login = (TextView) findViewById(R.id.login);
			login.setText(Util.getIdentifierList(this).get(QQIdentifierIndex));
			mSelfIdentifier = Util.getIdentifierList(this).get(QQIdentifierIndex);
		} else {
			finish();
		}
		Log.e(TAG, "=Test=WL_DEBUG CreateRoomActivity onCreate");
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.e(TAG, "=Test=WL_DEBUG CreateRoomActivity onPause");
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		refreshWaitingDialog();
		Log.e(TAG, "=Test=WL_DEBUG CreateRoomActivity onResume");
	}	

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mBroadcastReceiver != null) {
			unregisterReceiver(mBroadcastReceiver);		
		}

		Log.e(TAG, "=Test=WL_DEBUG CreateRoomActivity onDestroy");
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;

		switch (id) {
		case DIALOG_QQ_LIST:
			final String[] identifierList = Util.getIdentifierList(this)
					.toArray(new String[0]);			
			dialog = new AlertDialog.Builder(this).setTitle(R.string.receive)
					.setItems(identifierList, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							mReceiveQQListIndex = whichButton;
							mReceiveIdentifier = identifierList[whichButton];
							peerIdentifierEditior.setText(identifierList[whichButton]);
							peerIdentifierEditior.setSelection(identifierList[whichButton].length());
						}
					}).create();
			break;

		case DIALOG_WAITING:
			mDialogWaiting = new ProgressDialog(this);
			mDialogWaiting.setTitle(R.string.dialog_waitting_title);
			mDialogWaiting.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			mDialogWaiting.setMax(MAX_PROGRESS);
			mDialogWaiting.setButton(DialogInterface.BUTTON_NEGATIVE,
					getText(android.R.string.cancel),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							closeRoom();
							isSender = isReceiver = false;							
						}
					});
			mDialogWaiting
					.setOnCancelListener(new DialogInterface.OnCancelListener() {

						@Override
						public void onCancel(DialogInterface dialog) {
							Log.e(TAG, "WL_DEBUG onCancel");
							closeRoom();
							isSender = isReceiver = false;							
						}
					});
			dialog = mDialogWaiting;
			break;

		case DIALOG_INVITE:
			mDialogInvite = new AlertDialog.Builder(this)
					.setTitle(R.string.invite_title)
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									mQavsdkControl.accept();
									refreshWaitingDialog();
								}
							})
					.setNegativeButton(android.R.string.cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									mQavsdkControl.refuse();
									isSender = isReceiver = false;									
									refreshWaitingDialog();
								}
							})
					.setOnCancelListener(
							new DialogInterface.OnCancelListener() {

								@Override
								public void onCancel(DialogInterface dialog) {
									Log.e(TAG, "WL_DEBUG onCancel");
									mQavsdkControl.refuse();
									isSender = isReceiver = false;
									refreshWaitingDialog();
								}
							}).create();
			dialog = mDialogInvite;
			break;

		case DIALOG_CREATE_ROOM:
			dialog = mDialogCreateRoom = Util.newProgressDialog(this,
					R.string.at_create_room);
			break;

		case DIALOG_CLOSE_ROOM:
			dialog = mDialogCloseRoom = Util.newProgressDialog(this,
					R.string.at_close_room);
			break;

		case DIALOG_CREATE_ROOM_ERROR:
			dialog = Util.newErrorDialog(this, R.string.create_room_failed);
			break;

		case DIALOG_CLOSE_ROOM_ERROR:
			dialog = Util.newErrorDialog(this, R.string.close_room_failed);
			break;

		case DIALOG_AT_ACCEPT:
			dialog = mDialogAtAccept = Util.newProgressDialog(this,
					R.string.at_accept);
			break;
		case DIALOG_ACCEPT_ERROR:
			dialog = Util.newErrorDialog(this, R.string.accept_failed);
			break;
		case DIALOG_AT_INVITE:
			dialog = mDialogAtInvite = Util.newProgressDialog(this,
					R.string.at_invite);
			break;
		case DIALOG_INVITE_ERROR:
			dialog = Util.newErrorDialog(this, R.string.invite_failed);
			break;
		case DIALOG_AT_REFUSE:
			dialog = mDialogAtRefuse = Util.newProgressDialog(this,
					R.string.at_refuse);
			break;
		case DIALOG_REFUSE_ERROR:
			dialog = Util.newErrorDialog(this, R.string.refuse_failed);
			break;
		case DIALOG_AT_JOIN_ROOM:
			dialog = mDialogAtJoinRoom = Util.newProgressDialog(this,
					R.string.at_join_room);
			break;
		case DIALOG_JOIN_ROOM_ERROR:
			dialog = Util.newErrorDialog(this, R.string.join_room_failed);
			break;
		default:
			break;
		}
		return dialog;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {
		case DIALOG_CREATE_ROOM_ERROR:
			((AlertDialog) dialog)
					.setMessage(getString(R.string.error_code_prefix)
							+ mCreateRoomErrorCode);
			break;
		case DIALOG_CLOSE_ROOM_ERROR:
			((AlertDialog) dialog)
					.setMessage(getString(R.string.error_code_prefix)
							+ mCloseRoomErrorCode);
			break;
		case DIALOG_ACCEPT_ERROR:
			((AlertDialog) dialog)
					.setMessage(getString(R.string.error_code_prefix)
							+ mAcceptErrorCode);
			break;
		case DIALOG_INVITE_ERROR:
			((AlertDialog) dialog)
					.setMessage(getString(R.string.error_code_prefix)
							+ mInviteErrorCode);
			break;
		case DIALOG_REFUSE_ERROR:
			((AlertDialog) dialog)
					.setMessage(getString(R.string.error_code_prefix)
							+ mRefuseErrorCode);
			break;
		case DIALOG_JOIN_ROOM_ERROR:
			((AlertDialog) dialog)
					.setMessage(getString(R.string.error_code_prefix)
							+ mJoinRoomErrorCode);
			break;
		default:
			break;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.receiveButton:
			showDialog(DIALOG_QQ_LIST);
			break;
		case R.id.accept_audio:
			if (Util.isNetworkAvailable(getApplicationContext())) {				
				if (TextUtils.isEmpty(mSelfIdentifier)) {
					Toast.makeText(getApplicationContext(), getString(R.string.help_msg_send_account_error), Toast.LENGTH_SHORT).show();		
				} else if (TextUtils.isEmpty(mReceiveIdentifier)) {
					Toast.makeText(getApplicationContext(), getString(R.string.help_msg_recv_account_error), Toast.LENGTH_SHORT).show();		
				} else {
					if (mSelfIdentifier.equals(mReceiveIdentifier)) {
						Toast.makeText(getApplicationContext(), getString(R.string.help_msg_account_send_equal_recv), Toast.LENGTH_SHORT).show();				
					} else {
						invite(false);
						isSender = true;		
					}
				}
			} else {
				Toast.makeText(getApplicationContext(), getString(R.string.notify_no_network), Toast.LENGTH_SHORT).show();
			}

			break;
		case R.id.accept_video:
			if (Util.isNetworkAvailable(getApplicationContext())) {
				if (TextUtils.isEmpty(mSelfIdentifier)) {
					Toast.makeText(getApplicationContext(), getString(R.string.help_msg_send_account_error), Toast.LENGTH_SHORT).show();		
				} else if (TextUtils.isEmpty(mReceiveIdentifier)) {
					Toast.makeText(getApplicationContext(), getString(R.string.help_msg_recv_account_error), Toast.LENGTH_SHORT).show();		
				} else {
					if (mSelfIdentifier.equals(mReceiveIdentifier)) {
						Toast.makeText(getApplicationContext(), getString(R.string.help_msg_account_send_equal_recv), Toast.LENGTH_SHORT).show();				
					} else {
						invite(true);
						isSender = true;		
					}
				}									
			} else {
				Toast.makeText(getApplicationContext(), getString(R.string.notify_no_network), Toast.LENGTH_SHORT).show();
			}			

			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.e(TAG, "WL_DEBUG onActivityResult");
		isSender = isReceiver = false;
		if ((mQavsdkControl != null) && (mQavsdkControl.getAVContext() != null) && (mQavsdkControl.getAVContext().getAudioCtrl() != null)) {
			mQavsdkControl.getAVContext().getAudioCtrl().stopTRAEService();
		}
		closeRoom();
	}

	private long getRoomId() {
		long roomId = 0;
		EditText roomIdEditText = (EditText) findViewById(R.id.roomIdEditText);
		String roomIdStr = roomIdEditText.getText().toString();

		try {
			roomId = Long.parseLong(roomIdStr);
		} catch (Exception e) {
			Log.e(TAG, "WL_DEBUG getRelationId e = " + e);
		}

		return roomId;
	}

	private void invite(boolean isVideo) {
		mReceiveIdentifier = peerIdentifierEditior.getText().toString();
		if (TextUtils.isEmpty(mReceiveIdentifier)) 
			return;
		
		mQavsdkControl.invite(mReceiveIdentifier, isVideo);
		refreshWaitingDialog();	

	}

	private void refreshWaitingDialog() {
		runOnUiThread(new Runnable() {		
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Util.switchWaitingDialog(ctx, mDialogCreateRoom, DIALOG_CREATE_ROOM,
						mQavsdkControl.getIsInCreateRoom());
				Util.switchWaitingDialog(ctx, mDialogCloseRoom, DIALOG_CLOSE_ROOM,
						mQavsdkControl.getIsInCloseRoom());
				Util.switchWaitingDialog(ctx, mDialogAtAccept, DIALOG_AT_ACCEPT,
						mQavsdkControl.getIsInAccept());
				Util.switchWaitingDialog(ctx, mDialogAtInvite, DIALOG_AT_INVITE,
						mQavsdkControl.getIsInInvite());
				Util.switchWaitingDialog(ctx, mDialogAtRefuse, DIALOG_AT_REFUSE,
						mQavsdkControl.getIsInRefuse());
				Util.switchWaitingDialog(ctx, mDialogAtJoinRoom, DIALOG_AT_JOIN_ROOM,
						mQavsdkControl.getIsInJoinRoom());	
			}
		});

	}
	
	private void dismissAllWaitingDialog() {
		runOnUiThread(new Runnable() {		
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Util.switchWaitingDialog(ctx, mDialogCreateRoom, DIALOG_CREATE_ROOM, false);
				Util.switchWaitingDialog(ctx, mDialogCloseRoom, DIALOG_CLOSE_ROOM, false);
				Util.switchWaitingDialog(ctx, mDialogAtAccept, DIALOG_AT_ACCEPT, false);
				Util.switchWaitingDialog(ctx, mDialogAtInvite, DIALOG_AT_INVITE, false);
				Util.switchWaitingDialog(ctx, mDialogAtRefuse, DIALOG_AT_REFUSE, false);
				Util.switchWaitingDialog(ctx, mDialogAtJoinRoom, DIALOG_AT_JOIN_ROOM, false);
			}
		});
	}
	


	private void startActivity() {
		Log.e(TAG, "WL_DEBUG startActivity");
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

	private void closeRoom() {
		mCloseRoomErrorCode = mQavsdkControl.exitRoom();

		if (mCloseRoomErrorCode != AVConstants.AV_ERROR_OK) {
			showDialog(DIALOG_CLOSE_ROOM_ERROR);
		}

		refreshWaitingDialog();
	}
}