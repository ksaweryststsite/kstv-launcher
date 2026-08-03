package pl.ksawery.ktvlauncher.ui.home

import android.content.Context
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.SystemClock
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import pl.ksawery.ktvlauncher.BuildConfig
import pl.ksawery.ktvlauncher.R
import pl.ksawery.ktvlauncher.model.LaunchableApp
import pl.ksawery.ktvlauncher.model.WatchNextItem
import pl.ksawery.ktvlauncher.model.WatchNextStatus
import pl.ksawery.ktvlauncher.ui.theme.KtvColors

private enum class LauncherScreen {
    Home,
    Apps,
    Settings,
    Favorites,
    DockPicker,
}

@Composable
fun HomeRoute(
    viewModel: HomeViewModel,
    onLaunchApp: (LaunchableApp) -> Unit,
    onPickWallpaper: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenWifi: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenAppInfo: (LaunchableApp) -> Unit,
    onUninstallApp: (LaunchableApp) -> Unit,
    onLaunchWatchNext: (WatchNextItem) -> Unit,
    onRequestWatchNextAccess: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        uiState = uiState,
        onLaunchApp = onLaunchApp,
        onPickWallpaper = onPickWallpaper,
        onOpenSystemSettings = onOpenSettings,
        onOpenWifi = onOpenWifi,
        onOpenNotifications = onOpenNotifications,
        onOpenAppInfo = onOpenAppInfo,
        onUninstallApp = onUninstallApp,
        onLaunchWatchNext = onLaunchWatchNext,
        onRequestWatchNextAccess = onRequestWatchNextAccess,
        onToggleFavorite = viewModel::toggleFavorite,
        onMoveFavorite = viewModel::moveFavorite,
        onSetDockShortcut = viewModel::setDockShortcut,
        onToggleContinueWatching = { enabled ->
            viewModel.setContinueWatchingEnabled(enabled)
            if (enabled) onRequestWatchNextAccess()
        },
    )
}

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onLaunchApp: (LaunchableApp) -> Unit,
    onPickWallpaper: () -> Unit,
    onOpenSystemSettings: () -> Unit,
    onOpenWifi: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenAppInfo: (LaunchableApp) -> Unit,
    onUninstallApp: (LaunchableApp) -> Unit,
    onLaunchWatchNext: (WatchNextItem) -> Unit,
    onRequestWatchNextAccess: () -> Unit,
    onToggleFavorite: (LaunchableApp) -> Unit,
    onMoveFavorite: (LaunchableApp, Int) -> Unit,
    onSetDockShortcut: (Int, LaunchableApp) -> Unit,
    onToggleContinueWatching: (Boolean) -> Unit,
) {
    var screen by rememberSaveable { mutableStateOf(LauncherScreen.Home) }
    var showInfo by rememberSaveable { mutableStateOf(false) }
    var contextApp by remember { mutableStateOf<LaunchableApp?>(null) }
    var dockPickerSlot by rememberSaveable { mutableStateOf(0) }

    BackHandler(enabled = true) {
        when {
            contextApp != null -> contextApp = null
            showInfo -> showInfo = false
            screen == LauncherScreen.Favorites || screen == LauncherScreen.DockPicker -> {
                screen = LauncherScreen.Settings
            }
            screen != LauncherScreen.Home -> screen = LauncherScreen.Home
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LauncherBackground(uiState.wallpaperUri)

        when (screen) {
            LauncherScreen.Home -> HomeDashboard(
                uiState = uiState,
                onLaunchApp = onLaunchApp,
                onOpenApps = { screen = LauncherScreen.Apps },
                onOpenLauncherSettings = { screen = LauncherScreen.Settings },
                onOpenInfo = { showInfo = true },
                onOpenWifi = onOpenWifi,
                onOpenNotifications = onOpenNotifications,
                onLaunchWatchNext = onLaunchWatchNext,
                onRequestWatchNextAccess = onRequestWatchNextAccess,
                onLongClickApp = { contextApp = it },
            )

            LauncherScreen.Apps -> AllAppsScreen(
                apps = uiState.apps,
                onLaunchApp = onLaunchApp,
                onLongClickApp = { contextApp = it },
            )

            LauncherScreen.Settings -> LauncherSettingsScreen(
                uiState = uiState,
                onPickWallpaper = onPickWallpaper,
                onEditFavorites = { screen = LauncherScreen.Favorites },
                onPickDockShortcut = { slot ->
                    dockPickerSlot = slot
                    screen = LauncherScreen.DockPicker
                },
                onToggleContinueWatching = onToggleContinueWatching,
                onRequestWatchNextAccess = onRequestWatchNextAccess,
                onOpenSystemSettings = onOpenSystemSettings,
            )

            LauncherScreen.Favorites -> FavoritesEditor(
                apps = uiState.apps,
                favorites = uiState.favorites,
                onToggleFavorite = onToggleFavorite,
                onLongClickApp = { contextApp = it },
            )

            LauncherScreen.DockPicker -> DockShortcutPicker(
                slot = dockPickerSlot,
                apps = uiState.apps,
                onSelect = { app ->
                    onSetDockShortcut(dockPickerSlot, app)
                    screen = LauncherScreen.Settings
                },
            )
        }

        if (showInfo) {
            InfoOverlay(onClose = { showInfo = false })
        }

        contextApp?.let { app ->
            AppContextMenu(
                app = app,
                isFavorite = app in uiState.favorites,
                onClose = { contextApp = null },
                onOpen = {
                    contextApp = null
                    onLaunchApp(app)
                },
                onToggleFavorite = {
                    onToggleFavorite(app)
                    contextApp = null
                },
                onMoveLeft = { onMoveFavorite(app, -1) },
                onMoveRight = { onMoveFavorite(app, 1) },
                onSetDock = { slot ->
                    onSetDockShortcut(slot, app)
                    contextApp = null
                },
                onAppInfo = {
                    contextApp = null
                    onOpenAppInfo(app)
                },
                onUninstall = {
                    contextApp = null
                    onUninstallApp(app)
                },
            )
        }
    }
}

@Composable
private fun LauncherBackground(customWallpaperUri: String?) {
    val context = LocalContext.current
    val customWallpaper by produceState<ImageBitmap?>(
        initialValue = null,
        key1 = customWallpaperUri,
    ) {
        value = customWallpaperUri?.let { uri ->
            withContext(Dispatchers.IO) { decodeWallpaper(context, Uri.parse(uri)) }
        }
    }

    if (customWallpaper != null) {
        Image(
            bitmap = customWallpaper!!,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
    } else {
        Image(
            painter = painterResource(R.drawable.launcher_wallpaper),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0f to Color(0x12000000),
                    0.52f to Color(0x26000000),
                    0.72f to Color(0xB9080A0E),
                    1f to Color(0xF2080A0E),
                ),
            ),
    )
}

@Composable
private fun HomeDashboard(
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
        StatusAndClock(
            onOpenWifi = onOpenWifi,
            onOpenNotifications = onOpenNotifications,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 26.dp, end = 34.dp),
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .height(maxHeight * 0.43f)
                .background(Color(0x8F090C11))
                .border(
                    width = 1.dp,
                    color = Color(0x22FFFFFF),
                    shape = RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp),
                ),
        )

        GreetingAndWeather(
            uiState = uiState,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(y = maxHeight * 0.39f)
                .padding(start = 34.dp),
        )

        ContentShelf(
            watchNext = uiState.watchNext,
            watchNextStatus = uiState.watchNextStatus,
            apps = uiState.apps,
            favorites = uiState.favorites,
            onLaunchApp = onLaunchApp,
            onLaunchWatchNext = onLaunchWatchNext,
            onRequestWatchNextAccess = onRequestWatchNextAccess,
            onLongClickApp = onLongClickApp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(y = maxHeight * 0.59f)
                .padding(horizontal = 34.dp),
        )

        Dock(
            shortcuts = uiState.dockShortcuts,
            onOpenSettings = onOpenLauncherSettings,
            onOpenApps = onOpenApps,
            onOpenInfo = onOpenInfo,
            onLaunchApp = onLaunchApp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(y = maxHeight * 0.81f)
                .padding(horizontal = 34.dp),
        )
    }
}

