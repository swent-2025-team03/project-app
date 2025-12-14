package com.android.agrihealth.data.model.alert

object AlertRepositoryProvider {

  private var overrideRepo: AlertRepository? = null
  private val _repository: AlertRepository by lazy { AlertRepositoryFirestore() }

  fun set(repo: AlertRepository) {
    overrideRepo = repo
  }

  fun reset() {
    overrideRepo = null
  }

  fun get(): AlertRepository = overrideRepo ?: _repository
}
