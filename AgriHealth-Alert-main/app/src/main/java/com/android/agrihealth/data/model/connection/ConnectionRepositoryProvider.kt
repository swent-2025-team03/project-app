package com.android.agrihealth.data.model.connection

import com.android.agrihealth.core.constants.FirestoreSchema.Collections.FARMER_TO_OFFICE
import com.android.agrihealth.core.constants.FirestoreSchema.Collections.VET_TO_OFFICE

/** Singletons providing the connection repositories used to link various users to each other */
object ConnectionRepositoryProvider {
  val farmerToOfficeRepository by lazy { ConnectionRepository(connectionType = FARMER_TO_OFFICE) }
  val vetToOfficeRepository by lazy { ConnectionRepository(connectionType = VET_TO_OFFICE) }
}
