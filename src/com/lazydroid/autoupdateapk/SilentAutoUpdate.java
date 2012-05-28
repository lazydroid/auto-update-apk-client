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

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

// ***************************
// *** WARNING *** WARNING ***
// ***************************
//
// this class is very experimental and intended for people who well understand
// what is going on and what kind of unexpected results or strange behavior may
// occur in some cases. please use AutoUpdateApk instead of this class if in doubt

public class SilentAutoUpdate extends AutoUpdateApk {

	// this class is supposed to be instantiated in any of your activities or,
	// better yet, in Application subclass. Something along the lines of:
	//
	//	private SilentAutoUpdate sau;	<-- you need to add this line of code
	//
	//	public void onCreate(Bundle savedInstanceState) {
	//		super.onCreate(savedInstanceState);
	//		setContentView(R.layout.main);
	//
	//		sau = new SilentAutoUpdate(getApplicationContext());	<-- and add this line too
	//

	SilentAutoUpdate(Context ctx) {
		super(ctx);
	}

	//
	// ---------- everything below this line is private and does not belong to the public API ----------
	//
	protected void raise_notification() {
		String update_file = preferences.getString(UPDATE_FILE, "");
		boolean silent_update_failed = preferences.getBoolean(SILENT_FAILED, false);
		if( update_file.length() > 0 && !silent_update_failed ) {
			final String libs = "LD_LIBRARY_PATH=/vendor/lib:/system/lib ";
			final String[] commands = {
					libs + "pm install -r " + context.getFilesDir().getAbsolutePath() + "/" + update_file,
					libs + "am start -n " + context.getPackageName() + "/" + get_main_activity()
			};
			execute_as_root(commands);	// not supposed to return if successful
			preferences.edit().putBoolean(SILENT_FAILED, true).commit();	// avoid silent update loop
		}
		super.raise_notification();
	}

	// this is not guaranteed to work 100%, should be rewritten.
	//
	// if your application fails to restart after silent upgrade,
	// you may try to replace this function with a simple statement:
	//
	//		return ".YourMainActivity";
	//
	private String get_main_activity() {
		PackageManager pm = context.getPackageManager();
		String packageName = context.getPackageName();

		try {
			final int flags = PackageManager.GET_ACTIVITIES;
			PackageInfo packageInfo = pm.getPackageInfo(packageName, flags);
			for( ActivityInfo ai : packageInfo.activities ) {
				if( ai.exported ) {
					return ai.name;
				}
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		Log_e(TAG, "get_main_activity() failed");
		return "";
	}

	private void execute_as_root( String[] commands ) {
		try {
			// Do the magic
			Process p = Runtime.getRuntime().exec( "su" );
			InputStream es = p.getErrorStream();
			DataOutputStream os = new DataOutputStream(p.getOutputStream());

			for( String command : commands ) {
				//Log.i(TAG,command);
				os.writeBytes(command + "\n");
			}
			os.writeBytes("exit\n");
			os.flush();

			int read;
			byte[] buffer = new byte[4096];
			String output = new String();
			while ((read = es.read(buffer)) > 0) {
			    output += new String(buffer, 0, read);
			}

			p.waitFor();
			Log_e(TAG, output.trim() + " (" + p.exitValue() + ")");
		} catch (IOException e) {
			Log_e(TAG, e.getMessage());
		} catch (InterruptedException e) {
			Log_e(TAG, e.getMessage());
		}
	}
}
