package com.android.sample.data.model

// This file's goal is to define the data structures used in the app. This is a preliminary version
// that we will probably modify in the soon future so we can better fit the app's needs.
// We are keeping it simple for now, but we will likely need to add more fields and relations later.

// Represents the possible statuses for a veterinary report.
// ESCALATED is intentionally handled separately by the UI (dedicated button).
enum class ReportStatus {
  PENDING,
  IN_PROGRESS,
  RESOLVED,
  ESCALATED
}

// Represents the type of user currently using the app.
// Used to adapt the UI (farmer vs. vet behavior).
enum class UserRole {
  FARMER,
  VET,
  AUTHORITY
}

// Simple location data structure, same as is the Bootcamp
data class Location(val latitude: Double, val longitude: Double, val name: String? = null)

// Main data class describing a veterinary report.
// Some fields will be resolved from IDs later (e.g., farmerName from farmerId).
data class Report(
    val id: String,
    val title: String,
    val description: String,
    val photoUri: String?, // For now, unused (will show placeholder)
    val farmerId: String,
    val vetId: String?,
    val status: ReportStatus,
    val answer: String?,
    val location: Location?
)
