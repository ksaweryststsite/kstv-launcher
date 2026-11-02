package pl.ksawery.ktvlauncher.ui.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import pl.ksawery.ktvlauncher.model.LaunchAnimationMode
import pl.ksawery.ktvlauncher.model.LaunchableApp

/**
 * All coordinates are raw root pixels. Passive focus tracking is used only to
 * position the launch animation; the D-pad focus animations are not changed.
 */
internal object AppLaunchAnchor {
    var focusedBounds: Rect? = null
}

internal data class AppLaunchPresentation(
    val app: LaunchableApp,
    val mode: LaunchAnimationMode,
    val bounds: Rect?,
)

/**
 * A real, compositor-driven app-launch zoom. Runs immediately after OK release,
 * BEFORE we invoke startActivity, so the user sees feedback even when a target
 * app (Netflix/Prime/etc.) needs seconds to initialize.
 *
 * Only graphicsLayer transforms animate; there are no per-frame image decodes,
 * blur allocations, or subtree relayouts. Android controls the hand-off after
 * onComplete(), and may impose its own platform splash/window transition.
 */
@Composable
internal fun AppLaunchMotion(
    presentation: AppLaunchPresentation,
    onComplete: () -> Unit,
) {
    val durationMs = when (presentation.mode) {
        LaunchAnimationMode.AppleTV -> 190
        LaunchAnimationMode.GoogleTV -> 145
        LaunchAnimationMode.Fade -> 115
        LaunchAnimationMode.System, LaunchAnimationMode.Off -> 0
    }
    val progress = remember(presentation) { Animatable(0f) }

    LaunchedEffect(presentation) {
        if (durationMs == 0) {
            onComplete()
        } else {
            progress.animateTo(1f, animationSpec = tween(durationMs, easing = FastOutSlowInEasing))
            onComplete()
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val widthPx = constraints.maxWidth.toFloat().coerceAtLeast(1f)
        val heightPx = constraints.maxHeight.toFloat().coerceAtLeast(1f)
        val middleX = widthPx / 2f
        val middleY = heightPx / 2f
        val origin = presentation.bounds?.takeIf {
            it.width > 5f && it.height > 5f &&
                it.left < widthPx && it.top < heightPx &&
                it.right > 0f && it.bottom > 0f
        } ?: Rect(middleX - 34f, middleY - 34f, middleX + 34f, middleY + 34f)

        val initialScaleX = (origin.width / widthPx).coerceIn(0.015f, 1f)
        val initialScaleY = (origin.height / heightPx).coerceIn(0.015f, 1f)
        val offsetX = origin.center.x - middleX
        val offsetY = origin.center.y - middleY
        val iconWidth = with(LocalDensity.current) { 158.dp.toPx() }
        val iconStartScale = (origin.width / iconWidth).coerceIn(0.15f, 1.25f)
        val apple = presentation.mode == LaunchAnimationMode.AppleTV
        val fade = presentation.mode == LaunchAnimationMode.Fade

        val accent = if (apple) Color(0xFF354F89) else Color(0xFF294668)
        Box(modifier = Modifier.fillMaxSize()) {
            // Screen-wide panel starts from the focused application's actual tile.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val t = progress.value
                        transformOrigin = TransformOrigin.Center
                        if (fade) {
                            scaleX = 1.035f - 0.035f * t
                            scaleY = 1.035f - 0.035f * t
                            alpha = t
                        } else {
                            scaleX = initialScaleX + (1f - initialScaleX) * t
                            scaleY = initialScaleY + (1f - initialScaleY) * t
                            translationX = offsetX * (1f - t)
                            translationY = offsetY * (1f - t)
                            alpha = 1f
                        }
                        clip = true
                        shape = RoundedCornerShape(42.dp)
                    }
                    .background(
                        Brush.linearGradient(
                            colors = listOf(accent, Color(0xFF121B2B), Color(0xFF070C16)),
                        ),
                    ),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                listOf(Color(0x384D7ABF), Color.Transparent),
                            ),
                        ),
                )
            }

            // Separate icon layer: begins at its real size/position, then
            // gracefully enlarges while the panel fills the television.
            Image(
                bitmap = presentation.app.icon,
                contentDescription = presentation.app.label,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(158.dp)
                    .graphicsLayer {
                        val t = progress.value
                        scaleX = iconStartScale + (if (apple) 1.22f else 1.05f - iconStartScale) * t
                        scaleY = scaleX
                        translationX = offsetX * (1f - t)
                        translationY = offsetY * (1f - t)
                        alpha = if (fade) t else 1f
                    },
            )
        }
    }
}
