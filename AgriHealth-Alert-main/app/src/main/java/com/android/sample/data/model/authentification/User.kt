package com.android.sample.data.model.authentification

data class User(
    val uid: String,
    val name: String,
    val surname: String,
    val role: UserRole,
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
