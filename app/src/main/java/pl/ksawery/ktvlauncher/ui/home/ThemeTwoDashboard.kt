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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.ksawery.ktvlauncher.model.LaunchableApp
import pl.ksawery.ktvlauncher.model.MediaPlaybackInfo
import pl.ksawery.ktvlauncher.model.WatchNextItem
import pl.ksawery.ktvlauncher.model.WatchNextStatus
import pl.ksawery.ktvlauncher.ui.theme.KtvColors

@Composable
internal fun ThemeTwoDashboard(
    isThemeThree: Boolean,
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

        uiState.mediaPlayback
            ?.takeIf { uiState.mediaWidgetEnabled && it.isPlaying }
            ?.let { playback ->
                ActiveMediaWidget(
                    playback = playback,
                    appIcon = uiState.apps.firstOrNull {
                        it.componentName.packageName == playback.packageName
                    }?.icon,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 34.dp, start = 42.dp),
                )
            }

        ThemeTwoGreeting(
            uiState = uiState,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(y = maxHeight * if (isThemeThree) 0.42f else 0.41f)
                .padding(start = 42.dp),
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(start = 32.dp, end = 32.dp, bottom = 28.dp),
        ) {
            ThemeTwoShelf(
                uiState = uiState,
                onLaunchApp = onLaunchApp,
                onLaunchWatchNext = onLaunchWatchNext,
                onRequestWatchNextAccess = onRequestWatchNextAccess,
                onLongClickApp = onLongClickApp,
                isThemeThree = isThemeThree,
            )
            ThemeTwoActionBar(
                isThemeThree = isThemeThree,
                shortcuts = uiState.dockShortcuts,
                onLaunchApp = onLaunchApp,
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
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale("pl", "PL"))
    }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        while (true) {
            now = LocalDateTime.now()
            delay(30_000)
        }
    }

    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        modifier = modifier,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier
                .padding(top = 2.dp)
                .clip(RoundedCornerShape(19.dp))
                .background(Color(0x8F10141B))
                .padding(horizontal = 8.dp, vertical = 5.dp),
        ) {
            ThemeTwoIconButton(Icons.Rounded.Wifi, "Sieć", onOpenWifi)
            ThemeTwoCounterButton(onOpenNotifications)
            ThemeTwoIconButton(Icons.Rounded.Settings, "Ustawienia", onOpenSettings)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                now.format(timeFormatter),
                color = KtvColors.TextPrimary,
                fontSize = 29.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                now.format(dateFormatter).replaceFirstChar(Char::uppercase),
                color = KtvColors.TextSecondary,
                fontSize = 11.sp,
            )
        }
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
private fun ThemeTwoCounterButton(onClick: () -> Unit) {
    ThemeTwoFocusable(
        onClick = onClick,
        shape = CircleShape,
        modifier = Modifier.size(26.dp),
    ) { focused ->
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(if (focused) Color(0x40FFFFFF) else Color.Transparent)
                .border(1.dp, KtvColors.TextSecondary, CircleShape),
        ) {
            Text("11", color = KtvColors.TextPrimary, fontSize = 10.sp)
        }
    }
}

@Composable
internal fun ActiveMediaWidget(
    playback: MediaPlaybackInfo,
    appIcon: ImageBitmap?,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp),
        modifier = modifier.width(232.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(Color(0x9A111720)),
        ) {
            if (appIcon != null) {
                Image(
                    bitmap = appIcon,
                    contentDescription = playback.packageName,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(5.dp),
                )
            } else {
                Icon(
                    Icons.Rounded.PlayArrow,
                    contentDescription = null,
                    tint = KtvColors.TextPrimary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                playback.title.ifBlank { "Odtwarzanie" },
                color = KtvColors.TextPrimary,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                playback.subtitle.ifBlank { playback.packageName },
                color = KtvColors.TextSecondary,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
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
            listOf(
                uiState.weather?.symbol ?: "☁",
                (uiState.weather?.temperatureCelsius ?: "--").toString() + "°C",
                uiState.weather?.description ?: "Słonecznie",
            ).joinToString("  "),
            color = KtvColors.TextSecondary,
            fontSize = 12.sp,
        )
    }
}

