package com.lazydroid.autoupdateapk;

import java.io.DataOutputStream;
import java.io.IOException;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

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
		if( update_file.length() > 0 ) {
			String[] commands = {
					"pm install -r " + context.getFilesDir().getAbsolutePath() + "/" + update_file,
					"am start -S -n " + context.getPackageName() + "/" + get_main_activity()
			};
			if( execute_as_root(commands) ) {
				Log.v(TAG, "silently updated: " + update_file);
			} else {
				super.raise_notification();
			}
		}
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
			int flags = PackageManager.GET_ACTIVITIES;
			PackageInfo packageInfo = pm.getPackageInfo(packageName, flags);
			for( ActivityInfo ai : packageInfo.activities ) {
				if( ai.exported ) {
					return ai.name;
				}
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		Log.e(TAG, "get_main_activity() failed");
		return "";
	}

	private boolean execute_as_root( String[] commands ) {
		Process p = null;
		try {
			// Get root privileges
			p = Runtime.getRuntime().exec("su");

			// Do the magic
			DataOutputStream os = new DataOutputStream(p.getOutputStream());
			for( String command : commands ) {
				Log.i(TAG,command);
				os.writeBytes(command + "\n");
			}

			// Close the terminal
			os.writeBytes("exit\n");
			os.flush();
			try {
				p.waitFor();
				if( p.exitValue() == 255 ) {
					Log.e(TAG, "command failed");
					return false;	// failure
				} else {
					Log.e(TAG, "command succeeded");
					return true;	// success
				}
			} catch (InterruptedException e) {
				Log.e(TAG, "su interrupted");
				e.printStackTrace();
				return false;
			}
		} catch (IOException e) {
			Log.e(TAG, "su IO error");
			e.printStackTrace();
			return false;
		} finally {
            if( p != null) p.destroy();
        }
	}
}
