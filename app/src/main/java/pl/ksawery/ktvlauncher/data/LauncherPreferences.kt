package pl.ksawery.ktvlauncher.data

import android.content.ComponentName
import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import pl.ksawery.ktvlauncher.model.HomeFocusMode
import pl.ksawery.ktvlauncher.model.LauncherThemeMode
import pl.ksawery.ktvlauncher.model.ShelfMode
import pl.ksawery.ktvlauncher.model.UiScale

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

    fun featuredComponents(): List<ComponentName> = readComponentList(KEY_FEATURED)

    fun setFeaturedComponents(components: List<ComponentName>) {
        writeComponentList(KEY_FEATURED, components.take(MAX_FEATURED_APPS))
    }

    fun appOrderComponents(): List<ComponentName> = readComponentList(KEY_APP_ORDER)

    fun setAppOrderComponents(components: List<ComponentName>) {
        writeComponentList(KEY_APP_ORDER, components)
    }

    fun shelfMode(): ShelfMode = runCatching {
        ShelfMode.valueOf(preferences.getString(KEY_SHELF_MODE, null).orEmpty())
    }.getOrDefault(ShelfMode.WatchNext)

    fun setShelfMode(mode: ShelfMode) {
        preferences.edit().putString(KEY_SHELF_MODE, mode.name).apply()
    }

    fun homeFocusMode(): HomeFocusMode = runCatching {
        HomeFocusMode.valueOf(preferences.getString(KEY_HOME_FOCUS_MODE, null).orEmpty())
    }.getOrDefault(HomeFocusMode.KeepCurrent)

    fun setHomeFocusMode(mode: HomeFocusMode) {
        preferences.edit().putString(KEY_HOME_FOCUS_MODE, mode.name).apply()
    }

    fun continueWatchingEnabled(): Boolean =
        preferences.getBoolean(KEY_CONTINUE_WATCHING, true)

    fun setContinueWatchingEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_CONTINUE_WATCHING, enabled).apply()
    }

    fun watchNextSourcePackage(): String? =
        preferences.getString(KEY_WATCH_NEXT_SOURCE, null)?.takeIf(String::isNotBlank)

    fun setWatchNextSourcePackage(packageName: String?) {
        preferences.edit().apply {
            if (packageName == null) remove(KEY_WATCH_NEXT_SOURCE)
            else putString(KEY_WATCH_NEXT_SOURCE, packageName)
        }.apply()
    }

    fun dockBackgroundEnabled(): Boolean =
        preferences.getBoolean(KEY_DOCK_BACKGROUND, true)

    fun setDockBackgroundEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_DOCK_BACKGROUND, enabled).apply()
    }

    fun uiScale(): UiScale = runCatching {
        UiScale.valueOf(preferences.getString(KEY_UI_SCALE, null).orEmpty())
    }.getOrDefault(UiScale.Auto)

    fun setUiScale(scale: UiScale) {
        preferences.edit().putString(KEY_UI_SCALE, scale.name).apply()
    }

    fun launcherTheme(): LauncherThemeMode = runCatching {
        LauncherThemeMode.valueOf(preferences.getString(KEY_LAUNCHER_THEME, null).orEmpty())
    }.getOrDefault(LauncherThemeMode.Theme1)

    fun setLauncherTheme(theme: LauncherThemeMode) {
        preferences.edit().putString(KEY_LAUNCHER_THEME, theme.name).apply()
    }

    fun themeName(theme: LauncherThemeMode): String = preferences.getString(
        themeNameKey(theme),
        themeDefaultName(theme),
    ).orEmpty()

    fun setThemeName(theme: LauncherThemeMode, name: String) {
        preferences.edit().putString(
            themeNameKey(theme),
            name.trim().take(24).ifBlank { themeDefaultName(theme) },
        ).apply()
    }

    fun mediaWidgetEnabled(): Boolean = preferences.getBoolean(KEY_MEDIA_WIDGET, false)

    fun setMediaWidgetEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_MEDIA_WIDGET, enabled).apply()
    }

    fun wallpaperUri(theme: LauncherThemeMode): String? {
        val scoped = preferences.getString(wallpaperKey(theme), null)
        return scoped ?: if (theme == LauncherThemeMode.Theme1) {
            preferences.getString(KEY_WALLPAPER_URI_LEGACY, null)
        } else {
            null
        }
    }

    fun setWallpaperUri(theme: LauncherThemeMode, uri: String) {
        preferences.edit().putString(wallpaperKey(theme), uri).apply()
    }

    fun resetWallpaper(theme: LauncherThemeMode) {
        preferences.edit().remove(wallpaperKey(theme)).apply()
    }

    fun exportProfile(): String = JSONObject().apply {
        put("version", PROFILE_VERSION)
        put("favorites", componentArray(favoriteComponents()))
        put("dock", componentArray(dockComponents()))
        put("featured", componentArray(featuredComponents()))
        put("appOrder", componentArray(appOrderComponents()))
        put("shelfMode", shelfMode().name)
        put("homeFocusMode", homeFocusMode().name)
        put("continueWatching", continueWatchingEnabled())
        put("watchNextSource", watchNextSourcePackage())
        put("dockBackground", dockBackgroundEnabled())
        put("uiScale", uiScale().name)
        put("launcherTheme", launcherTheme().name)
        put("themeOneName", themeName(LauncherThemeMode.Theme1))
        put("themeTwoName", themeName(LauncherThemeMode.Theme2))
        put("themeThreeName", themeName(LauncherThemeMode.Theme3))
        put("mediaWidget", mediaWidgetEnabled())
    }.toString(2)

    fun importProfile(json: String): Boolean = runCatching {
        val profile = JSONObject(json)
        setFavoriteComponents(profile.componentList("favorites"))
        setDockComponents(profile.componentList("dock"))
        setFeaturedComponents(profile.componentList("featured"))
        setAppOrderComponents(profile.componentList("appOrder"))
        profile.optString("shelfMode").takeIf(String::isNotBlank)?.let {
            setShelfMode(ShelfMode.valueOf(it))
        }
        profile.optString("homeFocusMode").takeIf(String::isNotBlank)?.let {
            setHomeFocusMode(HomeFocusMode.valueOf(it))
        }
        if (profile.has("continueWatching")) {
            setContinueWatchingEnabled(profile.optBoolean("continueWatching", true))
        }
        setWatchNextSourcePackage(profile.optString("watchNextSource").takeIf(String::isNotBlank))
        if (profile.has("dockBackground")) {
            setDockBackgroundEnabled(profile.optBoolean("dockBackground", true))
        }
        profile.optString("uiScale").takeIf(String::isNotBlank)?.let {
            setUiScale(UiScale.valueOf(it))
        }
        profile.optString("launcherTheme").takeIf(String::isNotBlank)?.let {
            setLauncherTheme(LauncherThemeMode.valueOf(it))
        }
        if (profile.has("themeOneName")) {
            setThemeName(LauncherThemeMode.Theme1, profile.optString("themeOneName"))
        }
        if (profile.has("themeTwoName")) {
            setThemeName(LauncherThemeMode.Theme2, profile.optString("themeTwoName"))
        }
        if (profile.has("themeThreeName")) {
            setThemeName(LauncherThemeMode.Theme3, profile.optString("themeThreeName"))
        }
        if (profile.has("mediaWidget")) {
            setMediaWidgetEnabled(profile.optBoolean("mediaWidget", false))
        }
        true
    }.getOrDefault(false)

    private fun themeNameKey(theme: LauncherThemeMode): String = when (theme) {
        LauncherThemeMode.Theme1 -> KEY_THEME_ONE_NAME
        LauncherThemeMode.Theme2 -> KEY_THEME_TWO_NAME
        LauncherThemeMode.Theme3 -> KEY_THEME_THREE_NAME
    }

    private fun themeDefaultName(theme: LauncherThemeMode): String = when (theme) {
        LauncherThemeMode.Theme1 -> "Theme 1"
        LauncherThemeMode.Theme2 -> "Theme 2"
        LauncherThemeMode.Theme3 -> "Theme 3"
    }

    private fun wallpaperKey(theme: LauncherThemeMode): String = when (theme) {
        LauncherThemeMode.Theme1 -> KEY_WALLPAPER_THEME_ONE
        LauncherThemeMode.Theme2 -> KEY_WALLPAPER_THEME_TWO
        LauncherThemeMode.Theme3 -> KEY_WALLPAPER_THEME_THREE
    }

    private fun componentArray(components: List<ComponentName>) = JSONArray().apply {
        components.forEach { put(it.flattenToShortString()) }
    }

    private fun JSONObject.componentList(key: String): List<ComponentName> {
        val values = optJSONArray(key) ?: return emptyList()
        return buildList {
            for (index in 0 until values.length()) {
                ComponentName.unflattenFromString(values.optString(index))?.let(::add)
            }
        }
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
        const val KEY_FEATURED = "featured_components"
        const val KEY_APP_ORDER = "app_order_components"
        const val KEY_SHELF_MODE = "shelf_mode"
        const val KEY_HOME_FOCUS_MODE = "home_focus_mode"
        const val KEY_CONTINUE_WATCHING = "continue_watching_enabled"
        const val KEY_WATCH_NEXT_SOURCE = "watch_next_source_package"
        const val KEY_DOCK_BACKGROUND = "dock_background_enabled"
        const val KEY_UI_SCALE = "ui_scale"
        const val KEY_LAUNCHER_THEME = "launcher_theme"
        const val KEY_THEME_ONE_NAME = "theme_one_name"
        const val KEY_THEME_TWO_NAME = "theme_two_name"
        const val KEY_THEME_THREE_NAME = "theme_three_name"
        const val KEY_MEDIA_WIDGET = "media_widget_enabled"
        const val KEY_WALLPAPER_URI_LEGACY = "wallpaper_uri_stage2d"
        const val KEY_WALLPAPER_THEME_ONE = "wallpaper_uri_theme_one"
        const val KEY_WALLPAPER_THEME_TWO = "wallpaper_uri_theme_two"
        const val KEY_WALLPAPER_THEME_THREE = "wallpaper_uri_theme_three"
        const val PROFILE_VERSION = 3
        const val MAX_RECENT = 8
        const val MAX_FAVORITES = 8
        const val MAX_DOCK_SHORTCUTS = 3
        const val MAX_FEATURED_APPS = 2
    }
}
