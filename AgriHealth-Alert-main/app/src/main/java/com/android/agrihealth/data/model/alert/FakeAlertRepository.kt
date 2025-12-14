package com.android.agrihealth.data.model.alert

import com.android.agrihealth.data.model.location.Location
import java.time.LocalDate
import kotlinx.coroutines.delay

class FakeAlertRepository(private val delayMs: Long = 0L) : AlertRepository {

  private val alerts =
      listOf(
          Alert(
              "1",
              "Heatwave Warning",
              "High temperatures expected",
              LocalDate.of(2025, 11, 22),
              "Lausanne",
              listOf(AlertZone(Location(46.5191, 6.5668), 10_000.0))),
          Alert(
              "2",
              "Drought Risk",
              "Rainfall expected to be low",
              LocalDate.of(2025, 11, 22),
              "Lausanne",
              listOf(AlertZone(Location(46.5191, 6.5668), 10_000.0))),
          Alert(
              "3",
              "Pest Outbreak",
              "Caterpillar infestation possible",
              LocalDate.of(2025, 11, 22),
              "Montreux",
              listOf(AlertZone(Location(46.4316, 6.9119), 10_000.0))),
          Alert(
              "4",
              "Another mock alert item to test what happens if details are too long!",
              "So I have to write a really long description here... blah blah mousudeni netagire de nanikakebaiika wakaranai kara toriaezu nihongo ni sureba meccha mojisuu kasegerukigasuru waai, unn motto kakuhituyouga arisoudesu! doushiyo, nanikakoukana, kyouha ohiru nanitabeyoukana- hisashiburi ni indian no toko ikitaina- saikinn channtoshita shokuji amari tottenaikara channto yasai to gohan wo tabeyou!",
              LocalDate.of(2025, 11, 29),
              "Montreux",
              listOf(AlertZone(Location(46.4316, 6.9119), 10_000.0))))

  val allAlerts: List<Alert>
    get() = alerts

  override suspend fun getAlerts(): List<Alert> {
    delay(delayMs)
    return alerts
  }

  override suspend fun getAlertById(alertId: String): Alert? {
    delay(delayMs)
    return alerts.find { it.id == alertId }
  }

  override fun getPreviousAlert(currentId: String): Alert? {
    val index = alerts.indexOfFirst { it.id == currentId }
    return if (index > 0) alerts[index - 1] else null
  }

  override fun getNextAlert(currentId: String): Alert? {
    val index = alerts.indexOfFirst { it.id == currentId }
    return if (index < alerts.size - 1) alerts[index + 1] else null
  }
}
