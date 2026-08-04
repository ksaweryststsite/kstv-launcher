package pl.ksawery.ktvlauncher.ui.home

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.ksawery.ktvlauncher.model.LaunchableApp
import pl.ksawery.ktvlauncher.model.WatchNextItem
import pl.ksawery.ktvlauncher.model.WatchNextStatus
import pl.ksawery.ktvlauncher.ui.theme.KtvColors

@Composable
internal fun ThemeTwoDashboard(
    uiState: HomeUiState,
    onLaunchApp: (LaunchableApp) -> Unit,
    onOpenApps: () -> Unit,
    onOpenLauncherSettings: () -> Unit,
    onOpenInfo: () -> Unit,
    onOpenWifi: () -> Unit,
    onOpenNotifications: () -> Unit,
    onLaunchWatchNext: (WatchNextItem) -> Unit,
    onRequestWatchNextAccess: () -> Unit,
    onLongClickApp: (LaunchableApp) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        ThemeTwoTopBar(
            onOpenWifi = onOpenWifi,
            onOpenNotifications = onOpenNotifications,
            onOpenSettings = onOpenLauncherSettings,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 26.dp, end = 34.dp),
        )

        if (uiState.mediaWidgetEnabled) {
            ThemeTwoMediaWidget(
                uiState = uiState,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 112.dp, end = 34.dp),
            )
        }

        ThemeTwoGreeting(
            uiState = uiState,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(y = maxHeight * 0.55f)
                .padding(start = 42.dp),
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(18.dp),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(start = 42.dp, end = 42.dp, bottom = 28.dp),
        ) {
            ThemeTwoShelf(
                uiState = uiState,
                onLaunchApp = onLaunchApp,
                onLaunchWatchNext = onLaunchWatchNext,
                onRequestWatchNextAccess = onRequestWatchNextAccess,
                onLongClickApp = onLongClickApp,
            )
            ThemeTwoActionBar(
                onOpenApps = onOpenApps,
                onOpenSettings = onOpenLauncherSettings,
                onOpenInfo = onOpenInfo,
            )
        }
    }
}

@Composable
private fun ThemeTwoTopBar(
    onOpenWifi: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(horizontalAlignment = Alignment.End, modifier = modifier) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0x8F10141B))
                .padding(horizontal = 6.dp, vertical = 4.dp),
        ) {
            ThemeTwoIconButton(Icons.Rounded.Wifi, "Sieć", onOpenWifi)
            ThemeTwoIconButton(Icons.Rounded.Info, "Powiadomienia", onOpenNotifications)
            ThemeTwoIconButton(Icons.Rounded.Settings, "Ustawienia", onOpenSettings)
        }
        Spacer(Modifier.height(7.dp))
        Text("14:09", color = KtvColors.TextPrimary, fontSize = 29.sp, fontWeight = FontWeight.SemiBold)
        Text("Sobota, 3 maja", color = KtvColors.TextSecondary, fontSize = 11.sp)
    }
}

@Composable
private fun ThemeTwoIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    ThemeTwoFocusable(
        onClick = onClick,
        shape = CircleShape,
        modifier = Modifier.size(26.dp),
    ) { focused ->
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(if (focused) Color(0x40FFFFFF) else Color.Transparent),
        ) {
            Icon(icon, label, tint = KtvColors.TextPrimary, modifier = Modifier.size(15.dp))
        }
    }
}