@Composable
private fun StatusAndClock(
    onOpenWifi: () -> Unit,
    onOpenNotifications: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val isOnline = remember { isNetworkOnline(context) }
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale("pl", "PL"))
    }

    LaunchedEffect(Unit) {
        while (true) {
            now = LocalDateTime.now()
            delay(30_000)
        }
    }

    Row(modifier = modifier, verticalAlignment = Alignment.Top) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .height(38.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0x8C171A20))
                .border(1.dp, Color(0x18FFFFFF), RoundedCornerShape(20.dp))
                .padding(horizontal = 14.dp),
        ) {
            StatusIconButton(
                icon = Icons.Rounded.Wifi,
                contentDescription = "Wi-Fi",
                tint = if (isOnline) KtvColors.TextPrimary else KtvColors.TextSecondary,
                onClick = onOpenWifi,
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(22.dp)
                    .border(1.dp, KtvColors.TextSecondary, CircleShape),
            ) {
                Text("11", color = KtvColors.TextPrimary, fontSize = 10.sp)
            }
            StatusIconButton(
                icon = Icons.Rounded.NotificationsNone,
                contentDescription = "Powiadomienia",
                tint = KtvColors.TextPrimary,
                onClick = onOpenNotifications,
            )
        }
        Spacer(Modifier.width(20.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = now.format(timeFormatter),
                color = KtvColors.TextPrimary,
                fontSize = 31.sp,
                fontWeight = FontWeight.Normal,
            )
            Text(
                text = now.format(dateFormatter).replaceFirstChar(Char::uppercase),
                color = KtvColors.TextSecondary,
                fontSize = 13.sp,
            )
        }
    }
}

