package com.android.agrihealth.data.model.alert

object AlertRepositoryProvider {
  private val _repository: AlertRepository by lazy { AlertRepositoryFirestore() }

  var repository: AlertRepository = AlertRepositoryProvider._repository
}
