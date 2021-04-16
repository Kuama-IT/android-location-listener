package com.example.locationbackground

import android.app.Service
import android.content.Intent
import android.os.IBinder

class BackgroundService : Service() {

    private lateinit var locationHandler: LocationHandler

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        locationHandler = LocationHandler.Builder()
            .context(this)
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        locationHandler.sendLocation()
        return Service.START_STICKY
    }

    override fun onDestroy() {
        stopSelf()
    }


}