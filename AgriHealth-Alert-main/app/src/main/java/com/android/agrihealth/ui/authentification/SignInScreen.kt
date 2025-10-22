package com.android.agrihealth.ui.authentification

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.agrihealth.R

private val FieldBg = Color(0xFFF0F6F1)
private val ButtonBg = Color(0xFF9BB9B4)
private val TitleColor = Color(0xFF000000)

object SignInScreenTestTags {
  const val SCREEN = "SignInScreen"
  const val LOGIN_BUTTON = "loginButton"
  const val SIGN_UP_BUTTON = "signUpButton"
  const val LOGIN_TITLE = "loginTitle"
  const val EMAIL_FIELD = "emailField"
  const val PASSWORD_FIELD = "passwordField"
  const val FORGOT_PASSWORD = "forgotPassword"
  const val LOGIN_DIVIDER = "loginDivider"
  const val GOOGLE_LOGIN_BUTTON = "googleLoginButton"
}

@Composable
fun SignInScreen(
    modifier: Modifier = Modifier,
    credentialManager: CredentialManager = CredentialManager.create(LocalContext.current),
    onForgotPasswordClick: () -> Unit = {},
    onSignedIn: () -> Unit = {},
    goToSignUp: () -> Unit = {},
    signInViewModel: SignInViewModel = viewModel()
) {

  val signInUIState by signInViewModel.uiState.collectAsState()

  val context = LocalContext.current

  LaunchedEffect(signInUIState.user) { signInUIState.user?.let { onSignedIn() } }

  Box(
      modifier = modifier.background(FieldBg).fillMaxSize().testTag(SignInScreenTestTags.SCREEN),
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
                  onClick = { signInViewModel.signInWithEmailAndPassword() },
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
              Spacer(Modifier.height(32.dp))
              GoogleSignInButton {
                signInViewModel.signInWithGoogle(
                    context = context, credentialManager = credentialManager)
              }
            }
      }
}

// function from bootcamp-25-B3-Solution
@Composable
fun GoogleSignInButton(onSignInClick: () -> Unit) {
  Button(
      onClick = onSignInClick,
      colors = ButtonDefaults.buttonColors(containerColor = Color.White), // Button color
      shape = RoundedCornerShape(50), // Circular edges for the button
      modifier =
          Modifier.padding(8.dp)
              .height(48.dp) // Adjust height as needed
              .testTag(SignInScreenTestTags.GOOGLE_LOGIN_BUTTON)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()) {
              // Load the Google logo from resources
              Image(
                  painter =
                      painterResource(id = R.drawable.google_logo), // Ensure this drawable exists
                  contentDescription = "Google Logo",
                  modifier =
                      Modifier.size(30.dp) // Size of the Google logo
                          .padding(end = 8.dp))

              // Text for the button
              Text(
                  text = "Sign in with Google",
                  color = Color.Gray, // Text color
                  fontSize = 16.sp, // Font size
                  fontWeight = FontWeight.Medium)
            }
      }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun SignInScreenPreview() {
  MaterialTheme { SignInScreen() }
}
