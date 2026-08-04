package pl.ksawery.ktvlauncher.ui.home

import android.content.Context
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.focusable
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items as lazyItems
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.ksawery.ktvlauncher.BuildConfig
import pl.ksawery.ktvlauncher.R
import pl.ksawery.ktvlauncher.model.LaunchableApp
import pl.ksawery.ktvlauncher.model.LauncherThemeMode
import pl.ksawery.ktvlauncher.model.ShelfMode
import pl.ksawery.ktvlauncher.model.UiScale
import pl.ksawery.ktvlauncher.model.WatchNextItem
import pl.ksawery.ktvlauncher.model.WatchNextStatus
import pl.ksawery.ktvlauncher.ui.theme.KtvColors

private enum class LauncherScreen {
    Home,
    Apps,
    Settings,
    Favorites,
    DockPicker,
    FeaturedPicker,
    ThemeNames,
}

private enum class SettingsSection {
    Appearance,
    Layout,
    Dock,
    Apps,
    System,
}

@Composable
fun HomeRoute(
    viewModel: HomeViewModel,
    homeRequest: Int = 0,
    onLaunchApp: (LaunchableApp) -> Unit,
    onPickWallpaper: () -> Unit,
    onExportProfile: () -> Unit,
    onImportProfile: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenWifi: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenMediaAccess: () -> Unit,
    onOpenAppInfo: (LaunchableApp) -> Unit,
    onUninstallApp: (LaunchableApp) -> Unit,
    onLaunchWatchNext: (WatchNextItem) -> Unit,
    onRequestWatchNextAccess: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        uiState = uiState,
        homeRequest = homeRequest,
        onLaunchApp = onLaunchApp,
        onPickWallpaper = onPickWallpaper,
        onExportProfile = onExportProfile,
        onImportProfile = onImportProfile,
        onOpenSystemSettings = onOpenSettings,
        onOpenWifi = onOpenWifi,
        onOpenNotifications = onOpenNotifications,
        onOpenMediaAccess = onOpenMediaAccess,
        onOpenAppInfo = onOpenAppInfo,
        onUninstallApp = onUninstallApp,
        onLaunchWatchNext = onLaunchWatchNext,
        onRequestWatchNextAccess = onRequestWatchNextAccess,
        onToggleFavorite = viewModel::toggleFavorite,
        onMoveFavorite = viewModel::moveFavorite,
        onSetDockShortcut = viewModel::setDockShortcut,
        onSetFeaturedApp = viewModel::setFeaturedApp,
        onCycleShelfMode = viewModel::cycleShelfMode,
        onResetWallpaper = viewModel::resetWallpaper,
        onToggleDockBackground = viewModel::toggleDockBackground,
        onCycleUiScale = viewModel::cycleUiScale,
        onCycleWatchNextSource = viewModel::cycleWatchNextSource,
        onCycleLauncherTheme = viewModel::cycleLauncherTheme,
        onToggleMediaWidget = viewModel::toggleMediaWidget,
        onSaveThemeNames = viewModel::setThemeNames,
        onMoveApp = viewModel::moveApp,
        onToggleContinueWatching = { enabled ->
            viewModel.setContinueWatchingEnabled(enabled)
            if (enabled) onRequestWatchNextAccess()
        },
    )
}

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    homeRequest: Int,
    onLaunchApp: (LaunchableApp) -> Unit,
    onPickWallpaper: () -> Unit,
    onExportProfile: () -> Unit,
    onImportProfile: () -> Unit,
    onOpenSystemSettings: () -> Unit,
    onOpenWifi: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenMediaAccess: () -> Unit,
    onOpenAppInfo: (LaunchableApp) -> Unit,
    onUninstallApp: (LaunchableApp) -> Unit,
    onLaunchWatchNext: (WatchNextItem) -> Unit,
    onRequestWatchNextAccess: () -> Unit,
    onToggleFavorite: (LaunchableApp) -> Unit,
    onMoveFavorite: (LaunchableApp, Int) -> Unit,
    onSetDockShortcut: (Int, LaunchableApp) -> Unit,
    onSetFeaturedApp: (Int, LaunchableApp) -> Unit,
    onCycleShelfMode: () -> Unit,
    onResetWallpaper: () -> Unit,
    onToggleDockBackground: () -> Unit,
    onCycleUiScale: () -> Unit,
    onCycleWatchNextSource: () -> Unit,
    onCycleLauncherTheme: () -> Unit,
    onToggleMediaWidget: () -> Unit,
    onSaveThemeNames: (String, String, String) -> Unit,
    onMoveApp: (LaunchableApp, Int) -> Unit,
    onToggleContinueWatching: (Boolean) -> Unit,
) {
    var screen by rememberSaveable { mutableStateOf(LauncherScreen.Home) }
    var showInfo by rememberSaveable { mutableStateOf(false) }
    var contextApp by remember { mutableStateOf<LaunchableApp?>(null) }
    var dockPickerSlot by rememberSaveable { mutableStateOf(0) }
    var featuredPickerSlot by rememberSaveable { mutableStateOf(0) }
    var settingsSection by rememberSaveable { mutableStateOf(SettingsSection.Appearance) }
    var showResetWallpaperConfirmation by rememberSaveable { mutableStateOf(false) }
    val baseDensity = LocalDensity.current

    LaunchedEffect(homeRequest) {
        screen = LauncherScreen.Home
        showInfo = false
        contextApp = null
        showResetWallpaperConfirmation = false
    }

    CompositionLocalProvider(
        LocalDensity provides Density(
            density = baseDensity.density * uiState.uiScale.multiplier,
            fontScale = baseDensity.fontScale,
        ),
    ) {

    BackHandler(enabled = true) {
        when {
            contextApp != null -> contextApp = null
            showInfo -> showInfo = false
            screen == LauncherScreen.Favorites ||
                screen == LauncherScreen.DockPicker ||
                screen == LauncherScreen.FeaturedPicker ||
                screen == LauncherScreen.ThemeNames -> {
                screen = LauncherScreen.Settings
            }
            screen != LauncherScreen.Home -> screen = LauncherScreen.Home
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LauncherBackground(uiState.wallpaperUri, uiState.launcherTheme)

        when (screen) {
            LauncherScreen.Home -> {
                if (uiState.launcherTheme == LauncherThemeMode.Theme2 ||
                    uiState.launcherTheme == LauncherThemeMode.Theme3
                ) {
                    ThemeTwoDashboard(
                        isThemeThree = uiState.launcherTheme == LauncherThemeMode.Theme3,
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
                } else {
                    HomeDashboard(
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
                }
            }

            LauncherScreen.Apps -> AllAppsScreen(
                apps = uiState.apps,
                recentlyAdded = uiState.recentlyAdded,
                onLaunchApp = onLaunchApp,
                onLongClickApp = { contextApp = it },
            )

            LauncherScreen.Settings -> LauncherSettingsScreen(
                uiState = uiState,
                section = settingsSection,
                onSectionChange = { settingsSection = it },
                onPickWallpaper = onPickWallpaper,
                onResetWallpaper = { showResetWallpaperConfirmation = true },
                onExportProfile = onExportProfile,
                onImportProfile = onImportProfile,
                onOpenAllApps = { screen = LauncherScreen.Apps },
                onEditFavorites = { screen = LauncherScreen.Favorites },
                onEditThemeNames = { screen = LauncherScreen.ThemeNames },
                onPickDockShortcut = { slot ->
                    dockPickerSlot = slot
                    screen = LauncherScreen.DockPicker
                },
                onPickFeaturedApp = { slot ->
                    featuredPickerSlot = slot
                    screen = LauncherScreen.FeaturedPicker
                },
                onCycleShelfMode = onCycleShelfMode,
                onToggleDockBackground = onToggleDockBackground,
                onCycleUiScale = onCycleUiScale,
                onCycleWatchNextSource = onCycleWatchNextSource,
                onCycleLauncherTheme = onCycleLauncherTheme,
                onToggleMediaWidget = onToggleMediaWidget,
                onToggleContinueWatching = onToggleContinueWatching,
                onRequestWatchNextAccess = onRequestWatchNextAccess,
                onOpenSystemSettings = onOpenSystemSettings,
                onOpenMediaAccess = onOpenMediaAccess,
            )

            LauncherScreen.ThemeNames -> ThemeNamesEditor(
                themeOneName = uiState.themeOneName,
                themeTwoName = uiState.themeTwoName,
                themeThreeName = uiState.themeThreeName,
                onSave = { first, second, third ->
                    onSaveThemeNames(first, second, third)
                    screen = LauncherScreen.Settings
                },
                onCancel = { screen = LauncherScreen.Settings },
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

            LauncherScreen.FeaturedPicker -> DockShortcutPicker(
                slot = featuredPickerSlot,
                apps = uiState.apps,
                title = "Duży skrót ${featuredPickerSlot + 1}",
                onSelect = { app ->
                    onSetFeaturedApp(featuredPickerSlot, app)
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
                onMoveAppUp = { onMoveApp(app, -1) },
                onMoveAppDown = { onMoveApp(app, 1) },
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
        if (showResetWallpaperConfirmation) {
            ResetWallpaperConfirmation(
                onConfirm = {
                    onResetWallpaper()
                    showResetWallpaperConfirmation = false
                },
                onCancel = { showResetWallpaperConfirmation = false },
            )
        }
    }
    }
}

@Composable
private fun LauncherBackground(
    customWallpaperUri: String?,
    theme: LauncherThemeMode,
) {
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color(0x12000000),
                        0.70f to Color(0x7A05070A),
                        1f to Color(0xDD05070A),
                    ),
                ),
        )
        return
    }

    when (theme) {
        LauncherThemeMode.Theme1 -> {
            Image(
                painter = painterResource(R.drawable.launcher_wallpaper),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
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
        LauncherThemeMode.Theme2 -> ThemeTwoBackground()
        LauncherThemeMode.Theme3 -> ThemeThreeBackground()
    }
}

@Composable
private fun ThemeTwoBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF05080E),
                    Color(0xFF111824),
                    Color(0xFF283240),
                    Color(0xFF10151C),
                    Color(0xFF05070A),
                ),
                startY = 0f,
                endY = height * 0.76f,
            ),
        )
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0x264E617C),
                    Color(0x0B6A7785),
                    Color.Transparent,
                ),
                center = Offset(width * 0.54f, height * 0.37f),
                radius = width * 0.72f,
            ),
        )

        val mainRidge = Path().apply {
            moveTo(-width * 0.04f, height * 0.40f)
            cubicTo(
                width * 0.16f, height * 0.27f,
                width * 0.32f, height * 0.49f,
                width * 0.54f, height * 0.60f,
            )
            cubicTo(
                width * 0.72f, height * 0.71f,
                width * 0.85f, height * 0.47f,
                width * 1.05f, height * 0.39f,
            )
            lineTo(width * 1.05f, height * 0.70f)
            lineTo(-width * 0.04f, height * 0.70f)
            close()
        }
        drawPath(
            path = mainRidge,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF030508), Color(0xFF090D12)),
                startY = height * 0.32f,
                endY = height * 0.71f,
            ),
        )

        val mainRidgeLight = Path().apply {
            moveTo(-width * 0.04f, height * 0.40f)
            cubicTo(
                width * 0.16f, height * 0.27f,
                width * 0.32f, height * 0.49f,
                width * 0.54f, height * 0.60f,
            )
            cubicTo(
                width * 0.72f, height * 0.71f,
                width * 0.85f, height * 0.47f,
                width * 1.05f, height * 0.39f,
            )
        }
        drawPath(mainRidgeLight, Color(0x243C2716), style = Stroke(width = 28.dp.toPx()))
        drawPath(mainRidgeLight, Color(0x80D7A768), style = Stroke(width = 2.2.dp.toPx()))

        val innerRidge = Path().apply {
            moveTo(width * 0.31f, height * 0.53f)
            cubicTo(
                width * 0.43f, height * 0.50f,
                width * 0.54f, height * 0.70f,
                width * 0.69f, height * 0.56f,
            )
            cubicTo(
                width * 0.80f, height * 0.47f,
                width * 0.86f, height * 0.63f,
                width * 0.95f, height * 0.51f,
            )
        }
        drawPath(innerRidge, Color(0x1EBC8A4A), style = Stroke(width = 22.dp.toPx()))
        drawPath(innerRidge, Color(0x6EC49356), style = Stroke(width = 1.6.dp.toPx()))

        val lowerRidge = Path().apply {
            moveTo(width * 0.36f, height * 0.61f)
            cubicTo(
                width * 0.50f, height * 0.68f,
                width * 0.63f, height * 0.71f,
                width * 0.76f, height * 0.58f,
            )
            cubicTo(
                width * 0.86f, height * 0.49f,
                width * 0.94f, height * 0.48f,
                width * 1.04f, height * 0.44f,
            )
        }
        drawPath(lowerRidge, Color(0x2AB68148), style = Stroke(width = 24.dp.toPx()))
        drawPath(lowerRidge, Color(0x7BD6A260), style = Stroke(width = 1.8.dp.toPx()))

        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    Color(0x9B05070A),
                    Color(0xE805070A),
                ),
                startY = height * 0.62f,
                endY = height,
            ),
        )
    }
}


