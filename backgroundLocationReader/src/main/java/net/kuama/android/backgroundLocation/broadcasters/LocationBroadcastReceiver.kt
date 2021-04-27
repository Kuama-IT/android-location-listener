package net.kuama.android.backgroundLocation.broadcasters

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Custom broadcast receiver that reads the Intent sent from [net.kuama.android.backgroundLocation.LocationRequestManager]
 * The Intent contains two extra fields: latitude and longitude
 * Modify the action in this method for your custom use
 */
abstract class LocationBroadcastReceiver : BroadcastReceiver() {

    /**
     * Once a broadcast is sent by [net.kuama.android.backgroundLocation.LocationRequestManager]
     * it reads the content of the intent, in particular the extras containing the latitude and longitude
     */
    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.extras?.let { onLocation(it.getDouble("latitude"), it.getDouble("longitude")) }
    }

    abstract fun onLocation(latitude: Double, longitude: Double)
}