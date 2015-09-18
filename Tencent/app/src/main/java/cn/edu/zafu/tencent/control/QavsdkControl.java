package cn.edu.zafu.tencent.control;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.tencent.av.sdk.AVContext;
import com.tencent.av.sdk.AVRoom;

import cn.edu.zafu.tencent.R;
import cn.edu.zafu.tencent.constant.DemoConstants;

public class QavsdkControl {
	private static final String TAG = "QavsdkControl";
	private AVContextControl mAVContextControl = null;
	private AVInvitationControl mAVInvitationControl = null;
	private AVRoomControl mAVRoomControl = null;
	private AVUIControl mAVUIControl = null;
	private AVVideoControl mAVVideoControl = null;
	private AVAudioControl mAVAudioControl = null;
	private boolean isVideo = false;
	public QavsdkControl(Context context) {
		try {
			System.loadLibrary("xplatform");
			System.loadLibrary("qav_graphics");
			System.loadLibrary("qavsdk");	
		} catch (UnsatisfiedLinkError e) {
			// TODO: handle exception
		}

		mAVContextControl = new AVContextControl(context);
		mAVInvitationControl = new AVInvitationControl(context);
		mAVRoomControl = new AVRoomControl(context);
		mAVVideoControl = new AVVideoControl(context);
		mAVAudioControl = new AVAudioControl(context);
		Log.e(TAG, "WL_DEBUG QavsdkControl");
	}

	/**
	 * 启动SDK系统
	 * 
	 * @param identifier
	 *            用户身份的唯一标识
	 * @param usersig
	 *            用户身份的校验信息
	 */
	public int startContext(String identifier, String usersig) {
		if (mAVContextControl == null)
			return DemoConstants.DEMO_ERROR_NULL_POINTER;
		return mAVContextControl.startContext(identifier, usersig);
	}

	/**
	 * 关闭SDK系统
	 */
	public void stopContext() {
		if (mAVContextControl != null) {
			mAVContextControl.stopContext();
		}
	}

	public boolean hasAVContext() {
		if (mAVContextControl == null)
			return false;		
		return mAVContextControl.hasAVContext();
	}

	public String getSelfIdentifier() {
		if (mAVContextControl == null)
			return null;			
		return mAVContextControl.getSelfIdentifier();
	}

	public String getPeerIdentifier() {
		if (mAVContextControl == null)
			return null;		
		return mAVContextControl.getPeerIdentifier();
	}

	void setPeerIdentifier(String peerIdentifier) {
		if (mAVContextControl != null) {
			mAVContextControl.setPeerIdentifier(peerIdentifier);		
		}
	}

	/**
	 * 创建房间
	 * 
	 * @param isVideo
	 *            是否有视频
	 */
	public void enterRoom(long roomId, String peerIdentifier, boolean isVideo) {
		if(roomId == 0) {
			if (mAVAudioControl != null) {
				mAVAudioControl.initAVAudio();		
			}
			if (mAVVideoControl != null) {
				mAVVideoControl.initAVVideo();			
			}
		}
		this.isVideo = isVideo;
		if (mAVRoomControl != null) {
			mAVRoomControl.enterRoom(roomId, peerIdentifier, isVideo);		
		}
	}

	/** 关闭房间 */
	public int exitRoom() {
		if (mAVRoomControl == null)
			return DemoConstants.DEMO_ERROR_NULL_POINTER;		
		return mAVRoomControl.exitRoom();
	}

	AVRoom getRoom() {
		AVContext avContext = getAVContext();

		return avContext != null ? avContext.getRoom() : null;
	}

	public long getRoomId() {
		if (mAVRoomControl == null)
			return DemoConstants.DEMO_ERROR_ROOM_NOT_EXIST;
		return mAVRoomControl.getRoomId();
	}

	void setRoomId(long roomId) {
		if (mAVRoomControl != null) {
			mAVRoomControl.setRoomId(roomId);	
		}
	}

	public boolean getIsInStartContext() {
		if (mAVContextControl == null)
			return false;			
		return mAVContextControl.getIsInStartContext();
	}

	public boolean getIsInStopContext() {
		if (mAVContextControl == null)
			return false;			
		return mAVContextControl.getIsInStopContext();
	}

	public boolean getIsInCreateRoom() {
		if (mAVRoomControl == null)
			return false;			
		return mAVRoomControl.getIsInCreateRoom();
	}

	public boolean getIsInCloseRoom() {
		if (mAVRoomControl == null)
			return false;			
		return mAVRoomControl.getIsInCloseRoom();
	}

	public boolean getIsInJoinRoom() {
		if (mAVRoomControl == null)
			return false;			
		return mAVRoomControl.getIsInJoinRoom();
	}

	public boolean getIsVideo() {
		if (mAVRoomControl == null)
			return false;		
		return mAVRoomControl.getIsVideo();
	}
	public void setLocalHasVideo(String identifier, int videoSrcType, boolean isRemoteHasVideo) {
		if (mAVUIControl != null) {
			mAVUIControl.setLocalHasVideo(identifier, videoSrcType, isRemoteHasVideo, false, false);
		}
	}

	public AVContext getAVContext() {
		if (mAVContextControl == null)
			return null;			
		return mAVContextControl.getAVContext();
	}