@Composable
private fun ThemeThreeBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF05080F),
                    Color(0xFF111A28),
                    Color(0xFF3D4856),
                    Color(0xFF171B22),
                    Color(0xFF05070A),
                ),
                startY = 0f,
                endY = height * 0.75f,
            ),
        )
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x738F765B), Color(0x244D5260), Color.Transparent),
                center = Offset(width * 0.47f, height * 0.52f),
                radius = width * 0.62f,
            ),
        )

        val mainRidge = Path().apply {
            moveTo(-width * 0.03f, height * 0.40f)
            cubicTo(width * 0.14f, height * 0.27f, width * 0.28f, height * 0.43f, width * 0.45f, height * 0.55f)
            cubicTo(width * 0.61f, height * 0.67f, width * 0.77f, height * 0.53f, width * 1.04f, height * 0.39f)
            lineTo(width * 1.04f, height * 0.69f)
            lineTo(-width * 0.03f, height * 0.69f)
            close()
        }
        drawPath(
            path = mainRidge,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF020407), Color(0xFF0A0D12)),
                startY = height * 0.31f,
                endY = height * 0.70f,
            ),
        )

        val rim = Path().apply {
            moveTo(-width * 0.03f, height * 0.40f)
            cubicTo(width * 0.14f, height * 0.27f, width * 0.28f, height * 0.43f, width * 0.45f, height * 0.55f)
            cubicTo(width * 0.61f, height * 0.67f, width * 0.77f, height * 0.53f, width * 1.04f, height * 0.39f)
        }
        drawPath(rim, Color(0x368A6439), style = Stroke(width = 38.dp.toPx()))
        drawPath(rim, Color(0xB7E3B978), style = Stroke(width = 1.7.dp.toPx()))

        val innerWave = Path().apply {
            moveTo(width * 0.34f, height * 0.54f)
            cubicTo(width * 0.49f, height * 0.52f, width * 0.54f, height * 0.68f, width * 0.66f, height * 0.58f)
            cubicTo(width * 0.78f, height * 0.47f, width * 0.84f, height * 0.60f, width * 0.98f, height * 0.48f)
        }
        drawPath(innerWave, Color(0x306D4D2C), style = Stroke(width = 30.dp.toPx()))
        drawPath(innerWave, Color(0x84D1A768), style = Stroke(width = 1.2.dp.toPx()))

        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.Transparent, Color(0x7C05070A), Color(0xE605070A)),
                startY = height * 0.60f,
                endY = height,
            ),
        )
    }
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
                        .align(Alignment.TopEnd)
                        .padding(top = 134.dp, end = 34.dp),
                )
            }

        GreetingAndWeather(
            uiState = uiState,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(y = maxHeight * 0.39f)
                .padding(start = 34.dp),
        )

        UnifiedDock(
            uiState = uiState,
            onOpenSettings = onOpenLauncherSettings,
            onOpenApps = onOpenApps,
            onOpenInfo = onOpenInfo,
            onLaunchApp = onLaunchApp,
            onLaunchWatchNext = onLaunchWatchNext,
            onRequestWatchNextAccess = onRequestWatchNextAccess,
            onLongClickApp = onLongClickApp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 22.dp)
                .padding(bottom = 16.dp),
        )
    }
}

