package com.android.agrihealth.data.model.connection

import com.android.agrihealth.data.model.connection.FirestoreSchema.Collections.FARMER_TO_OFFICE
import com.android.agrihealth.data.model.connection.FirestoreSchema.Collections.VET_TO_OFFICE

object ConnectionRepositoryProvider {
  private val _farmerToOfficeRepository by lazy {
    ConnectionRepository(connectionType = FARMER_TO_OFFICE)
  }
  private val _vetToOfficeRepository by lazy {
    ConnectionRepository(connectionType = VET_TO_OFFICE)
  }

  val farmerToOfficeRepository = _farmerToOfficeRepository
  val vetToOfficeRepository = _vetToOfficeRepository
}
