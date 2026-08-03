package pl.ksawery.ktvlauncher.data

import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import pl.ksawery.ktvlauncher.model.WeatherSnapshot

class WeatherRepository {
    suspend fun currentForBienczyce(): WeatherSnapshot = withContext(Dispatchers.IO) {
        val connection = URL(FORECAST_URL).openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = 5_000
            connection.readTimeout = 5_000
            connection.requestMethod = "GET"
            check(connection.responseCode in 200..299)

            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val current = JSONObject(body).getJSONObject("current")
            val temperature = current.getDouble("temperature_2m").roundToInt()
            val weatherCode = current.getInt("weather_code")
            val presentation = weatherPresentation(weatherCode)

            WeatherSnapshot(
                temperatureCelsius = temperature,
                description = presentation.first,
                symbol = presentation.second,
            )
        } finally {
            connection.disconnect()
        }
    }

    private fun weatherPresentation(code: Int): Pair<String, String> = when (code) {
        0 -> "Bezchmurnie" to "☀"
        1, 2 -> "Częściowe zachmurzenie" to "⛅"
        3 -> "Pochmurno" to "☁"
        45, 48 -> "Mgła" to "☁"
        in 51..57 -> "Mżawka" to "☂"
        in 61..67, in 80..82 -> "Deszcz" to "☂"
        in 71..77, in 85..86 -> "Śnieg" to "❄"
        in 95..99 -> "Burza" to "ϟ"
        else -> "Warunki zmienne" to "☁"
    }

    private companion object {
        const val FORECAST_URL =
            "https://api.open-meteo.com/v1/forecast" +
                "?latitude=50.0873&longitude=20.0371" +
                "&current=temperature_2m,weather_code" +
                "&timezone=Europe%2FWarsaw"
    }
}