@Composable
private fun UnifiedDock(
    uiState: HomeUiState,
    onOpenSettings: () -> Unit,
    onOpenApps: () -> Unit,
    onOpenInfo: () -> Unit,
    onLaunchApp: (LaunchableApp) -> Unit,
    onLaunchWatchNext: (WatchNextItem) -> Unit,
    onRequestWatchNextAccess: () -> Unit,
    onLongClickApp: (LaunchableApp) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(176.dp)
            .clip(RoundedCornerShape(18.dp))
            .then(
                if (uiState.dockBackgroundEnabled) {
                    Modifier
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0x4A20252B), Color(0x6E080C12)),
                            ),
                        )
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0x10FFFFFF), Color.Transparent),
                                center = Offset(920f, -90f),
                                radius = 820f,
                            ),
                        )
                        .border(1.dp, Color(0x38FFFFFF), RoundedCornerShape(18.dp))
                } else {
                    Modifier
                },
            )
            .padding(start = 14.dp, top = 8.dp, end = 14.dp, bottom = 8.dp),
    ) {
        ContentShelf(
            shelfMode = uiState.shelfMode,
            watchNext = uiState.watchNext,
            watchNextStatus = uiState.watchNextStatus,
            apps = uiState.apps,
            featuredApps = uiState.featuredApps,
            favorites = uiState.favorites,
            onLaunchApp = onLaunchApp,
            onLaunchWatchNext = onLaunchWatchNext,
            onRequestWatchNextAccess = onRequestWatchNextAccess,
            onLongClickApp = onLongClickApp,
        )
        Spacer(Modifier.height(6.dp))
        Dock(
            shortcuts = uiState.dockShortcuts,
            onOpenSettings = onOpenSettings,
            onOpenApps = onOpenApps,
            onOpenInfo = onOpenInfo,
            onLaunchApp = onLaunchApp,
        )
    }
}

