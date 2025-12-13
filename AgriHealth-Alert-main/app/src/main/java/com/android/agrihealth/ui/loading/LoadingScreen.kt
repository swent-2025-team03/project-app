package com.android.agrihealth.ui.loading

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

object LoadingTestTags {
  const val ROOT = "loading_overlay_root"
  const val SCRIM = "loading_overlay_scrim"
  const val SPINNER = "loading_overlay_spinner"
}

@Composable
fun LoadingOverlay(isLoading: Boolean, content: @Composable () -> Unit) {
  Box(modifier = Modifier.fillMaxSize().testTag(LoadingTestTags.ROOT)) {
    content()

    if (isLoading) {
      Box(
          modifier =
              Modifier.fillMaxSize()
                  .background(MaterialTheme.colorScheme.background.copy(alpha = 0.6f))
                  .testTag(LoadingTestTags.SCRIM)
                  .zIndex(100f),
          contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.onBackground,
                strokeWidth = 6.dp,
                modifier = Modifier.size(64.dp).testTag(LoadingTestTags.SPINNER))
          }
    }
  }
}

@Preview
@Composable
fun PreviewLoadingOverlay() {
  LoadingOverlay(isLoading = true) { Box(Modifier.testTag("content")) }
}
