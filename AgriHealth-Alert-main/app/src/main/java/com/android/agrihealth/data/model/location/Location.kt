package com.android.agrihealth.data.model.location

data class Location(val latitude: Double, val longitude: Double, val name: String? = null)

/** Generate a Location object from a data map, used to convert firestore documents. */
fun locationFromMap(locationData: Map<*, *>?): Location? {
  return locationData?.let {
    Location(
        latitude = it["latitude"] as? Double ?: 0.0,
        longitude = it["longitude"] as? Double ?: 0.0,
        name = it["name"] as? String ?: "")
  }
}
