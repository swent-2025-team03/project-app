package com.android.agrihealth.data.model.alert

interface AlertRepository {
  suspend fun getAlerts(): List<Alert>

  suspend fun getAlertById(alertId: String): Alert?

  fun getPreviousAlert(currentId: String): Alert?

  fun getNextAlert(currentId: String): Alert?
}
