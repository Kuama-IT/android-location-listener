package net.kuama.android.backgroundLocation.broadcasters

import android.content.Context
import android.content.Intent

/**
 * This interface provides a function that sends a broadcast
 */
interface Broadcaster {
    fun broadcast(intent: Intent)
}

/**
 * This class provides an implementation of the [Broadcaster] interface
 * It takes a @param context that will be used to send the broadcast
 */
class LocationBroadcaster(private val context: Context) : Broadcaster {

    /**
     * This function takes a @param intent that will be sent inside the broadcast
     */
    override fun broadcast(intent: Intent) {
        context.sendBroadcast(intent)
    }
}