@Composable
private fun StatusIconButton(
    icon: ImageVector,
    contentDescription: String,
    tint: Color,
    onClick: () -> Unit,
) {
    FocusableSurface(
        onClick = onClick,
        focusedScale = 1.08f,
        shape = CircleShape,
        modifier = Modifier.size(25.dp),
    ) { focused ->
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(if (focused) Color(0x40FFFFFF) else Color.Transparent),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(19.dp),
            )
        }
    }
}

@Composable
private fun GreetingAndWeather(uiState: HomeUiState, modifier: Modifier = Modifier) {
    var hour by remember { mutableStateOf(LocalDateTime.now().hour) }
    LaunchedEffect(Unit) {
        while (true) {
            hour = LocalDateTime.now().hour
            delay(60_000)
        }
    }
    Column(modifier = modifier) {
        Text(
            text = greetingFor(hour),
            color = KtvColors.TextSecondary,
            fontSize = 17.sp,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Miłego oglądania!",
            color = KtvColors.TextPrimary,
            fontSize = 29.sp,
            fontWeight = FontWeight.Medium,
        )
        Spacer(Modifier.height(9.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = uiState.weather?.symbol ?: "☁",
                color = KtvColors.TextPrimary,
                fontSize = 23.sp,
            )
            Spacer(Modifier.width(7.dp))
            Text(
                text = uiState.weather?.let { "${it.temperatureCelsius}°C" } ?: "--°C",
                color = KtvColors.TextPrimary,
                fontSize = 17.sp,
            )
            Spacer(Modifier.width(16.dp))
            Text(
                text = uiState.weather?.description ?: "Kraków, Bieńczyce",
                color = KtvColors.TextSecondary,
                fontSize = 14.sp,
            )
        }
    }
}

