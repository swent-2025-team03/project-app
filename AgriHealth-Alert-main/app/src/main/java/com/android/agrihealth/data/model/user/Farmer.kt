package com.android.agrihealth.data.model.user

import com.android.agrihealth.data.model.location.Location

data class Farmer(
    override var uid: String,
    override val firstname: String,
    override val lastname: String,
    override val email: String,
    val address: Location?, // This should be the farm location, set just after creating an account.
    val linkedVets: List<String> =
        emptyList<String>(), // List of vet IDs associated with the farmer
    val defaultVet: String? // Default vet ID for quick access, can be changed in profile screen
) : User(uid, firstname, lastname, UserRole.FARMER, email)
