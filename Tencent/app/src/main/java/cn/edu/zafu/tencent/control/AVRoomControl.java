package cn.edu.zafu.tencent.control;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.tencent.av.sdk.AVConstants;
import com.tencent.av.sdk.AVContext;
import com.tencent.av.sdk.AVEndpoint;
import com.tencent.av.sdk.AVRoom;
import com.tencent.av.sdk.AVRoomPair;

import cn.edu.zafu.tencent.app.App;
import cn.edu.zafu.tencent.util.Util;

class AVRoomControl {
	private static final String TAG = "AVRoomControl";
	private boolean mIsInCreateRoom = false;
	private boolean mIsInCloseRoom = false;
	private boolean mIsInJoinRoom = false;
	private boolean mIsVideo = false;
	private long mRoomId = 0;
	private Context mContext = null;
	private boolean mIsCreateRoom = true;
	
	private boolean mPeerHasAudio = false;
	private boolean mPeerHasVideo = false;

	private AVRoomPair.Delegate mRoomDelegate = new AVRoomPair.Delegate() {
		// 创建房间成功回调
		protected void onEnterRoomComplete(int result) {
			Log.e(TAG, "WL_DEBUG mRoomDelegate.onEnterRoomComplete result = "
					+ result);
			mIsInCreateRoom = false;
			mIsInJoinRoom = false;
			
			if(mIsCreateRoom)
			{
				QavsdkControl qavsdkControl = ((App) mContext).getQavsdkControl();
				AVRoomPair roomPair = (AVRoomPair) qavsdkControl.getRoom();
				if (roomPair != null && result == AVConstants.AV_ERROR_OK) {
					mRoomId = roomPair.getRoomId();
					qavsdkControl.inviteIntenal();
					Log.d(TAG, "onEnterRoomComplete. roomId = " + mRoomId);
				} else {
					mRoomId = 0;
					Log.e(TAG, "onEnterRoomComplete. mRoomPair == null");
				}
				mContext.sendBroadcast(new Intent(Util.ACTION_ROOM_CREATE_COMPLETE)
						.putExtra(Util.EXTRA_ROOM_ID, mRoomId).putExtra(
								Util.EXTRA_AV_ERROR_RESULT, result));
			}
			else
			{
				Log.d(TAG, "OnRoomJoinComplete. result = " + result);
				mContext.sendBroadcast(new Intent(Util.ACTION_ROOM_JOIN_COMPLETE)
						.putExtra(Util.EXTRA_AV_ERROR_RESULT, result));
			}
		}

		// 离开房间成功回调
		protected void onExitRoomComplete(int result) {
			Log.d(TAG, "onExitRoomComplete. result = " + result);
			mIsInCloseRoom = false;
			mContext.sendBroadcast(new Intent(Util.ACTION_CLOSE_ROOM_COMPLETE));	
		}
		
		protected void onEndpointsEnterRoom(int endpointCount, AVEndpoint endpointList[]) {
			Log.d(TAG, "WL_DEBUG onEndpointsEnterRoom. endpointCount = " + endpointCount);
			//nothing to do
			//mContext.sendBroadcast(new Intent(Util.ACTION_PEER_ENTER));
		}

		protected void onEndpointsExitRoom(int endpointCount, AVEndpoint endpointList[]) {
			Log.d(TAG, "WL_DEBUG onEndpointsExitRoom. endpointCount = " + endpointCount);
			mContext.sendBroadcast(new Intent(Util.ACTION_PEER_LEAVE));
		}

		protected void onEndpointsUpdateInfo(int endpointCount, AVEndpoint endpointList[]) {
			Log.d(TAG, "WL_DEBUG onEndpointsUpdateInfo. endpointCount = " + endpointCount);
			
			if(endpointCount == 0)return;
			
			QavsdkControl qavsdkControl = ((App) mContext).getQavsdkControl();
			
			String peerIdentifier = qavsdkControl.getPeerIdentifier();
			boolean peerHasAudioOld = mPeerHasAudio;
			boolean peerHasVideoOld = mPeerHasVideo;
			int i = 0;
			for(; i < endpointCount; i++)
			{				
				if(peerIdentifier.equals(endpointList[i].getId()))
				{
					mPeerHasAudio = endpointList[i].hasAudio();
					mPeerHasVideo = endpointList[i].hasVideo();
					break;
				}
			}
			
			if(i < endpointCount)
			{
				Log.d(TAG, "onEndpointsUpdateInfo. mPeerHasAudio = " + mPeerHasAudio + ", peerHasAudioOld = " + peerHasAudioOld
						+ ", mPeerHasVideo = " + mPeerHasVideo+ ", peerHasVideoOld = " + peerHasVideoOld);
				mContext.sendStickyBroadcast(new Intent(mPeerHasAudio ? Util.ACTION_PEER_MIC_OPEN : Util.ACTION_PEER_MIC_CLOSE));
				
				mContext.sendStickyBroadcast(new Intent(mPeerHasVideo ? Util.ACTION_PEER_CAMERA_OPEN : Util.ACTION_PEER_CAMERA_CLOSE));
			}			
		}
		
		protected void OnPrivilegeDiffNotify(int privilege) {
			Log.d(TAG, "OnPrivilegeDiffNotify. privilege = " + privilege);
		}
		/*
		protected void OnCameraStart() 
		{
			Log.d(TAG, "OnCameraStart"  );	
			mContext.sendBroadcast(new Intent(Util.ACTION_PEER_CAMERA_OPEN));			
		}
		protected void OnCameraClose() 
		{
			Log.d(TAG, "OnCameraClose"  );	
			mContext.sendBroadcast(new Intent(Util.ACTION_PEER_CAMERA_CLOSE));	
		}		
		protected void OnMicStart()
		{
			mContext.sendBroadcast(new Intent(Util.ACTION_PEER_MIC_OPEN));				
			Log.d(TAG, "OnMicStart"  );	
		}
		protected void OnMicClose()
		{
			mContext.sendBroadcast(new Intent(Util.ACTION_PEER_MIC_CLOSE));				
			Log.d(TAG, "OnMicClose"  );	
//			mContext.sendBroadcast(new Intent(Util.ACTION_PEER_LEAVE));
		}	
		*/	
	
	};


