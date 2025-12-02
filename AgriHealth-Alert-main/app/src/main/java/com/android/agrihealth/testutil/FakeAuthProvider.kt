package com.android.agrihealth.testutil

import com.android.agrihealth.data.model.authentification.AuthProvider

class FakeAuthProvider(
    override var currentUserUid: String? = null
) : AuthProvider
