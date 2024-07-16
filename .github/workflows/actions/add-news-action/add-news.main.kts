#!/usr/bin/env kotlinc -script
@file:Repository("https://repo1.maven.org/maven2")
@file:DependsOn("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
@file:Suppress("PLUGIN_IS_NOT_ENABLED")

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

@Serializable
data class NewsResponse(
    val news: List<NewsObject>
)

@Serializable
data class NewsObject(
    val id: String,
    val pages: List<NewsItem>,
    val reference: AuthorObject?
)

@Serializable
data class NewsItem(
    val title: String,
    val description: String,
    val thumbnailURL: String
)

@Serializable
data class AuthorObject(
    val author: String,
    val reference: String
)

val json = Json {
    prettyPrint = true
    ignoreUnknownKeys = true

}

main(args)
fun main(args: Array<String>) {

    println("Adding news to file")
    println("received arguments: \n${args.joinToString { it }}")
    
    val filePath = args.first()
    val issueNumber = args[1]
    val issueBody = args.last()

    val id = issueNumber

    val authorData = fetchAuthorData(issueBody)
    println("Author data => $authorData")

    val pageData = parseStringPages(issueBody)
    println("Page data => $pageData")
    
    val thumbnail = issueBody.getStringForField("thumbnail")


    val file = File(filePath)
    val jsonContent = file.readText()
    val newsJson = json.decodeFromString<NewsResponse>(jsonContent)

    val pages = pageData.toMutableList()
    pages[0] = pages.first().copy(thumbnailURL = thumbnail)
    
    val newItem = NewsObject(id, pageData, authorData)

    val modifiedNews = newsJson.copy(news = newsJson.news.plus(newItem))

    val newJsonContent = json.encodeToString(modifiedNews)

    file.writeText(newJsonContent)

    updateRemote("News added to $filePath")
}

fun updateRemote(message: String) {
    println(message)
    executeGitCommand(listOf("git", "add", "."))
    executeGitCommand(listOf("git", "commit", "-m", message))
    executeGitCommand(listOf("git", "push"))
}

fun String.getStringForField(field: String) = this.substring(this.indexOf(field), this.indexOf("\n", this.indexOf(field)))


fun executeGitCommand(command: List<String>) {
    val processBuilder = ProcessBuilder(command)
    processBuilder.redirectErrorStream(true)
    val process = processBuilder.start()

    val reader = BufferedReader(InputStreamReader(process.inputStream))
    var line: String?
    while (reader.readLine().also { line = it } != null) {
        println(line)
    }

    val exitCode = process.waitFor()
    if (exitCode != 0) {
        println("Error executing command: $command")
    }
}


fun parseStringPages(pagesArray: String): List<NewsItem> {
    val pagesStringArray = pagesArray
        .replace("[", "")
        .replace("]", "")
    val pagesItems = pagesStringArray.split("},").map {
        json.decodeFromString<NewsItem>(it)
    }
    return pagesItems
}

fun getCommand(script: String): String? {
    val rootPath = System.getProperty("user.dir")
    val workFlowPath = ".github/workflows/actions/"
    val actionPath = "add-news-action"
    val scriptFile = File("$rootPath/$workFlowPath/$actionPath/$script")
    if (scriptFile.exists()) {
        return scriptFile.name
    } else return null
}

fun fetchAuthorData(body: String): AuthorObject? {
    getCommand("/helpers/news-reference-builder.kts")?.let {
        val command = listOf("kotlinc", "-script", it, body)

        val processBuilder = ProcessBuilder(command)
        processBuilder.redirectErrorStream(true)

        val process = processBuilder.start()
        val reader = BufferedReader(InputStreamReader(process.inputStream))

        var result: String?
        while (reader.readLine().also { result = it } != null) {
            println(result)
        }

        process.waitFor()
        val authorData = result
        val json = Json {
            prettyPrint = true
            ignoreUnknownKeys = true
        }
        try {
            return json.decodeFromString<AuthorObject>(authorData ?: "")
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }


    } ?: return null
}


