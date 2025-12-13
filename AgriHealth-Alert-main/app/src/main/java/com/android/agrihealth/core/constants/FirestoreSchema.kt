package com.android.agrihealth.core.constants

object FirestoreSchema {
  object Collections {
    const val FARMER_TO_OFFICE = "farmerToOffice"
    const val VET_TO_OFFICE = "vetToOffice"
    const val CONNECT_CODES = "ConnectCodes"
  }

  object ConnectCodes {
    const val CODE = "code"
    const val OFFICE_ID = "officeId"
    const val STATUS = "status"
    const val CREATED_AT = "createdAt"
    const val USED_AT = "usedAt"
    const val CLAIMED_BY = "claimedBy"
  }

  object Status {
    const val OPEN = "OPEN"
    const val USED = "USED"
  }
}