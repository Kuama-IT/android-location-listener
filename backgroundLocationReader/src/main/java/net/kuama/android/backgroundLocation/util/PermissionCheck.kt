package net.kuama.android.backgroundLocation.util

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

/**
 * This interface provides a method to check permissions in the application
 */
interface PermissionChecker {
    fun check(permission: String): Boolean
}

/**
 * This class is not used because all the functionality are provided by [SetupChecker]
 * It takes a @param context that will be used to check if a certain permission is granted
 */
class PermissionCheck(private val context: Context) : PermissionChecker {

    /**
     * This function checks if a certain @param permission is granted in the context under analysis
     * @return true if the permission is granted, false otherwise
     */
    override fun check(permission: String): Boolean =
        ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

}