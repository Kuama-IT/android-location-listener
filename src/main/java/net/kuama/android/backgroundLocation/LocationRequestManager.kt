package net.kuama.android.backgroundLocation

import com.google.android.gms.location.FusedLocationProviderClient
import io.reactivex.rxjava3.core.Flowable

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
    private val locationStream: LocationStream
) {

    /**
     * Inner builder class that creates the LocationHandler object
     */
    class Builder {

        private var fusedLocationProviderClient: FusedLocationProviderClient? = null
        fun fusedLocationProviderClient(fusedLocationProviderClient: FusedLocationProviderClient) =
            apply {
                this.fusedLocationProviderClient = fusedLocationProviderClient
            }

        /**
         * This function creates a [LocationRequestManager] with the parameters given in the builder
         * @throws FusedLocationProviderClient when it's not provided a [FusedLocationProviderClient]
         */
        fun build(): LocationRequestManager {

            // setup of the variables

            val fusedLocationProviderClient = checkNotNullOr(
                fusedLocationProviderClient, MissingFusedLocationProviderException()
            ) { "Please provide a FusedLocationProviderClient" }

            return LocationRequestManager(
                locationStream = LocationStream(fusedLocationProviderClient)
            )
        }
    }

    /**
     * Retrieve the last known location
     */
    fun readLocation(): Flowable<Position> = locationStream
        .listen()
}
