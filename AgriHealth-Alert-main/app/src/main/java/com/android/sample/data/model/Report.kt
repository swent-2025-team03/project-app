package com.android.sample.data.model

enum class ReportStatus {
    PENDING,
    IN_PROGRESS,
    RESOLVED,
    ESCALATED
}

enum class UserRole {
    FARMER,
    VET,
    AUTHORITY
}

data class Location(
    val latitude: Double,
    val longitude: Double,
    val name: String? = null
)

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