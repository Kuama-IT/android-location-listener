package com.example.locationbackground

import android.app.Activity
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.TestCase
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.jar.Manifest

@RunWith(AndroidJUnit4::class)
class LocationHandlerTest : TestCase(){

    private lateinit var activity: Activity
    private lateinit var locationHandler: LocationHandler

    @Before
    fun setupActivity(){
        activity = Activity()
        locationHandler = LocationHandler(activity)
    }

    @Test
    fun checkPermissionTrue(){
        //controlla se ha i permessi
        assertThat(locationHandler.checkPermission(), `is`(true) )
    }
}
