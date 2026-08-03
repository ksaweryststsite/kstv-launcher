package pl.ksawery.ktvlauncher

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import pl.ksawery.ktvlauncher.data.AndroidAppRepository
import pl.ksawery.ktvlauncher.data.LauncherPreferences
import pl.ksawery.ktvlauncher.data.WatchNextRepository
import pl.ksawery.ktvlauncher.data.WeatherRepository
import pl.ksawery.ktvlauncher.domain.AppLauncher
import pl.ksawery.ktvlauncher.ui.home.HomeRoute
import pl.ksawery.ktvlauncher.ui.home.HomeViewModel
import pl.ksawery.ktvlauncher.ui.home.HomeViewModelFactory
import pl.ksawery.ktvlauncher.ui.theme.KtvLauncherTheme

class MainActivity : ComponentActivity() {
    private lateinit var homeViewModel: HomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureFullscreen()

        val appRepository = AndroidAppRepository(packageManager, packageName)
        val preferences = LauncherPreferences(this)
        val viewModelFactory = HomeViewModelFactory(
            appRepository = appRepository,
            weatherRepository = WeatherRepository(),
            watchNextRepository = WatchNextRepository(this),
            preferences = preferences,
        )
        val appLauncher = AppLauncher(this)
        homeViewModel = ViewModelProvider(this, viewModelFactory)[HomeViewModel::class.java]

        setContent {
            val wallpaperPicker = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.OpenDocument(),
            ) { uri ->
                uri?.takeIf(::isValidWallpaper)?.let {
                    runCatching {
                        contentResolver.takePersistableUriPermission(
                            it,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION,
                        )
                    }
                    homeViewModel.setWallpaperUri(it.toString())
                }
            }
            val profileExporter = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.CreateDocument("application/json"),
            ) { uri ->
                uri?.let {
                    runCatching {
                        contentResolver.openOutputStream(it)?.bufferedWriter()?.use { writer ->
                            writer.write(preferences.exportProfile())
                        }
                    }
                }
            }
            val profileImporter = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.OpenDocument(),
            ) { uri ->
                uri?.let {
                    val profile = runCatching {
                        contentResolver.openInputStream(it)?.bufferedReader()?.use { reader ->
                            reader.readText()
                        }
                    }.getOrNull()
                    if (profile != null && preferences.importProfile(profile)) {
                        homeViewModel.reloadProfile()
                    }
                }
            }
            val tvListingsPermission = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission(),
            ) {
                homeViewModel.refreshWatchNext()
            }
            KtvLauncherTheme {
                HomeRoute(
                    viewModel = homeViewModel,
                    onLaunchApp = { app ->
                        homeViewModel.recordLaunch(app)
                        appLauncher.launch(app)
                    },
                    onPickWallpaper = { wallpaperPicker.launch(arrayOf("image/*")) },
                    onExportProfile = { profileExporter.launch("kstv-launcher-profile.json") },
                    onImportProfile = {
                        profileImporter.launch(arrayOf("application/json", "text/plain"))
                    },
                    onOpenSettings = { openSystemSettings(Settings.ACTION_SETTINGS) },
                    onOpenWifi = { openSystemSettings(Settings.ACTION_WIFI_SETTINGS) },
                    onOpenNotifications = { openSystemSettings(ACTION_NOTIFICATION_SETTINGS) },
                    onOpenAppInfo = { app ->
                        startActivity(
                            Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.parse("package:${app.componentName.packageName}"),
                            ),
                        )
                    },
                    onUninstallApp = { app ->
                        startActivity(
                            Intent(
                                Intent.ACTION_DELETE,
                                Uri.parse("package:${app.componentName.packageName}"),
                            ),
                        )
                    },
                    onLaunchWatchNext = { item ->
                        item.intentUri?.let { intentUri ->
                            runCatching {
                                startActivity(Intent.parseUri(intentUri, Intent.URI_INTENT_SCHEME))
                            }
                        }
                    },
                    onRequestWatchNextAccess = {
                        tvListingsPermission.launch(READ_TV_LISTINGS_PERMISSION)
                    },
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        configureFullscreen()
        if (::homeViewModel.isInitialized) {
            homeViewModel.refreshApps()
            homeViewModel.refreshWeather()
        }
    }

    private fun isValidWallpaper(uri: Uri): Boolean = runCatching {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        }
        options.outWidth >= 640 && options.outHeight >= 360
    }.getOrDefault(false)

    private fun configureFullscreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun openSystemSettings(action: String) {
        runCatching { startActivity(Intent(action)) }
            .onFailure { startActivity(Intent(Settings.ACTION_SETTINGS)) }
    }

    private companion object {
        const val ACTION_NOTIFICATION_SETTINGS = "android.settings.NOTIFICATION_SETTINGS"
        const val READ_TV_LISTINGS_PERMISSION = "android.permission.READ_TV_LISTINGS"
    }
}
