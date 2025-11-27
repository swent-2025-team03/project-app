package com.android.agrihealth.data.model.user

import com.android.agrihealth.data.model.location.Location

data class Farmer(
    override var uid: String,
    override val firstname: String,
    override val lastname: String,
    override val email: String,
    override val address:
        Location?, // This should be the farm location, set just after creating an account.
    val linkedOffices: List<String> =
        emptyList<String>(), // List of office IDs associated with the farmer
    var defaultOffice:
        String?, // Default office ID for quick access, can be changed in profile screen
    override val isGoogleAccount: Boolean = false,
    override val description: String? = null
) : User(uid, firstname, lastname, UserRole.FARMER, email, address, isGoogleAccount, description)
