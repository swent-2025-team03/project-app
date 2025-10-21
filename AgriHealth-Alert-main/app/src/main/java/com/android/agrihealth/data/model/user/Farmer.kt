package com.android.agrihealth.data.model.user

import com.android.agrihealth.data.model.location.Location

// TODO: review pertinence, and missing fields
data class Farmer(
    val farmerId: String, // Need to discuss if we keep a String
    val name: String,
    val forenames: String,
    val email: String,
    val address: Location, // This should be the farm location, set just after creating an account.
    val linkedVets: List<String> =
        emptyList<String>(), // List of vet IDs associated with the farmer
    val defaultVet: String // Default vet ID for quick access, can be changed in profile screen
)
