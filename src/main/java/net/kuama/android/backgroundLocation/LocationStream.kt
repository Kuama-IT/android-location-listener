package net.kuama.android.backgroundLocation

import android.annotation.SuppressLint
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.subjects.BehaviorSubject

data class Position(val latitude: Double, val longitude: Double)

val Location.position: Position
    get() = Position(latitude = this.latitude, longitude = this.longitude)

/**
 * This class manages to transform the stream of [Location] in a [Flowable] of Location
 * It determines the behaviour when is subscribed and when is disposed
 */
class LocationStream(private val locationClient: FusedLocationProviderClient) {

    /**
     * Setup the observable object that will manage the stream of locations
     */
    private val subject: BehaviorSubject<Position> = BehaviorSubject.create()

    /**
     * Setup the location request
     */
    private val locationRequest = LocationRequest.create()
        .apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 0
            fastestInterval = 0
        }

    /**
     * Each time it receives a new location, the observable adjourns the value of the location
     */
    private val onLocation = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            val location = result.lastLocation ?: return
            subject.onNext(location.position)
        }
    }

    /**
     * It determines the behaviour when it is subscribed and when it is disposed
     * It transforms a stream of [Location] in a stream of [Flowable] of Locations.
     */
    @SuppressLint("MissingPermission")
    fun listen(): Flowable<Position> = subject
        .doOnSubscribe {
            locationClient
                .requestLocationUpdates(
                    locationRequest,
                    onLocation,
                    null
                )
        }
        .doOnDispose {
            locationClient.removeLocationUpdates(onLocation)
        }
        .toFlowable(BackpressureStrategy.LATEST)
}