@Composable
private fun ThemeTwoMediaWidget(
    uiState: HomeUiState,
    modifier: Modifier = Modifier,
) {
    val playback = uiState.mediaPlayback
    Column(
        horizontalAlignment = Alignment.End,
        modifier = modifier.width(186.dp),
    ) {
        Text("Odtwarzacz", color = KtvColors.TextPrimary, fontSize = 12.sp)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xB3161D27)),
            ) {
                Icon(Icons.Rounded.PlayArrow, null, tint = KtvColors.TextPrimary, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(9.dp))
            Column {
                Text(
                    playback?.title?.ifBlank { "Nieznany utwór" } ?: "Brak aktywnej sesji",
                    color = KtvColors.TextPrimary,
                    fontSize = 12.sp,
                    maxLines = 1,
                )
                Text(
                    playback?.subtitle?.ifBlank { playback.packageName } ?: "Uruchom Spotify",
                    color = KtvColors.TextSecondary,
                    fontSize = 10.sp,
                    maxLines = 1,
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Icon(Icons.Rounded.PlayArrow, null, tint = KtvColors.TextPrimary, modifier = Modifier.size(18.dp))
            Icon(Icons.Rounded.PlayArrow, null, tint = KtvColors.TextPrimary, modifier = Modifier.size(18.dp))
            Icon(Icons.Rounded.PlayArrow, null, tint = KtvColors.TextPrimary, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun ThemeTwoGreeting(uiState: HomeUiState, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text("Dzień dobry", color = KtvColors.TextSecondary, fontSize = 13.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            "Miłego oglądania!",
            color = KtvColors.TextPrimary,
            fontSize = 25.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "${uiState.weather?.symbol ?: "☁"}  ${uiState.weather?.temperatureCelsius ?: "--"}°C  ${uiState.weather?.description ?: "Słonecznie"}",
            color = KtvColors.TextSecondary,
            fontSize = 12.sp,
        )
    }
}

@Composable
private fun ThemeTwoShelf(
    uiState: HomeUiState,
    onLaunchApp: (LaunchableApp) -> Unit,
    onLaunchWatchNext: (WatchNextItem) -> Unit,
    onRequestWatchNextAccess: () -> Unit,
    onLongClickApp: (LaunchableApp) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(34.dp)) {
        Column {
            ThemeTwoSectionTitle("Kontynuuj oglądanie")
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (uiState.watchNext.isEmpty()) {
                    ThemeTwoWatchNextEmpty(uiState.watchNextStatus, onRequestWatchNextAccess)
                } else {
                    uiState.watchNext.take(2).forEach { item ->
                        ThemeTwoWatchNextCard(
                            item = item,
                            appIcon = uiState.apps.firstOrNull {
                                it.componentName.packageName == item.packageName
                            }?.icon,
                            onClick = { onLaunchWatchNext(item) },
                        )
                    }
                }
            }
        }
        Column {
            ThemeTwoSectionTitle("Ulubione aplikacje")
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                uiState.favorites.take(8).forEach { app ->
                    ThemeTwoFavoriteTile(
                        app = app,
                        onClick = { onLaunchApp(app) },
                        onLongClick = { onLongClickApp(app) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeTwoSectionTitle(value: String) {
    Text(value, color = KtvColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
}

@Composable
private fun ThemeTwoWatchNextCard(
    item: WatchNextItem,
    appIcon: ImageBitmap?,
    onClick: () -> Unit,
) {
    val poster = themeTwoUriImage(item.posterUri)
    ThemeTwoFocusable(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .width(166.dp)
            .height(88.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xB8111620)),
        ) {
            when {
                poster != null -> Image(
                    bitmap = poster,
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                appIcon != null -> Image(
                    bitmap = appIcon,
                    contentDescription = item.title,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(45.dp),
                )
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .size(27.dp)
                    .clip(CircleShape)
                    .background(Color(0xA610141B)),
            ) {
                Icon(Icons.Rounded.PlayArrow, null, tint = KtvColors.TextPrimary, modifier = Modifier.size(17.dp))
            }
            item.progressPercent?.let { progress ->
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth(progress)
                        .height(2.dp)
                        .background(Color(0xFFF4F6F8)),
                )
            }
        }
    }
}

@Composable
private fun ThemeTwoWatchNextEmpty(status: WatchNextStatus, onClick: () -> Unit) {
    ThemeTwoFocusable(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .width(166.dp)
            .height(88.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xA8111620)),
        ) {
            Text(
                if (status == WatchNextStatus.Unavailable) "Włącz dostęp do listy" else "Brak materiałów",
                color = KtvColors.TextSecondary,
                fontSize = 11.sp,
            )
        }
    }
}

@Composable
private fun ThemeTwoFavoriteTile(
    app: LaunchableApp,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    ThemeTwoFocusable(
        onClick = onClick,
        onLongClick = onLongClick,
        shape = RoundedCornerShape(15.dp),
        modifier = Modifier.size(60.dp),
    ) {
        Image(
            bitmap = app.icon,
            contentDescription = app.label,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(15.dp)),
        )
    }
}

@Composable
private fun ThemeTwoActionBar(
    onOpenApps: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenInfo: () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        ThemeTwoAction("Filmy", Icons.Rounded.PlayArrow, {}, Modifier.weight(1f))
        ThemeTwoAction("Seriale", Icons.Rounded.PlayArrow, {}, Modifier.weight(1f))
        ThemeTwoAction("Sport", Icons.Rounded.PlayArrow, {}, Modifier.weight(1f))
        ThemeTwoAction("Muzyka", Icons.Rounded.PlayArrow, {}, Modifier.weight(1f))
        ThemeTwoAction("Aplikacje", Icons.Rounded.Apps, onOpenApps, Modifier.weight(1f))
        ThemeTwoAction("Ustawienia", Icons.Rounded.Settings, onOpenSettings, Modifier.weight(1f))
        ThemeTwoAction("Informacje", Icons.Rounded.Info, onOpenInfo, Modifier.weight(1f))
    }
}

@Composable
private fun ThemeTwoAction(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ThemeTwoFocusable(
        onClick = onClick,
        shape = RoundedCornerShape(15.dp),
        modifier = modifier.height(48.dp),
    ) { focused ->
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .background(if (focused) Color(0xA82B3440) else Color(0xA0111620))
                .padding(horizontal = 10.dp),
        ) {
            Icon(icon, null, tint = KtvColors.TextPrimary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(7.dp))
            Text(label, color = KtvColors.TextPrimary, fontSize = 12.sp, maxLines = 1)
        }
    }
}

@Composable
private fun ThemeTwoFocusable(
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    shape: androidx.compose.ui.graphics.Shape,
    modifier: Modifier = Modifier,
    content: @Composable (Boolean) -> Unit,
) {
    var focused by remember { mutableStateOf(false) }
    var keyDown by remember { mutableStateOf(false) }
    var longPressTriggered by remember { mutableStateOf(false) }
    var longPressJob by remember { mutableStateOf<Job?>(null) }
    val scope = rememberCoroutineScope()
    val scale by animateFloatAsState(
        targetValue = if (focused) 1.045f else 1f,
        animationSpec = tween(120),
        label = "themeTwoFocus",
    )
    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.shape = shape
                clip = true
            }
            .border(
                if (focused) 3.dp else 0.dp,
                if (focused) Color(0xE7101620) else Color.Transparent,
                shape,
            )
            .border(
                if (focused) 1.dp else 0.dp,
                if (focused) Color(0xFFF6F8FB) else Color.Transparent,
                shape,
            )
            .onFocusChanged { focused = it.isFocused }
            .onKeyEvent { event ->
                val isActivation = event.key == Key.DirectionCenter ||
                    event.key == Key.Enter ||
                    event.key == Key.NumPadEnter
                if (!isActivation) return@onKeyEvent false
                when (event.type) {
                    KeyEventType.KeyDown -> {
                        keyDown = true
                        if (longPressJob == null && onLongClick != null) {
                            longPressTriggered = false
                            longPressJob = scope.launch {
                                delay(550)
                                longPressJob = null
                                longPressTriggered = true
                                onLongClick()
                            }
                        }
                        true
                    }
                    KeyEventType.KeyUp -> {
                        if (keyDown && !longPressTriggered) onClick()
                        keyDown = false
                        longPressJob?.cancel()
                        longPressJob = null
                        longPressTriggered = false
                        true
                    }
                    else -> false
                }
            }
            .focusable(),
    ) {
        content(focused)
    }
}

@Composable
private fun themeTwoUriImage(uri: String?): ImageBitmap? {
    val context = LocalContext.current
    val image by produceState<ImageBitmap?>(initialValue = null, key1 = uri) {
        value = uri?.let { value ->
            withContext(Dispatchers.IO) {
                runCatching {
                    val parsed = Uri.parse(value)
                    val stream = when (parsed.scheme) {
                        "http", "https" -> URL(value).openStream()
                        else -> context.contentResolver.openInputStream(parsed)
                    }
                    stream?.use { BitmapFactory.decodeStream(it)?.asImageBitmap() }
                }.getOrNull()
            }
        }
    }
    return image
}
