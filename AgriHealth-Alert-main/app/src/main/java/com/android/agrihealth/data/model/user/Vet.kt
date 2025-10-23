package com.android.agrihealth.data.model.user

import com.android.agrihealth.data.model.location.Location

// TODO: review pertinence, and missing fields
data class Vet(
    val vetId: String, // Need to discuss if we keep a String
    override val firstname: String,
    override val lastname: String,
    override val email: String,
    val address:
        Location?, // This should be the veterinary practice location, set just after creating an
    // account.
    val linkedFarmers: List<String> =
        emptyList<String>() // List of farmer IDs associated with the vet
) : User(vetId, firstname, lastname, UserRole.VET, email)
