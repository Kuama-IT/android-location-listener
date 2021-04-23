package net.kuama.android.backgroundLocation

import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import io.mockk.*
import io.reactivex.rxjava3.core.Flowable
import org.junit.After
import org.junit.Before
import org.junit.Test

class LocationStreamTest {

    private val locationClient: FusedLocationProviderClient = mockk()
    private val locationStream = LocationStream(locationClient)

    @Before
    fun setup() {

        val callbackSlot = slot<LocationCallback>()

        val locationResult: LocationResult = mockk()

        val location: Location = mockk()

        every { location.latitude } returns 10.0
        every { location.longitude } returns 10.0

        every { locationResult.lastLocation } returns location

        every {
            locationClient.requestLocationUpdates(
                any(),
                capture(callbackSlot),
                any()
            )
        } answers {

            val callback: LocationCallback = callbackSlot.captured
            callback.onLocationResult(locationResult)

            mockk()
        }

    }

    @After
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `it exposes a stream of locations`() {
        // Arrange: see [setup]

        // Act
        val stream = locationStream.listen()

        // Assert
        stream
            .test()
            .assertValue { location ->
                location.longitude == 10.0 &&
                        location.latitude == 10.0
            }


    }

    @Test
    fun `it removes location listener when stream gets disposed`() {
        // Arrange: see [setup]

        every { locationClient.removeLocationUpdates(any<LocationCallback>()) } returns mockk()
        // Act
        val stream: Flowable<Location> = locationStream.listen()
        val subscription = stream.subscribe()

        subscription.dispose()

        // Assert
        verify(exactly = 1) {
            locationClient.removeLocationUpdates(any<LocationCallback>())
        }
    }

}