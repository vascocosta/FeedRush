import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.net.URI

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun App(urls: List<String>) {
    var fetching by remember { mutableStateOf("Update") }
    var items by remember { mutableStateOf(listOf<NewsItem>()) }
    var filter by remember { mutableStateOf(TextFieldValue("")) }
    var darkTheme by remember { mutableStateOf(true) }
    val listState = rememberLazyListState()
    val sources = listOf("ALL") + urls.map { URI(it).host.uppercase() }
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf("ALL") } // Set default option here

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
                            .height(50.dp)
                            .onKeyEvent { keyEvent ->
                                if (keyEvent.key == Key.Enter) {
                                    fetching = "..."
                                    composableScope.launch {
                                        items = fetchFeeds(urls, filter.text)
                                        fetching = "Update"
                                        listState.scrollToItem(0)
                                    }
                                    true
                                } else {
                                    false
                                }
                            },
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
                    Spacer(modifier = Modifier.width(10.dp))
                    Box {
                        Button(
                            modifier = Modifier
                                .height(50.dp)
                                .width((250.dp)),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color(50, 150, 200),
                                contentColor = Color(223, 223, 223)
                            ),
                            onClick = { expanded = true }
                        ) {
                            Text(if (expanded) "Source" else selectedOption)
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            sources.forEach { option ->
                                DropdownMenuItem(onClick = {
                                    selectedOption = option
                                    expanded = false
                                    fetching = "..."
                                    composableScope.launch {
                                        items = if (selectedOption == "ALL") {
                                            fetchFeeds(urls, "")
                                        } else {
                                            fetchFeeds(urls, selectedOption)
                                        }
                                        fetching = "Update"
                                        listState.scrollToItem(0)
                                    }
                                }) {
                                    Text(option)
                                }
                            }
                        }
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