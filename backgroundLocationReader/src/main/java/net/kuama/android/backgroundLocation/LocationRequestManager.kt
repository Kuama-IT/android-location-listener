package net.kuama.android.backgroundLocation

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Intent
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import io.reactivex.rxjava3.core.Flowable
import net.kuama.android.backgroundLocation.broadcasters.Broadcaster
import net.kuama.android.backgroundLocation.util.Checker
import net.kuama.android.backgroundLocation.util.SetupChecker

/**
 * Personal exception that will be throw when a broadcaster is not given in the builder
 */
class MissingBroadcasterException : Throwable()

/**
 * Personal exception that will be throw when a fusedLocationProvider is not given in the builder
 */
class MissingFusedLocationProviderException : Throwable()

//alternativa
//class MissingPermissionCheckerException : Throwable()
//
//class MissingGPSCheckerException : Throwable()

/**
 * Personal exception that will be throw when a setupChecker is not given in the builder
 */
class MissingSetupCheckerException : Throwable()

/**
 * This method extends the functionality of [checkNotNull] throwing an exception
 */
internal inline fun <T : Any> checkNotNullOr(
    value: T?,
    exception: Throwable,
    lazyMessage: () -> Any
): T = try {
    checkNotNull(value, lazyMessage)
} catch (ex: IllegalStateException) {
    throw exception
}

/**
 * This class manages to retrieve the current user location
 */
class LocationRequestManager private constructor(
    private val broadcaster: Broadcaster,
    private val locationStream: LocationStream
) {

    companion object {
        /**
         * The ID code that must be used when requesting the position permission
         */
        const val REQUEST_ID = 1000
    }

    /**
     * Inner builder class that creates the LocationHandler object
     */
    class Builder {

        //Alternativa
//        private var gpsChecker: GPSChecker? = null
//        fun gpsChecker(gpsChecker: GPSChecker) = apply {
//            this.gpsChecker = gpsChecker
//        }

//        private var permissionChecker: PermissionChecker? = null
//        fun permissionChecker(permissionChecker: PermissionChecker) = apply {
//            this.permissionChecker = permissionChecker
//        }

        private var fusedLocationProviderClient: FusedLocationProviderClient? = null
        fun fusedLocationProviderClient(fusedLocationProviderClient: FusedLocationProviderClient) =
            apply {
                this.fusedLocationProviderClient = fusedLocationProviderClient
            }


        private var broadcaster: Broadcaster? = null
        fun broadcaster(broadcaster: Broadcaster) = apply {
            this.broadcaster = broadcaster
        }

        private var setupChecker: Checker? = null
        fun setupChecker(setupChecker: Checker) = apply {
            this.setupChecker = setupChecker
        }

        /**
         * This function creates a [LocationRequestManager] with the parameters given in the builder
         * @throws MissingSetupCheckerException when it's not provided a [SetupChecker]
         * @throws FusedLocationProviderClient when it's not provided a [FusedLocationProviderClient]
         * @throws MissingBroadcasterException when it's not provided a [Broadcaster]
         */
        fun build(): LocationRequestManager {

            //setup of the variables

            val fusedLocationProviderClient = checkNotNullOr(
                fusedLocationProviderClient, MissingFusedLocationProviderException()
            ) { "Please provide a FusedLocationProviderClient" }


            val broadcaster = checkNotNullOr(
                broadcaster,
                MissingBroadcasterException()
            ) { "Please provide a Broadcaster" }


            val setupChecker = checkNotNullOr(
                setupChecker,
                MissingSetupCheckerException(),
            ) { "Please provide a setup permission checker" }


            // if the GPS provider is not enabled, it displays an error message
            if (!setupChecker.gpsEnabled())
                error("Please activate GPS before using this class")

            // if the ACCESS_FINE_LOCATION permission is not granted, it displays an error message
            if (!setupChecker.permissionCheck(ACCESS_FINE_LOCATION)) {
                error("Please require user's permission to use ACCESS_FINE_LOCATION before using this class")
            }

            //alternativa
//            val gpsChecker = checkNotNullOr(
//                gpsChecker,
//                MissingGPSCheckerException()
//            ) { "Please provide a GPS checker" }

            //alternativa
//            val permissionChecker = checkNotNullOr(
//                permissionChecker, MissingPermissionCheckerException()
//            ) { "Please provide a PermissionChecker" }


            //alternativa
//            if (!gpsChecker.checkGPSActive()) {
//                error("Please activate GPS before using this class")
//            }
//
//            if (!permissionChecker.check(ACCESS_FINE_LOCATION)) {
//                error("Please require user's permission to use ACCESS_FINE_LOCATION before using this class")
//            }

            return LocationRequestManager(
                broadcaster = broadcaster,
                locationStream = LocationStream(fusedLocationProviderClient)
            )

        }
    }


    /**
     * Value used in the intent send by the broadcast
     */
    private val actionName = javaClass.name


    /**
     * Each time a new value for the location is set, it calls [sendIntent] that will send a broadcast
     */
    private var location: Location? = null
        set(value) {
            field = value
            sendIntent()
        }


    /**
     * Retrieve the last known location
     */
    fun readLocation(): Flowable<Location> = getLocation()

    /**
     * Send a broadcast message containing the latitude, longitude
     * The broadcast will be sent with [net.kuama.android.backgroundLocation.broadcasters.LocationBroadcaster]
     */
    private fun sendIntent() {

        val intent = Intent().apply {
            action = actionName
            flags = Intent.FLAG_INCLUDE_STOPPED_PACKAGES
            putExtra("latitude", location?.latitude)
            putExtra("longitude", location?.longitude)
        }

        broadcaster.broadcast(intent)
    }

    /**
     * Send the request to get the location
     */
    private fun getLocation() = locationStream
        .listen()
        .map {
            location = it
            it
        }
}