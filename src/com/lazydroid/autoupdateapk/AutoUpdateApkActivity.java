package com.lazydroid.autoupdateapk;

import android.app.Activity;
import android.os.Bundle;

public class AutoUpdateApkActivity extends Activity {

	// declare updater class member here (or in the Application)
	private AutoUpdateApk aua;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		aua = new AutoUpdateApk(getApplicationContext());	// <-- don't forget to instantiate 
	}
}