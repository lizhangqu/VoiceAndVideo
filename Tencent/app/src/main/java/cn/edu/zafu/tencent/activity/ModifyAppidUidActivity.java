package cn.edu.zafu.tencent.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

import cn.edu.zafu.tencent.R;
import cn.edu.zafu.tencent.util.Util;

public class ModifyAppidUidActivity extends Activity implements OnClickListener,TextWatcher {
	private static final String TAG = "ModifyAppidUidActivity";
	private Context ctx;
	private EditText etAppid;
	private EditText etUid;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ctx = this;
		setContentView(R.layout.modify_appid_uid_activity);
		findViewById(R.id.ok).setOnClickListener(this);
		findViewById(R.id.cancel).setOnClickListener(this);
		
		etAppid = (EditText) findViewById(R.id.edit_text_appid);
		etUid = (EditText) findViewById(R.id.edit_text_uid);

		etAppid.addTextChangedListener(this);
		etUid.addTextChangedListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ok:
			if (save()) {
				setResult(RESULT_OK);
				finish();		
			} 

			break;
		case R.id.cancel:
			finish();
			break;
		default:
			break;
		}
	}

	private boolean save() {
		String appid = etAppid.getText().toString();
		String uid = etUid.getText().toString();
		if (TextUtils.isEmpty(appid)) {
			Toast.makeText(getApplicationContext(), getString(R.string.notify_appid_empty), Toast.LENGTH_SHORT).show();
			return false;
		} 
		
		if (TextUtils.isEmpty(uid)) {
			Toast.makeText(getApplicationContext(), getString(R.string.notify_uid_empty), Toast.LENGTH_SHORT).show();
			return false;
		} 
		
		Util.modifyAppid = appid;
		Util.modifyUid = uid;		
		return true;
	}

	@Override
	public void afterTextChanged(Editable s) {
		findViewById(R.id.ok).setEnabled( (!TextUtils.isEmpty(etAppid.getText().toString())) &&
				(!TextUtils.isEmpty(etUid.getText().toString())));
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
}