#!/usr/bin/env kotlinc -script -Xplugin=.github/workflows/.temp/kotlinx-serialization-compiler-1.9.22-plugin.jar
@file:Repository("https://repo1.maven.org/maven2")
@file:DependsOn("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

@Serializable
data class NewsObject(
    val id: String? = null,
    val pages: List<NewsItem> = emptyList(),
    val reference: AuthorObject? = null,
)

@Serializable
data class NewsItem(
    val title: String? = null,
    val description: String? = null,
    val thumbnailURL: String? = null,
)

@Serializable
data class AuthorObject(
    val author: String? = null,
    val reference: String? = null,
)

@Serializable
data class NewsResponse(
    val news: List<NewsObject> = emptyList(),
)
val logHelper = LogHelper()
val json = Json { prettyPrint = true
    ignoreUnknownKeys = true
}

fun main(args: Array<String>) {
    
    logHelper.startGroup("Fetching last included news")
    logHelper.logDebug("Arguments: ${args.joinToString()}")
    val newsResource = File("resources/news.json").readText()

    val newsData: NewsResponse = json.decodeFromString(newsResource)
    val descriptionText = generateMDText(newsData.news.last())
    val descriptionFile = File(".github/workflows/.temp/last-new.txt")
    descriptionFile.writeText(descriptionText)
    
    logHelper.logNotice("Temp news file created")

    logHelper.endGroup()
}

fun generateMDText(newsObject: NewsObject) : String {
    val newsItem = newsObject.pages.first()
    val pagesDescription = newsObject.pages.joinToString("\n") {
       "\n## ${it.title}\n" +
        it.description.toString()
    }

    return """
        |![${newsItem.title}](${newsItem.thumbnailURL})  
        |## ${newsItem.title}
        | $pagesDescription
        | Published by [${newsObject.reference?.author}](${newsObject.reference?.reference})
        |
    """.trimMargin()

}

class LogHelper {

    fun startGroup(title: String) {
        println("::group::$title")
        logInfo("Group started: $title")
        logWarning("Remember to close the group")
    }

    fun logNotice(message: String) {
        println("::notice::$message")
    }

    fun logError(message: String) {
        println("::error::$message")
    }

    fun logWarning(message: String) {
        println("::warning::$message")
    }

    fun logInfo(message: String) {
        println("::notice::$message")
    }

    fun logDebug(message: String) {
        println("::debug::$message")
    }

    fun endGroup() {
        println("::endgroup::")
    }

}

main(args)
