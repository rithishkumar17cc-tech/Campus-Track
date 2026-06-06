package com.example.security

import kotlin.math.*

object GpsUtils {

    /**
     * Calculates the distance in meters between two points using the Haversine formula.
     */
    fun calculateDistanceMeters(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val earthRadius = 6371000.0 // meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }

    data class LocationPreset(
        val name: String,
        val latitude: Double,
        val longitude: Double,
        val isMockLocation: Boolean = false,
        val isFakeGps: Boolean = false
    )

    // The core location of our target college is:
    // Near Airport, Madurai Rd, Valayankulam, Tamil Nadu 625022, India (Latitude: 9.8075, Longitude: 78.0915)
    val CampusLocationPreset = LocationPreset(
        name = "Valayankulam Campus Administration Office",
        latitude = 9.8075,
        longitude = 78.0915
    )

    val Presets = listOf(
        LocationPreset(
            name = "Campus Gate (Inside Area)",
            latitude = 9.8078,
            longitude = 78.0918,
            isMockLocation = false,
            isFakeGps = false
        ),
        LocationPreset(
            name = "Campus Hostel Building (Inside Area)",
            latitude = 9.8072,
            longitude = 78.0912,
            isMockLocation = false,
            isFakeGps = false
        ),
        LocationPreset(
            name = "Madurai Airport Runway (Out of bounds - 2.5km)",
            latitude = 9.8344,
            longitude = 78.0934,
            isMockLocation = false,
            isFakeGps = false
        ),
        LocationPreset(
            name = "Madurai Temple (Out of bounds - 12.5km)",
            latitude = 9.9179,
            longitude = 78.1193,
            isMockLocation = false,
            isFakeGps = false
        ),
        LocationPreset(
            name = "Simulated Location via Mock Spoofing App (Suspicious)",
            latitude = 9.8075,
            longitude = 78.0915,
            isMockLocation = true,
            isFakeGps = true
        )
    )
}
