package com.android.agrihealth.data.model.authentification

import kotlin.enums.enumEntries

data class User(
    val uid: String = "placeholder",
    val name: String,
    val surname: String,
    val role: UserRole,
    val email: String
)

enum class UserRole {
  FARMER,
  VETERINARIAN,
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