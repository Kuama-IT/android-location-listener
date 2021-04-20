package com.example.locationbackground

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Custom broadcast receiver that displays the location information retrieved from the
 * LocationHandler.kt broadcast in the console log
 */
class MyBroadcastReceiver : BroadcastReceiver() {

    // net.kuama.android. ....

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        val latitude = intent.extras?.get("latitude")
        val longitude = intent.extras?.get("longitude")
        val city = intent.extras?.get("city")
        val country = intent.extras?.get("country")
        //for debug display the latitude, longitude, city and country
        Log.d(
            "Location",
            "latitude:${latitude}, \nlongitude:${longitude}, \ncity:{$city}, \ncountry:{$country}"
        )

        val intent = Intent(context, BackgroundService::class.java)
        context.stopService(intent)
    }
}