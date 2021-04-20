package com.example.locationbackground

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import io.reactivex.rxjava3.disposables.Disposable

/**
 * Service class that works in the background.
 * Manage a LocationHandler instance to retrieve location data.
 *
 * Should start on boot
 * Should not stop
 */
const val NOTIFICATION_ID = 110

class BackgroundService : Service() {

    private var subscription: Disposable? = null
    private lateinit var locationHandler: LocationHandler
    private lateinit var notification: Notification

    /**
     * @return null because it won't be stopped
     */
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    /**
     * When created it instantiate the LocationHandler object
     */
    override fun onCreate() {
        locationHandler = LocationHandler.Builder()
            .context(this)
            .onError { Log.e("BKG", it.localizedMessage, it) }
            .build()
        startForeground()

    }

    /**
     * When it is started, send a broadcast with the current location
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        subscription = locationHandler.readLocation().subscribe(
            { locationUpdate: GeocodedLocation ->

                //for debug display the latitude, longitude, city and country
                Log.d(
                    "Location",
                    "latitude:${locationUpdate.location.latitude}, \nlongitude:${locationUpdate.location.longitude}, \ncity:{${locationUpdate.city}}, \ncountry:{${locationUpdate.country}}"
                )
            },
            {
                Log.e("BKG", it.localizedMessage, it)
            }
        )

        return START_STICKY
    }

    private fun stopReadingLocation() {
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.cancel(NOTIFICATION_ID)
        subscription?.takeIf { !it.isDisposed }?.dispose()
//        stopForeground(true)
//        stopSelf()
    }

    /**
     * Called when the service is being stopped
     */
    override fun onDestroy() {
        stopReadingLocation()
    }

    /**
     * Links the service to a notification bar that keeps the service on "foreground'
     *  so it won't be killed even if the app that launches the service is killed
     */
    private fun startForeground() {
        val channelId = R.string.channel_name.toString()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createNotificationChannel(
                R.string.channel_name.toString(),
                R.string.channel_description.toString()
            )
        val notificationBuilder = NotificationCompat.Builder(this, channelId)

        val intent = Intent(this, MyBroadcastReceiver::class.java)
        val pendingIntent =
            PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)

        // set the notification that will be linked to the service
        val builder = notificationBuilder
            .setContentTitle(getText(R.string.notification_title))
            .setContentText(getText(R.string.notification_message))
            .setSmallIcon(R.drawable.ic_location_on)
            .addAction(R.drawable.ic_location_off, "Stop", pendingIntent)
            .setOngoing(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.apply {
                priority = NotificationManager.IMPORTANCE_DEFAULT
                setCategory(Notification.CATEGORY_SERVICE)
            }
        }
        notification = builder.build()
        startForeground(NOTIFICATION_ID, notification)
    }


    /**
     * Usable only with Android's version Oreo or later
     * It creates a notification channel
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String) {
        val chan = NotificationChannel(
            channelId,
            channelName, NotificationManager.IMPORTANCE_DEFAULT
        )
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
    }


}