package pl.ksawery.ktvlauncher.data

import android.content.ComponentName
import android.content.Context

class LauncherPreferences(context: Context) {
    private val preferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    fun recentComponents(): List<ComponentName> = preferences
        .getStringSet(KEY_RECENT, emptySet())
        .orEmpty()
        .sortedBy { entry -> entry.substringBefore('|').toIntOrNull() ?: Int.MAX_VALUE }
        .mapNotNull { entry -> ComponentName.unflattenFromString(entry.substringAfter('|')) }

    fun recordLaunch(componentName: ComponentName) {
        val current = recentComponents()
            .filterNot { it == componentName }
            .toMutableList()
            .apply { add(0, componentName) }
            .take(MAX_RECENT)

        val encoded = current
            .mapIndexed { index, component -> "$index|${component.flattenToShortString()}" }
            .toSet()
        preferences.edit().putStringSet(KEY_RECENT, encoded).apply()
    }

    fun wallpaperUri(): String? = preferences.getString(KEY_WALLPAPER_URI, null)

    fun setWallpaperUri(uri: String) {
        preferences.edit().putString(KEY_WALLPAPER_URI, uri).apply()
    }

    private companion object {
        const val FILE_NAME = "launcher_preferences"
        const val KEY_RECENT = "recent_components"
        const val KEY_WALLPAPER_URI = "wallpaper_uri"
        const val MAX_RECENT = 8
    }
}

