package pl.ksawery.ktvlauncher

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.remember
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import pl.ksawery.ktvlauncher.data.AndroidAppRepository
import pl.ksawery.ktvlauncher.data.LauncherPreferences
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

        val appRepository = AndroidAppRepository(
            packageManager = packageManager,
            ownPackageName = packageName,
        )
        val preferences = LauncherPreferences(this)
        val viewModelFactory = HomeViewModelFactory(
            appRepository = appRepository,
            weatherRepository = WeatherRepository(),
            preferences = preferences,
        )
        val appLauncher = AppLauncher(this)
        homeViewModel = ViewModelProvider(this, viewModelFactory)[HomeViewModel::class.java]

        setContent {
            val wallpaperPicker = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.OpenDocument(),
            ) { uri ->
                uri?.let {
                    runCatching {
                        contentResolver.takePersistableUriPermission(
                            it,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION,
                        )
                    }
                    homeViewModel.setWallpaperUri(it.toString())
                }
            }
            val openSettings = remember(this) {
                { startActivity(Intent(Settings.ACTION_SETTINGS)) }
            }

            KtvLauncherTheme {
                HomeRoute(
                    viewModel = homeViewModel,
                    onLaunchApp = { app ->
                        homeViewModel.recordLaunch(app)
                        appLauncher.launch(app)
                    },
                    onPickWallpaper = { wallpaperPicker.launch(arrayOf("image/*")) },
                    onOpenSettings = openSettings,
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

    private fun configureFullscreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}
