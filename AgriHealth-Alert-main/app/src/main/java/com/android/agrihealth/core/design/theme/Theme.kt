package com.android.agrihealth.core.design.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.core.view.WindowCompat
import com.android.agrihealth.data.model.report.ReportStatus

private val DarkColorScheme =
    darkColorScheme(primary = Purple80, secondary = PurpleGrey80, tertiary = Pink80)

private val LightColorScheme =
    lightColorScheme(
        background = Color.White,
        onBackground = Color.Black,
        surface = FieldBg,
        onSurface = Color.Black,
        primary = ButtonBg,
        onPrimary = Color.Black,
        secondary = Color.White,
        onSecondary = Color.Black,
        secondaryContainer = SurfaceDim,
        onSecondaryContainer = Color.Black,
        surfaceVariant = FieldBg,
        onSurfaceVariant = Color.Black,
        surfaceTint = Color.Transparent,
        primaryContainer = ButtonBg,
        onPrimaryContainer = Color.Black,
        surfaceBright = Color.White,
        tertiaryContainer = Color.LightGray,
        onTertiaryContainer = Color.Black,

        // Keep default values
        // error = ,
        // onError = ,
        // errorContainer = ,
        // onErrorContainer = ,
        // tertiary = ,
        // inversePrimary = ,
        // onTertiary = ,
        // inverseSurface = ,
        // inverseOnSurface = ,
        // outline = ,
        // outlineVariant = ,
        // scrim = ,
        // surfaceDim = ,
        // surfaceContainer = ,
        // surfaceContainerHigh = ,
        // surfaceContainerHighest = ,
        // surfaceContainerLow = ,
        // surfaceContainerLowest = ,
    )

@Composable
fun AgriHealthAppTheme(
    darkTheme: Boolean = false,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
  val colorScheme =
      when {
        false && dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
          val context = LocalContext.current
          if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
      }
  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as Activity).window
      window.statusBarColor = colorScheme.primary.toArgb()
      WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
    }
  }

  val typography =
      Typography(displaySmall = Typography().displaySmall.copy(fontWeight = FontWeight.Bold))

  MaterialTheme(colorScheme = colorScheme, typography = typography, content = content)
}

@Composable
fun statusColor(status: ReportStatus): Color {
  val colors = LocalStatusColors.current
  return when (status) {
    ReportStatus.PENDING -> colors.pending
    ReportStatus.IN_PROGRESS -> colors.inProgress
    ReportStatus.RESOLVED -> colors.resolved
    ReportStatus.SPAM -> colors.spam
  }
}
