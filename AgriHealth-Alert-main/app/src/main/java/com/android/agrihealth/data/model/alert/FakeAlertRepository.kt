package com.android.agrihealth.data.model.alert

import java.time.LocalDate

class FakeAlertRepository : AlertRepository {
    override suspend fun getAlerts(): List<Alert> {
        return listOf(
            Alert("1", "Heatwave Warning", "High temperatures expected", LocalDate.of(2025, 11, 22), "Vaud, Switzerland"),
            Alert("2", "Drought Risk", "Rainfall expected to be low", LocalDate.of(2025, 11, 22), "Vaud, Switzerland"),
            Alert("3", "Pest Outbreak", "Caterpillar infestation possible", LocalDate.of(2025, 11, 22), "Vaud, Switzerland")
        )
    }
}