package com.android.agrihealth.data.model.user

import com.android.agrihealth.data.model.location.Location

data class Vet(
    override var uid: String,
    override val firstname: String,
    override val lastname: String,
    override val email: String,
    override val address: Location?,
    // This should be the veterinary practice location, set just after creating an account.
    val farmerConnectCodes: List<String> = emptyList(),
    val vetConnectCodes: List<String> = emptyList(),
    val officeId: String? = null, // Which office the vet belongs to (unique)
    override val isGoogleAccount: Boolean = false,
    override val description: String? = null,
    override val collected: Boolean = false
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
        collected)
