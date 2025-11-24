package com.android.agrihealth.core.design.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val FieldBg = Color(0xFFF0F6F1)
val SurfaceVariant = Color(0xFFCCE7DA)
val SurfaceDim = Color(0xFFCFD9CF)
val ButtonBg = Color(0xFF9BB9B4)
val TitleColor = Color(0xFF000000)
val Test = Color(0xFF9AB8B3)

val DarkFieldBg = Color(0xFF1B1F1A)
val DarkSurfaceVariant = Color(0xFF2C3A2F)
val DarkSurfaceDim = Color(0xFF2F3B34)
val DarkButtonBg = Color(0xFF4B6B65)
val DarkTitleColor = Color(0xFFFFFFFF)
val DarkTest = Color(0xFF3A5B55)

data class StatusColors(
    val pending: Color = Color(0xFFBDBDBD),
    val inProgress: Color = Color(0xFFFFF59D),
    val resolved: Color = Color(0xFFA5D6A7),
    val spam: Color = Color(0xFFEF9A9A),
    val onPending: Color = TitleColor,
    val onInProgress: Color = TitleColor,
    val onResolved: Color = TitleColor,
    val onSpam: Color = TitleColor,
)

val LocalStatusColors = staticCompositionLocalOf { StatusColors() }
