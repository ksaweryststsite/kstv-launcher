package pl.ksawery.ktvlauncher.data

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.text.Collator
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pl.ksawery.ktvlauncher.model.LaunchableApp

class AndroidAppRepository(
    private val packageManager: PackageManager,
    private val ownPackageName: String,
) : AppRepository {

    override suspend fun getLaunchableApps(): List<LaunchableApp> = withContext(Dispatchers.IO) {
        val candidates = buildList {
            addAll(queryCategory(Intent.CATEGORY_LEANBACK_LAUNCHER))
            addAll(queryCategory(Intent.CATEGORY_LAUNCHER))
        }

        val collator = Collator.getInstance(Locale("pl", "PL"))
        candidates
            .asSequence()
            .filterNot { it.activityInfo.packageName == ownPackageName }
            .distinctBy { info ->
                ComponentName(info.activityInfo.packageName, info.activityInfo.name)
            }
            .mapNotNull(::toLaunchableApp)
            .sortedWith { left, right -> collator.compare(left.label, right.label) }
            .toList()
    }

    private fun queryCategory(category: String): List<ResolveInfo> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(category)
        return packageManager.queryIntentActivities(
            intent,
            0,
        )
    }

    private fun toLaunchableApp(info: ResolveInfo): LaunchableApp? = runCatching {
        val activityInfo = info.activityInfo
        LaunchableApp(
            label = info.loadLabel(packageManager).toString().trim().ifEmpty {
                activityInfo.packageName
            },
            componentName = ComponentName(activityInfo.packageName, activityInfo.name),
            icon = info.loadIcon(packageManager).toImageBitmap(144),
        )
    }.getOrNull()
}

private fun Drawable.toImageBitmap(sizePx: Int): ImageBitmap {
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val oldBounds = Rect(bounds)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)
    setBounds(oldBounds)
    return bitmap.asImageBitmap()
}
