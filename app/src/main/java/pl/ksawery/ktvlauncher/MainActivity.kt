package pl.ksawery.ktvlauncher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import pl.ksawery.ktvlauncher.data.AndroidAppRepository
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

        val repository = AndroidAppRepository(
            packageManager = packageManager,
            ownPackageName = packageName,
        )
        val appLauncher = AppLauncher(this)
        val viewModelFactory = HomeViewModelFactory(repository)
        homeViewModel = ViewModelProvider(this, viewModelFactory)[HomeViewModel::class.java]

        setContent {
            KtvLauncherTheme {
                HomeRoute(
                    viewModel = homeViewModel,
                    onLaunchApp = appLauncher::launch,
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        configureFullscreen()
        if (::homeViewModel.isInitialized) {
            homeViewModel.refreshApps()
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
