package com.example.backgroundlocation

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.locationbackground.BackgroundService
import com.example.locationbackground.LocationHandler
import com.example.locationbackground.MyBroadcastReceiver


class MainActivity : AppCompatActivity() {

    //debug with the button
    private lateinit var backgroundLocationIntent: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //registra il ricevitore broadcast per ricevere i messaggi broadcast inviati dal Service
        val onLocationHandlerIntent = IntentFilter("com.example.locationbackground.LocationHandler")
        val broadcastReceiver = MyBroadcastReceiver()
        registerReceiver(broadcastReceiver, onLocationHandlerIntent)

        backgroundLocationIntent = Intent(this, BackgroundService::class.java)

        val startButton = findViewById<Button>(R.id.bt_location)
        startButton.setOnClickListener {
//            handler.sendLocation()
            //invia Intent per far iniziare il service BackgroundService
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
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ), LocationHandler.REQUEST_ID
        )
    }

    fun checkPermission(): Boolean {

        return (ActivityCompat.checkSelfPermission(
            this, android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(
            this, android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
                )
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
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

    /**
     * When the app is destroyed, the service is stopped also
     */
    override fun onDestroy() {
        super.onDestroy()
    }
}