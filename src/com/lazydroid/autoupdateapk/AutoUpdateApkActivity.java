package com.lazydroid.autoupdateapk;

import android.app.Activity;
import android.os.Bundle;

public class AutoUpdateApkActivity extends Activity {

	// declare updater class member here (or in the Application)
	@SuppressWarnings("unused")
	private SilentAutoUpdate aua;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		aua = new SilentAutoUpdate(getApplicationContext());	// <-- don't forget to instantiate
	}
}