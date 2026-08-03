package pl.ksawery.ktvlauncher.model

data class WatchNextItem(
    val id: Long,
    val title: String,
    val description: String?,
    val packageName: String?,
    val intentUri: String?,
    val posterUri: String?,
    val progressPercent: Float?,
)

enum class WatchNextStatus {
    Loading,
    Ready,
    Empty,
    Unavailable,
}

