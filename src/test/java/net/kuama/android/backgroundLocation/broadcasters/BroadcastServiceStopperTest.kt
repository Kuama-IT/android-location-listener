package net.kuama.android.backgroundLocation.broadcasters

import android.content.Context
import android.content.Intent
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Test

class BroadcastServiceStopperTest {

    @Test
    fun `it sends a broadcast to stop location detection retrieval`() {
        val context: Context = mockk()
        val stopper = BroadcastServiceStopper()
        val intent = Intent()
        val intentSlot = slot<Intent>()
        every { context.stopService(capture(intentSlot)) } returns true

        stopper.onReceive(context, intent)

        verify {
            context.stopService(any())
        }
    }
}