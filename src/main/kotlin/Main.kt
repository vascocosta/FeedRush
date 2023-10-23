import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.prof18.rssparser.*
import java.awt.Desktop
import java.net.URI
import kotlinx.coroutines.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class NewsItem(val title: String?, val link: String?, val dateTime: LocalDateTime?)

suspend fun fetchFeeds(urls: List<String>, search: String = ""): List<NewsItem> = coroutineScope {
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
        DateTimeFormatter.ofPattern("E, dd MMM yyyy HH:mm:ss O", Locale.ENGLISH)
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
            NewsItem(it.title, it.link, dateTime)
        }
        .filter { it.title?.lowercase()?.contains(search.lowercase()) ?: false }
        .sortedByDescending { it.dateTime }
}

@Composable
fun Item(title: String, url: String, date: LocalDateTime?) {
    val link = buildAnnotatedString {
        withStyle(style = SpanStyle(fontSize = 1.8.em, color = Color(50, 150, 200))) {
            append(title)
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = Color.Gray,
                shape = RoundedCornerShape(10.dp)
            )
            .padding(20.dp)
    ) {
        ClickableText(
            text = link,
            onClick = {
                Desktop.getDesktop().browse(URI(url))
            }
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(date?.toString() ?: "")
    }
}

@Composable
fun ItemsList(items: List<NewsItem>) {
    LazyColumn {
        items(items.size) { index ->
            val item = items[index]
            Item(
                item.title.toString(),
                item.link.toString(),
                item.dateTime
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
@Preview
fun App(urls: List<String>) {
    var fetching by remember { mutableStateOf("Update") }
    var items by remember { mutableStateOf(listOf<NewsItem>()) }
    var filter by remember { mutableStateOf(TextFieldValue("")) }
    fetching = "..."

    rememberCoroutineScope().launch {
        items = fetchFeeds(urls, filter.text)
        fetching = "Update"
    }

    MaterialTheme {
        Column(
            modifier = Modifier
                .padding(20.dp)
        ) {
            Row {
                val composableScope = rememberCoroutineScope()
                TextField(
                    modifier = Modifier
                        .height(50.dp),
                    value = filter,
                    onValueChange = { newText ->
                        filter = newText
                    },
                    label = { Text("Filter") },
                    singleLine = true,
                )
                Spacer(modifier = Modifier.width(20.dp))
                Button(
                    modifier = Modifier
                        .height(50.dp)
                        .width((100.dp)),
                    onClick = {
                        filter = TextFieldValue("")
                    }) {
                    Text("Clear")
                }
                Spacer(modifier = Modifier.width(10.dp))
                Button(
                    modifier = Modifier
                        .height(50.dp)
                        .width((100.dp)),
                    onClick = {
                        fetching = "..."
                        composableScope.launch {
                            items = fetchFeeds(urls, filter.text)
                            fetching = "Update"
                        }
                    }) {
                    Text(fetching)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            ItemsList(items)
        }
    }
}

fun main() = application {
    val urls = listOf(
        "https://hnrss.org/newest?points=100",
        "https://www.motorsport.com/rss/f1/news/"
    )
    Window(title = "Feed Rush", onCloseRequest = ::exitApplication) {
        App(urls)
    }
}
