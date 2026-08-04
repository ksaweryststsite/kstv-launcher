package pl.ksawery.ktvlauncher.data

import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import pl.ksawery.ktvlauncher.KstvMediaNotificationListener
import pl.ksawery.ktvlauncher.model.MediaPlaybackInfo

class MediaPlaybackRepository(context: Context) {
    private val appContext = context.applicationContext
    private val sessionManager = appContext.getSystemService(MediaSessionManager::class.java)
    private val listenerComponent = ComponentName(appContext, KstvMediaNotificationListener::class.java)

    fun currentPlayback(): MediaPlaybackInfo? = runCatching {
        val controller = sessionManager.getActiveSessions(listenerComponent)
            .firstOrNull { candidate ->
                val state = candidate.playbackState?.state
                candidate.metadata != null && (
                    state == PlaybackState.STATE_PLAYING ||
                        state == PlaybackState.STATE_BUFFERING
                    )
            }
            ?: return null
        val metadata = controller.metadata ?: return null
        MediaPlaybackInfo(
            packageName = controller.packageName,
            title = metadata.getString(MediaMetadata.METADATA_KEY_TITLE)
                ?: metadata.description.title?.toString().orEmpty(),
            subtitle = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST)
                ?: metadata.description.subtitle?.toString().orEmpty(),
            isPlaying = true,
        )
    }.getOrNull()
}
