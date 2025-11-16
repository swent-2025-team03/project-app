package com.android.agrihealth.data.model.office

/** Repository interface for reading and writing Office entities. */
interface OfficeRepository {

  /** Generates a new unique office ID. */
  fun getNewUid(): String

  /**
   * Creates a new office entry.
   *
   * Success:
   * - Office is persisted with the given ID.
   *
   * Failure:
   * - Permission denied (only vets can create).
   * - Network or backend errors.
   */
  suspend fun addOffice(office: Office)

  /**
   * Updates an existing office.
   *
   * Success:
   * - Office fields are updated (except immutable fields like ownerId, enforced by rules).
   *
   * Failure:
   * - Office does not exist.
   * - Permission denied (only owner can update).
   * - Network/server issues.
   */
  suspend fun updateOffice(office: Office)

  /**
   * Deletes an existing office.
   *
   * Success:
   * - Office document is removed.
   *
   * Failure:
   * - User is not the owner.
   * - Office does not exist.
   */
  suspend fun deleteOffice(id: String)

  /**
   * Reads an office by ID.
   *
   * Success:
   * - Returns Result.success with the Office.
   *
   * Failure:
   * - Office does not exist.
   * - Permission denied.
   * - Network/server error.
   */
  suspend fun getOffice(id: String): Result<Office>
}
