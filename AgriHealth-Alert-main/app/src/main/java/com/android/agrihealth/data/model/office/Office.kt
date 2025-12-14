package com.android.agrihealth.data.model.office

import com.android.agrihealth.data.model.location.Location

/**
 * An Office represents a veterinary practice / group of vets.
 *
 * @param id Firestore doc id / uid
 * @param name Display name
 * @param address Physical address (Location nullable)
 * @param description Optional description
 * @param vets List of vet UIDs working in this office (can be empty)
 * @param ownerId UID of the vet who created / owns the office (has full control initially)
 */
data class Office(
    val id: String,
    val name: String,
    val address: Location? = null,
    val description: String? = null,
    val vets: List<String> = emptyList(),
    val ownerId: String, // uid of the vet who created it
    val photoUrl: String? = null
)
