package com.android.agrihealth.previews.report

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.agrihealth.core.design.theme.AgriHealthAppTheme
import com.android.agrihealth.testhelpers.fakes.FakeAddReportViewModel
import com.android.agrihealth.testhelpers.fakes.FakeUserViewModel
import com.android.agrihealth.ui.report.AddReportScreen

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun AddReportScreenPreview() {
  AgriHealthAppTheme {
    AddReportScreen(
        userViewModel = FakeUserViewModel(), addReportViewModel = FakeAddReportViewModel())
  }
}
