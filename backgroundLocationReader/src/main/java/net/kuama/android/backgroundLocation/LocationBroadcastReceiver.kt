package net.kuama.android.backgroundLocation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * This class is the custom broadcast receiver that reads the Intent sent from the service
 * The Intent contains two extra fields: latitude and longitude
 * Modify the action in this method for your custom use
 */
class LocationBroadcastReceiver:BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("Location", "\nlatitude:${intent!!.extras!!["latitude"]}\nlongitude${intent.extras!!["longitude"]}")
    }
}