package com.android.agrihealth.data.model.alert

import java.time.LocalDate

data class Alert(
    val id: String,
    val title: String,
    val description: String,
    val outbreakDate: LocalDate,
    val region: String
)
