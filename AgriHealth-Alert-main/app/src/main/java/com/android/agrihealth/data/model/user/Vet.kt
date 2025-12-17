package com.android.agrihealth.data.model.user

import com.android.agrihealth.data.model.location.Location

/** User of type Vet, can receive reports from Farmers and answer them */
data class Vet(
    override var uid: String,
    override val firstname: String,
    override val lastname: String,
    override val email: String,
    override val address: Location?, // This should be the veterinary practice location
    val farmerConnectCodes: List<String> = emptyList(),
    val vetConnectCodes: List<String> = emptyList(),
    val officeId: String? = null, // Which office the vet belongs to (unique)
    override val isGoogleAccount: Boolean = false,
    override val description: String? = null,
    override val collected: Boolean = false,
    override val deviceTokensFCM: Set<String> = emptySet(),
    override val photoURL: String? = null
) :
    User(
        uid,
        firstname,
        lastname,
        UserRole.VET,
        email,
        address,
        isGoogleAccount,
        description,
        collected,
        deviceTokensFCM,
        photoURL)
