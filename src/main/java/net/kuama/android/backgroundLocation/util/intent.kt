package net.kuama.android.backgroundLocation.util

import android.content.Intent
import net.kuama.android.backgroundLocation.Position

val Intent.position: Position?
    get() {
        val latitude: Double? = extras?.getDouble("latitude")
        val longitude: Double? = extras?.getDouble("longitude")

        return if (latitude !== null && longitude !== null) {
            Position(latitude, longitude)
        } else null
    }
