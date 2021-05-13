# BackgroundLocation
How to use

1.Register the service and the broadcast receiver in your Manifest file.
```xml
   <application
        android:enabled="true">  
        ...   

        <receiver
            android:name="BroadcastServiceStopper"
            android:enabled="true"
            android:exported="true">
            <intent-filter>
                <action android:name="BackgroundService" />
            </intent-filter>
        </receiver>

        <service
            android:name="BackgroundService"
            android:foregroundServiceType="location" />
        </application>
```

2. Check within your code that the user granted the permissions for android.permission.ACCESS_FINE_LOCATION 
  and that the GPS Provider is enabled, if not ask for the permissions that are missing.

3. Launch ASAP the service with an Intent

```kotlin
      val backgroundLocationIntent = Intent(this, BackgroundService::class.java)
      //ask permissions and then start the service
      startService(backgroundLocationIntent)
```

    
4. To register the location updates, you should implement a broadcast receiver extending the class
 BroadcastReceiver and implement the method on receive. In that method you will be able to retrieve
 the latitude and the longitude from the extra of the intent 
 ```kotlin
    val latitude: Double = intent!!.extras!!.get("latitude")
    val longitude: Double = intent.extras!!.get("longitude")
```
 and manage the data as you wish.
 
5. Register your broadcast receiver inside your code ASAP (ex. onCreate() of your MainActivity) like that:
```kotlin
        val myBroadcastReceiver = MyBroadcastReceiver()
        val intentFilter =
            IntentFilter("BackgroundService").apply {
                addAction("BackgroundService")
            }
        registerReceiver(myBroadcastReceiver, intentFilter)
```
Note: the actionName set in the IntentFilter is MANDATORY, don't edit those lines.

6. When you need to stop the service, simply call 
```kotlin
        stopService(backgroundLocationIntent)
```
