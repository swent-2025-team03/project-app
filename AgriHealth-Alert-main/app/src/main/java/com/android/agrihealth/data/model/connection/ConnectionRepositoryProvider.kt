package com.android.agrihealth.data.model.connection

object ConnectionRepositoryProvider {
  private val _farmerToOfficeRepository by lazy {
    ConnectionRepository(connectionType = FirestoreSchema.Collections.FARMER_TO_OFFICE)
  }
  private val _vetToOfficeRepository by lazy {
    ConnectionRepository(connectionType = FirestoreSchema.Collections.VET_TO_OFFICE)
  }

  val farmerToOfficeRepository = _farmerToOfficeRepository
  val vetToOfficeRepository = _vetToOfficeRepository
}
