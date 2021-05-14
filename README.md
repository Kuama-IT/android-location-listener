[![](https://jitpack.io/v/Kuama-IT/android-location-listener.svg)](https://jitpack.io/#Kuama-IT/android-location-listener)
# Android Location Listener
This library allows you to receive location updates from the phone even when your app gets killed.

## How to install

Add it in your root `build.gradle` at the end of repositories:

```groovy
allprojects {
	repositories {
		// other repositories..
		maven { url 'https://jitpack.io' }
	}
}
```

Add the dependency

```groovy
dependencies {
	implementation 'com.github.Kuama-IT:android-location-listener:Tag'
}
```

## How To Use

Register the service and the broadcast receiver in your `AndroidManifest.xml` file:

```xml
<application
android:enabled="true">  
...   

    <receiver
        android:name="net.kuama.android.backgroundLocation.broadcasters.BroadcastServiceStopper"
        android:enabled="true"
        android:exported="true">
            <intent-filter>
                <action android:name="net.kuama.android.backgroundLocation.service.BackgroundService" />
            </intent-filter>
    </receiver>
    
    <service
        android:name="net.kuama.android.backgroundLocation.service.BackgroundService"
        android:foregroundServiceType="location" />
</application>
```

Check within your code that the user granted the permissions for `android.permission.ACCESS_FINE_LOCATION` and that the GPS Provider is enabled.
If not ask for the permissions that are missing and activate the GPS. 
If you miss to do one of these tow things  our service will throw an `IllegalStateException`.

Launch the service with an Intent

```kotlin
// TODO Remember to ask for android.permission.ACCESS_FINE_LOCATION before starting the service
startService(Intent(this, BackgroundService::class.java))
```

To register the location updates, you should implement a broadcast receiver extending the class `BroadcastReceiver` and implement the method on receive. In that method you will be able to retrieve
 the latitude and the longitude from the extra of the intent 
 ```kotlin
class MyLocationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val position:Position? = intent?.position
    }
}
```
and manage the data as you wish.
 
Finally, register your broadcast receiver inside your code:

```kotlin
val myBroadcastReceiver = MyLocationReceiver()
val intentFilter = IntentFilter(BackgroundService::class.java.name)
registerReceiver(myBroadcastReceiver, intentFilter)
```

Whenever you need to stop the service, call 

```kotlin
stopService(backgroundLocationIntent)
```