@Composable
private fun StatusAndClock(
    onOpenWifi: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenSettings: () -> Unit,
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

    Column(modifier = modifier, horizontalAlignment = Alignment.End) {
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
        Spacer(Modifier.height(9.dp))
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
            StatusIconButton(
                icon = Icons.Rounded.Settings,
                contentDescription = "Ustawienia launchera",
                tint = KtvColors.TextPrimary,
                onClick = onOpenSettings,
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
    shelfMode: ShelfMode,
    watchNext: List<WatchNextItem>,
    watchNextStatus: WatchNextStatus,
    apps: List<LaunchableApp>,
    featuredApps: List<LaunchableApp>,
    favorites: List<LaunchableApp>,
    onLaunchApp: (LaunchableApp) -> Unit,
    onLaunchWatchNext: (WatchNextItem) -> Unit,
    onRequestWatchNextAccess: () -> Unit,
    onLongClickApp: (LaunchableApp) -> Unit,
    modifier: Modifier = Modifier,
) {
    val firstFocusRequester = remember { FocusRequester() }
    LaunchedEffect(
        shelfMode,
        watchNextStatus,
        featuredApps.firstOrNull()?.stableKey,
        favorites.firstOrNull()?.stableKey,
    ) {
        if (watchNext.isNotEmpty() || featuredApps.isNotEmpty() || favorites.isNotEmpty()) {
            firstFocusRequester.requestFocus()
        }
    }

    Row(
        verticalAlignment = Alignment.Top,
        modifier = modifier
            .fillMaxWidth()
            .height(102.dp),
    ) {
        if (shelfMode != ShelfMode.Hidden) {
            Column(
                modifier = Modifier
                    .width(326.dp)
                    .padding(horizontal = 10.dp),
            ) {
                SectionTitle(
                    if (shelfMode == ShelfMode.WatchNext) {
                        "Kontynuuj oglądanie"
                    } else {
                        "Najważniejsze aplikacje"
                    },
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(
                        10.dp,
                        Alignment.CenterHorizontally,
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(76.dp),
                ) {
                    when (shelfMode) {
                        ShelfMode.WatchNext -> {
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

                        ShelfMode.AppShortcuts -> {
                            featuredApps.take(2).forEachIndexed { index, app ->
                                FeaturedAppCard(
                                    app = app,
                                    onClick = { onLaunchApp(app) },
                                    onLongClick = { onLongClickApp(app) },
                                    modifier = if (index == 0) {
                                        Modifier.focusRequester(firstFocusRequester)
                                    } else {
                                        Modifier
                                    },
                                )
                            }
                        }

                        ShelfMode.Hidden -> Unit
                    }
                }
            }

            Spacer(Modifier.width(24.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            SectionTitle("Ulubione aplikacje")
            Spacer(Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.height(76.dp),
            ) {
                favorites.take(8).forEachIndexed { index, app ->
                    FavoriteTile(
                        app = app,
                        onClick = { onLaunchApp(app) },
                        onLongClick = { onLongClickApp(app) },
                        modifier = if (
                            index == 0 &&
                            (
                                shelfMode == ShelfMode.Hidden ||
                                    (
                                        shelfMode == ShelfMode.AppShortcuts &&
                                            featuredApps.isEmpty()
                                    )
                            )
                        ) {
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
private fun FeaturedAppCard(
    app: LaunchableApp,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FocusableSurface(
        onClick = onClick,
        onLongClick = onLongClick,
        focusedScale = 1.035f,
        shape = RoundedCornerShape(9.dp),
        modifier = modifier
            .width(148.dp)
            .height(64.dp),
    ) { focused ->
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        if (focused) {
                            listOf(Color(0xA4363D48), Color(0xD312171E))
                        } else {
                            listOf(Color(0x70242A32), Color(0xB20B0F14))
                        },
                    ),
                )
                .padding(horizontal = 13.dp),
        ) {
            Image(
                bitmap = app.icon,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(9.dp)),
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = app.label,
                color = if (focused) KtvColors.TextPrimary else KtvColors.TextSecondary,
                fontSize = 10.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = Modifier.height(20.dp),
    ) {
        Text(
            text = text,
            color = KtvColors.TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            maxLines = 1,
        )
    }
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
        shape = RoundedCornerShape(9.dp),
        modifier = modifier
            .width(148.dp)
            .height(64.dp),
    ) { focused ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xB30B0E13)),
        ) {
            if (poster != null) {
                Image(
                    bitmap = poster,
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else if (appIcon != null) {
                Image(
                    bitmap = appIcon,
                    contentDescription = item.title,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(48.dp),
                )
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 9.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(0x70080A0E)),
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = null,
                    tint = KtvColors.TextPrimary,
                    modifier = Modifier.size(16.dp),
                )
            }

            item.progressPercent?.let { progress ->
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth(progress)
                        .height(2.dp)
                        .background(Color(0xFFECEFF3)),
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
        shape = RoundedCornerShape(9.dp),
        modifier = modifier
            .width(148.dp)
            .height(64.dp),
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
            .height(48.dp),
    ) {
        DockAction("Ustawienia", Icons.Rounded.Settings, onOpenSettings, Modifier.weight(1f))
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
                .background(
                    Brush.verticalGradient(
                        if (focused) {
                            listOf(Color(0x80515A68), Color(0xA61A2029))
                        } else {
                            listOf(Color(0x3D343C48), Color(0x6810151C))
                        },
                    ),
                )
                .border(
                    1.dp,
                    if (focused) Color(0x8AFFFFFF) else Color(0x24FFFFFF),
                    RoundedCornerShape(9.dp),
                )
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
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        if (focused) {
                            listOf(Color(0x80515A68), Color(0xA61A2029))
                        } else {
                            listOf(Color(0x3D343C48), Color(0x6810151C))
                        },
                    ),
                )
                .border(
                    1.dp,
                    if (focused) Color(0x8AFFFFFF) else Color(0x24FFFFFF),
                    RoundedCornerShape(9.dp),
                )
                .padding(horizontal = 12.dp),
        ) {
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
private fun LauncherSettingsScreen(
    uiState: HomeUiState,
    section: SettingsSection,
    onSectionChange: (SettingsSection) -> Unit,
    onPickWallpaper: () -> Unit,
    onResetWallpaper: () -> Unit,
    onExportProfile: () -> Unit,
    onImportProfile: () -> Unit,
    onOpenAllApps: () -> Unit,
    onEditFavorites: () -> Unit,
    onEditThemeNames: () -> Unit,
    onPickDockShortcut: (Int) -> Unit,
    onPickFeaturedApp: (Int) -> Unit,
    onCycleShelfMode: () -> Unit,
    onToggleDockBackground: () -> Unit,
    onCycleUiScale: () -> Unit,
    onCycleWatchNextSource: () -> Unit,
    onCycleLauncherTheme: () -> Unit,
    onToggleMediaWidget: () -> Unit,
    onToggleContinueWatching: (Boolean) -> Unit,
    onRequestWatchNextAccess: () -> Unit,
    onOpenSystemSettings: () -> Unit,
    onOpenMediaAccess: () -> Unit,
) {
    val firstFocusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { firstFocusRequester.requestFocus() }
    val watchNextLabel = when (uiState.watchNextStatus) {
        WatchNextStatus.Loading -> "Sprawdzanie danych…"
        WatchNextStatus.Ready -> "Dostępne materiały: ${uiState.watchNext.size}"
        WatchNextStatus.Empty -> "Brak opublikowanych materiałów"
        WatchNextStatus.Unavailable -> "Wymagany dostęp systemowy"
    }
    val shelfModeLabel = when (uiState.shelfMode) {
        ShelfMode.WatchNext -> "Kontynuuj oglądanie"
        ShelfMode.AppShortcuts -> "Dwa duże skróty"
        ShelfMode.Hidden -> "Ukryta"
    }
    val scaleLabel = when (uiState.uiScale) {
        UiScale.Auto -> "Automatyczna"
        UiScale.Compact -> "Mniejsza"
        UiScale.Comfortable -> "Większa"
    }
    val watchSourceLabel = uiState.watchNextSourcePackage?.let { packageName ->
        uiState.watchNextSources.firstOrNull {
            it.componentName.packageName == packageName
        }?.label ?: packageName
    } ?: "Wszystkie dostępne"

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xF0080B10))
            .padding(horizontal = 36.dp, vertical = 28.dp),
    ) {
        Column(modifier = Modifier.width(220.dp)) {
            Text("Ustawienia", color = KtvColors.TextPrimary, fontSize = 27.sp)
            Text("KSTV Launcher", color = KtvColors.TextSecondary, fontSize = 12.sp)
            Spacer(Modifier.height(24.dp))
            SettingsSection.entries.forEachIndexed { index, item ->
                SettingsNavigationRow(
                    title = when (item) {
                        SettingsSection.Appearance -> "Wygląd"
                        SettingsSection.Layout -> "Układ ekranu"
                        SettingsSection.Dock -> "Dock i skróty"
                        SettingsSection.Apps -> "Aplikacje"
                        SettingsSection.System -> "System"
                    },
                    selected = section == item,
                    onClick = { onSectionChange(item) },
                    modifier = if (index == 0) {
                        Modifier.focusRequester(firstFocusRequester)
                    } else Modifier,
                )
                Spacer(Modifier.height(6.dp))
            }
        }
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 26.dp)
                .width(1.dp)
                .background(Color(0x2EFFFFFF)),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = when (section) {
                    SettingsSection.Appearance -> "Wygląd"
                    SettingsSection.Layout -> "Układ ekranu"
                    SettingsSection.Dock -> "Dock i skróty"
                    SettingsSection.Apps -> "Aplikacje"
                    SettingsSection.System -> "System i profil"
                },
                color = KtvColors.TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.height(18.dp))
            when (section) {
                SettingsSection.Appearance -> {
                    val themeLabel = when (uiState.launcherTheme) {
                        LauncherThemeMode.Theme1 -> uiState.themeOneName
                        LauncherThemeMode.Theme2 -> uiState.themeTwoName
                        LauncherThemeMode.Theme3 -> uiState.themeThreeName
                    }
                    SettingRow(
                        "Motyw launchera",
                        "$themeLabel · OK, aby przełączyć",
                        Icons.Rounded.Wallpaper,
                        onCycleLauncherTheme,
                    )
                    SettingsSpacer()
                    SettingRow(
                        "Nazwy motywów",
                        "${uiState.themeOneName} / ${uiState.themeTwoName} / ${uiState.themeThreeName}",
                        Icons.Rounded.Info,
                        onEditThemeNames,
                    )
                    SettingsSpacer()
                    SettingRow("Wybierz tapetę", "Obraz z pamięci urządzenia", Icons.Rounded.Wallpaper, onPickWallpaper)
                    SettingsSpacer()
                    SettingRow("Przywróć domyślną", "Wbudowana tapeta KSTV", Icons.Rounded.Wallpaper, onResetWallpaper)
                    SettingsSpacer()
                    SettingRow(
                        "Widżet odtwarzacza",
                        "Spotify i inne aktywne sesje multimediów",
                        Icons.Rounded.PlayArrow,
                        onToggleMediaWidget,
                        trailingText = if (uiState.mediaWidgetEnabled) "Wł." else "Wył.",
                    )
                    SettingsSpacer()
                    SettingRow(
                        "Dostęp do odtwarzacza",
                        "Włącz usługę KSTV Media w ustawieniach Androida",
                        Icons.Rounded.Settings,
                        onOpenMediaAccess,
                    )
                }
                SettingsSection.Layout -> {
                    SettingRow("Skala interfejsu", "$scaleLabel · OK, aby zmienić", Icons.Rounded.Apps, onCycleUiScale)
                    SettingsSpacer()
                    SettingRow("Lewa sekcja", "$shelfModeLabel · OK, aby zmienić", Icons.Rounded.PlayArrow, onCycleShelfMode)
                    SettingsSpacer()
                    SettingRow(
                        "Kontynuuj oglądanie",
                        watchNextLabel,
                        Icons.Rounded.PlayArrow,
                        onClick = {
                            if (uiState.continueWatchingEnabled && uiState.watchNextStatus == WatchNextStatus.Unavailable) {
                                onRequestWatchNextAccess()
                            } else {
                                onToggleContinueWatching(!uiState.continueWatchingEnabled)
                            }
                        },
                        trailingText = if (uiState.continueWatchingEnabled) "Wł." else "Wył.",
                    )
                    SettingsSpacer()
                    SettingRow(
                        "Źródło kontynuacji",
                        "$watchSourceLabel · tylko aplikacje udostępniające dane",
                        Icons.Rounded.PlayArrow,
                        onCycleWatchNextSource,
                    )
                }
                SettingsSection.Dock -> {
                    SettingRow(
                        "Szklane tło docka",
                        "Elementy pozostają zawsze widoczne",
                        Icons.Rounded.Apps,
                        onToggleDockBackground,
                        trailingText = if (uiState.dockBackgroundEnabled) "Wł." else "Wył.",
                    )
                    SettingsSpacer()
                    repeat(2) { slot ->
                        SettingRow(
                            "Duży skrót ${slot + 1}",
                            uiState.featuredApps.getOrNull(slot)?.label ?: "Nie ustawiono",
                            Icons.Rounded.Apps,
                            { onPickFeaturedApp(slot) },
                        )
                        SettingsSpacer()
                    }
                    repeat(3) { slot ->
                        SettingRow(
                            "Dolny skrót ${slot + 1}",
                            uiState.dockShortcuts.getOrNull(slot)?.label ?: "Nie ustawiono",
                            Icons.Rounded.PlayArrow,
                            { onPickDockShortcut(slot) },
                        )
                        if (slot < 2) SettingsSpacer()
                    }
                }
                SettingsSection.Apps -> {
                    SettingRow("Ulubione aplikacje", "Wybrane: ${uiState.favorites.size} / 8", Icons.Rounded.Apps, onEditFavorites)
                    SettingsSpacer()
                    SettingRow(
                        "Kolejność wszystkich aplikacji",
                        "Przytrzymaj aplikację i wybierz przeniesienie wyżej lub niżej",
                        Icons.Rounded.Apps,
                        onClick = onOpenAllApps,
                    )
                }
                SettingsSection.System -> {
                    SettingRow("Eksportuj profil", "Zapisz układ i skróty do pliku JSON", Icons.Rounded.Info, onExportProfile)
                    SettingsSpacer()
                    SettingRow("Importuj profil", "Odtwórz układ na drugim telewizorze", Icons.Rounded.Info, onImportProfile)
                    SettingsSpacer()
                    SettingRow("Ustawienia systemowe", "Sieć, dźwięk i urządzenie", Icons.Rounded.Settings, onOpenSystemSettings)
                }
            }
        }
    }
}

@Composable
private fun SettingsNavigationRow(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FocusableSurface(
        onClick = onClick,
        focusedScale = 1.015f,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp),
    ) { focused ->
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .background(
                    when {
                        focused -> Color(0xD9333942)
                        selected -> Color(0x8F242A32)
                        else -> Color.Transparent
                    },
                )
                .padding(horizontal = 14.dp),
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(20.dp)
                    .background(if (selected) KtvColors.Accent else Color.Transparent),
            )
            Spacer(Modifier.width(12.dp))
            Text(title, color = KtvColors.TextPrimary, fontSize = 13.sp)
        }
    }
}

