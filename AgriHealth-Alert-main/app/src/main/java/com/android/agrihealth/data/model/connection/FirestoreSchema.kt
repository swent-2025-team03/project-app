package com.android.agrihealth.data.model.connection

object FirestoreSchema {
  object Collections {
    const val CONNECT_CODES = "connect_codes"
    const val CONNECTIONS = "connections"
  }

  object ConnectCodes {
    const val CODE = "code"
    const val VET_ID = "vetId"
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
