package pl.ksawery.ktvlauncher.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pl.ksawery.ktvlauncher.model.WatchNextItem

class WatchNextRepository(private val context: Context) {
    suspend fun load(): List<WatchNextItem> = withContext(Dispatchers.IO) {
        val items = mutableListOf<WatchNextItem>()
        context.contentResolver.query(WATCH_NEXT_URI, null, null, null, null)?.use { cursor ->
            while (cursor.moveToNext() && items.size < MAX_ITEMS) {
                val duration = cursor.longOrNull("duration_millis")
                val position = cursor.longOrNull("last_playback_position_millis")
                val progress = if (duration != null && duration > 0L && position != null) {
                    (position.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
                } else {
                    null
                }

                items += WatchNextItem(
                    id = cursor.longOrNull("_id") ?: items.size.toLong(),
                    title = cursor.stringOrNull("title") ?: "Materiał",
                    description = cursor.stringOrNull("description"),
                    packageName = cursor.stringOrNull("package_name"),
                    intentUri = cursor.stringOrNull("intent_uri"),
                    posterUri = cursor.stringOrNull("poster_art_uri"),
                    progressPercent = progress,
                )
            }
        }
        items
    }

    private fun android.database.Cursor.stringOrNull(column: String): String? {
        val index = getColumnIndex(column)
        return if (index >= 0 && !isNull(index)) getString(index) else null
    }

    private fun android.database.Cursor.longOrNull(column: String): Long? {
        val index = getColumnIndex(column)
        return if (index >= 0 && !isNull(index)) getLong(index) else null
    }

    private companion object {
        val WATCH_NEXT_URI: Uri = Uri.parse("content://android.media.tv/watch_next_program")
        const val MAX_ITEMS = 24
    }
}
