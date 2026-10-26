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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import pl.ksawery.ktvlauncher.model.HomeFocusMode
import pl.ksawery.ktvlauncher.model.LaunchableApp
import pl.ksawery.ktvlauncher.model.WatchNextItem

// Fourth KSTV theme. Native Compose UI with no Google TV platform dependencies.
// Content is only sourced from installed applications and Android TV Watch Next.
@Composable
internal fun GoogleTvDashboard(
    homeFocusRequest: Int,
    uiState: HomeUiState,
    onLaunchApp: (LaunchableApp) -> Unit,
    onOpenApps: () -> Unit,
    onOpenLauncherSettings: () -> Unit,
    onLaunchWatchNext: (WatchNextItem) -> Unit,
    onLongClickApp: (LaunchableApp) -> Unit,
) {
    val pinned = (uiState.favorites.ifEmpty { uiState.apps.take(12) }).distinctBy { it.stableKey }
    val firstAppFocus = remember { FocusRequester() }
    var now by remember { mutableStateOf(LocalDateTime.now()) }

    LaunchedEffect(Unit) {
        while (true) {
            now = LocalDateTime.now()
            delay(60_000)
        }
    }
    LaunchedEffect(homeFocusRequest, pinned.size) {
        if (pinned.isNotEmpty() &&
            homeFocusRequest > 0 &&
            uiState.homeFocusMode != HomeFocusMode.KeepCurrent
        ) {
            delay(100)
            firstAppFocus.requestFocus()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF101D30), Color(0xFF101B29), Color(0xFF080E17)),
                ),
            ),
    ) {
        // Navigation, clock and system settings.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(67.dp)
                .padding(start = 36.dp, end = 37.dp, top = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("G", color = Color(0xFFE6EBF6), fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(19.dp))
            GtvTopPill(label = "Dla Ciebie", selected = true, onClick = {})
            Spacer(Modifier.width(8.dp))
            GtvTopPill(label = "Aplikacje", selected = false, onClick = onOpenApps)
            Spacer(Modifier.width(8.dp))
            GtvTopPill(label = "Biblioteka", selected = false, onClick = onOpenApps)
            Spacer(Modifier.weight(1f))
            Text(
                text = now.format(DateTimeFormatter.ofPattern("HH:mm")),
                fontSize = 13.sp,
                color = Color(0xFFCBD5E3),
            )
            Spacer(Modifier.width(15.dp))
            GtvFocusSurface(
                onClick = onOpenLauncherSettings,
                shape = CircleShape,
                modifier = Modifier.size(36.dp),
            ) { focused ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (focused) Color(0xFF56677C) else Color(0x60313E50),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        tint = Color.White,
                        contentDescription = "Ustawienia KSTV",
                        modifier = Modifier.size(19.dp),
                    )
                }
            }
        }

        // Spotlight changes from real Android TV Watch Next entries when available.
        GtvHero(
            watchNext = uiState.watchNext.firstOrNull(),
            pinned = pinned,
            onOpenApps = onOpenApps,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(start = 36.dp, end = 36.dp, top = 1.dp, bottom = 11.dp),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 39.dp, end = 39.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Twoje aplikacje", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Medium)
            Spacer(Modifier.weight(1f))
            Text("Google TV — wygląd", color = Color(0xFF8495AC), fontSize = 10.sp)
        }

        LazyRow(
            contentPadding = PaddingValues(start = 38.dp, end = 38.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(92.dp),
        ) {
            items(pinned, key = { it.stableKey }) { app ->
                val isFirst = pinned.firstOrNull()?.stableKey == app.stableKey
                GtvAppTile(
                    app = app,
                    onClick = { onLaunchApp(app) },
                    onLongClick = { onLongClickApp(app) },
                    modifier = if (isFirst) Modifier.focusRequester(firstAppFocus) else Modifier,
                )
            }
            item {
                GtvFocusSurface(
                    onClick = onOpenApps,
                    shape = CircleShape,
                    modifier = Modifier.size(62.dp),
                ) { focused ->
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(if (focused) Color(0xFFDCE7F8) else Color(0xFF2D415A)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Rounded.Apps,
                            contentDescription = "Wszystkie aplikacje",
                            tint = if (focused) Color(0xFF131B2C) else Color.White,
                        )
                    }
                }
            }
        }

        val hasWatchNext = uiState.watchNext.isNotEmpty()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 39.dp, end = 39.dp, top = 2.dp, bottom = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                if (hasWatchNext) "Kontynuuj oglądanie" else "Ostatnio uruchamiane",
                fontSize = 16.sp,
                color = Color(0xFFF4F7FC),
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.weight(1f))
            Text("KSTV", fontSize = 11.sp, color = Color(0xFF8EA3BD))
        }
        if (hasWatchNext) {
            LazyRow(
                contentPadding = PaddingValues(start = 38.dp, end = 38.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(11.dp),
                modifier = Modifier.fillMaxWidth().height(118.dp),
            ) {
                items(uiState.watchNext.take(15)) { item ->
                    GtvWatchCard(
                        item = item,
                        onClick = {
                            if (!item.intentUri.isNullOrBlank()) {
                                onLaunchWatchNext(item)
                            } else {
                                val app = uiState.apps.firstOrNull {
                                    it.componentName.packageName == item.packageName
                                }
                                if (app != null) onLaunchApp(app) else onOpenApps()
                            }
                        },
                    )
                }
            }
        } else {
            LazyRow(
                contentPadding = PaddingValues(start = 38.dp, end = 38.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(11.dp),
                modifier = Modifier.fillMaxWidth().height(118.dp),
            ) {
                items(uiState.recent.ifEmpty { uiState.apps.take(7) }, key = { it.stableKey }) { app ->
                    GtvRecentCard(app, onClick = { onLaunchApp(app) })
                }
            }
        }
    }
}

