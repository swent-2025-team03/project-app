package com.android.agrihealth.previews.authentication

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.agrihealth.core.design.theme.AgriHealthAppTheme
import com.android.agrihealth.testhelpers.fakes.FakeAuthRepository
import com.android.agrihealth.ui.authentification.ResetPasswordScreen
import com.android.agrihealth.ui.authentification.ResetPasswordViewModel

@Preview
@Composable
fun ResetPasswordScreenPreview() {
  val vm = ResetPasswordViewModel(FakeAuthRepository())
  AgriHealthAppTheme { ResetPasswordScreen(vm = vm) }
}
