package pl.ksawery.ktvlauncher.model

import android.content.ComponentName
import androidx.compose.ui.graphics.ImageBitmap

data class LaunchableApp(
    val label: String,
    val componentName: ComponentName,
    val icon: ImageBitmap,
) {
    val stableKey: String = componentName.flattenToShortString()
}