@Composable
private fun GtvHero(
    watchNext: WatchNextItem?,
    pinned: List<LaunchableApp>,
    onOpenApps: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val artwork = GtvPoster(watchNext?.posterUri)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(19.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(Color(0xFF17263D), Color(0xFF283C5D), Color(0xFF395D81)),
                ),
            ),
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(0.55f)
                    .padding(start = 28.dp, top = 26.dp, bottom = 18.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    "NA TWOIM EKRANIE",
                    color = Color(0xFFAEC6E9),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = watchNext?.title ?: "Wszystko, co lubisz oglądać.",
                    color = Color.White,
                    fontSize = 27.sp,
                    lineHeight = 32.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(9.dp))
                Text(
                    text = watchNext?.description?.take(135)
                        ?: "Twoje aplikacje, filmy i ulubione treści w jednym miejscu.",
                    color = Color(0xFFCFD9E7),
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(16.dp))
                GtvFocusSurface(
                    onClick = onOpenApps,
                    shape = RoundedCornerShape(30.dp),
                    modifier = Modifier.width(168.dp).height(39.dp),
                ) { focused ->
                    Box(
                        modifier = Modifier.fillMaxSize()
                            .background(if (focused) Color.White else Color(0xFFDFE8F4)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "Otwórz aplikacje",
                            color = Color(0xFF141F2E),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                        )
                    }
                }
            }
            Box(
                modifier = Modifier.weight(0.45f).fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                if (artwork != null) {
                    Image(
                        bitmap = artwork,
                        contentDescription = watchNext?.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                    Box(
                        Modifier.fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF243958), Color.Transparent),
                                ),
                            ),
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(end = 22.dp),
                    ) {
                        pinned.take(3).forEachIndexed { index, app ->
                            Box(
                                modifier = Modifier
                                    .size(if (index == 1) 93.dp else 76.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(
                                        if (index == 1) Color(0xFF6881A6)
                                        else Color(0xA92B405B),
                                    )
                                    .border(
                                        1.dp,
                                        Color(0x44FFFFFF),
                                        RoundedCornerShape(18.dp),
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Image(
                                    app.icon,
                                    contentDescription = app.label,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.size(if (index == 1) 69.dp else 53.dp)
                                        .clip(RoundedCornerShape(13.dp)),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GtvAppTile(
    app: LaunchableApp,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Short OK launches the app. Long OK can be added through KSTV's context menu.
    // The icon itself is the actual Android-installed app icon.
    Column(
        modifier = modifier.width(66.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        GtvFocusSurface(
            onClick = onClick,
            shape = CircleShape,
            modifier = Modifier.size(62.dp),
        ) { focused ->
            Box(
                modifier = Modifier.fillMaxSize()
                    .background(if (focused) Color(0xFFEAF0FB) else Color(0xFF30435B)),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    bitmap = app.icon,
                    contentDescription = app.label,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(15.dp)),
                )
            }
        }
        Spacer(Modifier.height(5.dp))
        Text(
            app.label,
            fontSize = 10.sp,
            color = Color(0xFFDDE5F1),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun GtvWatchCard(item: WatchNextItem, onClick: () -> Unit) {
    val artwork = GtvPoster(item.posterUri)
    GtvFocusSurface(
        onClick = onClick,
        shape = RoundedCornerShape(13.dp),
        modifier = Modifier.width(174.dp).height(98.dp),
    ) { focused ->
        Box(
            Modifier.fillMaxSize().background(
                Brush.linearGradient(
                    listOf(Color(0xFF3C6387), Color(0xFF16263D), Color(0xFF131B2A)),
                ),
            ),
        ) {
            if (artwork != null) {
                Image(
                    bitmap = artwork,
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Box(
                modifier = Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color(0xB7000915)),
                    ),
                ),
            )
            Text(
                item.title,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.align(Alignment.BottomStart).padding(11.dp),
            )
            item.progressPercent?.let { progress ->
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .height(3.dp)
                        .align(Alignment.BottomStart)
                        .background(Color(0x5595A5B9)),
                )
                Box(
                    modifier = Modifier.fillMaxWidth(progress.coerceIn(0f, 1f))
                        .height(3.dp)
                        .align(Alignment.BottomStart)
                        .background(if (focused) Color.White else Color(0xFFAAC7F5)),
                )
            }
        }
    }
}

@Composable
private fun GtvRecentCard(app: LaunchableApp, onClick: () -> Unit) {
    GtvFocusSurface(
        onClick = onClick,
        shape = RoundedCornerShape(13.dp),
        modifier = Modifier.width(174.dp).height(98.dp),
    ) { _ ->
        Box(
            modifier = Modifier.fillMaxSize()
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF364E6E), Color(0xFF1D2A3D)),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    bitmap = app.icon,
                    contentDescription = app.label,
                    modifier = Modifier.size(44.dp).clip(RoundedCornerShape(10.dp)),
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    app.label,
                    color = Color.White,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun GtvTopPill(label: String, selected: Boolean, onClick: () -> Unit) {
    GtvFocusSurface(
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier.width(if (selected) 91.dp else 86.dp).height(34.dp),
    ) { focused ->
        Box(
            modifier = Modifier.fillMaxSize().background(
                if (focused) Color(0xFFF1F5FF)
                else if (selected) Color(0xFF334967)
                else Color.Transparent,
            ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                label,
                color = if (focused) Color(0xFF142338) else Color.White,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun GtvFocusSurface(
    onClick: () -> Unit,
    shape: Shape,
    modifier: Modifier = Modifier,
    content: @Composable (focused: Boolean) -> Unit,
) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (focused) 1.07f else 1f,
        animationSpec = tween(durationMillis = 160),
        label = "googleTvFocus",
    )
    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .onFocusChanged { focused = it.isFocused }
            .onKeyEvent { event ->
                val ok = event.key == Key.DirectionCenter ||
                    event.key == Key.Enter || event.key == Key.NumPadEnter
                if (ok && event.type == KeyEventType.KeyUp) {
                    onClick()
                    true
                } else {
                    ok && event.type == KeyEventType.KeyDown
                }
            }
            .focusable(),
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
                .clip(shape)
                .border(
                    width = if (focused) 2.5.dp else 0.dp,
                    color = if (focused) Color.White else Color.Transparent,
                    shape = shape,
                ),
        ) {
            content(focused)
        }
    }
}

@Composable
private fun GtvPoster(rawUri: String?): ImageBitmap? {
    val context = LocalContext.current
    val poster by produceState<ImageBitmap?>(initialValue = null, key1 = rawUri) {
        value = rawUri?.let { uri ->
            withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openInputStream(Uri.parse(uri))?.use { stream ->
                        val options = BitmapFactory.Options().apply {
                            inPreferredConfig = BitmapFactory.Config.RGB_565
                            inSampleSize = 2
                        }
                        BitmapFactory.decodeStream(stream, null, options)?.asImageBitmap()
                    }
                }.getOrNull()
            }
        }
    }
    return poster
}
