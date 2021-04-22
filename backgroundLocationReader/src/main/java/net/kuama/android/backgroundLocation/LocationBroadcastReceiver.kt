package net.kuama.android.backgroundLocation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Custom broadcast receiver that reads the Intent sent from LocationHandler.kt
 * The Intent contains two extra fields: latitude and longitude
 * Modify the action in this method for your custom use
 */
open class LocationBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(
            "Location",
            "\nlatitude:${intent!!.extras!!["latitude"]}\nlongitude${intent.extras!!["longitude"]}"
        )
    }
}