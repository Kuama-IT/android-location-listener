package net.kuama.android.backgroundLocation

import android.annotation.SuppressLint
import android.location.Location
import com.google.android.gms.location.*
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.subjects.BehaviorSubject

class LocationStream(private val locationClient: FusedLocationProviderClient) {
    private val subject: BehaviorSubject<Location> = BehaviorSubject.create()
    private val locationRequest = LocationRequest.create()
        .apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 0
            fastestInterval = 0
        }

    private val onLocation = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            subject.onNext(result.lastLocation)
        }
    }

    @SuppressLint("MissingPermission")
    fun listen(): Flowable<Location> = subject
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