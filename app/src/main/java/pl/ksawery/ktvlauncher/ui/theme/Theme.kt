package pl.ksawery.ktvlauncher.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object KtvColors {
    val Background = Color(0xFF090C11)
    val BackgroundWarm = Color(0xFF211A15)
    val BackgroundCool = Color(0xFF0C121A)
    val Tile = Color(0xB31B2029)
    val TileFocused = Color(0xE629303A)
    val Border = Color(0x33FFFFFF)
    val Accent = Color(0xFFE8B56A)
    val TextPrimary = Color(0xFFF4F4F5)
    val TextSecondary = Color(0xFFB4B7BE)
}

private val KtvColorScheme = darkColorScheme(
    primary = KtvColors.Accent,
    background = KtvColors.Background,
    surface = KtvColors.Tile,
    onPrimary = KtvColors.Background,
    onBackground = KtvColors.TextPrimary,
    onSurface = KtvColors.TextPrimary,
)

@Composable
fun KtvLauncherTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = KtvColorScheme,
        content = content,
    )
}

