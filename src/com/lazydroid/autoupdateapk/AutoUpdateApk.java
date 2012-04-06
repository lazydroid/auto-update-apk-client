//
// Copyright (c) 2012 lenik terenin
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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.content.pm.Signature;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

public class AutoUpdateApk {
	private final static String TAG = "AutoUpdateApk";

	private final static String API_URL = "http://auto-update-apk.appspot.com/check";
//	private final static String API_URL = "http://www.auto-update-apk.com/check/weporiwepori/12123";

	private static Context context = null;
//	private static SharedPreferences preferences;

	private static int versionCode = 0;		// as low as it gets
	private static String packageName;
	private static String appName;
	private static int appIcon = android.R.drawable.ic_popup_reminder;

	public static final long MINUTES = 60 * 1000;
	public static final long HOURS = 60 * MINUTES;
	public static final long DAYS = 24 * HOURS;

	private static long UPDATE_INTERVAL = 3 * MINUTES;	//HOURS;	// how often to check
	private static boolean mobile_updates = false;		// download updates over wifi only

	private static Handler updateHandler = new Handler();

	private static int NOTIFICATION_ID = 0xDEADBEEF;

	AutoUpdateApk(Context ctx) {
		context = ctx;
		packageName = context.getPackageName();
		//preferences = context.getSharedPreferences( packageName + "_" + TAG, Context.MODE_PRIVATE);
		NOTIFICATION_ID += crc32(packageName);
		
		ApplicationInfo appinfo = context.getApplicationInfo();
		if( appinfo.icon != 0 ) {
			appIcon = appinfo.icon;
		} else {
			Log.w(TAG, "unable to find application icon");
		}
		if( appinfo.labelRes != 0 ) {
			appName = context.getString(appinfo.labelRes);
		} else {
			Log.w(TAG, "unable to find application label");
		}

		if( haveInternetPermissions() ) {
			context.registerReceiver( connectivity_receiver,
					new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
		}
	}
	
	// set icon for notification popup (default = application icon)
	public static void setIcon( int icon ) {
		appIcon = icon;
	}

	// set name to display in notification popup (default = application label)
	public static void setName( String name ) {
		appName = name;
	}

	// set update interval (in milliseconds)
	//
	// there are nice constants in this file: MINUTES, HOURS, DAYS
	// you may use them to specify update interval like: 5 * DAYS
	//
	// please, don't specify update interval below 1 hour, this might
	// be considered annoying behaviour and result in service suspension
	public static void setUpdateInterval(long interval) {
		if( interval > 60 * MINUTES ) {
			UPDATE_INTERVAL = interval;
		} else {
			Log.e(TAG, "update inteval is too short (less than 1 hour)");
		}
	}

	// software updates will use WiFi/Ethernet only
	public static void disableMobileUpdates() {
		mobile_updates = false;
	}

	// software updates will use any internet connection, including mobile
	// might be a good idea to have 'unlimited' plan on your 3.75G connection
	public static void enableMobileUpdates() {
		mobile_updates = true;
	}

	// call this if you want to perform update on demand
	// (checking for updates more often than once an hour is not recommended
	// and polling server every few minutes might be a reason for suspension)
	public void checkUpdates() {
		new checkUpdateTask().execute();
	}

//
// ---------- everything below this line is private and does not belong to the public API ----------
//
	private Runnable periodicUpdate = new Runnable() {
		@Override
		public void run() {
			checkUpdates();
			updateHandler.removeCallbacks(periodicUpdate);	// remove whatever others may have posted
			updateHandler.postDelayed(this, UPDATE_INTERVAL);
		}
	};

	private BroadcastReceiver connectivity_receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
//			boolean noConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
//			String reason = intent.getStringExtra(ConnectivityManager.EXTRA_REASON);
//			boolean isFailover = intent.getBooleanExtra(ConnectivityManager.EXTRA_IS_FAILOVER, false);
            
			NetworkInfo currentNetworkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
//			NetworkInfo otherNetworkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_OTHER_NETWORK_INFO);

			// do application-specific task(s) based on the current network state, such 
			// as enabling queuing of HTTP requests when currentNetworkInfo is connected etc.
			boolean not_mobile = currentNetworkInfo.getTypeName().equalsIgnoreCase("MOBILE") ? false : true;
			//Toast.makeText(context, "Network is " + (currentNetworkInfo.isConnected() ? "ON" : "OFF"), Toast.LENGTH_LONG).show();
			if( currentNetworkInfo.isConnected() && (mobile_updates || not_mobile) ) {
				checkUpdates();
				updateHandler.postDelayed(periodicUpdate, UPDATE_INTERVAL);
			} else {
				updateHandler.removeCallbacks(periodicUpdate);	// no network anyway
			}
		}
	};

	private class checkUpdateTask extends AsyncTask<Void,Void,String[]> {
		private HttpClient httpclient = new DefaultHttpClient();
		private HttpPost post = new HttpPost(API_URL);

		protected String[] doInBackground(Void... v) {
			long start = System.currentTimeMillis();

			try {
				StringEntity params = new StringEntity( "pkgname=" + packageName + "&version=" + versionCode );
				post.setHeader("Content-Type", "application/x-www-form-urlencoded");
				post.setEntity(params);
				String response = EntityUtils.toString( httpclient.execute( post ).getEntity(), "UTF-8" );
				Log.i(TAG, "response: " + response);
				return response.split("\n");
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				long elapsed = System.currentTimeMillis() - start;
				Log.v(TAG, "update checked in " + elapsed + "ms");
			}
			return null;
		}

		protected void onPreExecute()
		{
			// show progress bar or something
			Log.v(TAG, "checking if there's update on the server");
		}

		protected void onPostExecute(String[] result) {
			// kill progress bar here
			httpclient.getConnectionManager().shutdown();

			if( result != null ) {
				Log.v(TAG, "got reply from update server");
				String ns = Context.NOTIFICATION_SERVICE;
				NotificationManager nm = (NotificationManager) context.getSystemService(ns);
				if( result[0].equalsIgnoreCase("have update") ) {
					// raise popup
					Notification notification = new Notification(
							appIcon, appName + " update", System.currentTimeMillis());
					notification.flags |= Notification.FLAG_AUTO_CANCEL | Notification.FLAG_NO_CLEAR;

					CharSequence contentTitle = appName + " update available";
					CharSequence contentText = "Select to download and install";
					Intent notificationIntent = new Intent(Intent.ACTION_VIEW );
					notificationIntent.setData(Uri.parse(result[1]));
//					notificationIntent.setDataAndType(Uri.parse(result[1]),
//							"application/vnd.android.package-archive");
					PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

					notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
					nm.notify( NOTIFICATION_ID, notification);
				} else {
					nm.cancel( NOTIFICATION_ID );
				}
			} else {
				Log.v(TAG, "no reply from update server");
			}
		}
	}

	private boolean haveInternetPermissions() {
		final Map<String,Boolean> required_perms = new HashMap<String,Boolean>();
		required_perms.put( "android.permission.INTERNET", false );
		required_perms.put( "android.permission.ACCESS_WIFI_STATE", false );
		required_perms.put( "android.permission.ACCESS_NETWORK_STATE", false );

		PackageManager pm = context.getPackageManager();
		String packageName = context.getPackageName();
		int flags = PackageManager.GET_PERMISSIONS;
		PackageInfo packageInfo = null;

		try {
			packageInfo = pm.getPackageInfo(packageName, flags);
			versionCode = packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		if( packageInfo.requestedPermissions != null ) {
			for( String p : packageInfo.requestedPermissions ) {
				//Log.v(TAG, "permission: " + p.toString());
				required_perms.put( p, true);
			}
			if( ! required_perms.containsValue(false) ) {
				return true;	// permissions are in order
			}
			// something is missing
			for( Entry<String,Boolean> e : required_perms.entrySet() ) {
				if( !e.getValue() ) {
					Log.e(TAG, "required permission missing: " + e.getKey());
				}
			}
		}
		Log.e(TAG, "INTERNET/WIFI access required, but no permissions are found in Manifest");
		return false;
	}

	int crc32(String str) {
        byte bytes[] = str.getBytes();
        Checksum checksum = new CRC32();
        checksum.update(bytes,0,bytes.length);
        return (int) checksum.getValue();
	}
}
