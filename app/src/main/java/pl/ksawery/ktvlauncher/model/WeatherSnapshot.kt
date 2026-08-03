package pl.ksawery.ktvlauncher.model

data class WeatherSnapshot(
    val temperatureCelsius: Int,
    val description: String,
    val symbol: String,
)

