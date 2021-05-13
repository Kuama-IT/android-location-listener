package net.kuama.android.backgroundLocation.broadcasters

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import net.kuama.android.backgroundLocation.service.BackgroundService

/**
 * Custom broadcast receiver that stops the background service
 */
class BroadcastServiceStopper : BroadcastReceiver() {

    /**
     * Stop the background service when receiving the broadcast
     */
    override fun onReceive(context: Context, intent: Intent) {
        context.stopService(Intent(context, BackgroundService::class.java))
    }
}
