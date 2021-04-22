# BackgroundLocation
How to use

1.Register the service and the broadcast receivers in your Manifest file.
        <receiver
            android:name="net.kuama.android.backgroundLocation.MyBroadcastReceiver"
            android:enabled="true"
            android:exported="true">
            <intent-filter>
                <action android:name="net.kuama.android.backgroundLocation.BackgroundService" />
            </intent-filter>
        </receiver>
        <receiver android:name="net.kuama.android.backgroundLocation.LocationBroadcastReceiver"
            android:enabled="true"
            android:exported="true">
            <intent-filter>
                <action android:name="net.kuama.android.backgroundLocation.LocationHandler"/>
            </intent-filter>
        </receiver>

        <activity android:name="net.kuama.backgroundlocation.MainActivity">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <service
            android:name="net.kuama.android.backgroundLocation.BackgroundService"
            android:foregroundServiceType="location" />
            
2.Ask permissions for retrieving the Location in your Manifest file.

      <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
      <uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />
      <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    
3.Check within your code that the user granted the permissions

4.Launch ASAP the service with an Intent

      val backgroundLocationIntent = Intent(this, BackgroundService::class.java)
      startService(backgroundLocationIntent)
    
5.To modify the behavior of the receiver, edit the onReceive() method in LocationBroadcastReceiver.kt

6.When you need to stop the service, simply call stopService(backgroundLocationIntent)
