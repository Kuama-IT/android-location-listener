package com.example.backgroundlocation

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.locationbackground.LocationHandler


class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val handler = LocationHandler(this)


        //debug with the button
        val myButton = findViewById<Button>(R.id.bt_location)
        myButton.setOnClickListener {
            handler.sendLocation()
        }

    }

}