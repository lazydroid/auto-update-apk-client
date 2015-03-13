## Warning ##

SilentAutoUpdate is very experimental and intended for people who well understand what is going on and what kind of unexpected results or strange behaviour may occur in some cases. Please use AutoUpdateApk instead of this class if in doubt.

## How to use SilentAutoUpdate ##

Basically you have to change only 2 (two!) lines in your project and include the SilentAutoUpdate.java class (along with AutoUpdateApk.java) somewhere in the source tree.

```
	private SilentAutoUpdate sau;	<-- you need to add this line of code

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		sau = new SilentAutoUpdate(getApplicationContext());	<-- and add this line too
```

If you have used AutoUpdateApk, SilentAutoUpdate works exactly the same way, exposing the same member functions and same settings.

## What does it do? ##

Basically, SilentAutoUpdate is based on and totally compatible with AutoUpdateApk, so you may easily change from one to another by just replacing the class name in two before mentioned places.

The difference is, if user's phone is **rooted**, SilentAutoUpdate will try to utilize this feature to silently apply update without raising notifications and asking for user confirmation every time.

If the phone is not rooted, or user declines SuperUser request or for some reason silent update does not work properly, it will **automatically fallback to notifications**.

## Why should I avoid it? ##

There are several points of view regarding silent updates. From my point of view, the drawbacks outweight the convenience:

  * The phone belongs to the user, we should not do anything without users permission
  * Silent update, however silent, still disturbs the normal flow when terminating and replacing the .APK, so users might be upset about it
  * Relying on not very well documented features might lead to strange behaviour

Having said all that, I understand why people might want to use it, so here it is.

## Feedback ##

If you found a bug or need a feature, please, use [Issues](http://code.google.com/p/auto-update-apk-client/issues/list) section to give us feedback.