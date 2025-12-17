package com.android.agrihealth.previews.profile

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.agrihealth.core.design.theme.AgriHealthAppTheme
import com.android.agrihealth.ui.profile.ChangePasswordScreen
import com.android.agrihealth.ui.profile.FakeChangePasswordViewModel

/**
 * Preview of the ReportViewScreen for both farmer and vet roles. Allows testing of layout and
 * colors directly in Android Studio.
 */
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun ChangePasswordScreenPreview() {
  AgriHealthAppTheme {
    ChangePasswordScreen(
        userEmail = "notan@email.no",
        onBack = {},
        onUpdatePassword = {},
        changePasswordViewModel = FakeChangePasswordViewModel("password"))
  }
}
