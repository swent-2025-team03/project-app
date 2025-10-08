package com.android.sample.ui.authentification

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

object SignInScreenTestTags {
  const val SIGN_IN_BUTTON = "signInButton"
}

@Composable
fun SignInScreen(
    onSignedIn: () -> Unit,
) {
  Scaffold(
      content = { padding ->
        Button(onClick = onSignedIn, modifier = Modifier.padding(padding)) {
          Text(text = "Sign In")
        }
      })
}

@Preview
@Composable
fun SignInScreenPreview() {
  SignInScreen(onSignedIn = {})
}
