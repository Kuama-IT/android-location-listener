package net.kuama.android.backgroundLocation.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.LocationServices
import io.reactivex.rxjava3.disposables.Disposable
import net.kuama.android.backgroundLocation.LocationRequestManager
import net.kuama.android.backgroundLocation.R
import net.kuama.android.backgroundLocation.broadcasters.BroadcastServiceStopper
import net.kuama.android.backgroundLocation.broadcasters.LocationBroadcastReceiver
import net.kuama.android.backgroundLocation.broadcasters.LocationBroadcaster
import net.kuama.android.backgroundLocation.util.SetupChecker

/**
 * Service class that works in the background.
 * Manage a [LocationRequestManager] instance to retrieve location data.
 *
 * Should start on boot
 * Should not stop
 */
const val NOTIFICATION_ID = 110
const val LOCATION_ID = 100

class BackgroundService : Service() {

    private var subscription: Disposable? = null
    private lateinit var locationRequestManager: LocationRequestManager

    /**
     * The implementation of this method is mandatory
     * @return null because it won't be stopped
     */
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    /**
     * When created the service, a [LocationRequestManager] will be automatically created
     * The service will be linked to a [Notification] that will keep it alive even
     * if the app that triggered the service will be closed
     */
    override fun onCreate() {
        locationRequestManager = LocationRequestManager.Builder()
            .fusedLocationProviderClient(
                LocationServices.getFusedLocationProviderClient(this)
            )
            .setupChecker(SetupChecker((this)))
            //Alternative
//            .permissionChecker(PermissionCheck(this))
//            .gpsChecker(GPSCheck(getSystemService(LOCATION_SERVICE) as LocationManager))
            .broadcaster(LocationBroadcaster(this))
            .build()

        showNotification()
    }

    /**
     * When the service is started, it sends a broadcast with the current location
     * If there is an error, it logs the message with the tag BKG
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        subscription = locationRequestManager.readLocation().subscribe(
            { locationUpdate: Location ->
                sendBroadcastUpdate(locationUpdate)
            },
            {
                Log.e("BKG", it.localizedMessage, it)
            }
        )

        return START_STICKY
    }

    /**
     * Send a location update with a broadcast message to [LocationBroadcastReceiver]
     */
    private fun sendBroadcastUpdate(location: Location) {
        val intent = Intent(this, LocationBroadcastReceiver::class.java)
        intent.action = javaClass.toString()
        intent.putExtra("latitude", location.latitude)
        intent.putExtra("longitude", location.longitude)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            LOCATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        pendingIntent.send()
    }

    /**
     * Called when the service is being stopped
     * Send a broadcast to [BroadcastServiceStopper] and stops the subscription
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
     * Display a notification
     */
    private fun showNotification() {
        startForeground(NOTIFICATION_ID, createNotification())
    }


    /**
     * It creates a new Notification
     * Links the service to a notification bar that keeps the service on "foreground'
     *  so it won't be killed even if the app that launches the service is killed
     *  @return Notification
     */
    private fun createNotification(): Notification {

        // it creates the notification builder
        val notificationBuilder = NotificationCompat.Builder(
            this,
            R.string.channel_name.toString()
        )
        // it arranges the intent that will be fired when the stop button will be pressed
        val intent = Intent(this, BroadcastServiceStopper::class.java)
        val pendingIntent = PendingIntent
            .getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)

        // set the notification that will be linked to the service
        val builder = notificationBuilder
            .setContentTitle(getText(R.string.notification_title))
            .setContentText(getText(R.string.notification_message))
            .setSmallIcon(R.drawable.ic_location_on)
            .addAction(R.drawable.ic_location_off, getText(R.string.stop), pendingIntent)
            .setOngoing(true)
        // if Android version's is Oreo or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //it creates a notification channel
            createNotificationChannel(
                R.string.channel_name.toString(),
                R.string.channel_description.toString()
            )
            //it adds priority and category type to the notification
            builder.apply {
                priority = NotificationManager.IMPORTANCE_DEFAULT
                setCategory(Notification.CATEGORY_SERVICE)
            }
        }
        return builder.build()
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