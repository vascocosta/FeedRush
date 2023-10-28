import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.ui.window.Window
import com.prof18.rssparser.*
import java.time.format.DateTimeFormatter
import java.time.LocalDateTime
import java.util.Locale
import kotlinx.coroutines.*

data class NewsItem(val title: String?, val link: String?, val dateTime: LocalDateTime?, val description: String?)

suspend fun fetchFeeds(urls: List<String>, filter: String = ""): List<NewsItem> = coroutineScope {
    val rssParser = RssParser()
    val deferred = urls.map {
        async {
            try {
                rssParser.getRssChannel(it)
            } catch (_: Exception) {
                null
            }
        }
    }
    val channels = deferred.awaitAll().filterNotNull()
    val items = channels.map { it.items }.flatten()
    val formatters = listOf(
        DateTimeFormatter.ofPattern("E, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("E, dd MMM yyyy HH:mm:ss O", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("E, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH)
    )
    items
        .map {
            var dateTime: LocalDateTime? = null
            for (formatter in formatters) {
                try {
                    dateTime = LocalDateTime.parse(it.pubDate, formatter)
                    break
                } catch (_: Exception) {
                    // Here dateTime stays null and therefore is set to now below.
                }
            }
            if (dateTime == null) {
                dateTime = LocalDateTime.now()
            }
            NewsItem(it.title, it.link, dateTime, it.description)
        }
        .filter {
            val lowerTitle = it.title?.lowercase()
            val lowerFilter = filter.lowercase()
            val lowerLink = it.link?.lowercase()
            lowerTitle?.contains(lowerFilter) ?: false || (lowerLink?.contains(lowerFilter) ?: false)
        }
        .sortedByDescending { it.dateTime }
}

fun main() = application {
    val windowState = rememberWindowState(size = DpSize(950.dp, 1000.dp))
    var urls = emptyList<String>()
    try {
        urls = readUrls()
    } catch (e: Exception) {
        println(e.message)
    }
    Window(
        title = "Feed Rush",
        onCloseRequest = ::exitApplication,
        icon = painterResource("icon.ico"),
        state = windowState
    ) {
        App(urls)
    }
}
