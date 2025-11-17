package com.android.agrihealth.data.model.office

object OfficeRepositoryProvider {
  private val firestoreRepository by lazy { OfficeRepositoryFirestore() }

  fun get(): OfficeRepository = firestoreRepository
}