@Composable
private fun ContentShelf(
    watchNext: List<WatchNextItem>,
    watchNextStatus: WatchNextStatus,
    apps: List<LaunchableApp>,
    favorites: List<LaunchableApp>,
    onLaunchApp: (LaunchableApp) -> Unit,
    onLaunchWatchNext: (WatchNextItem) -> Unit,
    onRequestWatchNextAccess: () -> Unit,
    onLongClickApp: (LaunchableApp) -> Unit,
    modifier: Modifier = Modifier,
) {
    val firstFocusRequester = remember { FocusRequester() }
    LaunchedEffect(watchNextStatus, favorites.firstOrNull()?.stableKey) {
        if (watchNext.isNotEmpty() || favorites.isNotEmpty()) firstFocusRequester.requestFocus()
    }

    Row(
        verticalAlignment = Alignment.Top,
        modifier = modifier
            .fillMaxWidth()
            .height(106.dp),
    ) {
        Column(modifier = Modifier.width(350.dp)) {
            SectionTitle("Kontynuuj oglądanie")
            Spacer(Modifier.height(9.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (watchNext.isNotEmpty()) {
                    watchNext.take(2).forEachIndexed { index, item ->
                        WatchNextCard(
                            item = item,
                            appIcon = apps.firstOrNull {
                                it.componentName.packageName == item.packageName
                            }?.icon,
                            onClick = { onLaunchWatchNext(item) },
                            modifier = if (index == 0) {
                                Modifier.focusRequester(firstFocusRequester)
                            } else {
                                Modifier
                            },
                        )
                    }
                } else {
                    WatchNextEmptyCard(
                        status = watchNextStatus,
                        onClick = onRequestWatchNextAccess,
                        modifier = Modifier.focusRequester(firstFocusRequester),
                    )
                }
            }
        }

        Spacer(Modifier.width(18.dp))
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp)
                .background(Color(0x26FFFFFF)),
        )
        Spacer(Modifier.width(18.dp))

        Column(modifier = Modifier.weight(1f)) {
            SectionTitle("Ulubione aplikacje")
            Spacer(Modifier.height(9.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                favorites.take(8).forEach { app ->
                    FavoriteTile(
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
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = KtvColors.TextSecondary,
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
    )
}

@Composable
private fun WatchNextCard(
    item: WatchNextItem,
    appIcon: ImageBitmap?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val poster = rememberUriImage(item.posterUri)
    FocusableSurface(
        onClick = onClick,
        focusedScale = 1.035f,
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
            .width(170.dp)
            .height(82.dp),
    ) { focused ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xE51A1E25), Color(0xB30D1015)),
                    ),
                ),
        ) {
            if (poster != null) {
                Image(
                    bitmap = poster,
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color(0xD9090B0F)),
                            ),
                        ),
                )
            } else if (appIcon != null) {
                Image(
                    bitmap = appIcon,
                    contentDescription = item.title,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 14.dp, bottom = 18.dp)
                        .size(42.dp),
                )
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(Color(0xB3090B0F))
                    .padding(horizontal = 9.dp, vertical = 5.dp),
            ) {
                Text(
                    text = item.title,
                    color = KtvColors.TextPrimary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = item.description ?: if (focused) "Otwórz" else "Kontynuuj",
                    color = KtvColors.TextSecondary,
                    fontSize = 8.sp,
                )
            }
            Icon(
                imageVector = Icons.Rounded.PlayArrow,
                contentDescription = null,
                tint = KtvColors.TextPrimary,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 14.dp, bottom = 18.dp)
                    .size(22.dp),
            )
            item.progressPercent?.let { progress ->
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth(progress)
                        .height(2.dp)
                        .background(KtvColors.TextPrimary),
                )
            }
        }
    }
}

@Composable
private fun rememberUriImage(uri: String?): ImageBitmap? {
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

@Composable
private fun WatchNextEmptyCard(
    status: WatchNextStatus,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val message = when (status) {
        WatchNextStatus.Loading -> "Ładowanie…"
        WatchNextStatus.Empty -> "Brak materiałów"
        WatchNextStatus.Unavailable -> "Nadaj dostęp"
        WatchNextStatus.Ready -> "Brak materiałów"
    }
    FocusableSurface(
        onClick = onClick,
        focusedScale = 1.035f,
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
            .width(170.dp)
            .height(82.dp),
    ) { focused ->
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xA8171A20)),
        ) {
            Text(
                text = message,
                color = if (focused) KtvColors.TextPrimary else KtvColors.TextSecondary,
                fontSize = 11.sp,
            )
        }
    }
}

