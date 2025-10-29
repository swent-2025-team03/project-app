package com.android.agrihealth.data.model.user

import com.android.agrihealth.data.model.location.Location

data class Vet(
    override var uid: String,
    override val firstname: String,
    override val lastname: String,
    override val email: String,
    override val address:
        Location?, // This should be the veterinary practice location, set just after creating an
    // account.
    val linkedFarmers: List<String> =
        emptyList<String>() // List of farmer IDs associated with the vet
) : User(uid, firstname, lastname, UserRole.VET, email)