@Composable
private fun SettingsSpacer() = Spacer(Modifier.height(9.dp))

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
private fun ThemeNamesEditor(
    themeOneName: String,
    themeTwoName: String,
    themeThreeName: String,
    onSave: (String, String, String) -> Unit,
    onCancel: () -> Unit,
) {
    var firstName by rememberSaveable { mutableStateOf(themeOneName) }
    var secondName by rememberSaveable { mutableStateOf(themeTwoName) }
    var thirdName by rememberSaveable { mutableStateOf(themeThreeName) }
    val firstFocusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { firstFocusRequester.requestFocus() }
    BackHandler(onBack = onCancel)

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xF0080B10)).padding(horizontal = 54.dp, vertical = 34.dp),
    ) {
        Text("Nazwy motywów", color = KtvColors.TextPrimary, fontSize = 27.sp)
        Text("Wybierz pole, wpisz nazwę i zatwierdź Zapisz.", color = KtvColors.TextSecondary, fontSize = 12.sp)
        Spacer(Modifier.height(28.dp))
        ThemeNameField("Theme 1", firstName, { firstName = it }, Modifier.focusRequester(firstFocusRequester))
        Spacer(Modifier.height(16.dp))
        ThemeNameField("Theme 2", secondName, { secondName = it })
        Spacer(Modifier.height(16.dp))
        ThemeNameField("Theme 3", thirdName, { thirdName = it })
        Spacer(Modifier.height(28.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ContextMenuRow("Zapisz", { onSave(firstName, secondName, thirdName) }, Modifier.width(180.dp))
            ContextMenuRow("Anuluj", onCancel, Modifier.width(180.dp))
        }
    }
}