	public void onCreate(Context context, View contentView) {
		mAVUIControl = new AVUIControl(context, contentView.findViewById(R.id.av_video_layer_ui));
		if (mAVVideoControl != null) {
			mAVVideoControl.initAVVideo();		
		}
		if (mAVAudioControl != null) {
			mAVAudioControl.initAVAudio();		
		}
	}

	public void onResume() {
		if ((mAVContextControl != null) && (mAVContextControl.getAVContext() != null)) {
	 		mAVContextControl.getAVContext().onResume();	
		}
		if (mAVUIControl != null) {
			mAVUIControl.onResume();		
		}
	}

	public void onPause() {
		if ((mAVContextControl != null) && (mAVContextControl.getAVContext() != null)) {		
			mAVContextControl.getAVContext().onPause();
		}
		if (mAVUIControl != null) {	
			mAVUIControl.onPause();
		}
	}

	public void onDestroy() {
		if (mAVUIControl != null) {
			mAVUIControl.onDestroy();	
			mAVUIControl = null;
		}
	}


	public void setRemoteHasVideo(boolean isLocalHasVideo, String identifier) {
		if (mAVUIControl != null) {
			mAVUIControl.setRemoteHasVideo(isLocalHasVideo, false, identifier);	
		}
	}
	public void setLocalHasVideo(boolean isRemoteHasVideo, String identifier) {
		if (mAVUIControl != null) {
			mAVUIControl.setSmallVideoViewLayout(isRemoteHasVideo, identifier);	
		}
	}
	public void setSelfId(String key) {
		if (mAVUIControl != null) {
			mAVUIControl.setSelfId(key);
		}
	}	

	public int toggleEnableCamera() {
		if (mAVVideoControl == null) 
			return DemoConstants.DEMO_ERROR_NULL_POINTER;
		return mAVVideoControl.toggleEnableCamera();
	}
	
	public void setIsInOnOffCamera(boolean isInOnOffCamera) {
		if (mAVVideoControl != null) {
			mAVVideoControl.setIsInOnOffCamera(isInOnOffCamera);		
		}
	}

	public int toggleSwitchCamera() {
		if (mAVVideoControl == null) 
			return DemoConstants.DEMO_ERROR_NULL_POINTER;		
		return mAVVideoControl.toggleSwitchCamera();
	}

	public boolean getIsInOnOffCamera() {
		if (mAVVideoControl == null) 
			return false;		
		return mAVVideoControl.getIsInOnOffCamera();
	}

	public boolean getIsInSwitchCamera() {
		if (mAVVideoControl == null) 
			return false;			
		return mAVVideoControl.getIsInSwitchCamera();
	}
	
	public void setIsInSwitchCamera(boolean isInSwitchCamera) {
		if (mAVVideoControl != null) {
			mAVVideoControl.setIsInSwitchCamera(isInSwitchCamera);		
		}
	}

	public boolean getIsEnableCamera() {
		if (mAVVideoControl == null) 
			return false;			
		return mAVVideoControl.getIsEnableCamera();
	}

	public boolean getIsFrontCamera() {
		if (mAVVideoControl == null) 
			return false;			
		return mAVVideoControl.getIsFrontCamera();
	}

	void initAVInvitation() {
		if (mAVInvitationControl != null) {
			mAVInvitationControl.initAVInvitation();		
		}
	}

	public void invite(String peerIdentifier, boolean isVideo) {
		setPeerIdentifier(peerIdentifier);
		enterRoom(0, peerIdentifier, isVideo);
	}

	void inviteIntenal() {
		if (mAVInvitationControl != null) {
			mAVInvitationControl.invite();	
		}
	}

	public void accept() {
		if (mAVInvitationControl != null) {
			mAVInvitationControl.accept();		
		}
	}

	public void refuse() {
		if (mAVInvitationControl != null) {
			mAVInvitationControl.refuse();	
		}
	}

	public boolean getIsInInvite() {
		if (mAVInvitationControl == null) 
			return false;			
		return mAVInvitationControl.getIsInInvite();
	}

	public boolean getIsInAccept() {
		if (mAVInvitationControl == null) 
			return false;			
		return mAVInvitationControl.getIsInAccept();
	}

	public boolean getIsInRefuse() {
		if (mAVInvitationControl == null) 
			return false;			
		return mAVInvitationControl.getIsInRefuse();
	}

	void uninitAVInvitation() {
		if (mAVInvitationControl != null) {
			mAVInvitationControl.uninitAVInvitation();		
		}
	}

	public boolean getHandfreeChecked() {
		if (mAVAudioControl == null) 
			return false;			
		return mAVAudioControl.getHandfreeChecked();
	}
	
	
	public AVVideoControl getAVVideoControl() {
		return mAVVideoControl;
	}
	public void setRotation(int rotation) {
		if (mAVUIControl != null) {
			mAVUIControl.setRotation(rotation);
		}
	}
	
	public String getQualityTips( ) {
		if (mAVUIControl == null) 
			return null;			
		return mAVUIControl.getQualityTips();
	}	
	
	public boolean isVideo() {
		return isVideo;
	}
	
	public boolean isDefaultAppid() {
		if (mAVContextControl == null) 
			return false;		
		return mAVContextControl.isDefaultAppid();
	}
	
	public boolean isDefaultUid() {
		if (mAVContextControl == null) 
			return false;			
		return mAVContextControl.isDefaultUid();
	}
	
	public void setNetType(int netType) {
		if (mAVRoomControl == null)return ;
		mAVRoomControl.setNetType(netType);
	}
}