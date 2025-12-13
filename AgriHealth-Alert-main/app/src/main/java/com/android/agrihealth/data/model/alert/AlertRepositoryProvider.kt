package com.android.agrihealth.data.model.alert

object AlertRepositoryProvider {
  private val _repository: AlertRepository by lazy { FakeAlertRepository() }

  var repository: AlertRepository = AlertRepositoryProvider._repository
}
