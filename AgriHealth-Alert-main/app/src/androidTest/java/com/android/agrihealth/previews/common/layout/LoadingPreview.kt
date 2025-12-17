package com.android.agrihealth.previews.common.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import com.android.agrihealth.ui.common.layout.LoadingOverlay

@Preview
@Composable
fun PreviewLoadingOverlay() {
  LoadingOverlay(isLoading = true) { Box(Modifier.testTag("content")) }
}
