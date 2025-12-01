package com.android.agrihealth.data.model.office

object OfficeRepositoryProvider {
  private val firestoreRepository by lazy { OfficeRepositoryFirestore() }

  // Current repository, overridable for tests
  private var currentRepository: OfficeRepository = firestoreRepository

  fun get(): OfficeRepository = currentRepository

  // Allow tests to inject a fake/in-memory repository
  fun set(repository: OfficeRepository) {
    currentRepository = repository
  }
}
