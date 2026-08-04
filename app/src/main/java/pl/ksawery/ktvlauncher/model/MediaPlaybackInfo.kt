package pl.ksawery.ktvlauncher.model

data class MediaPlaybackInfo(
    val packageName: String,
    val title: String,
    val subtitle: String,
    val isPlaying: Boolean,
)