@Composable
private fun ThemeTwoShelf(
    isThemeThree: Boolean,
    uiState: HomeUiState,
    onLaunchApp: (LaunchableApp) -> Unit,
    onLaunchWatchNext: (WatchNextItem) -> Unit,
    onRequestWatchNextAccess: () -> Unit,
    onLongClickApp: (LaunchableApp) -> Unit,
) {
    Row(verticalAlignment = if (isThemeThree) Alignment.Top else Alignment.Bottom) {
        Column {
            ThemeTwoSectionTitle("Kontynuuj oglądanie")
            Spacer(Modifier.height(11.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (uiState.watchNext.isEmpty()) {
                    ThemeTwoWatchNextEmpty(uiState.watchNextStatus, onRequestWatchNextAccess, isThemeThree)
                } else {
                    uiState.watchNext.take(2).forEach { item ->
                        ThemeTwoWatchNextCard(
                            item = item,
                            appIcon = uiState.apps.firstOrNull {
                                it.componentName.packageName == item.packageName
                            }?.icon,
                            onClick = { onLaunchWatchNext(item) },
                            isThemeThree = isThemeThree,
                        )
                    }
                }
            }
        }
        Spacer(Modifier.width(24.dp))
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(72.dp)
                .background(Color(0x22FFFFFF)),
        )
        Spacer(Modifier.width(18.dp))
        Column {
            ThemeTwoSectionTitle("Ulubione aplikacje")
            Spacer(Modifier.height(11.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                uiState.favorites.take(8).forEach { app ->
                    ThemeTwoFavoriteTile(
                        app = app,
                        onClick = { onLaunchApp(app) },
                        onLongClick = { onLongClickApp(app) },
                        isThemeThree = isThemeThree,
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeTwoSectionTitle(value: String) {
    Text(value, color = KtvColors.TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Normal)
}

@Composable
private fun ThemeTwoWatchNextCard(
    item: WatchNextItem,
    appIcon: ImageBitmap?,
    onClick: () -> Unit,
    isThemeThree: Boolean,
) {
    val poster = themeTwoUriImage(item.posterUri)
    ThemeTwoFocusable(
        onClick = onClick,
        shape = RoundedCornerShape(17.dp),
        modifier = Modifier
            .width(if (isThemeThree) 164.dp else 150.dp)
            .height(if (isThemeThree) 88.dp else 80.dp),
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
private fun ThemeTwoWatchNextEmpty(
    status: WatchNextStatus,
    onClick: () -> Unit,
    isThemeThree: Boolean,
) {
    ThemeTwoFocusable(
        onClick = onClick,
        shape = RoundedCornerShape(17.dp),
        modifier = Modifier
            .width(if (isThemeThree) 164.dp else 150.dp)
            .height(if (isThemeThree) 88.dp else 80.dp),
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
    isThemeThree: Boolean,
) {
    ThemeTwoFocusable(
        onClick = onClick,
        onLongClick = onLongClick,
        shape = RoundedCornerShape(13.dp),
        modifier = Modifier.size(if (isThemeThree) 52.dp else 48.dp),
    ) {
        Image(
            bitmap = app.icon,
            contentDescription = app.label,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(13.dp)),
        )
    }
}

@Composable
private fun ThemeTwoActionBar(
    isThemeThree: Boolean,
    shortcuts: List<LaunchableApp>,
    onLaunchApp: (LaunchableApp) -> Unit,
    onOpenApps: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenInfo: () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        ThemeTwoTextAction("Ustawienia", onOpenSettings, isThemeThree, Modifier.weight(1f))
        ThemeTwoTextAction("Aplikacje", onOpenApps, isThemeThree, Modifier.weight(1f))
        ThemeTwoTextAction("Informacje", onOpenInfo, isThemeThree, Modifier.weight(1f))
        shortcuts.take(3).forEach { app ->
            ThemeTwoTextAction(app.label, { onLaunchApp(app) }, isThemeThree, Modifier.weight(1f))
        }
        repeat(3 - shortcuts.take(3).size) {
            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun ThemeTwoTextAction(
    label: String,
    onClick: () -> Unit,
    isThemeThree: Boolean,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(if (isThemeThree) 10.dp else 12.dp)
    ThemeTwoFocusable(
        onClick = onClick,
        shape = shape,
        modifier = modifier.height(52.dp),
    ) { focused ->
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (focused) {
                        Color(0xC0242B35)
                    } else if (isThemeThree) {
                        Color(0xA9121820)
                    } else {
                        Color(0xA7080C12)
                    },
                    shape,
                )
                .border(
                    1.dp,
                    if (focused) Color(0x90F6F8FB) else Color(0x24FFFFFF),
                    shape,
                )
                .padding(horizontal = 12.dp),
        ) {
            Text(
                label,
                color = KtvColors.TextPrimary,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
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
