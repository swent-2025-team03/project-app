package com.android.agrihealth.data.model.auth

import com.google.firebase.Firebase
import com.google.firebase.auth.auth

object FirebaseAuthProvider : AuthProvider {
    override fun currentUserId(): String? = Firebase.auth.currentUser?.uid
}

