package com.android.agrihealth.data.model.authentification


import com.google.firebase.auth.FirebaseAuth

class FirebaseAuthProvider(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : AuthProvider {

    override val currentUserUid: String?
        get() = auth.currentUser?.uid
}
