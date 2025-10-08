package com.android.sample.ui.authentification

import android.util.Patterns

interface SignInAndSignUpCommons {
  val email: String
  val password: String

  fun isValid(): Boolean = Patterns.EMAIL_ADDRESS.matcher(email).matches() && password.length > 6
}
