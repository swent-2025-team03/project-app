package com.android.agrihealth.data.model.office

/**
 * Repository interface for creating, reading, updating, and deleting Office entities.
 *
 * Implementations must enforce the following permission rules:
 * - Only **vets** (users with role = "vet") may create offices.
 * - Only the **office owner** (office.ownerId) may update or delete their office.
 */
interface OfficeRepository {

  /**
   * Generates a new unique office ID.
   *
   * Only a Vet can be able to create a new Office, thus a new OfficeId
   */
  fun getNewUid(): String

  /**
   * Creates a new office.
   *
   * Only a Vet can be able to create a new Office
   */
  suspend fun addOffice(office: Office)

  /**
   * Updates an existing office.
   *
   * Only the Owner of the Office (Vet) can update the Office, except the vet list that can be
   * updated by any Vet
   */
  suspend fun updateOffice(office: Office)

  /**
   * Deletes an office by ID.
   *
   * Only the Owner of the Office (Vet) can delete the Office
   */
  suspend fun deleteOffice(id: String)

  /**
   * Reads an office by ID.
   *
   * Only Members of an Office and Farmers who send reports to said Office can retrieve its data
   *
   * Always returns a Result, never throws.
   */
  suspend fun getOffice(id: String): Result<Office>

  /**
   * Gets the IDs of all Vet users belonging to the given office ID.
   *
   * @param officeId The ID of the office to filter Vets by.
   * @return A list of Vet user IDs assigned to that office.
   */
  suspend fun getVetsInOffice(officeId: String): List<String>
}
