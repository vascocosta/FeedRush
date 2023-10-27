import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
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
        .filter { it.title?.lowercase()?.contains(filter.lowercase()) ?: false }
        .sortedByDescending { it.dateTime }
}


@Composable
fun App(urls: List<String>) {
    var fetching by remember { mutableStateOf("Update") }
    var items by remember { mutableStateOf(listOf<NewsItem>()) }
    var filter by remember { mutableStateOf(TextFieldValue("")) }
    var darkTheme by remember { mutableStateOf(true) }
    val listState = rememberLazyListState()

    fetching = "..."

    rememberCoroutineScope().launch {
        items = fetchFeeds(urls, filter.text)
        fetching = "Update"
    }

    MaterialTheme(
        colors = if (darkTheme) {
            darkColors(background = Color(32, 32, 32))
        } else {
            lightColors(background = Color(223, 223, 223))
        }
    ) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colors.background)) {
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
                        colors = TextFieldDefaults.textFieldColors(
                            textColor = Color(132, 132, 132),
                            cursorColor = Color(50, 150, 200),
                            focusedIndicatorColor = Color(50, 150, 200),
                            unfocusedIndicatorColor = Color(132, 132, 132),
                            focusedLabelColor = Color(132, 132, 132),
                            unfocusedLabelColor = Color(132, 132, 132),
                        )
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    Button(
                        modifier = Modifier
                            .height(50.dp)
                            .width((100.dp)),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(50, 150, 200),
                            contentColor = Color(223, 223, 223)
                        ),
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
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(50, 150, 200),
                            contentColor = Color(223, 223, 223)
                        ),
                        onClick = {
                            fetching = "..."
                            composableScope.launch {
                                items = fetchFeeds(urls, filter.text)
                                fetching = "Update"
                                listState.scrollToItem(0)
                            }
                        }) {
                        Text(fetching)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        modifier = Modifier
                            .height(50.dp)
                            .width((100.dp)),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(50, 150, 200),
                            contentColor = Color(223, 223, 223)
                        ),
                        onClick = {
                            darkTheme = !darkTheme
                        }
                    ) {
                        Text(
                            if (darkTheme) {
                                "Light"
                            } else {
                                "Dark"
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                ItemsList(items, listState)
            }
        }
    }
}

fun main() = application {
    val windowState = rememberWindowState(size = DpSize(800.dp, 1000.dp))
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
