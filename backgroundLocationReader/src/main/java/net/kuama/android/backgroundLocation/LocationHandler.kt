package net.kuama.android

import android.Manifest.permission
import android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import io.reactivex.rxjava3.core.Flowable
import net.kuama.android.backgroundLocation.LocationStream


interface Broadcaster {
    fun broadcast(intent: Intent)
}

data class GeocodedLocation(val location: Location)


/**
 * Class that manages to retrieve the current user location
 */
class LocationHandler private constructor(
    private val fusedLocationClient: FusedLocationProviderClient,
    private val broadcaster: Broadcaster,
    private val locationGetter: LocationStream,
    val onError: ((Throwable) -> Unit)?
) {

    companion object {
        const val REQUEST_ID = 1000

        /**
         * Check if we have a @param permission
         */
        private fun checkPermission(context: Context, permission: String) =
            ActivityCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED

        /**
         * Check all permissions to access the location
         * @return true if we have background or fine permission
         */
        private fun checkPermissions(context: Context): Boolean =
            checkPermission(context, ACCESS_BACKGROUND_LOCATION)
                    || checkPermission(context, permission.ACCESS_FINE_LOCATION)

        /**
         * Check if the GPS services are active
         */
        private fun checkGPSActive(locationManager: LocationManager): Boolean {
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }
    }

    class Builder {

        private var context: Context? = null
        fun context(context: Context) = apply {
            this.context = context
        }


        private var onError: ((Throwable) -> Unit)? = null
        fun onError(onError: (Throwable) -> Unit) = apply {
            this.onError = onError
        }

        /**
         * @throws IllegalStateException when location permissions are not granted
         * @throws IllegalStateException when GPS is off
         */
        fun build(): LocationHandler {
            context?.let {
                if (!checkPermissions(it)) {
                    error("Please require user's permission to use either ACCESS_BACKGROUND_LOCATION or ACCESS_FINE_LOCATION before using this class")
                }

                val locationManager =
                    it.getSystemService(Context.LOCATION_SERVICE) as LocationManager

                if (!checkGPSActive(locationManager)) {
                    error("Please activate GPS before using this class")
                }

                return LocationHandler(
                    LocationServices.getFusedLocationProviderClient(it),
                    broadcaster = object : Broadcaster {
                        override fun broadcast(intent: Intent) {
                            it.sendBroadcast(intent)
                        }
                    },
                    locationGetter = LocationStream(
                        LocationServices.getFusedLocationProviderClient(
                            it
                        )
                    ),
                    onError = onError
                )
            } ?: error("Please provide a context using the context() method before calling build()")

        }
    }


    private val actionName = javaClass.name

    //each time we set a new value to the location, calls sendIntent that sends a broadcast
    private var location: Location? = null
        set(value) {
            field = value
            sendIntent()
        }


    var accuracy: Float? = null


    /**
     * Retrieve the last known location
     */
    fun readLocation(): Flowable<GeocodedLocation> = getLocation()

    /**
     * Send a broadcast message containing the latitude, longitude, city and country
     */
    private fun sendIntent() {

        val intent = Intent().apply {
            action = actionName
            flags = Intent.FLAG_INCLUDE_STOPPED_PACKAGES
            putExtra("latitude", location?.latitude)
            putExtra("longitude", location?.longitude)
        }

        broadcaster.broadcast(intent)
        //Log.d("Location", "city ${getCity()}")
    }


    /**
     * Return the last known location
     *
     * No need to check for permissions, see [Builder.build]
     */
    @SuppressLint("MissingPermission")
    fun getLastKnownLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { task: Location?
            ->
            //if task is not null assign the new value of the location
            if (task != null) {
                location = task
                //set accuracy
                if (accuracy != null)
                    task.accuracy = accuracy as Float
            } else {
                //retrieve new value of the location
                getLocation()
            }


        }

        fusedLocationClient.lastLocation.addOnFailureListener { error: Exception ->
            getLocation()

            onError?.invoke(error)
        }

    }


    /**
     * Send the request to get the location
     */
    private fun getLocation() = locationGetter
        .listen()
        .map { location ->
            GeocodedLocation(
                location = location
            )
        }



}