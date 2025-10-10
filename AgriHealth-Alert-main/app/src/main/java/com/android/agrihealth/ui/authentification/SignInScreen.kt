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
private val LogInButtonTestTag = "LogInButton"
private val SignUpButtonTestTag = "SignUpButton"
private val TitleTestTag = "LoginTitle"
private val EmailFieldTestTag = "EmailField"
private val PasswordFieldTestTag = "PasswordField"
private val ForgotPasswordTestTag = "ForgotPassword"
private val DividerTestTag = "LoginDivider"

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
                  modifier = Modifier.testTag(TitleTestTag))

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
                  modifier = Modifier.fillMaxWidth().height(56.dp).testTag(EmailFieldTestTag))

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
                  modifier = Modifier.fillMaxWidth().height(56.dp).testTag(PasswordFieldTestTag))

              Spacer(Modifier.height(8.dp))

              Row(Modifier.fillMaxWidth()) {
                Spacer(Modifier.weight(1f))
                Text(
                    text = "Password forget",
                    fontSize = 14.sp,
                    color = Color.Black,
                    textAlign = TextAlign.End,
                    modifier =
                        Modifier.padding(top = 4.dp)
                            .clickable { onForgotPasswordClick() }
                            .testTag(ForgotPasswordTestTag))
              }

              Spacer(Modifier.height(16.dp))

              HorizontalDivider(
                  color = Color.Black,
                  thickness = 1.dp,
                  modifier = Modifier.fillMaxWidth(0.8f).testTag(DividerTestTag))

              Spacer(Modifier.height(24.dp))

              Button(
                  onClick = { signInViewModel.signIn() },
                  shape = RoundedCornerShape(28.dp),
                  colors = ButtonDefaults.buttonColors(containerColor = ButtonBg),
                  modifier = Modifier.fillMaxWidth().height(56.dp).testTag(LogInButtonTestTag)) {
                    Text("Log in", fontSize = 24.sp, color = Color.Black)
                  }

              Spacer(Modifier.height(16.dp))

              Button(
                  onClick = goToSignUp,
                  shape = RoundedCornerShape(28.dp),
                  colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                  modifier = Modifier.fillMaxWidth().height(56.dp).testTag(SignUpButtonTestTag)) {
                    Text("Sign up", fontSize = 24.sp, color = ButtonBg)
                  }
            }
      }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun SignInScreenPreview() {
  MaterialTheme { SignInScreen() }
}
