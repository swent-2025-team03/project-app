package com.android.agrihealth.previews.authentication

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.agrihealth.core.design.theme.AgriHealthAppTheme
import com.android.agrihealth.testhelpers.fakes.FakeAuthRepository
import com.android.agrihealth.testhelpers.fakes.FakeUserViewModel
import com.android.agrihealth.ui.authentification.SignUpScreen
import com.android.agrihealth.ui.authentification.SignUpViewModel

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun SignUpScreenPreview() {
  val authRepo = FakeAuthRepository()
  val vm = object : SignUpViewModel(authRepo) {}

  AgriHealthAppTheme { SignUpScreen(signUpViewModel = vm, userViewModel = FakeUserViewModel()) }
}
