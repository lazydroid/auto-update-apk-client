# Private Android applications also like to get updated #

However, if you don't put them on the Market (aka Google Play), you don't get updates. Here's our solution:

  1. download small AutoUpdateApk java class and include it in your code
  1. compile and upload the application to our server

All your users will get notified about the update and will be able to download and install the new version.

## How to use AutoUpdateApk ##

Basically you have to change only 2 (two!) lines in your project and include the AutoUpdateApk.java class somewhere in the source tree.

```
	private AutoUpdateApk aua;	<-- you need to add this line of code

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		aua = new AutoUpdateApk(getApplicationContext());	<-- and add this line too
```

AutoUpdateApk class is supposed to be instantiated in any of your Activities or, better yet, in Application subclass. After that you have to call the constructor and pass the application context (or activity context) as a parameter.

That is all!

## Where to download AutoUpdateApk ##

You may check the [Downloads](http://code.google.com/p/auto-update-apk-client/downloads/list) section of this project for .zip/.tgz archive of sample application, which includes all necessary files. Or if you are familiar with Git, you may visit the [Source](http://code.google.com/p/auto-update-apk-client/source/checkout) section and get a copy of the current repository.

## Requirements ##

AutoUpdateApk supports Android software version 2.1 (Froyo) and higher.

To access the web-site from the customer device, an internet connection is required.

Since you don't put your software on Android Market, you have to enable "Unknown Sources (Allow installation of non-Market applications)" in Settings :: Applications. Otherwise you won't be able to install your software or updates.

The first installation of the software on customers devices ought to be done by manually following download link or scanning QR code. After that, the AutoUpdateApk will check and download updates automatically.

## Permissions ##

You have to set the following permissions in your Android Manifest:

  * android:name="android.permission.INTERNET"
  * android:name="android.permission.ACCESS\_NETWORK\_STATE"
  * android:name="android.permission.ACCESS\_WIFI\_STATE"

The first one allows your application to access the internet, the other two allow to avoid draining the battery when the appropriate network is not available.

## More info ##

If you need the detailed explanation of the available class members of AutoUpdateApk, you may check [Reference Guide](RefGuide.md) page.

## Feedback ##

If you found a bug or need a feature, please, use Issues section to give us feedback.

Code diffs or code snippets are given the highest priority, then bug reports with detailed explanation of how to reproduce the problem, and, finally, everything else which does not clearly specify the problem or without necessary explanation.

## I did everything as told and it still does not work!!! ##

Please, refer to the  [issue 22](https://code.google.com/p/auto-update-apk-client/issues/detail?id=22) and [issue 23](https://code.google.com/p/auto-update-apk-client/issues/detail?id=23) (use "Search: All issues") regarding the initial testing. There are two main points:

  1. Use logcat to see the output, there should be the message "MD5: XXX" when AutoUpdateAPK starts and more messages when the server is asked for update and even more messages when the update gets retrieved.
  1. There are two constants in the source code that control how often the update is performed (default is about every 3 hours). You may temporary change the default update intervals (UPDATE\_INTERVAL and WAKEUP\_INTERVAL) to smaller values (1 minute?), and, please, don't forget to change them back after you've finished debugging.
  1. Make sure there's an application file uploaded to [the server](http://auto-update-apk.com/), otherwise it has no choice, but reply with "no update".