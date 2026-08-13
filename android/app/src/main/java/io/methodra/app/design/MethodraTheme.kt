package io.methodra.app.design

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object MethodraColors {
    val Obsidian = Color(0xFF0B0D0F)
    val Basalt = Color(0xFF171A1E)
    val ElevatedStone = Color(0xFF22262B)
    val Bone = Color(0xFFF3EFE6)
    val Muted = Color(0xFFA9ADB3)
    val Amber = Color(0xFFF0A44B)
    val MineralBlue = Color(0xFF65B5FF)
    val Positive = Color(0xFF65C18C)
    val Caution = Color(0xFFD7A85B)
    val Danger = Color(0xFFE68A83)
}

private val MethodraScheme = darkColorScheme(
    primary = MethodraColors.Amber,
    onPrimary = MethodraColors.Obsidian,
    secondary = MethodraColors.MineralBlue,
    background = MethodraColors.Obsidian,
    onBackground = MethodraColors.Bone,
    surface = MethodraColors.Basalt,
    onSurface = MethodraColors.Bone,
    surfaceVariant = MethodraColors.ElevatedStone,
    onSurfaceVariant = MethodraColors.Muted,
    error = MethodraColors.Danger
)

@Composable
fun MethodraTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MethodraScheme,
        typography = MaterialTheme.typography.copy(
            headlineLarge = TextStyle(
                fontFamily = FontFamily.Serif,
                fontSize = 34.sp,
                lineHeight = 40.sp,
                fontWeight = FontWeight.SemiBold
            ),
            headlineMedium = TextStyle(
                fontFamily = FontFamily.Serif,
                fontSize = 28.sp,
                lineHeight = 34.sp,
                fontWeight = FontWeight.SemiBold
            ),
            titleLarge = TextStyle(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold),
            bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
            bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 21.sp),
            labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        ),
        content = content
    )
}
