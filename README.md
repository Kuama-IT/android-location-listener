# BackgroundLocation
How to use

1.Register the service and the broadcast receivers in your Manifest file.
```xml
   <application>  
        ...   
        <receiver
            android:name="net.kuama.android.backgroundLocation.broadcasters.BroadcastServiceStopper"
            android:enabled="true"
            android:exported="true">
            <intent-filter>
                <action android:name="net.kuama.android.backgroundLocation.service.BackgroundService" />
            </intent-filter>
        </receiver>
        <receiver android:name="net.kuama.android.backgroundLocation.broadcasters.LocationBroadcastReceiver"
            android:enabled="true"
            android:exported="true">
            <intent-filter>
                <action android:name="net.kuama.android.backgroundLocation.LocationRequestManager"/>
            </intent-filter>
        </receiver>

        <service
            android:name="net.kuama.android.backgroundLocation.service.BackgroundService"
            android:foregroundServiceType="location" />
        </application>
```

3. Check within your code that the user granted the permissions for 
   android.permission.ACCESS_FINE_LOCATION, if not ask for the permissions.

4. Launch ASAP the service with an Intent

```kotlin
      val backgroundLocationIntent = Intent(this, BackgroundService::class.java)
      //ask permissions and then start the service
      startService(backgroundLocationIntent)
```

    
5. To modify the behavior of the broadcast receiver, you should implement 
    the onReceive() method of the abstract class [LocationBroadcastReceiver.kt] and change the name
    of the receiver in the manifest with your custom BroadcastReceiver.


6. When you need to stop the service, simply call stopService(backgroundLocationIntent)
