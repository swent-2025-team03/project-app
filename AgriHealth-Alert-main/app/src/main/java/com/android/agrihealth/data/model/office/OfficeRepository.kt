package com.android.agrihealth.data.model.office

interface OfficeRepository {
  suspend fun addOffice(office: Office)

  suspend fun updateOffice(office: Office)

  suspend fun deleteOffice(id: String)

  suspend fun getOffice(id: String): Result<Office>

  suspend fun listOffices():
      Result<List<Office>> // Added this one in case we need to list all offices
}
