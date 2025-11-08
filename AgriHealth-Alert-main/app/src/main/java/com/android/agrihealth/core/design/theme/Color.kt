package com.android.agrihealth.core.design.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

val FieldBg = Color(0xFFF0F6F1)
val SurfaceVariant = Color(0xFFCCE7DA)
val SurfaceDim = Color(0xFFCFD9CF)
val ButtonBg = Color(0xFF9BB9B4)
val TitleColor = Color(0xFF000000)
val Test = Color(0xFF9AB8B3)

data class StatusColors(
    val pending: Color = Color(0xFFBDBDBD),
    val inProgress: Color = Color(0xFFFFF59D),
    val resolved: Color = Color(0xFFA5D6A7),
    val spam: Color = Color(0xFFEF9A9A)
)

val LocalStatusColors = staticCompositionLocalOf { StatusColors() }
