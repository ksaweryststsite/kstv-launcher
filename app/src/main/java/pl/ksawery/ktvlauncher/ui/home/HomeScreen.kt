package pl.ksawery.ktvlauncher.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.graphicsLayer
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay
import pl.ksawery.ktvlauncher.R
import pl.ksawery.ktvlauncher.model.LaunchableApp
import pl.ksawery.ktvlauncher.ui.theme.KtvColors

@Composable
fun HomeRoute(
    viewModel: HomeViewModel,
    onLaunchApp: (LaunchableApp) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        uiState = uiState,
        onLaunchApp = onLaunchApp,
    )
}

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onLaunchApp: (LaunchableApp) -> Unit,
) {
    BackHandler(enabled = true) { }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(
                        KtvColors.BackgroundWarm,
                        KtvColors.Background,
                        KtvColors.BackgroundCool,
                    ),
                ),
            ),
    ) {
        when (uiState) {
            HomeUiState.Loading -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = KtvColors.Accent,
            )

            is HomeUiState.Error -> EmptyMessage(
                text = stringResource(R.string.empty_apps),
                modifier = Modifier.align(Alignment.Center),
            )

            is HomeUiState.Ready -> LauncherContent(
                apps = uiState.apps,
                onLaunchApp = onLaunchApp,
            )
        }
    }
}

@Composable
private fun LauncherContent(
    apps: List<LaunchableApp>,
    onLaunchApp: (LaunchableApp) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 56.dp, vertical = 32.dp),
    ) {
        Header()
        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.apps_section_title),
            color = KtvColors.TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Medium,
        )
        Spacer(Modifier.height(16.dp))

        if (apps.isEmpty()) {
            EmptyMessage(stringResource(R.string.empty_apps))
        } else {
            AppGrid(apps, onLaunchApp)
        }
    }
}

@Composable
private fun Header() {
    val formatter = remember { DateTimeFormatter.ofPattern("HH:mm", Locale("pl", "PL")) }
    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale("pl", "PL"))
    }
    var now by remember { mutableStateOf(LocalDateTime.now()) }

    LaunchedEffect(Unit) {
        while (true) {
            now = LocalDateTime.now()
            delay(30_000)
        }
    }

    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp),
    ) {
        Column {
            Text(
                text = greetingFor(now.hour),
                color = KtvColors.TextSecondary,
                fontSize = 18.sp,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Miłego oglądania!",
                color = KtvColors.TextPrimary,
                fontSize = 34.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Spacer(Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = now.format(formatter),
                color = KtvColors.TextPrimary,
                fontSize = 38.sp,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = now.format(dateFormatter).replaceFirstChar(Char::uppercase),
                color = KtvColors.TextSecondary,
                fontSize = 16.sp,
            )
        }
    }
}

@Composable
private fun AppGrid(
    apps: List<LaunchableApp>,
    onLaunchApp: (LaunchableApp) -> Unit,
) {
    val firstFocusRequester = remember { FocusRequester() }

    LaunchedEffect(apps.firstOrNull()?.stableKey) {
        if (apps.isNotEmpty()) firstFocusRequester.requestFocus()
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(8),
        horizontalArrangement = Arrangement.spacedBy(22.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        contentPadding = PaddingValues(start = 6.dp, end = 6.dp, bottom = 28.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(
            items = apps,
            key = LaunchableApp::stableKey,
        ) { app ->
            AppTile(
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

@Composable
private fun AppTile(
    app: LaunchableApp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (focused) 1.09f else 1f,
        animationSpec = spring(stiffness = 520f, dampingRatio = 0.78f),
        label = "appTileScale",
    )
    val borderColor = if (focused) KtvColors.Accent else KtvColors.Border

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .height(118.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                shadowElevation = if (focused) 18.dp.toPx() else 4.dp.toPx()
                shape = RoundedCornerShape(20.dp)
                clip = false
            }
            .onFocusChanged { focused = it.isFocused }
            .onKeyEvent { event ->
                val isActivationKey = event.key == Key.DirectionCenter ||
                    event.key == Key.Enter ||
                    event.key == Key.NumPadEnter
                if (isActivationKey && event.type == KeyEventType.KeyUp) {
                    onClick()
                    true
                } else {
                    false
                }
            }
            .focusable(),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(82.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(if (focused) KtvColors.TileFocused else KtvColors.Tile)
                .border(1.dp, borderColor, RoundedCornerShape(18.dp))
                .padding(10.dp),
        ) {
            Image(
                bitmap = app.icon,
                contentDescription = app.label,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = app.label,
            color = if (focused) KtvColors.TextPrimary else KtvColors.TextSecondary,
            fontSize = 14.sp,
            fontWeight = if (focused) FontWeight.Medium else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun EmptyMessage(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = KtvColors.TextSecondary,
        fontSize = 18.sp,
        modifier = modifier,
    )
}

internal fun greetingFor(hour: Int): String = when (hour) {
    in 5..11 -> "Dzień dobry"
    in 12..17 -> "Dobre popołudnie"
    else -> "Dobry wieczór"
}
