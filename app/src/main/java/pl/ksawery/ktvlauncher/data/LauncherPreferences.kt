package pl.ksawery.ktvlauncher.data

import android.content.ComponentName
import android.content.Context

class LauncherPreferences(context: Context) {
    private val preferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    fun recentComponents(): List<ComponentName> = readComponentList(KEY_RECENT)

    fun recordLaunch(componentName: ComponentName) {
        val current = recentComponents()
            .filterNot { it == componentName }
            .toMutableList()
            .apply { add(0, componentName) }
            .take(MAX_RECENT)
        writeComponentList(KEY_RECENT, current)
    }

    fun favoriteComponents(): List<ComponentName> = readComponentList(KEY_FAVORITES)

    fun setFavoriteComponents(components: List<ComponentName>) {
        writeComponentList(KEY_FAVORITES, components.take(MAX_FAVORITES))
    }

    fun dockComponents(): List<ComponentName> = readComponentList(KEY_DOCK)

    fun setDockComponents(components: List<ComponentName>) {
        writeComponentList(KEY_DOCK, components.take(MAX_DOCK_SHORTCUTS))
    }

    fun continueWatchingEnabled(): Boolean =
        preferences.getBoolean(KEY_CONTINUE_WATCHING, true)

    fun setContinueWatchingEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_CONTINUE_WATCHING, enabled).apply()
    }

    fun wallpaperUri(): String? = preferences.getString(KEY_WALLPAPER_URI, null)

    fun setWallpaperUri(uri: String) {
        preferences.edit().putString(KEY_WALLPAPER_URI, uri).apply()
    }

    private fun readComponentList(key: String): List<ComponentName> = preferences
        .getString(key, null)
        ?.lineSequence()
        ?.mapNotNull(ComponentName::unflattenFromString)
        ?.toList()
        .orEmpty()

    private fun writeComponentList(key: String, components: List<ComponentName>) {
        val value = components.joinToString("\n", transform = ComponentName::flattenToShortString)
        preferences.edit().putString(key, value).apply()
    }

    private companion object {
        const val FILE_NAME = "launcher_preferences"
        const val KEY_RECENT = "recent_components_v2"
        const val KEY_FAVORITES = "favorite_components"
        const val KEY_DOCK = "dock_components"
        const val KEY_CONTINUE_WATCHING = "continue_watching_enabled"
        const val KEY_WALLPAPER_URI = "wallpaper_uri"
        const val MAX_RECENT = 8
        const val MAX_FAVORITES = 8
        const val MAX_DOCK_SHORTCUTS = 3
    }
}
