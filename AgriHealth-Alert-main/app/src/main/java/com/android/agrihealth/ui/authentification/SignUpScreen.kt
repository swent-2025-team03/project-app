package com.android.agrihealth.ui.authentification

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

object SignUpScreenTestTags {
  const val SIGN_IN_BUTTON = "signInButton"
}

@Composable
fun SignUpScreen(
    onSignedUp: () -> Unit,
) {
  Scaffold(
      content = { padding ->
        Button(onClick = onSignedUp, modifier = Modifier.padding(padding)) {
          Text(text = "Sign In")
        }
      })
}
