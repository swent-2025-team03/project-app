package com.android.agrihealth.ui.authentification

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

private val FieldBg = Color(0xFFF0F6F1)
private val ButtonBg = Color(0xFF9BB9B4)
private val TitleColor = Color(0xFF000000)

object SignInScreenTestTags {
  const val LOGIN_BUTTON = "loginButton"
  const val SIGN_UP_BUTTON = "signUpButton"
  const val LOGIN_TITLE = "loginTitle"
  const val EMAIL_FIELD = "emailField"
  const val PASSWORD_FIELD = "passwordField"
  const val FORGOT_PASSWORD = "forgotPassword"
  const val LOGIN_DIVIDER = "loginDivider"
}

@Composable
fun SignInScreen(
    modifier: Modifier = Modifier,
    onForgotPasswordClick: () -> Unit = {},
    onSignedIn: () -> Unit = {},
    goToSignUp: () -> Unit = {},
    signInViewModel: SignInViewModel = viewModel()
) {

  val signInUIState by signInViewModel.uiState.collectAsState()

  LaunchedEffect(signInUIState.user) { signInUIState.user?.let { onSignedIn() } }

  Box(
      modifier = modifier.background(FieldBg).fillMaxSize(),
      contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
              Spacer(Modifier.height(96.dp))

              Text(
                  text = "AgriHealth",
                  fontSize = 40.sp,
                  fontWeight = FontWeight.Medium,
                  color = TitleColor,
                  modifier = Modifier.testTag(SignInScreenTestTags.LOGIN_TITLE))

              Spacer(Modifier.height(56.dp))

              TextField(
                  value = signInUIState.email,
                  onValueChange = { signInViewModel.setEmail(it) },
                  placeholder = { Text("Email") },
                  singleLine = true,
                  shape = RoundedCornerShape(28.dp),
                  colors =
                      TextFieldDefaults.colors(
                          focusedContainerColor = FieldBg,
                          unfocusedContainerColor = FieldBg,
                          disabledContainerColor = FieldBg,
                          focusedIndicatorColor = Color.Transparent,
                          unfocusedIndicatorColor = Color.Transparent),
                  modifier =
                      Modifier.fillMaxWidth()
                          .height(56.dp)
                          .testTag(SignInScreenTestTags.EMAIL_FIELD))

              Spacer(Modifier.height(16.dp))

              TextField(
                  value = signInUIState.password,
                  onValueChange = { signInViewModel.setPassword(it) },
                  placeholder = { Text("Password") },
                  singleLine = true,
                  visualTransformation = PasswordVisualTransformation(),
                  shape = RoundedCornerShape(28.dp),
                  colors =
                      TextFieldDefaults.colors(
                          focusedContainerColor = FieldBg,
                          unfocusedContainerColor = FieldBg,
                          disabledContainerColor = FieldBg,
                          focusedIndicatorColor = Color.Transparent,
                          unfocusedIndicatorColor = Color.Transparent),
                  modifier =
                      Modifier.fillMaxWidth()
                          .height(56.dp)
                          .testTag(SignInScreenTestTags.PASSWORD_FIELD))

              Spacer(Modifier.height(8.dp))

              Row(Modifier.fillMaxWidth()) {
                Spacer(Modifier.weight(1f))
                Text(
                    text = "Forgot password",
                    fontSize = 14.sp,
                    color = Color.Black,
                    textAlign = TextAlign.End,
                    modifier =
                        Modifier.padding(top = 4.dp)
                            .clickable { onForgotPasswordClick() }
                            .testTag(SignInScreenTestTags.FORGOT_PASSWORD))
              }

              Spacer(Modifier.height(16.dp))

              HorizontalDivider(
                  color = Color.Black,
                  thickness = 1.dp,
                  modifier =
                      Modifier.fillMaxWidth(0.8f).testTag(SignInScreenTestTags.LOGIN_DIVIDER))
              Spacer(Modifier.height(24.dp))

              Button(
                  onClick = { signInViewModel.signIn() },
                  shape = RoundedCornerShape(28.dp),
                  colors = ButtonDefaults.buttonColors(containerColor = ButtonBg),
                  modifier =
                      Modifier.fillMaxWidth()
                          .height(56.dp)
                          .testTag(SignInScreenTestTags.LOGIN_BUTTON)) {
                    Text("Log In", color = Color.Black)
                  }

              Spacer(Modifier.height(16.dp))

              Button(
                  onClick = goToSignUp,
                  shape = RoundedCornerShape(28.dp),
                  colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                  modifier =
                      Modifier.fillMaxWidth()
                          .height(56.dp)
                          .testTag(SignInScreenTestTags.SIGN_UP_BUTTON)) {
                    Text("Create an account", color = Color.Black)
                  }
            }
      }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun SignInScreenPreview() {
  MaterialTheme { SignInScreen() }
}
