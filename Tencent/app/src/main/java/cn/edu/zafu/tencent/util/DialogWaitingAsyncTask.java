package cn.edu.zafu.tencent.util;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

public class DialogWaitingAsyncTask extends AsyncTask<Integer, Integer, String> {
	private static final String TAG = "DialogWaitingAsyncTask";
	private ProgressDialog mDialogWaiting = null;
	private long mStartTime;
	private int mMaxSenconds;

	public DialogWaitingAsyncTask(ProgressDialog progressDialog) {
		super();
		mDialogWaiting = progressDialog;
	}

	@Override
	protected String doInBackground(Integer... params) {
		mMaxSenconds = params[0];
		int maxProgress = mDialogWaiting.getMax();

		while (mDialogWaiting.isShowing()
				&& updateProgress(maxProgress) < maxProgress) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				Log.e(TAG, "WL_DEBUG doInBackground e = " + e);
			}
		}

		return null;
	}

	@Override
	protected void onPostExecute(String result) {
		if (mDialogWaiting.isShowing()) {
			mDialogWaiting.cancel();
		}
	}

	@Override
	protected void onPreExecute() {
		mStartTime = System.currentTimeMillis();
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		int vlaue = values[0];
		mDialogWaiting.setProgress(vlaue);
	}

	private int updateProgress(int maxProgress) {
		long curTime = System.currentTimeMillis();
		int curProgress = (int) ((curTime - mStartTime) * maxProgress / (mMaxSenconds * 1000));
		//publishProgress(curProgress);

		return curProgress;
	}
}