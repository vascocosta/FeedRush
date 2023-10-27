import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import java.awt.Desktop
import java.net.URI
import java.time.LocalDateTime

@Composable
fun Item(title: String?, url: String?, date: LocalDateTime?, description: String?) {
    val link = buildAnnotatedString {
        withStyle(style = SpanStyle(fontSize = 1.8.em, color = Color(70, 170, 210))) {
            append(stripHtml(title))
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
                Desktop.getDesktop().browse(URI(url ?: ""))
            }
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            color = Color(128, 128, 128),
            text = date?.toString()?.replace("T", " ") ?: ""
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            color = Color(128, 128, 128),
            text = stripHtml(description) ?: ""
        )
    }
}

@Composable
fun ItemsList(items: List<NewsItem>, listState: LazyListState) {
    LazyColumn(
        state = listState
    ) {
        items(items.size) { index ->
            val item = items[index]
            Item(
                item.title,
                item.link,
                item.dateTime,
                item.description
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}