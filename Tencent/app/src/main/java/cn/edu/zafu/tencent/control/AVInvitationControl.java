package cn.edu.zafu.tencent.control;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.tencent.av.sdk.AVInvitation;
import com.tencent.av.sdk.AVRoom;

import cn.edu.zafu.tencent.app.App;
import cn.edu.zafu.tencent.util.Util;

class AVInvitationControl {
	private static final String TAG = "AVInvitationControl";
	private boolean mIsInInvite = false;
	private boolean mIsInAccept = false;
	private boolean mIsInRefuse = false;
	private AVInvitation mAVInvitation = new AVInvitation();
	private Context mContext = null;

	// 接收方收到邀请通知
	private AVInvitation.Delegate mAVInvitationDelegate = new AVInvitation.Delegate() {
		// 接收方收到邀请通知
		@Override
		protected void onInvitationReceived(String identifier, long roomId,
				int avMode) {
			super.onInvitationReceived(identifier, roomId, avMode);
			QavsdkControl qavsdkControl = ((App) mContext)
					.getQavsdkControl();
			qavsdkControl.setRoomId(roomId);
			qavsdkControl.setPeerIdentifier(identifier);
			Log.e(TAG,
					"WL_DEBUG mAVInvitationDelegate.onInvitationReceived mRoomId = "
							+ roomId);
			Log.e(TAG,
					"WL_DEBUG mAVInvitationDelegate.onInvitationReceived mPeerIdentifier = "
							+ identifier);
			Log.e(TAG,
					"WL_DEBUG mAVInvitationDelegate.onInvitationReceived avMode = "
							+ avMode);
			mContext.sendBroadcast(new Intent(Util.ACTION_RECV_INVITE)
					.putExtra(Util.EXTRA_IS_VIDEO,
							avMode == AVRoom.AV_MODE_VIDEO));
		}

		// 发起方收到接收方接受邀请通知
		@Override
		protected void onInvitationAccepted() {
			super.onInvitationAccepted();
			mContext.sendBroadcast(new Intent(Util.ACTION_INVITE_ACCEPTED));
		}

		// 发起方收到接收方拒绝邀请通知
		@Override
		protected void onInvitationRefused() {
			super.onInvitationRefused();
			mContext.sendBroadcast(new Intent(Util.ACTION_INVITE_REFUSED));
		}

		// 发起方取消邀请通知
		@Override
		protected void onInvitationCanceled(String identifier) {
			super.onInvitationCanceled(identifier);
			mContext.sendBroadcast(new Intent(Util.ACTION_INVITE_CANCELED));
		}
	};

	// 发起方收到邀请完成通知
	private AVInvitation.InviteCompleteCallback mAVInvitationInviteCompleteCallback = new AVInvitation.InviteCompleteCallback() {
		@Override
		protected void onComplete(int result) {
			super.onComplete(result);
			mIsInInvite = false;
			mContext.sendBroadcast(new Intent(Util.ACTION_INVITE_COMPLETE)
					.putExtra(Util.EXTRA_AV_ERROR_RESULT, result));
		}
	};

	// 接收方接受邀请完成通知
	private AVInvitation.AcceptCompleteCallback mAVInvitationAcceptCompleteCallback = new AVInvitation.AcceptCompleteCallback() {
		@Override
		protected void onComplete(int result) {
			super.onComplete(result);
			mIsInAccept = false;
			QavsdkControl qavsdkControl = ((App) mContext)
					.getQavsdkControl();
			long roomId = qavsdkControl.getRoomId();
			String peerIdentifier = qavsdkControl.getPeerIdentifier();
			String selfIdentifier = qavsdkControl.getSelfIdentifier();
			Log.e(TAG,
					"WL_DEBUG mAVInvitationAcceptCompleteCallback.onComplete mRoomId = "
							+ roomId);
			Log.e(TAG,
					"WL_DEBUG mAVInvitationAcceptCompleteCallback.onComplete mPeerIdentifier = "
							+ peerIdentifier);
			mContext.sendBroadcast(new Intent(Util.ACTION_ACCEPT_COMPLETE)
					.putExtra(Util.EXTRA_IDENTIFIER, peerIdentifier)
					.putExtra(Util.EXTRA_SELF_IDENTIFIER, selfIdentifier)					
					.putExtra(Util.EXTRA_ROOM_ID, roomId)
					.putExtra(Util.EXTRA_AV_ERROR_RESULT, result));
		}
	};

	// 接收方拒绝邀请完成通知
	private AVInvitation.RefuseCompleteCallback mAVInvitationRefuseCompleteCallback = new AVInvitation.RefuseCompleteCallback() {
		@Override
		protected void onComplete(int result) {
			super.onComplete(result);
			mIsInRefuse = false;
			Log.e(TAG,
					"WL_DEBUG mAVInvitationRefuseCompleteCallback.onComplete");
			mContext.sendBroadcast(new Intent(Util.ACTION_REFUSE_COMPLETE)
					.putExtra(Util.EXTRA_AV_ERROR_RESULT, result));
		}
	};

	AVInvitationControl(Context context) {
		mContext = context;
	}

	void initAVInvitation() {
		mAVInvitation.init();
		mAVInvitation.setDelegate(mAVInvitationDelegate);
	}

	void invite() {
		QavsdkControl qavsdkControl = ((App) mContext)
				.getQavsdkControl();
		mAVInvitation.invite(qavsdkControl.getPeerIdentifier(),
				qavsdkControl.getRoomId(), mAVInvitationInviteCompleteCallback);
		mIsInInvite = true;
	}

	void accept() {
		QavsdkControl qavsdkControl = ((App) mContext)
				.getQavsdkControl();
		String peerIdentifier = qavsdkControl.getPeerIdentifier();
		Log.e(TAG, "WL_DEBUG accept peerIdentifier = " + peerIdentifier);
		mAVInvitation.accept(peerIdentifier, mAVInvitationAcceptCompleteCallback);
		mIsInAccept = true;
	}

	void refuse() {
		QavsdkControl qavsdkControl = ((App) mContext)
				.getQavsdkControl();
		String peerIdentifier = qavsdkControl.getPeerIdentifier();
		Log.e(TAG, "WL_DEBUG refuse peerIdentifier = " + peerIdentifier);
		mAVInvitation.refuse(peerIdentifier, mAVInvitationRefuseCompleteCallback);
		mIsInRefuse = true;
	}

	boolean getIsInInvite() {
		return mIsInInvite;
	}

	boolean getIsInAccept() {
		return mIsInAccept;
	}

	boolean getIsInRefuse() {
		return mIsInRefuse;
	}

	void uninitAVInvitation() {
		mAVInvitation.uninit();
	}
}