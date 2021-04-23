package net.kuama.backgroundlocation

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.backgroundlocation.R
import net.kuama.android.backgroundLocation.LocationRequestManager
import net.kuama.android.backgroundLocation.service.BackgroundService


class MainActivity : AppCompatActivity() {

    //debug with the button
    private lateinit var backgroundLocationIntent: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        backgroundLocationIntent = Intent(this, BackgroundService::class.java)

        val startButton = findViewById<Button>(R.id.bt_location)
        startButton.setOnClickListener {
            //send the Intent to start the service from BackgroundService.kt
            if (checkGPSActive()) {
                if (checkPermission()) {
                    startService(backgroundLocationIntent)
                } else
                    getPermission()
            } else
                Toast.makeText(this, "Please activate your GPS", Toast.LENGTH_SHORT)
                    .show()
        }

        val stopButton = findViewById<Button>(R.id.bt_stop)
        stopButton.setOnClickListener {
            stopService(backgroundLocationIntent)
        }


    }

    /**
     * Get the user permissions to use the position
     */
    private fun getPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ), LocationRequestManager.REQUEST_ID
        )
    }

    fun checkPermission(): Boolean {

        return ActivityCompat.checkSelfPermission(
            this, android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }


    /**
     * Check if the GPS services are active
     */
    private fun checkGPSActive(): Boolean {
        val locationManager =
            this.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

}