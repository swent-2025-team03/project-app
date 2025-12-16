package com.android.agrihealth.data.model.user

import com.android.agrihealth.data.model.location.Location

/** User of type Farmer, can send reports to vets */
data class Farmer(
    override var uid: String,
    override val firstname: String,
    override val lastname: String,
    override val email: String,
    override val address: Location?, // This should be the farm location
    val linkedOffices: List<String> = emptyList(), // List of offices associated with the farmer
    var defaultOffice: String?, // Default office for quick access, can be changed in profile screen
    override val isGoogleAccount: Boolean = false,
    override val description: String? = null,
    override val collected: Boolean = false,
    override val deviceTokensFCM: Set<String> = emptySet()
) :
    User(
        uid,
        firstname,
        lastname,
        UserRole.FARMER,
        email,
        address,
        isGoogleAccount,
        description,
        collected,
        deviceTokensFCM)
