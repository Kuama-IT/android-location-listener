package com.example.locationbackground

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import java.lang.Exception
import java.util.*

class LocationHandler(private val myActivity: Activity) {

    //request id, selected random num
    private val requestId = 80
    private var fusedLocationClient = LocationServices.getFusedLocationProviderClient(myActivity)
    private val actionName = "com.example.backgroundlocation"
    private lateinit var locationRequest: LocationRequest

    //each time we set a new value to the location, calls sendIntent that sends a broadcast
    private var location: Location? = null
        set(value) {
            field = value
            sendIntent()
        }


    var accuracy: Float? = null


    /**
     * Check if we have the permission to access the position
     */
    fun checkPermission(): Boolean {

        return (ActivityCompat.checkSelfPermission(
            myActivity, android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(
            myActivity, android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
                )
    }


    /**
     * Get the user permissions to use the position
     */
    private fun getPermission() {
        ActivityCompat.requestPermissions(
            myActivity, arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ), requestId
        )
    }

    /**
     * Check if the GPS services are active
     */
    private fun checkGPSActive(): Boolean {
        val locationManager =
            myActivity.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

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
        myActivity.sendBroadcast(intent)
    }


    /**
     * Return the last known location
     */
    private fun getLastKnownLocation(): Location? {
        if (checkPermission()) {
            if (checkGPSActive()) {
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

                fusedLocationClient.lastLocation.addOnFailureListener { task: Exception ->
                    getLocation()
                    //toast for errors
                    Toast.makeText(myActivity, task.message, Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(
                    myActivity, "Please activate your GPS", Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            getPermission()
        }
        return location
    }


    /**
     * Set and send the request to get the location
     */
    private fun getLocation() {
        locationRequest = LocationRequest.create()
        locationRequest.apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 0
            fastestInterval = 0
        }
        if (checkPermission())
            myActivity.intent = Intent(myActivity, myActivity::class.java)
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            PendingIntent.getService(
                myActivity,
                requestId,
                myActivity.intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        )
    }

    /**
     * Return the current city
     */
    fun getCity(): String? {
        var city = ""
        val geocoder = Geocoder(myActivity, Locale.getDefault())
        location?.let {
            val result = geocoder.getFromLocation(it.latitude, it.longitude, 1)
            city = result[0].locality
            return city
        }
        return null
    }

    /**
     * Return the current country name
     */
    fun getCountry(): String? {
        var country = ""
        val geocoder = Geocoder(myActivity, Locale.getDefault())
        location?.let {
            val result = geocoder.getFromLocation(it.latitude, it.longitude, 1)
            country = result[0].countryName
            return country
        }

        return null
    }

    /**
     * Return the current latitude
     */
    fun getLatitude(): Double? {
        return location?.latitude
    }

    /**
     * Return the current longitude
     */
    fun getLongitude(): Double? {
        return location?.longitude
    }
}