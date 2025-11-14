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
    open val description: String? = null
)

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

fun roleFromDisplayString(role: String): UserRole {
  return UserRole.entries.firstOrNull { it.displayString().lowercase() == role.lowercase() }
      ?: throw IllegalArgumentException("Invalid role")
}
