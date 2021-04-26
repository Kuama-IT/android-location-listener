package net.kuama.android.backgroundLocation.util

/**
 * Interface that will provide functions that will check the permissions and
 * that the GPS service is active
 */
interface Checker {

    fun gpsEnabled(): Boolean

    fun permissionCheck(permission: String): Boolean
}
