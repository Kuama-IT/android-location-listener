package net.kuama.android.backgroundLocation

import android.content.Intent
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import io.reactivex.rxjava3.core.Flowable
import net.kuama.android.backgroundLocation.broadcasters.Broadcaster
import net.kuama.android.backgroundLocation.util.Checker

/**
 * Personal exception that will be throw when a broadcaster is not given in the builder
 */
class MissingBroadcasterException : Throwable()

/**
 * Personal exception that will be throw when a fusedLocationProvider is not given in the builder
 */
class MissingFusedLocationProviderException : Throwable()


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