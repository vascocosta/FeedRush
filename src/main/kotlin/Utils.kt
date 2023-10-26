import java.io.File

fun stripHtml(html: String?): String? {
    val htmlTagRegex = "<.*?>".toRegex()
    return html?.replace(htmlTagRegex, "")
}

fun readUrls(): List<String> {
    try {
        return File("feeds.txt").bufferedReader().readLines()
    } catch (_: Exception) {
        throw Exception("Could not read feed urls.")
    }
}