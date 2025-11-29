package com.android.agrihealth.data.model.alert

import com.android.agrihealth.data.model.location.Location
import java.time.LocalDate

data class AlertZone(val center: Location, val radiusMeters: Double)

data class Alert(
    val id: String,
    val title: String,
    val description: String,
    val outbreakDate: LocalDate,
    val region: String,
    val zones: List<AlertZone>
)
