package com.android.agrihealth.data.model.user

import com.android.agrihealth.data.model.location.Location

sealed class User(
    open var uid: String = "placeholder",
    open val firstname: String,
    open val lastname: String,
    open val role: UserRole,
    open val email: String,
    open val address: Location?,
    open val isGoogleAccount: Boolean = false,
    open val description: String? = null,
    open val deviceTokensFCM: Set<String> = emptySet() // Notifications/Firebase messaging service
)

/** Copies fields common to farmers and vets, because User is a sealed class */
fun User.copyCommon(
    uid: String = this.uid,
    firstname: String = this.firstname,
    lastname: String = this.lastname,
    email: String = this.email,
    address: Location? = this.address,
    isGoogleAccount: Boolean = this.isGoogleAccount,
    description: String? = this.description,
    deviceTokensFCM: Set<String> = this.deviceTokensFCM
): User {
  return when (this) {
    is Farmer ->
        Farmer(
            uid,
            firstname,
            lastname,
            email,
            address,
            this.linkedOffices,
            this.defaultOffice,
            isGoogleAccount,
            description,
            deviceTokensFCM)
    is Vet ->
        Vet(
            uid,
            firstname,
            lastname,
            email,
            address,
            this.validCodes,
            this.officeId,
            isGoogleAccount,
            description,
            deviceTokensFCM)
  }
}

enum class UserRole {
  FARMER,
  VET
}

/**
 * Converts UserRole enum to a more readable display String (camel case).
 *
 * @return A string representation of the UserRole, formatted for display.
 */
fun UserRole.displayString(): String =
    name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

fun roleFromDisplayString(role: String): UserRole? {
  return UserRole.entries.firstOrNull { it.displayString().lowercase() == role.lowercase() }
}
