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
   * Success:
   * - Always returns a newly generated unique ID.
   *
   * Failure:
   * - Never fails (pure local operation).
   */
  fun getNewUid(): String

  /**
   * Creates a new office.
   *
   * Success:
   * - A new office document is persisted using the ID inside the Office object.
   *
   * Failure:
   * - **Permission denied**: Happens when the current user is not a vet (only vets are allowed to
   *   create offices).
   * - Network or backend errors (e.g., Firestore exceptions).
   */
  suspend fun addOffice(office: Office)

  /**
   * Updates an existing office.
   *
   * Success:
   * - Office fields are updated.
   * - Fields that are immutable (like ownerId) must NOT be changed.
   *
   * Failure:
   * - **Office does not exist**: No office with the given ID is found.
   * - **Permission denied**: Happens when the current user is NOT the owner of the office.
   * - Network or backend errors.
   */
  suspend fun updateOffice(office: Office)

  /**
   * Deletes an office by ID.
   *
   * Success:
   * - The office document is removed.
   *
   * Failure:
   * - **Office does not exist**: No office with the given ID is found.
   * - **Permission denied**: Happens when the current user is NOT the owner of the office.
   * - Network or backend errors.
   */
  suspend fun deleteOffice(id: String)

  /**
   * Reads an office by ID.
   *
   * Success:
   * - Returns Result.success(Office) with the retrieved office.
   *
   * Failure:
   * - **Office does not exist**.
   * - **Permission denied**: Happens when the current user does not have read access (depends on
   *   your Firestore security rules).
   * - Network or backend errors.
   *
   * Always returns a Result, never throws.
   */
  suspend fun getOffice(id: String): Result<Office>
}
