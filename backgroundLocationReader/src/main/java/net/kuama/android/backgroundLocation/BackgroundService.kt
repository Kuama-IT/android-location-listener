package net.kuama.android.backgroundLocation

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import io.reactivex.rxjava3.disposables.Disposable
import net.kuama.android.GeocodedLocation
import net.kuama.android.LocationHandler

/**
 * Service class that works in the background.
 * Manage a LocationHandler instance to retrieve location data.
 *
 * Should start on boot
 * Should not stop
 */
const val NOTIFICATION_ID = 110
const val LOCATION_ID = 100

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
                val intent = Intent(this, LocationBroadcastReceiver::class.java)
                intent.action = javaClass.toString()
                intent.putExtra("latitude", locationUpdate.location.latitude)
                intent.putExtra("longitude", locationUpdate.location.longitude)
                val pendingIntent = PendingIntent.getBroadcast(this, LOCATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                pendingIntent.send()
            },
            {
                Log.e("BKG", it.localizedMessage, it)
            }
        )

        return START_STICKY
    }

    /**
     * Called when the service is being stopped
     * Send a broadcast to MyBroadcastReceiver.kt and stops the subscription
     * so it won't continue to receive locations
     */
    private fun stopReadingLocation() {
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.cancel(NOTIFICATION_ID)
        subscription?.takeIf { !it.isDisposed }?.dispose()
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
            .addAction(R.drawable.ic_location_off, getText(R.string.stop), pendingIntent)
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