@Composable
private fun FavoriteTile(
    app: LaunchableApp,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FocusableSurface(
        onClick = onClick,
        onLongClick = onLongClick,
        focusedScale = 1.08f,
        shape = RoundedCornerShape(13.dp),
        modifier = modifier.size(54.dp),
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
private fun Dock(
    shortcuts: List<LaunchableApp>,
    onOpenSettings: () -> Unit,
    onOpenApps: () -> Unit,
    onOpenInfo: () -> Unit,
    onLaunchApp: (LaunchableApp) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp),
    ) {
        DockAction("Ustawienia launchera", Icons.Rounded.Settings, onOpenSettings, Modifier.weight(1f))
        DockAction("Aplikacje", Icons.Rounded.Apps, onOpenApps, Modifier.weight(1f))
        DockAction("Informacje", Icons.Rounded.Info, onOpenInfo, Modifier.weight(1f))
        repeat(3) { index ->
            shortcuts.getOrNull(index)?.let { app ->
                DockShortcut(app, { onLaunchApp(app) }, Modifier.weight(1f))
            } ?: Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun DockAction(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FocusableSurface(
        onClick = onClick,
        focusedScale = 1.025f,
        shape = RoundedCornerShape(9.dp),
        modifier = modifier.fillMaxHeight(),
    ) { focused ->
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .background(if (focused) Color(0xD9272C34) else Color(0xA8171A20))
                .padding(horizontal = 12.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (focused) KtvColors.TextPrimary else KtvColors.TextSecondary,
                modifier = Modifier.size(21.dp),
            )
            Spacer(Modifier.width(9.dp))
            Text(
                text = label,
                color = if (focused) KtvColors.TextPrimary else KtvColors.TextSecondary,
                fontSize = 13.sp,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun DockShortcut(
    app: LaunchableApp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FocusableSurface(
        onClick = onClick,
        focusedScale = 1.025f,
        shape = RoundedCornerShape(9.dp),
        modifier = modifier.fillMaxHeight(),
    ) { focused ->
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(if (focused) Color(0xD9272C34) else Color(0xA8171A20))
                .padding(horizontal = 12.dp),
        ) {
            Text(
                text = app.label,
                color = if (focused) KtvColors.TextPrimary else KtvColors.TextSecondary,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun LauncherSettingsScreen(
    uiState: HomeUiState,
    onPickWallpaper: () -> Unit,
    onEditFavorites: () -> Unit,
    onPickDockShortcut: (Int) -> Unit,
    onToggleContinueWatching: (Boolean) -> Unit,
    onRequestWatchNextAccess: () -> Unit,
    onOpenSystemSettings: () -> Unit,
) {
    val firstFocusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { firstFocusRequester.requestFocus() }
    val watchNextLabel = when (uiState.watchNextStatus) {
        WatchNextStatus.Loading -> "Sprawdzanie danych…"
        WatchNextStatus.Ready -> "Dostępne materiały: ${uiState.watchNext.size}"
        WatchNextStatus.Empty -> "Brak opublikowanych materiałów"
        WatchNextStatus.Unavailable -> "Wymagany dostęp systemowy"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xD6080A0E))
            .padding(horizontal = 70.dp, vertical = 34.dp),
    ) {
        Text(
            "Ustawienia launchera",
            color = KtvColors.TextPrimary,
            fontSize = 27.sp,
            fontWeight = FontWeight.Medium,
        )
        Spacer(Modifier.height(22.dp))

        SettingRow(
            title = "Tapeta",
            value = "Wybierz obraz z pamięci",
            icon = Icons.Rounded.Wallpaper,
            onClick = onPickWallpaper,
            modifier = Modifier.focusRequester(firstFocusRequester),
        )
        Spacer(Modifier.height(9.dp))
        SettingRow(
            title = "Ulubione aplikacje",
            value = "Wybrane: ${uiState.favorites.size} / 8",
            icon = Icons.Rounded.Apps,
            onClick = onEditFavorites,
        )
        Spacer(Modifier.height(9.dp))
        repeat(3) { slot ->
            SettingRow(
                title = "Skrót docka ${slot + 1}",
                value = uiState.dockShortcuts.getOrNull(slot)?.label ?: "Nie ustawiono",
                icon = Icons.Rounded.PlayArrow,
                onClick = { onPickDockShortcut(slot) },
            )
            Spacer(Modifier.height(9.dp))
        }
        SettingRow(
            title = "Kontynuuj oglądanie",
            value = watchNextLabel,
            icon = Icons.Rounded.PlayArrow,
            onClick = {
                if (
                    uiState.continueWatchingEnabled &&
                    uiState.watchNextStatus == WatchNextStatus.Unavailable
                ) {
                    onRequestWatchNextAccess()
                } else {
                    onToggleContinueWatching(!uiState.continueWatchingEnabled)
                }
            },
            trailingText = if (uiState.continueWatchingEnabled) "Wł." else "Wył.",
        )
        Spacer(Modifier.height(9.dp))
        SettingRow(
            title = "Ustawienia systemowe",
            value = "Sieć, dźwięk i urządzenie",
            icon = Icons.Rounded.Settings,
            onClick = onOpenSystemSettings,
        )
    }
}

@Composable
private fun SettingRow(
    title: String,
    value: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingText: String? = null,
) {
    FocusableSurface(
        onClick = onClick,
        focusedScale = 1.012f,
        shape = RoundedCornerShape(9.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
    ) { focused ->
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .background(if (focused) Color(0xD9272C34) else Color(0xA8171A20))
                .padding(horizontal = 16.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (focused) KtvColors.TextPrimary else KtvColors.TextSecondary,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(13.dp))
            Column {
                Text(title, color = KtvColors.TextPrimary, fontSize = 13.sp)
                Text(value, color = KtvColors.TextSecondary, fontSize = 10.sp)
            }
            Spacer(Modifier.weight(1f))
            trailingText?.let {
                Text(it, color = KtvColors.Accent, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun FavoritesEditor(
    apps: List<LaunchableApp>,
    favorites: List<LaunchableApp>,
    onToggleFavorite: (LaunchableApp) -> Unit,
    onLongClickApp: (LaunchableApp) -> Unit,
) {
    val firstFocusRequester = remember { FocusRequester() }
    LaunchedEffect(apps.firstOrNull()?.stableKey) {
        if (apps.isNotEmpty()) firstFocusRequester.requestFocus()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xD6080A0E))
            .padding(horizontal = 54.dp, vertical = 30.dp),
    ) {
        Text(
            "Ulubione aplikacje",
            color = KtvColors.TextPrimary,
            fontSize = 27.sp,
            fontWeight = FontWeight.Medium,
        )
        Spacer(Modifier.height(20.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(8),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(apps, key = LaunchableApp::stableKey) { app ->
                EditorAppTile(
                    app = app,
                    selectedIndex = favorites.indexOf(app),
                    onClick = { onToggleFavorite(app) },
                    onLongClick = { onLongClickApp(app) },
                    modifier = if (app == apps.first()) {
                        Modifier.focusRequester(firstFocusRequester)
                    } else Modifier,
                )
            }
        }
    }
}

@Composable
private fun EditorAppTile(
    app: LaunchableApp,
    selectedIndex: Int,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FocusableSurface(
        onClick = onClick,
        onLongClick = onLongClick,
        focusedScale = 1.055f,
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(94.dp),
    ) { focused ->
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(14.dp)),
            ) {
                Image(
                    bitmap = app.icon,
                    contentDescription = app.label,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                )
                if (selectedIndex >= 0) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(19.dp)
                            .background(KtvColors.Accent, CircleShape),
                    ) {
                        Text("${selectedIndex + 1}", color = Color.Black, fontSize = 9.sp)
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                app.label,
                color = if (focused) KtvColors.TextPrimary else KtvColors.TextSecondary,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun DockShortcutPicker(
    slot: Int,
    apps: List<LaunchableApp>,
    onSelect: (LaunchableApp) -> Unit,
) {
    val firstFocusRequester = remember { FocusRequester() }
    LaunchedEffect(apps.firstOrNull()?.stableKey) {
        if (apps.isNotEmpty()) firstFocusRequester.requestFocus()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xD6080A0E))
            .padding(horizontal = 54.dp, vertical = 30.dp),
    ) {
        Text(
            "Skrót docka ${slot + 1}",
            color = KtvColors.TextPrimary,
            fontSize = 27.sp,
            fontWeight = FontWeight.Medium,
        )
        Spacer(Modifier.height(20.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(8),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(apps, key = LaunchableApp::stableKey) { app ->
                AllAppsTile(
                    app = app,
                    onClick = { onSelect(app) },
                    onLongClick = { },
                    modifier = if (app == apps.first()) {
                        Modifier.focusRequester(firstFocusRequester)
                    } else Modifier,
                )
            }
        }
    }
}

@Composable
private fun AllAppsScreen(
    apps: List<LaunchableApp>,
    onLaunchApp: (LaunchableApp) -> Unit,
    onLongClickApp: (LaunchableApp) -> Unit,
) {
    val firstFocusRequester = remember { FocusRequester() }
    LaunchedEffect(apps.firstOrNull()?.stableKey) {
        if (apps.isNotEmpty()) firstFocusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xC9080A0E))
            .padding(horizontal = 54.dp, vertical = 30.dp),
    ) {
        Text(
            text = "Aplikacje",
            color = KtvColors.TextPrimary,
            fontSize = 27.sp,
            fontWeight = FontWeight.Medium,
        )
        Spacer(Modifier.height(20.dp))

        if (apps.isEmpty()) {
            CircularProgressIndicator(color = KtvColors.Accent)
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(8),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(apps, key = LaunchableApp::stableKey) { app ->
                    AllAppsTile(
                        app = app,
                        onClick = { onLaunchApp(app) },
                        onLongClick = { onLongClickApp(app) },
                        modifier = if (app == apps.first()) {
                            Modifier.focusRequester(firstFocusRequester)
                        } else {
                            Modifier
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun AllAppsTile(
    app: LaunchableApp,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FocusableSurface(
        onClick = onClick,
        onLongClick = onLongClick,
        focusedScale = 1.055f,
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(94.dp),
    ) { focused ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize(),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (focused) Color(0xE5292E37) else Color(0xC21A1E25))
                    .padding(8.dp),
            ) {
                Image(
                    bitmap = app.icon,
                    contentDescription = app.label,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = app.label,
                color = if (focused) KtvColors.TextPrimary else KtvColors.TextSecondary,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun AppContextMenu(
    app: LaunchableApp,
    isFavorite: Boolean,
    onClose: () -> Unit,
    onOpen: () -> Unit,
    onToggleFavorite: () -> Unit,
    onMoveLeft: () -> Unit,
    onMoveRight: () -> Unit,
    onSetDock: (Int) -> Unit,
    onAppInfo: () -> Unit,
    onUninstall: () -> Unit,
) {
    val firstFocusRequester = remember { FocusRequester() }
    LaunchedEffect(app.stableKey) { firstFocusRequester.requestFocus() }
    BackHandler { onClose() }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xB3000000)),
    ) {
        Column(
            modifier = Modifier
                .width(330.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(Color(0xF21A1E25))
                .border(1.dp, Color(0x30FFFFFF), RoundedCornerShape(13.dp))
                .padding(14.dp),
        ) {
            Text(
                app.label,
                color = KtvColors.TextPrimary,
                fontSize = 19.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(10.dp))
            ContextMenuRow("Otwórz", onOpen, Modifier.focusRequester(firstFocusRequester))
            ContextMenuRow(
                if (isFavorite) "Usuń z ulubionych" else "Dodaj do ulubionych",
                onToggleFavorite,
            )
            if (isFavorite) {
                ContextMenuRow("Przesuń w lewo", onMoveLeft)
                ContextMenuRow("Przesuń w prawo", onMoveRight)
            }
            repeat(3) { slot ->
                ContextMenuRow("Ustaw jako skrót ${slot + 1}", { onSetDock(slot) })
            }
            ContextMenuRow("Informacje o aplikacji", onAppInfo)
            ContextMenuRow("Odinstaluj", onUninstall, danger = true)
        }
    }
}

@Composable
private fun ContextMenuRow(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    danger: Boolean = false,
) {
    FocusableSurface(
        onClick = onClick,
        focusedScale = 1.01f,
        shape = RoundedCornerShape(7.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(34.dp),
    ) { focused ->
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier
                .fillMaxSize()
                .background(if (focused) Color(0x40FFFFFF) else Color.Transparent)
                .padding(horizontal = 10.dp),
        ) {
            Text(
                label,
                color = if (danger) Color(0xFFFF8A8A) else KtvColors.TextPrimary,
                fontSize = 12.sp,
            )
        }
    }
}

@Composable
private fun InfoOverlay(onClose: () -> Unit) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xA6000000)),
    ) {
        FocusableSurface(
            onClick = onClose,
            focusedScale = 1f,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .focusRequester(focusRequester)
                .width(300.dp)
                .height(150.dp),
        ) { focused ->
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xF21A1E25))
                    .padding(24.dp),
            ) {
                Text("KSTV Launcher", color = KtvColors.TextPrimary, fontSize = 22.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Wersja ${BuildConfig.VERSION_NAME}",
                    color = KtvColors.TextSecondary,
                    fontSize = 14.sp,
                )
                Spacer(Modifier.height(18.dp))
                Text(
                    if (focused) "OK — zamknij" else "Naciśnij OK",
                    color = KtvColors.Accent,
                    fontSize = 12.sp,
                )
            }
        }
    }
}

@Composable
private fun FocusableSurface(
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    focusedScale: Float,
    shape: Shape,
    modifier: Modifier = Modifier,
    content: @Composable (focused: Boolean) -> Unit,
) {
    var focused by remember { mutableStateOf(false) }
    var pressedAt by remember { mutableStateOf(0L) }
    val scale by animateFloatAsState(
        targetValue = if (focused) focusedScale else 1f,
        animationSpec = tween(durationMillis = 105),
        label = "focusScale",
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
                width = 1.dp,
                color = if (focused) Color(0x99FFFFFF) else Color(0x24FFFFFF),
                shape = shape,
            )
            .onFocusChanged { focused = it.isFocused }
            .onKeyEvent { event ->
                val activationKey = event.key == Key.DirectionCenter ||
                    event.key == Key.Enter ||
                    event.key == Key.NumPadEnter
                if (!activationKey) return@onKeyEvent false

                when (event.type) {
                    KeyEventType.KeyDown -> {
                        if (pressedAt == 0L) pressedAt = SystemClock.elapsedRealtime()
                        true
                    }
                    KeyEventType.KeyUp -> {
                        val pressDuration = SystemClock.elapsedRealtime() - pressedAt
                        pressedAt = 0L
                        if (pressDuration >= 550L && onLongClick != null) {
                            onLongClick()
                        } else {
                            onClick()
                        }
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

internal fun greetingFor(hour: Int): String = when (hour) {
    in 5..19 -> "Dzień dobry"
    else -> "Dobry wieczór"
}

private fun isNetworkOnline(context: Context): Boolean {
    val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

private fun decodeWallpaper(context: Context, uri: Uri): ImageBitmap? = runCatching {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(uri)?.use {
        BitmapFactory.decodeStream(it, null, bounds)
    }
    val sampleSize = generateSequence(1) { it * 2 }
        .takeWhile { sample ->
            bounds.outWidth / sample > 1920 || bounds.outHeight / sample > 1080
        }
        .lastOrNull() ?: 1
    val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
    context.contentResolver.openInputStream(uri)?.use {
        BitmapFactory.decodeStream(it, null, options)?.asImageBitmap()
    }
}.getOrNull()
