package com.android.agrihealth.data.model.alert

import kotlin.math.*

/**
 * Calculates the distance between two points in meters.
 *
 * Uses the Haversine formula to compute the great-circle distance on the Earth's surface.
 *
 * @param lat1 Latitude of the first point
 * @param lon1 Longitude of the first point
 * @param lat2 Latitude of the second point
 * @param lon2 Longitude of the second point
 * @return Distance between the two points in meters
 */
fun distanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
  val R = 6371000.0 // Average radius of the Earth in meters
  val dLat = Math.toRadians(lat2 - lat1)
  val dLon = Math.toRadians(lon2 - lon1)
  val a =
      sin(dLat / 2).pow(2) +
          cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
  val c = 2 * atan2(sqrt(a), sqrt(1 - a))
  return R * c
}

/**
 * Checks if a user's location is within any of the Alert's zones.
 *
 * Each zone is represented as a circle with a center (latitude, longitude) and a radius in meters.
 * If the user's coordinates fall within at least one of the zones, the function returns true.
 *
 * @param userLat Latitude of the user's location
 * @param userLon Longitude of the user's location
 * @return true if the user is inside any zone, false otherwise
 */
fun Alert.containsUser(userLat: Double, userLon: Double): Boolean {
  return zones.orEmpty().any { zone ->
    val distance = distanceMeters(userLat, userLon, zone.center.latitude, zone.center.longitude)
    distance <= zone.radiusMeters
  }
}
