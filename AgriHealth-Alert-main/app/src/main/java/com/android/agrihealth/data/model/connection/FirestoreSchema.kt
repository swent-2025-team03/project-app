package com.android.agrihealth.data.model.connection

object FirestoreSchema {
  object Collections {
    const val FARMER_TO_OFFICE = "farmerToOffice"
    const val VET_TO_OFFICE = "vetToOffice"
    const val CONNECT_CODES = "ConnectCodes"
    const val CONNECTIONS = "Connections"
  }

  object ConnectCodes {
    const val CODE = "code"
    const val VET_ID = "vetId"
    const val OFFICE_ID = "officeId"
    const val STATUS = "status"
    const val CREATED_AT = "createdAt"
    const val TTL_MINUTES = "ttlMinutes"
    const val USED_AT = "usedAt"
    const val CLAIMED_BY = "claimedBy"
  }

  object Connections {
    const val VET_ID = "vetId"
    const val FARMER_ID = "farmerId"
    const val CREATED_AT = "createdAt"
    const val ACTIVE = "active"
  }

  object Status {
    const val OPEN = "OPEN"
    const val USED = "USED"
  }
}
