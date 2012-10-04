//
//	Copyright (c) 2012 lenik terenin
//
//	Licensed under the Apache License, Version 2.0 (the "License");
//	you may not use this file except in compliance with the License.
//	You may obtain a copy of the License at
//
//		http://www.apache.org/licenses/LICENSE-2.0
//
//	Unless required by applicable law or agreed to in writing, software
//	distributed under the License is distributed on an "AS IS" BASIS,
//	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//	See the License for the specific language governing permissions and
//	limitations under the License.

package com.lazydroid.autoupdateapk;

import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.os.Bundle;

public class AutoUpdateApkActivity extends Activity implements Observer {

	// declare updater class member here (or in the Application)
	@SuppressWarnings("unused")
	private AutoUpdateApk aua;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		aua = new AutoUpdateApk(getApplicationContext());	// <-- don't forget to instantiate

		aua.addObserver(this);	// see the remark below, next to update() method
	}

	// you only need to use this method and specify "implements Observer" and use "addObserver()"
	// in case you want to closely monitor what's the AutoUpdateApk is doing, otherwise just ignore
	// "implements Observer" and "addObserver()" and skip implementing this method.
	//
	// There are three kinds of update messages sent from AutoUpdateApk (more may be added later):
	// AUTOUPDATE_CHECKING, AUTOUPDATE_NO_UPDATE and AUTOUPDATE_GOT_UPDATE, which denote the start
	// of update checking process, and two possible outcomes.
	//
	@Override
	public void update(Observable observable, Object data) {
		if( ((String)data).equalsIgnoreCase(AutoUpdateApk.AUTOUPDATE_GOT_UPDATE) ) {
			android.util.Log.i("AutoUpdateApkActivity", "Have just received update!");
		}
		if( ((String)data).equalsIgnoreCase(AutoUpdateApk.AUTOUPDATE_HAVE_UPDATE) ) {
			android.util.Log.i("AutoUpdateApkActivity", "There's an update available!");
		}
	}
}
