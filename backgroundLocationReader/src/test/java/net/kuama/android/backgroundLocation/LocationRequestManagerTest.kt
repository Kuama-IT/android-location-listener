package net.kuama.android.backgroundLocation

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import net.kuama.android.backgroundLocation.broadcasters.Broadcaster
import org.junit.Test

class LocationRequestManagerTest {

//    @Test(expected = IllegalStateException::class)
//    fun `it throws IllegalStateException if built with null values`() {
//        LocationHandler.Builder().build()
//    }

    @Test(expected = MissingFusedLocationProviderException::class)
    fun `it throws MissingFusedLocationProviderException if built without a FusedLocationProvider`() {
        //Arrange
        val broadcaster = mockk<Broadcaster>()
        //Act
        LocationRequestManager.Builder()
            .broadcaster(broadcaster)
            .build()
        //Assert
    }


    @Test(expected = MissingBroadcasterException::class)
    fun `it throws MissingBroadcasterException if built without a PermissionChecker`() {
        //Arrange
        val fusedLocationProviderClient = mockk<FusedLocationProviderClient>()
        //Act
        LocationRequestManager.Builder()
            .fusedLocationProviderClient(fusedLocationProviderClient)
            .build()
        //Assert
    }


    @Test
    fun `it calls broadcaster when reading a new location`() {
        //Arrange
        val broadcaster = mockk<Broadcaster>(relaxed = true)
        val fusedLocationProviderClient = mockk<FusedLocationProviderClient>()

        val locationHandler = LocationRequestManager.Builder()
            .fusedLocationProviderClient(fusedLocationProviderClient)
            .broadcaster(broadcaster)
            .build()
        val callbackSlot = slot<LocationCallback>()

        every {
            fusedLocationProviderClient.requestLocationUpdates(
                any(),
                capture(callbackSlot),
                any()
            )
        } answers {

            val callback: LocationCallback = callbackSlot.captured
            callback.onLocationResult(mockk(relaxed = true))

            mockk()
        }

        every {
            fusedLocationProviderClient.removeLocationUpdates(any<LocationCallback>())
        } returns mockk(relaxed = true)

        //Act
        locationHandler.readLocation().subscribe()
        //Assert
        verify {
            broadcaster.broadcast(any())
        }
    }

}