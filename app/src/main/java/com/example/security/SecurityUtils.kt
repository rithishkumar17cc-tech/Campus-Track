package com.example.security

import android.os.Build
import java.io.File

object SecurityUtils {

    data class SecurityStatus(
        val isRooted: Boolean,
        val isEmulator: Boolean,
        val isMockLocation: Boolean,
        val isFakeGps: Boolean,
        val isDeviceValid: Boolean,
        val hasBreaches: Boolean
    )

    fun performSelfSecurityCheck(isMockLocationSimulated: Boolean = false, isFakeGpsSimulated: Boolean = false): SecurityStatus {
        val rooted = checkRootMethod()
        val emulator = checkEmulatorMethod()
        val mock = isMockLocationSimulated
        val fakeGps = isFakeGpsSimulated

        val hasBreaches = rooted || emulator || mock || fakeGps
        val deviceValid = !rooted && !emulator

        return SecurityStatus(
            isRooted = rooted,
            isEmulator = emulator,
            isMockLocation = mock,
            isFakeGps = fakeGps,
            isDeviceValid = deviceValid,
            hasBreaches = hasBreaches
        )
    }

    private fun checkRootMethod(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        )
        for (path in paths) {
            if (File(path).exists()) return true
        }

        // Check build tags
        val buildTags = Build.TAGS
        if (buildTags != null && buildTags.contains("test-keys")) {
            return true
        }

        return false
    }

    private fun checkEmulatorMethod(): Boolean {
        val rating = (if (Build.FINGERPRINT.startsWith("generic")
            || Build.FINGERPRINT.startsWith("unknown")
            || Build.MODEL.contains("google_sdk")
            || Build.MODEL.contains("Emulator")
            || Build.MODEL.contains("Android SDK built for x86")
            || Build.BOARD == "QC_Reference_Phone"
            || Build.HARDWARE.contains("goldfish")
            || Build.HARDWARE.contains("ranchu")
            || Build.MANUFACTURER.contains("Genymotion")
            || Build.PRODUCT.contains("sdk_gphone")
            || Build.PRODUCT.contains("google_sdk")
            || Build.PRODUCT.contains("sdk")
            || Build.PRODUCT.contains("sdk_x86")
            || Build.PRODUCT.contains("vbox86p")
            || Build.PRODUCT.contains("emulator")
            || Build.PRODUCT.contains("simulator")) 1 else 0)

        return rating > 0
    }
}
