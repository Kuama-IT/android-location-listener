package net.kuama.android.backgroundLocation.util

import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.app.ActivityCompat

/**
 * Interface that will provide functions that will check the permissions and
 * that the GPS service is active
 */
interface Checker {
    //alternativa
//    fun gpsCheck(permission: String, enabledProviders: List<String>): Boolean

    fun gpsEnabled(): Boolean

    fun permissionCheck(permission: String): Boolean
}

/**
 * Implementation of the [Checker] interface
 * It will take a @param context in which we have to check if there are the permissions needed
 */
class SetupChecker(private val context: Context) : Checker {

    /**
     * Alternative 1
     * It takes a @param permission and a @param list of the enabledProviders in the context in which is called
     * @return true if the GPS Provider is active, false otherwise
     */
//    override fun gpsCheck(permission: String, enabledProviders: List<String>): Boolean =
//        enabledProviders.contains(permission)


    /**
     * Alternative 2
     * It checks if the GPS Provider is enabled within the context given in the constructor
     * @return true if the GPS Provider is active, false otherwise
     */
    override fun gpsEnabled(): Boolean {
        val locationManager = context.getSystemService(LOCATION_SERVICE) as LocationManager
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
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED


}