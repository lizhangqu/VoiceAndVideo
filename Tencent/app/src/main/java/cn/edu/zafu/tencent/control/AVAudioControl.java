package cn.edu.zafu.tencent.control;

import android.content.Context;
import android.content.Intent;

import com.tencent.av.sdk.AVAudioCtrl;
import com.tencent.av.sdk.AVAudioCtrl.Delegate;

import cn.edu.zafu.tencent.app.App;
import cn.edu.zafu.tencent.util.Util;

public class AVAudioControl {
	private Context mContext = null;

	private Delegate mDelegate = new Delegate() {
		@Override
		protected void onOutputModeChange(int outputMode) {
			super.onOutputModeChange(outputMode);
			mContext.sendBroadcast(new Intent(Util.ACTION_OUTPUT_MODE_CHANGE));
		}
	};

	AVAudioControl(Context context) {
		mContext = context;
	}

	void initAVAudio() {
		QavsdkControl qavsdk = ((App) mContext)
				.getQavsdkControl();
		qavsdk.getAVContext().getAudioCtrl().setDelegate(mDelegate);
	}

	boolean getHandfreeChecked() {
		QavsdkControl qavsdk = ((App) mContext)
				.getQavsdkControl();
		return qavsdk.getAVContext().getAudioCtrl().getAudioOutputMode() == AVAudioCtrl.OUTPUT_MODE_HEADSET;
	}
}