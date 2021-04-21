package net.kuama.android.backgroundLocation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Custom broadcast receiver that stops the background service
 */
class MyBroadcastReceiver : BroadcastReceiver() {


    /**
     * Stop the background service when receiving the broadcast
     */
    override fun onReceive(context: Context, intent: Intent) {

        context.stopService(Intent(context, BackgroundService::class.java))

    }
}