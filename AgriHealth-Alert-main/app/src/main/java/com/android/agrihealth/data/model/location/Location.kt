package com.android.agrihealth.data.model.location

import com.google.android.gms.maps.model.LatLng
import kotlin.math.cos
import kotlin.math.sin

data class Location(val latitude: Double, val longitude: Double, val name: String? = null)

fun Location.toLatLng(): LatLng {
  return LatLng(this.latitude, this.longitude)
}

/** Generate a Location object from a data map, used to convert firestore documents. */
fun locationFromMap(locationData: Map<*, *>?): Location? {
  return locationData?.let {
    Location(
        latitude = it["latitude"] as? Double ?: 0.0,
        longitude = it["longitude"] as? Double ?: 0.0,
        name = it["name"] as? String ?: "")
  }
}

/**
 * Offset [lat] and [lng] by [distanceMeters] in the direction of [angleRadians].
 *
 * @param lat the latitude of the point to offset
 * @param lng the longitude of the point to offset
 * @param distanceMeters the distance in meters to offset by which the point is offset
 * @param angleRadians angle to offset by. 0 offset to the right, PI offset to the left.
 */
fun offsetLatLng(lat: Double, lng: Double, distanceMeters: Double, angleRadians: Double): Location {
  val earthRadius = 6371000.0 // meters
  val dLat = (distanceMeters / earthRadius) * sin(angleRadians)
  val dLng = (distanceMeters / (earthRadius * cos(Math.toRadians(lat)))) * cos(angleRadians)

  return Location(lat + Math.toDegrees(dLat), lng + Math.toDegrees(dLng))
  // return LatLng(lat + Math.toDegrees(dLat), lng + Math.toDegrees(dLng))
}
