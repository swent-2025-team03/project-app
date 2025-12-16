package com.android.agrihealth.data.model.alert

/** Repository to get alert data */
interface AlertRepository {
  /** Fetches alerts from the repository */
  suspend fun getAlerts(): List<Alert>

  /** Fetches a single alert matching the provided alert ID */
  suspend fun getAlertById(alertId: String): Alert?

  /** Fetches the alert that comes right before the given one */
  fun getPreviousAlert(currentId: String): Alert?

  /** Fetches the alert that comes right after the given one */
  fun getNextAlert(currentId: String): Alert?
}
