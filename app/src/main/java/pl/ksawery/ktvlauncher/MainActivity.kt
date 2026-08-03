package pl.ksawery.ktvlauncher

import android.content.ContentValues
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
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
                    onExportProfile = {
                        val exported = exportProfile(preferences.exportProfile())
                        Toast.makeText(
                            this,
                            if (exported) "Profil zapisany w Pobrane/KSTV" else "Nie udało się zapisać profilu",
                            Toast.LENGTH_LONG,
                        ).show()
                    },
                    onImportProfile = {
                        val profile = importLatestProfile()
                        val imported = profile != null && preferences.importProfile(profile)
                        if (imported) homeViewModel.reloadProfile()
                        Toast.makeText(
                            this,
                            if (imported) "Profil został wczytany" else "Nie znaleziono profilu w Pobrane/KSTV",
                            Toast.LENGTH_LONG,
                        ).show()
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

    private fun exportProfile(profile: String): Boolean {
        val values = ContentValues().apply {
            put(
                MediaStore.MediaColumns.DISPLAY_NAME,
                "$PROFILE_PREFIX${System.currentTimeMillis()}.json",
            )
            put(MediaStore.MediaColumns.MIME_TYPE, "application/json")
            put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                "${Environment.DIRECTORY_DOWNLOADS}/KSTV",
            )
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
        val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            ?: return false
        return runCatching {
            contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { writer ->
                writer.write(profile)
            } ?: error("No output stream")
            values.clear()
            values.put(MediaStore.MediaColumns.IS_PENDING, 0)
            contentResolver.update(uri, values, null, null)
            true
        }.getOrElse {
            contentResolver.delete(uri, null, null)
            false
        }
    }

    private fun importLatestProfile(): String? = runCatching {
        val projection = arrayOf(MediaStore.MediaColumns._ID)
        val selection = "${MediaStore.MediaColumns.DISPLAY_NAME} LIKE ?"
        val selectionArgs = arrayOf("$PROFILE_PREFIX%")
        val sortOrder = "${MediaStore.MediaColumns.DATE_MODIFIED} DESC"
        contentResolver.query(
            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder,
        )?.use { cursor ->
            if (!cursor.moveToFirst()) return@use null
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
            val uri = Uri.withAppendedPath(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                id.toString(),
            )
            contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
        }
    }.getOrNull()

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
        const val PROFILE_PREFIX = "kstv-launcher-profile-"
    }
}
