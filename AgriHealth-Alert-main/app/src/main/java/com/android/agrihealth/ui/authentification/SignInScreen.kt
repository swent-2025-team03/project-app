package com.android.agrihealth.ui.authentification

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview

object SignInScreenTestTags {
  const val SIGN_IN_BUTTON = "signInButton"
  const val SIGN_UP_BUTTON = "signUpButton"
}

@Composable
fun SignInScreen(
    onSignedIn: () -> Unit = {},
    goToSignUp: () -> Unit = {},
) {
  Scaffold(
      content = { padding ->
        Column {
          Button(
              onClick = onSignedIn,
              modifier = Modifier.padding(padding).testTag(SignInScreenTestTags.SIGN_IN_BUTTON)) {
                Text(text = "Sign In")
              }
          Button(
              onClick = goToSignUp,
              modifier = Modifier.padding(padding).testTag(SignInScreenTestTags.SIGN_UP_BUTTON)) {
                Text(text = "Sign Up")
              }
        }
      })
}

@Preview
@Composable
fun SignInScreenPreview() {
  SignInScreen(onSignedIn = {})
}