@Composable
private fun ThemeNameField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var focused by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .width(420.dp)
            .border(
                if (focused) 2.dp else 1.dp,
                if (focused) Color(0xFFF6F8FB) else Color(0x3AFFFFFF),
                RoundedCornerShape(10.dp),
            )
            .background(Color(0x8C141A22), RoundedCornerShape(10.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(label, color = KtvColors.TextSecondary, fontSize = 11.sp)
        Spacer(Modifier.height(5.dp))
        BasicTextField(
            value = value,
            onValueChange = { onValueChange(it.take(24)) },
            singleLine = true,
            textStyle = TextStyle(color = KtvColors.TextPrimary, fontSize = 18.sp),
            modifier = modifier
                .fillMaxWidth()
                .onFocusChanged { focused = it.isFocused },
        )
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
    title: String = "Skrót docka ${slot + 1}",
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
            title,
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
    recentlyAdded: List<LaunchableApp>,
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
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xD20B0F15), Color(0xF207090D)),
                ),
            )
            .padding(horizontal = 54.dp, vertical = 30.dp),
    ) {
        Text(
            text = "Aplikacje",
            color = KtvColors.TextPrimary,
            fontSize = 27.sp,
            fontWeight = FontWeight.Medium,
        )
        Spacer(Modifier.height(12.dp))

        if (recentlyAdded.isNotEmpty()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
            ) {
                Text(
                    text = "Ostatnio dodane",
                    color = KtvColors.TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.width(108.dp),
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    lazyItems(recentlyAdded, key = LaunchableApp::stableKey) { app ->
                        RecentlyAddedTile(
                            app = app,
                            onClick = { onLaunchApp(app) },
                            onLongClick = { onLongClickApp(app) },
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }

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
private fun RecentlyAddedTile(
    app: LaunchableApp,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    FocusableSurface(
        onClick = onClick,
        onLongClick = onLongClick,
        focusedScale = 1.04f,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .width(145.dp)
            .height(54.dp),
    ) { focused ->
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .background(if (focused) Color(0xD82A3038) else Color(0x8F15191F))
                .border(
                    1.dp,
                    if (focused) Color(0x99FFFFFF) else Color(0x24FFFFFF),
                    RoundedCornerShape(10.dp),
                )
                .padding(horizontal = 10.dp),
        ) {
            Image(
                bitmap = app.icon,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(38.dp),
            )
            Spacer(Modifier.width(9.dp))
            Text(
                text = app.label,
                color = KtvColors.TextPrimary,
                fontSize = 11.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
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
        showFocusBorder = false,
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
                    .size(68.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .border(
                        1.dp,
                        if (focused) Color(0xB8FFFFFF) else Color.Transparent,
                        RoundedCornerShape(14.dp),
                    )
                    .padding(3.dp),
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
    onMoveAppUp: () -> Unit,
    onMoveAppDown: () -> Unit,
    onSetDock: (Int) -> Unit,
    onAppInfo: () -> Unit,
    onUninstall: () -> Unit,
) {
    val firstFocusRequester = remember { FocusRequester() }
    var suppressActivationUntilRelease by remember(app.stableKey) { mutableStateOf(true) }
    LaunchedEffect(app.stableKey) { firstFocusRequester.requestFocus() }
    BackHandler { onClose() }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xB3000000))
            .onPreviewKeyEvent { event ->
                val activationKey = event.key == Key.DirectionCenter ||
                    event.key == Key.Enter ||
                    event.key == Key.NumPadEnter
                if (!activationKey || !suppressActivationUntilRelease) {
                    false
                } else {
                    if (event.type == KeyEventType.KeyUp) {
                        suppressActivationUntilRelease = false
                    }
                    true
                }
            },
    ) {
        Column(
            modifier = Modifier
                .focusGroup()
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
            ContextMenuRow("Przenieś wyżej na liście", onMoveAppUp)
            ContextMenuRow("Przenieś niżej na liście", onMoveAppDown)
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
private fun ResetWallpaperConfirmation(
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    val firstFocusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { firstFocusRequester.requestFocus() }
    BackHandler { onCancel() }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xB8000000)),
    ) {
        Column(
            modifier = Modifier
                .width(340.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(Color(0xF21A1E25))
                .border(1.dp, Color(0x38FFFFFF), RoundedCornerShape(13.dp))
                .padding(16.dp),
        ) {
            Text(
                "Przywrócić domyślną tapetę?",
                color = KtvColors.TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Obecnie wybrana tapeta zostanie usunięta.",
                color = KtvColors.TextSecondary,
                fontSize = 11.sp,
            )
            Spacer(Modifier.height(14.dp))
            ContextMenuRow("Anuluj", onCancel, Modifier.focusRequester(firstFocusRequester))
            Spacer(Modifier.height(4.dp))
            ContextMenuRow("Przywróć domyślną", onConfirm, danger = true)
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
                .height(170.dp),
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
                Spacer(Modifier.height(7.dp))
                Text(
                    "Created by Ksawery S. & Alex",
                    color = Color(0xFF8F98A6),
                    fontSize = 10.sp,
                )
                Spacer(Modifier.height(16.dp))
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
    showFocusBorder: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable (focused: Boolean) -> Unit,
) {
    var focused by remember { mutableStateOf(false) }
    var longPressJob by remember { mutableStateOf<Job?>(null) }
    var longPressTriggered by remember { mutableStateOf(false) }
    var keyDownStartedHere by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
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
                width = if (focused && showFocusBorder) 3.dp else 0.dp,
                color = if (focused && showFocusBorder) Color(0xE7101620) else Color.Transparent,
                shape = shape,
            )
            .border(
                width = if (focused && showFocusBorder) 1.dp else 0.dp,
                color = if (focused && showFocusBorder) Color(0xFFF6F8FB) else Color.Transparent,
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
                        keyDownStartedHere = true
                        if (longPressJob == null && onLongClick != null) {
                            longPressTriggered = false
                            longPressJob = scope.launch {
                                delay(LONG_PRESS_MILLIS)
                                longPressJob = null
                                longPressTriggered = true
                                onLongClick()
                            }
                        }
                        true
                    }
                    KeyEventType.KeyUp -> {
                        if (!keyDownStartedHere) return@onKeyEvent true
                        keyDownStartedHere = false
                        longPressJob?.cancel()
                        longPressJob = null
                        if (!longPressTriggered) {
                            onClick()
                        }
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

internal fun greetingFor(hour: Int): String = when (hour) {
    in 5..19 -> "Dzień dobry"
    else -> "Dobry wieczór"
}

private const val LONG_PRESS_MILLIS = 550L

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
