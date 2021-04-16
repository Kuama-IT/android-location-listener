package com.example.locationbackground

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * Service class that works in the background.
 * Manage a LocationHandler instance to retrieve location data.
 */
class BackgroundService : Service() {

    private lateinit var locationHandler: LocationHandler

    /**
     * @return null because it won't be stopped
     */
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    /**
     * When created it instantiate the LocationHandler object
     */
    override fun onCreate() {
        locationHandler = LocationHandler.Builder()
            .context(this)
            .build()
    }

    /**
     * When it is started, send a broadcast with the current location
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        locationHandler.sendLocation()
        return Service.START_STICKY
    }

    override fun onDestroy() {
        stopSelf()
    }


}