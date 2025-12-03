package com.android.agrihealth.data.model.alert

import com.android.agrihealth.data.model.location.Location
import kotlin.math.*

/**
 * Calculates the distance between two locations in meters.
 *
 * Uses the Haversine formula to compute the great-circle distance on the Earth's surface.
 *
 * @param location1 Location of the first point
 * @param location2 Location of the second point
 * @return Distance between the two points in meters
 */
fun distanceMeters(location1: Location, location2: Location): Double {
  val lat1 = location1.latitude
  val lon1 = location1.longitude
  val lat2 = location2.latitude
  val lon2 = location2.longitude
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
 * Returns the distance from the Alert's center to the given location if it is inside any zone, or
 * null if the location is outside all zones.
 *
 * Each zone is represented as a circle with a center (latitude, longitude) and a radius in meters.
 *
 * @param address The location to check
 * @return distance in meters from the alert's center if inside any zone, null otherwise
 */
fun Alert.getDistanceInsideZone(address: Location): Double? {
  return zones
      .orEmpty()
      .mapNotNull { zone ->
        val distance = distanceMeters(address, zone.center)
        if (distance <= zone.radiusMeters) distance else null
      }
      .minOrNull()
}
