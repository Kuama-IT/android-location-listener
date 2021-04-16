package com.example.locationbackground

import android.Manifest.*
import android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import java.lang.Exception
import java.util.*

interface Broadcaster {
    fun broadcast(intent: Intent)
}

interface LocationGetter {
    fun sendRequest()
}

interface GeocodeBuilder {
    fun getGeocoder():Geocoder
}

class LocationHandler private constructor(
    private val fusedLocationClient: FusedLocationProviderClient,
    private val locationManager: LocationManager,
    private val broadcaster: Broadcaster,
    private val locationGetter: LocationGetter,
    private val geocodeBuilder: GeocodeBuilder,
    val onError: ((Throwable) -> Unit)?
) {

    companion object {
        const val REQUEST_ID = 1000

        private fun checkPermission(context: Context, permission: String) =
            ActivityCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED

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
                    locationManager = locationManager,
                    broadcaster = object : Broadcaster {
                        override fun broadcast(intent: Intent) {
                            it.sendBroadcast(intent)
                        }
                    },
                    locationGetter = object : LocationGetter {
                        val locationRequest = LocationRequest.create()
                            .apply {
                                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                                interval = 0
                                fastestInterval = 0
                            }

                        @SuppressLint("MissingPermission")
                        override fun sendRequest() {
                            val intent = Intent(it, it::class.java)
                            LocationServices.getFusedLocationProviderClient(it)
                                .requestLocationUpdates(
                                    locationRequest,
                                    PendingIntent.getService(
                                        it,
                                        REQUEST_ID,
                                        intent,
                                        PendingIntent.FLAG_UPDATE_CURRENT
                                    )
                                )
                        }
                    },
                    geocodeBuilder = object : GeocodeBuilder{
                        override fun getGeocoder() :Geocoder {
                            return Geocoder(it, Locale.getDefault())
                        }
                    },
                    onError = onError
                )
            } ?: error("Please provide a context using the context() method before calling build()")

        }
    }


    private val actionName = javaClass.name
    private lateinit var locationRequest: LocationRequest

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
    fun sendLocation() {
        getLastKnownLocation()
    }

    /**
     * Send a broadcast message containing the latitude, longitude, city and country
     */
    private fun sendIntent() {

        val intent = Intent().apply {
            action = actionName
            flags = Intent.FLAG_INCLUDE_STOPPED_PACKAGES
            putExtra("latitude", location?.latitude)
            putExtra("longitude", location?.longitude)
            putExtra("city", getCity())
            putExtra("country", getCountry())
        }

        broadcaster.broadcast(intent)
    }


    /**
     * Return the last known location
     *
     * No need to check for permissions, see [Builder.build]
     */
    @SuppressLint("MissingPermission")
    private fun getLastKnownLocation(): Location? {
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
            //toast for errors
            onError?.invoke(error)
        }

        return location
    }


    /**
     * Send the request to get the location
     */
    private fun getLocation() {
        locationGetter.sendRequest()
    }

    /**
     * Return the current city
     */
    private fun getCity(): String? {
        var city: String
        location?.let {
            val result = geocodeBuilder.getGeocoder()
                .getFromLocation(it.latitude, it.longitude, 1)
            city = result[0].locality
            return city
        }
        return null
    }

    /**
     * Return the current country name
     */
    private fun getCountry(): String? {
        var country: String
        location?.let {
            val result = geocodeBuilder.getGeocoder()
                .getFromLocation(it.latitude, it.longitude, 1)
            country = result[0].countryName
            return country
        }

        return null
    }

}