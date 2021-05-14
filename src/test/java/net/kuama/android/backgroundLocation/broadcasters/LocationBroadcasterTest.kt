package net.kuama.android.backgroundLocation.broadcasters

import android.content.Context
import android.content.Intent
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class LocationBroadcasterTest {

    @Test
    fun `it sends a broadcast via the provided context`() {
        val context: Context = mockk()
        val intent = Intent()
        every { context.sendBroadcast(any()) } returns Unit

        val broadcaster = LocationBroadcaster(context)
        broadcaster.broadcast(intent)

        verify {
            context.sendBroadcast(intent)
        }
    }
}
