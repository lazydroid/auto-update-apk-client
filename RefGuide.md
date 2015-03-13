# Public member functions #

The following functions are available as a public API to the AutoUpdateApk.
```
AutoUpdateApk(Context ctx)
```
This is the default constructor, you have to use it to instantiate the class somewhere in your code. The best place is the Application subclass if you have one, second best is the most-used activity class. Please, don't try to add AutoUpdateApk member to the several different activities in your project, the results might be quite unexpected.

You have to pass the application context or current activity contest to the constructor, otherwise it won't work.

If there are any problems, there will be error messages in the log, so keep attention.
```
public static void setIcon( int icon )
```
This method sets the icon for notification popup. If you don't use this method, application icon will be used by default.
```
public static void setName( String name )
```
Set application name to be used in notification. By default the application name from the Manifest is used.
```
public static void setNotificationFlags( int flags )
```
Replaces the notification flags set when the Notification is created.

By default Notification.FLAG\_AUTO\_CANCEL | Notification.FLAG\_NO\_CLEAR are used.
```
public static void setUpdateInterval(long interval)
```
Set update interval (in milliseconds). There are nice constants: MINUTES, HOURS, DAYS, you may use them to specify update interval like: `5 * DAYS`.

Please, don't specify update interval less than 1 hour, this might be considered annoying behaviour and result in service suspension.
```
public static void disableMobileUpdates()
```
Disable update requests over mobile network. This is the default mode. In this mode updates are downloaded over Wi-Fi only.
```
public static void enableMobileUpdates()
```
Enable update requests over mobile network. Please, enable this only if you are sure all your customers do not mind downloading large applications (and paying for that).
```
public void checkUpdatesManually()
```
Use this function if you want to allow users to perform update on demand. Calling this function repeatedly usually is not a very good idea, you should use setUpdateInterval() for that purpose. Polling server every few minutes might be a reason for suspension.
```
public void clearSchedule()
```
Clears the update schedule, reverting to the default setting 00:00~24:00.
```
public void addSchedule(int start, int end)
```
Adds another interval to the current schedule. Parameters start and end are the limits of the update interval in hours. For example, addSchedule(10,15) allows updates to be run from 10am to 3pm of local time.

You may add several intervals, for example, addSchedule(23,24) and addSchedule(0,5) will allow updates to be run from 11pm to 5am.

# Private member functions #

There are other functions in the file, which are considered private and not accessible from the outside. Please, don't use them directly, since they may get changed or even removed in the new versions.