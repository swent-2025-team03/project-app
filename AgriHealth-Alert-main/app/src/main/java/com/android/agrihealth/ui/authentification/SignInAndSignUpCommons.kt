package com.android.agrihealth.ui.authentification

import android.util.Patterns
import com.google.firebase.auth.FirebaseUser

interface SignInAndSignUpCommons {
  val email: String
  val password: String
  val user: FirebaseUser?

  fun isValid(): Boolean = Patterns.EMAIL_ADDRESS.matcher(email).matches() && password.length > 6
}
