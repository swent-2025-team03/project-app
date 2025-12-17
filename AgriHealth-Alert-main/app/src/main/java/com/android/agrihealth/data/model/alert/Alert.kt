package com.android.agrihealth.data.model.alert

import com.android.agrihealth.data.model.location.Location
import java.time.LocalDate

/** A single zone where an alert is relevant */
data class AlertZone(val center: Location, val radiusMeters: Double)

/** Alert to show everyone when something special is happening at a certain place */
data class Alert(
    val id: String,
    val title: String,
    val description: String,
    val outbreakDate: LocalDate,
    val region: String? = null,
    val zones: List<AlertZone>? = null
)
