package com.android.agrihealth.data.model.alert

interface AlertRepository {
    suspend fun getAlerts(): List<Alert>
}