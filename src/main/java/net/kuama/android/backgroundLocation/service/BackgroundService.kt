package net.kuama.android.backgroundLocation.service

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.LocationServices
import io.reactivex.rxjava3.disposables.Disposable
import net.kuama.android.backgroundLocation.LocationRequestManager
import net.kuama.android.backgroundLocation.Position
import net.kuama.android.backgroundLocation.R
import net.kuama.android.backgroundLocation.broadcasters.BroadcastServiceStopper
import net.kuama.android.backgroundLocation.util.Checker

/**
 * Location notification ID
 * use this value if you want to cancel the notification
 */
const val NOTIFICATION_ID = 110
const val LATITUDE_EXTRA = "latitude"
const val LONGITUDE_EXTRA = "longitude"

/**
 * Service class that works in the background.
 * Manage a [LocationRequestManager] instance to retrieve location data.
 *
 * Should start on boot
 * Should not stop
 */
class BackgroundService : Service(), Checker {

    private var subscription: Disposable? = null
    private lateinit var locationRequestManager: LocationRequestManager

    /**
     * The implementation of this method is mandatory
     * @return null because we don't wont it to be stopped
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
            .build()

        // if the GPS provider is not enabled, it displays an error message
        if (!gpsEnabled())
            error("Please activate GPS before using this class")

        // if the ACCESS_FINE_LOCATION permission is not granted, it displays an error message
        if (!permissionCheck(ACCESS_FINE_LOCATION)) {
            error("Please require user's permission to use ACCESS_FINE_LOCATION before using this class")
        }
        showNotification()
    }

    /**
     * When the service is started, it sends a broadcast with the current location
     * If there is an error, it logs the message with the tag BKG
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        subscription = locationRequestManager.readLocation().subscribe(
            { locationUpdate: Position ->
                sendBroadcastUpdate(locationUpdate)
            },
            {
                Log.e("BKG", it.localizedMessage, it)
            }
        )

        return START_STICKY
    }

    /**
     * Send a location update with a broadcast message
     */
    private fun sendBroadcastUpdate(location: Position) {
        val actionName = javaClass.name
        val intent = Intent()
            .also {
                it.action = actionName
                it.flags = Intent.FLAG_INCLUDE_STOPPED_PACKAGES
                it.putExtra(LATITUDE_EXTRA, location.latitude)
                it.putExtra(LONGITUDE_EXTRA, location.longitude)
            }
        sendBroadcast(intent)
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
            // it creates a notification channel
            createNotificationChannel(
                R.string.channel_name.toString(),
                R.string.channel_description.toString()
            )
            // it adds priority and category type to the notification
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
        val notificationChannel = NotificationChannel(
            channelId,
            channelName, NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        }

        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(notificationChannel)
    }

    /**
     * It checks if the GPS Provider is enabled within the context given in the constructor
     * @return true if the GPS Provider is active, false otherwise
     */
    override fun gpsEnabled(): Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    /**
     * It checks if the context under analysis has the @param permission
     * It works only if a context is provided in the constructor
     * @return true if the @param permission is granted, false otherwise
     */
    override fun permissionCheck(permission: String): Boolean =
        ActivityCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
}
