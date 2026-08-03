package pl.ksawery.ktvlauncher.ui.home

import android.content.Context
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import pl.ksawery.ktvlauncher.BuildConfig
import pl.ksawery.ktvlauncher.R
import pl.ksawery.ktvlauncher.model.LaunchableApp
import pl.ksawery.ktvlauncher.ui.theme.KtvColors

private enum class LauncherScreen {
    Home,
    Apps,
}

@Composable
fun HomeRoute(
    viewModel: HomeViewModel,
    onLaunchApp: (LaunchableApp) -> Unit,
    onPickWallpaper: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        uiState = uiState,
        onLaunchApp = onLaunchApp,
        onPickWallpaper = onPickWallpaper,
        onOpenSettings = onOpenSettings,
    )
}

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onLaunchApp: (LaunchableApp) -> Unit,
    onPickWallpaper: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    var screen by rememberSaveable { mutableStateOf(LauncherScreen.Home) }
    var showInfo by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = true) {
        when {
            showInfo -> showInfo = false
            screen == LauncherScreen.Apps -> screen = LauncherScreen.Home
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LauncherBackground(uiState.wallpaperUri)

        when (screen) {
            LauncherScreen.Home -> HomeDashboard(
                uiState = uiState,
                onLaunchApp = onLaunchApp,
                onOpenApps = { screen = LauncherScreen.Apps },
                onPickWallpaper = onPickWallpaper,
                onOpenSettings = onOpenSettings,
                onOpenInfo = { showInfo = true },
            )

            LauncherScreen.Apps -> AllAppsScreen(
                apps = uiState.apps,
                onLaunchApp = onLaunchApp,
            )
        }

        if (showInfo) {
            InfoOverlay(onClose = { showInfo = false })
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
    onPickWallpaper: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenInfo: () -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        StatusAndClock(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 26.dp, end = 34.dp),
        )

        GreetingAndWeather(
            uiState = uiState,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(y = maxHeight * 0.39f)
                .padding(start = 34.dp),
        )

        ContentShelf(
            recent = if (uiState.recent.isNotEmpty()) {
                uiState.recent
            } else {
                uiState.favorites.take(2)
            },
            favorites = uiState.favorites,
            onLaunchApp = onLaunchApp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(y = maxHeight * 0.59f)
                .padding(horizontal = 34.dp),
        )

        Dock(
            shortcuts = uiState.favorites.take(2),
            onOpenSettings = onOpenSettings,
            onPickWallpaper = onPickWallpaper,
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
private fun StatusAndClock(modifier: Modifier = Modifier) {
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
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .height(38.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0x8C171A20))
                .border(1.dp, Color(0x18FFFFFF), RoundedCornerShape(20.dp))
                .padding(horizontal = 14.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.Wifi,
                contentDescription = "Wi-Fi",
                tint = if (isOnline) KtvColors.TextPrimary else KtvColors.TextSecondary,
                modifier = Modifier.size(20.dp),
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(22.dp)
                    .border(1.dp, KtvColors.TextSecondary, CircleShape),
            ) {
                Text("11", color = KtvColors.TextPrimary, fontSize = 10.sp)
            }
            Icon(
                imageVector = Icons.Rounded.NotificationsNone,
                contentDescription = "Powiadomienia",
                tint = KtvColors.TextPrimary,
                modifier = Modifier.size(20.dp),
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
    recent: List<LaunchableApp>,
    favorites: List<LaunchableApp>,
    onLaunchApp: (LaunchableApp) -> Unit,
    modifier: Modifier = Modifier,
) {
    val firstFocusRequester = remember { FocusRequester() }
    LaunchedEffect(recent.firstOrNull()?.stableKey) {
        if (recent.isNotEmpty()) firstFocusRequester.requestFocus()
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
                recent.take(2).forEachIndexed { index, app ->
                    ContinueCard(
                        app = app,
                        onClick = { onLaunchApp(app) },
                        modifier = if (index == 0) {
                            Modifier.focusRequester(firstFocusRequester)
                        } else {
                            Modifier
                        },
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
                    FavoriteTile(app = app, onClick = { onLaunchApp(app) })
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
private fun ContinueCard(
    app: LaunchableApp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
            Image(
                bitmap = app.icon,
                contentDescription = app.label,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 14.dp, bottom = 18.dp)
                    .size(42.dp),
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(Color(0xB3090B0F))
                    .padding(horizontal = 9.dp, vertical = 5.dp),
            ) {
                Text(
                    text = app.label,
                    color = KtvColors.TextPrimary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = if (focused) "Naciśnij OK" else "Wróć do aplikacji",
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
        }
    }
}

@Composable
private fun FavoriteTile(app: LaunchableApp, onClick: () -> Unit) {
    FocusableSurface(
        onClick = onClick,
        focusedScale = 1.08f,
        shape = RoundedCornerShape(13.dp),
        modifier = Modifier.size(54.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xD91A1E25))
                .padding(7.dp),
        ) {
            Image(
                bitmap = app.icon,
                contentDescription = app.label,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun Dock(
    shortcuts: List<LaunchableApp>,
    onOpenSettings: () -> Unit,
    onPickWallpaper: () -> Unit,
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
        DockAction("Ustawienia", Icons.Rounded.Settings, onOpenSettings, Modifier.weight(1f))
        DockAction("Tapeta", Icons.Rounded.Wallpaper, onPickWallpaper, Modifier.weight(1f))
        DockAction("Aplikacje", Icons.Rounded.Apps, onOpenApps, Modifier.weight(1f))
        DockAction("Informacje", Icons.Rounded.Info, onOpenInfo, Modifier.weight(1f))
        shortcuts.getOrNull(0)?.let { app ->
            DockShortcut(app, { onLaunchApp(app) }, Modifier.weight(1f))
        }
        shortcuts.getOrNull(1)?.let { app ->
            DockShortcut(app, { onLaunchApp(app) }, Modifier.weight(1f))
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
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .background(if (focused) Color(0xD9272C34) else Color(0xA8171A20))
                .padding(horizontal = 12.dp),
        ) {
            Image(
                bitmap = app.icon,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.width(9.dp))
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
private fun AllAppsScreen(
    apps: List<LaunchableApp>,
    onLaunchApp: (LaunchableApp) -> Unit,
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
    modifier: Modifier = Modifier,
) {
    FocusableSurface(
        onClick = onClick,
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
    focusedScale: Float,
    shape: RoundedCornerShape,
    modifier: Modifier = Modifier,
    content: @Composable (focused: Boolean) -> Unit,
) {
    var focused by remember { mutableStateOf(false) }
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
                if (activationKey && event.type == KeyEventType.KeyUp) {
                    onClick()
                    true
                } else {
                    false
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
