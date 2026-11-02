package pl.ksawery.ktvlauncher.ui.home

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.InputStream
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Bounded 12MB poster cache. Streaming services may publish oversized (4K) art.
internal object PosterLoader {
    private val cache = object : LruCache<String, ImageBitmap>(12 * 1024 * 1024) {
        override fun sizeOf(key: String, value: ImageBitmap): Int =
            (value.width * value.height * 2).coerceAtLeast(1)
    }

    suspend fun load(context: Context, source: String?): ImageBitmap? {
        val uri = source?.takeIf { it.isNotBlank() } ?: return null
        cache.get(uri)?.let { return it }
        return withContext(Dispatchers.IO) {
            runCatching {
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                open(context, uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
                if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@runCatching null
                var sample = 1
                while (bounds.outWidth / sample > 960 || bounds.outHeight / sample > 540) {
                    sample *= 2
                }
                val options = BitmapFactory.Options().apply {
                    inSampleSize = sample
                    inPreferredConfig = Bitmap.Config.RGB_565
                }
                val result = open(context, uri)?.use {
                    BitmapFactory.decodeStream(it, null, options)?.asImageBitmap()
                }
                if (result != null) cache.put(uri, result)
                result
            }.getOrNull()
        }
    }

    private fun open(context: Context, source: String): InputStream? {
        val parsed = Uri.parse(source)
        return when (parsed.scheme?.lowercase()) {
            "https", "http" -> URL(source).openConnection().apply {
                connectTimeout = 2500
                readTimeout = 3000
            }.getInputStream()
            "content", "file", "android.resource" -> context.contentResolver.openInputStream(parsed)
            else -> null
        }
    }
}
