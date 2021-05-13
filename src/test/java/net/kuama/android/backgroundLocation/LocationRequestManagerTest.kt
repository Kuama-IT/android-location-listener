package net.kuama.android.backgroundLocation

import org.junit.Test

class LocationRequestManagerTest {

    @Test(expected = MissingFusedLocationProviderException::class)
    fun `it throws MissingFusedLocationProviderException if built without a FusedLocationProvider`() {
        // Arrange
        // Act
        LocationRequestManager.Builder()
            .build()
        // Assert
    }

//    @Test
//    fun `it calls broadcaster when reading a new location`() {
//        // Arrange
//        val broadcaster = mockk<Broadcaster>(relaxed = true)
//        val fusedLocationProviderClient = mockk<FusedLocationProviderClient>()
//
//        val locationHandler = LocationRequestManager.Builder()
//            .fusedLocationProviderClient(fusedLocationProviderClient)
//            .build()
//        val callbackSlot = slot<LocationCallback>()
//
//        every {
//            fusedLocationProviderClient.requestLocationUpdates(
//                any(),
//                capture(callbackSlot),
//                any()
//            )
//        } answers {
//
//            val callback: LocationCallback = callbackSlot.captured
//            callback.onLocationResult(mockk(relaxed = true))
//
//            mockk()
//        }
//
//        every {
//            fusedLocationProviderClient.removeLocationUpdates(any<LocationCallback>())
//        } returns mockk(relaxed = true)
//
//        // Act
//        locationHandler
//            .readLocation()
//
//        // Assert
//        verify {
//            broadcaster.broadcast(any())
//        }
//    }
}
