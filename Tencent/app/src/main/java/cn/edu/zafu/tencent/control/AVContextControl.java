package cn.edu.zafu.tencent.control;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.tencent.TIMCallBack;
import com.tencent.TIMManager;
import com.tencent.TIMUser;
import com.tencent.av.sdk.AVConstants;
import com.tencent.av.sdk.AVContext;
import com.tencent.openqq.IMSdkInt;

import cn.edu.zafu.tencent.QavsdkApplication;
import cn.edu.zafu.tencent.util.Util;

class AVContextControl {
	private static final String TAG = "AvContextControl";
	private static String APP_ID_TEXT = "1104620500";
	private static String UID_TYPE = "107";
	private boolean mIsInStartContext = false;
	private boolean mIsInStopContext = false;
	private Context mContext;
	private AVContext mAVContext = null;
	private String mSelfIdentifier = "";
	private String mPeerIdentifier = "";
	private AVContext.Config mConfig = null;
	/**
	 * 启动SDK系统的回调函数
	 */
	private AVContext.StartContextCompleteCallback mStartContextCompleteCallback = new AVContext.StartContextCompleteCallback() {
		public void OnComplete(int result) {
			mIsInStartContext = false;
			if (result == AVConstants.AV_ERROR_OK) {
				QavsdkControl qavsdkControl = ((QavsdkApplication) mContext).getQavsdkControl();
				qavsdkControl.initAVInvitation();			
			}
			
			Log.d(TAG,
					"WL_DEBUG mStartContextCompleteCallback.OnComplete result = "
							+ result);
			mContext.sendBroadcast(new Intent(
					Util.ACTION_START_CONTEXT_COMPLETE).putExtra(
					Util.EXTRA_AV_ERROR_RESULT, result));
			
			if (result != AVConstants.AV_ERROR_OK) {
				mAVContext = null;
				Log.d(TAG, "WL_DEBUG mStartContextCompleteCallback mAVContext is null");
			}
		}
	};

	/**
	 * 关闭SDK系统的回调函数
	 */
	private AVContext.StopContextCompleteCallback mStopContextCompleteCallback = new AVContext.StopContextCompleteCallback() {
		public void OnComplete() {
			mIsInStopContext = false;

			logout();
		}
	};

	AVContextControl(Context context) {
		mContext = context;
	}
	
	/**
	 * 启动SDK系统
	 * 
	 * @param identifier
	 *            用户身份的唯一标识
	 * @param usersig
	 *            用户身份的校验信息
	 */
	int startContext(String identifier, String usersig) {
		int result = AVConstants.AV_ERROR_OK;

		if (!hasAVContext()) {			
			Log.d(TAG, "WL_DEBUG startContext identifier = " + identifier);
			Log.d(TAG, "WL_DEBUG startContext usersig = " + usersig);
			
			if (!TextUtils.isEmpty(Util.modifyAppid)) {
				APP_ID_TEXT = Util.modifyAppid;
			}
			if (!TextUtils.isEmpty(Util.modifyUid)) {
				UID_TYPE = Util.modifyUid;
			}			
			mConfig = new AVContext.Config(UID_TYPE,
					APP_ID_TEXT, identifier, usersig, APP_ID_TEXT);
			login();
		}
		
		return result;
	}

	/**
	 * 关闭SDK系统
	 */
	void stopContext() {
		if (hasAVContext()) {
			Log.d(TAG, "WL_DEBUG stopContext");
			QavsdkControl qavsdkControl = ((QavsdkApplication) mContext)
				.getQavsdkControl();
			qavsdkControl.uninitAVInvitation();
			mIsInStopContext = true;			
			mAVContext.stopContext(mStopContextCompleteCallback);			
		}
	}
	
	boolean getIsInStartContext() {
		return mIsInStartContext;
	}

	boolean getIsInStopContext() {
		return mIsInStopContext;
	}
	
	boolean hasAVContext() {
		return mAVContext != null;
	}
	
	AVContext getAVContext() {
		return mAVContext;
	}
	
	String getSelfIdentifier() {
		return mSelfIdentifier;
	}
	
	String getPeerIdentifier() {
		return mPeerIdentifier;
	}

	void setPeerIdentifier(String peerIdentifier) {
		mPeerIdentifier = peerIdentifier;
	}
	
	boolean isDefaultAppid() {
		return APP_ID_TEXT.equals(Util.DEFAULT_APP_ID_TEXT);
	}
	boolean isDefaultUid() {
		return UID_TYPE.equals(Util.DEFAULT_UID_TYPE);
	}
	
	private void login()
	{
		//请确保TIMManager.getInstance().init()一定执行在主线程
		TIMManager.getInstance().init(mContext);	
						
		TIMUser userId = new TIMUser();
		userId.setAccountType(UID_TYPE);   
		userId.setAppIdAt3rd(mConfig.appIdAt3rd);     
		userId.setIdentifier(mConfig.identifier);     

		/**
		 * 登陆所需信息
		 * 1.sdkAppId ： 创建应用时页面上分配的 sdkappid
		 * 2.uid ： 创建应用账号集成配置页面上分配的 accounttype
		 * 3.app_id_at3rd ： 第三方开放平台账号 appid，如果是自有的账号，那么直接填 sdkappid 的字符串形式
		 * 4.identifier ：用户标示符，也就是我们常说的用户 id
		 * 5.user_sig ：使用 tls 后台 api tls_gen_signature_ex 或者工具生成的 user_sig
		 */	
	    TIMManager.getInstance().login(
	    		Integer.parseInt(mConfig.sdkAppId),
	            userId,
	            mConfig.userSig,           //以前的accessToken
	            new TIMCallBack() {//回调接口，以前的listener

	                @Override
	                public void onSuccess() {
	    				Log.i(TAG, "init successfully. tiny id = " + IMSdkInt.get().getTinyId());
	    				onLogin(true, IMSdkInt.get().getTinyId());	
	                }

	                @Override
	                public void onError(int code, String desc) {
	    				Log.e(TAG, "init failed, imsdk error code  = " + code + ", desc = " + desc);
	    				onLogin(false, 0);
	                }
	            });
	}
	
	private void onLogin(boolean result, long tinyId)
	{
		if(result)
		{
			mAVContext = AVContext.createContext(mConfig);
			Log.d(TAG, "WL_DEBUG startContext mAVContext is null? " + (mAVContext == null));
			mSelfIdentifier = mConfig.identifier;

			int ret = mAVContext.startContext(mContext, mStartContextCompleteCallback);

			mIsInStartContext = true;
		}
		else
		{
			mStartContextCompleteCallback.OnComplete(AVConstants.AV_ERROR_INITSDKFAIL);
		}
	}
	
	private void logout()
	{		
		TIMManager.getInstance().logout();	
		onLogout(true);
	}
	;
	private void onLogout(boolean result)
	{
		Log.d(TAG, "WL_DEBUG mStopContextCompleteCallback.OnComplete");
		mAVContext.onDestroy();
		mAVContext = null;
		Log.d(TAG, "WL_DEBUG mStopContextCompleteCallback mAVContext is null");
		mIsInStopContext = false;
		mContext.sendBroadcast(new Intent(Util.ACTION_CLOSE_CONTEXT_COMPLETE));
	}

}