	AVRoomControl(Context context) {
		mContext = context;
	}

	/**
	 * 创建房间
	 * 
	 * @param isVideo
	 *            是否有视频
	 */
	void enterRoom(long roomId, String peerIdentifier, boolean isVideo) {	
		mIsCreateRoom = (roomId == 0 ? true : false);
		QavsdkControl qavsdkControl = ((App) mContext).getQavsdkControl();
		if ((qavsdkControl != null) && (qavsdkControl.getAVContext() != null)) {			
			Log.e(TAG, "WL_DEBUG enterRoom peerIdentifier = " + peerIdentifier + ", roomId = " + roomId + ", isVideo = " + isVideo);
			
			long authBits = AVRoom.AUTH_BITS_DEFUALT;//权限位；默认值是拥有所有权限。TODO：请业务侧填根据自己的情况填上权限位。
			byte[] authBuffer = null;//权限位加密串；TODO：请业务侧填上自己的加密串。
			int authBufferSize = 0;//权限位加密串长度；TODO：请业务侧填上自己的加密串长度。
			String controlRole = "";//角色名；多人房间专用。该角色名就是web端音视频参数配置工具所设置的角色名。TODO：请业务侧填根据自己的情况填上自己的角色名。
			AVRoom.Info roomInfo = new AVRoom.Info(AVRoom.AV_ROOM_PAIR, roomId, 0,
					isVideo ? AVRoom.AV_MODE_VIDEO : AVRoom.AV_MODE_AUDIO,
							peerIdentifier, authBits, authBuffer, authBufferSize, controlRole);
			// create room
			qavsdkControl.getAVContext().enterRoom(mRoomDelegate, roomInfo);
			if(mIsCreateRoom) mIsInCreateRoom = true;
			else mIsInJoinRoom = true;
			mIsVideo = isVideo;		
		} else {
			Log.e(TAG, "WL_DEBUG enterRoom qavsdkControl = " + (qavsdkControl==null));			
			mIsInCreateRoom = false;
			mIsInJoinRoom = false;
			mIsVideo = false;				
		}
	}



	/** 关闭房间 */
	int exitRoom() {
		Log.e(TAG, "WL_DEBUG exitRoom");
		QavsdkControl qavsdk = ((App) mContext).getQavsdkControl();
		if ((qavsdk != null) && (qavsdk.getAVContext() != null)) {
			AVContext avContext = qavsdk.getAVContext();
			int result = avContext.exitRoom();
			mIsInCloseRoom = true;	
			return result;	
		} else {
			Log.e(TAG, "WL_DEBUG exitRoom qavsdkControl = " + (qavsdk==null));					
			mIsInCloseRoom = false;	
			return -1;		
		}

	}

	boolean getIsInCreateRoom() {
		return mIsInCreateRoom;
	}

	boolean getIsInCloseRoom() {
		return mIsInCloseRoom;
	}

	boolean getIsInJoinRoom() {
		return mIsInJoinRoom;
	}

	boolean getIsVideo() {
		return mIsVideo;
	}

	long getRoomId() {
		return mRoomId;
	}

	void setRoomId(long roomId) {
		mRoomId = roomId;
	}
	
	public void setNetType(int netType) {
		QavsdkControl qavsdk = ((App) mContext).getQavsdkControl();
		AVContext avContext = qavsdk.getAVContext();
		AVRoomPair room = (AVRoomPair)avContext.getRoom();
	}
}