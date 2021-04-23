package net.kuama.android.backgroundLocation.util

import android.location.LocationManager

/**
 * Interface that will provide a function to check if the GPS provider is activated
 */
interface GPSChecker {
    fun checkGPSActive(): Boolean
}

/**
 * This class is not used for the moment because all the functionality are provided by [SetupChecker]
 * It takes a @param locationManager that will be used to check if either the GPS or
 * network provider are enabled
 */
class GPSCheck(private val locationManager: LocationManager) : GPSChecker {

    /**
     * This function checks if either the GPS or the network provider are enabled
     * @return true if either GPS_PROVIDER or NETWORK_PROVIDER are enable, false otherwise
     */
    override fun checkGPSActive(): Boolean =
        locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
}