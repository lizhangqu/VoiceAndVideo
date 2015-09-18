package cn.edu.zafu.tencent;

import android.app.Application;
import android.content.res.Configuration;
import android.util.Log;

import cn.edu.zafu.tencent.control.QavsdkControl;


public class QavsdkApplication extends Application {
	private static final String TAG = "QavsdkApplication";
	private QavsdkControl mQavsdkControl = null;

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		Log.e(TAG, "WL_DEBUG onConfigurationChanged");
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mQavsdkControl = new QavsdkControl(this);
		Log.e(TAG, "WL_DEBUG onCreate");
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		Log.e(TAG, "WL_DEBUG onLowMemory");
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		Log.e(TAG, "WL_DEBUG onTerminate");
	}

	@Override
	public void onTrimMemory(int level) {
		super.onTrimMemory(level);
		Log.e(TAG, "WL_DEBUG onTrimMemory");
	}

	public QavsdkControl getQavsdkControl() {
		return mQavsdkControl;
	}
}