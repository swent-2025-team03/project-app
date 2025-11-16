package com.android.agrihealth.data.model.auth

interface AuthProvider {
    fun currentUserId(): String?
}

