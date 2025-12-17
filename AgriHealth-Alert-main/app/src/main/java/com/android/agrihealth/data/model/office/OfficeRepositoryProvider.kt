package com.android.agrihealth.data.model.office

/** Singleton providing the office repository used throughout the app */
object OfficeRepositoryProvider {
  private var overrideRepo: OfficeRepository? = null
  private val firestoreRepository by lazy { OfficeRepositoryFirestore() }

  fun set(repo: OfficeRepository) {
    overrideRepo = repo
  }

  fun reset() {
    overrideRepo = null
  }

  fun get(): OfficeRepository = overrideRepo ?: firestoreRepository
}
