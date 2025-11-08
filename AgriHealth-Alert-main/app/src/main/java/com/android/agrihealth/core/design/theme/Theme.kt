package com.android.agrihealth.core.design.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.android.agrihealth.data.model.report.ReportStatus

private val DarkColorScheme =
    darkColorScheme(primary = Purple80, secondary = PurpleGrey80, tertiary = Pink80)

private val LightColorScheme =
    lightColorScheme(
        background = FieldBg,
        onBackground = Color.Black,
        surface = FieldBg,
        onSurface = Color.Black,
        primary = ButtonBg,
        onPrimary = Color.Black,
        secondary = Color.White,
        onSecondary = Color.Black,
        secondaryContainer = SurfaceDim,
        onSecondaryContainer = Color.Black,
        surfaceVariant = SurfaceVariant,
        onSurfaceVariant = Color.Black,
        surfaceTint = Color.Transparent,
        primaryContainer = ButtonBg,
        onPrimaryContainer = Color.Black,
        surfaceBright = Color.White,
        tertiaryContainer = Color.LightGray,
        onTertiaryContainer = Color.Black,
        outline = Color.LightGray,
        outlineVariant = Color.Black,

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
      Typography(
          displaySmall =
              Typography()
                  .displaySmall
                  .copy(fontSize = 40.sp, fontWeight = FontWeight.Medium, color = TitleColor),
          titleLarge = Typography.titleLarge.copy(fontWeight = FontWeight.Bold),
      )

  val shapes =
      Shapes(
          extraSmall = RoundedCornerShape(20.dp),
          small = RoundedCornerShape(20.dp),
          RoundedCornerShape(20.dp),
          RoundedCornerShape(20.dp),
          RoundedCornerShape(20.dp),
      )

  MaterialTheme(
      colorScheme = colorScheme, shapes = shapes, typography = typography, content = content)
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
