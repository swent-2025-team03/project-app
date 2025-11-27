package com.android.agrihealth.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.unit.dp

@Composable
fun maxTitleCharsForScreen(): Int {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val density = LocalDensity.current

    val titleStyle = MaterialTheme.typography.titleLarge
    val approxCharWidthPx = with(density) { titleStyle.fontSize.toPx() * 0.6f }

    val maxChars = with(density) { screenWidth.toPx() / approxCharWidthPx }
    return maxChars.toInt()
}
