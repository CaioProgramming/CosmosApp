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

val json =
    Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }
val logHelper = LogHelper()
val issueNumber = getArguments().first()
val branch = "news/${getArguments().first()}"
main(args)

fun getArguments() = args.joinToString().split(",").filter { it.isNotEmpty() }
fun main(args: Array<String>) {
    val argumentsList = getArguments()

    logHelper.run {
        startGroup("Arguments")
        logDebug("Arguments => ${getArguments()}")
        endGroup()
    }

    val issueBody = argumentsList.last()
    val issueTitle = argumentsList[1]

    val authorData = fetchAuthorData(issueBody)

    val pageData = parseStringPages(issueBody)

    val thumbnail = issueBody.getFieldForTag("thumbnail")
    logHelper.endGroup()

    logHelper.run {
        startGroup("News Data")
        logInfo("Issue number => $issueNumber")
        logInfo("Issue title => $issueTitle")
        logInfo("Branch => $branch")
        logInfo("Pages => $pageData")
        logInfo("Author => $authorData")
        logInfo("Thumbnail => $thumbnail")
        endGroup()

    }

    authorData?.let {
        var newItem = NewsObject(issueNumber, pageData, authorData)
        searchForFile("resources","news.json")?.let {
            val jsonContent = it.readText()
            val newsJson = json.decodeFromString<NewsResponse>(jsonContent)

            thumbnail?.let { thumb ->
                if (pageData.isNotEmpty()) {
                    val pages = pageData.toMutableList()
                    pages[0] = pages.first().copy(thumbnailURL = thumb)
                    newItem = newItem.copy(pages = pages)
                }
            }

            val modifiedNews = newsJson.copy(news = newsJson.news.plus(newItem))

            val newJsonContent = json.encodeToString(modifiedNews)
            deleteTempFiles()
            it.writeText(newJsonContent)
            noticeFileUpdate("News $issueNumber added to ${it.path}", it)
            updateRemote("News added to ${it.path}")

        } ?: run {
            logHelper.logError("File news.json not found")
        }

    }
}



fun searchForFile(dir: String = System.getProperty("user.dir"), fileName: String): File? {
    val rootPath = System.getProperty("user.dir")
    logHelper.startGroup("File search")
    logHelper.logDebug("Root files =>\n${File(rootPath).listFiles()?.joinToString("\n -") {  it.name }}")
    logHelper.logInfo("Requested directory => $dir")
    val rootFile = File("$rootPath/$dir")
    return if (rootFile.exists()) {
        val folders = rootFile.listFiles().joinToString("\n") { " - ${it.name}" }
        logHelper.logInfo("Current files on ${rootFile.path} => $folders")
        logHelper.logDebug("Searching for file $fileName in ${rootFile.path}")
        logHelper.endGroup()
        rootFile.listFiles().find { it.name == fileName }
    } else {
        logHelper.logError("Cant find file $fileName on $dir")
        logHelper.endGroup()
        null
    }

}

fun pullBranch() {
    executeGitCommand(listOf("git", "pull", "--rebase", "origin", branch))
}

fun fetchBranch() {
    // Fetch changes from the remote repository
    executeGitCommand(listOf("git", "fetch", "origin", branch))

    // Merge the fetched changes into your local branch
    val mergeResult = executeGitCommand(listOf("git", "merge", "origin/$branch"))

    // Check if merge was successful or if there were conflicts
    logHelper.startGroup("Branch fetch result")
    if (mergeResult.contains("Already up to date.")) {
        logHelper.logWarning("Branch is already up to date.")
    } else if (mergeResult.contains("CONFLICT")) {
        logHelper.logError("Merge conflicts detected. Please resolve them before pushing.")
    } else {
        logHelper.logDebug("Branch fetch successful.")
    }
}

fun updateRemote(message: String) {
    logHelper.run {
        startGroup("Updating remote")
        logInfo(message)
        fetchBranch()
        executeGitCommand(listOf("git", "add", "."))
        executeGitCommand(listOf("git", "commit", "-m", message))
        pullBranch()
        executeGitCommand(listOf("git", "push", "--set-upstream", "origin", branch))
        endGroup()
    }
}

fun executeGitCommand(command: List<String>): String {
    val output = StringBuilder()
    logHelper.startGroup("Executing ${command.size} git commands")
    val processBuilder = ProcessBuilder(command)
    processBuilder.redirectErrorStream(true)
    val process = processBuilder.start()

    val reader = BufferedReader(InputStreamReader(process.inputStream))
    var line: String?

    while (reader.readLine().also { line = it } != null) {
        output.append(line)
    }

    val exitCode = process.waitFor()
    if (exitCode != 0) {
        logHelper.logError("Error executing command: $command")
    }
    logHelper.endGroup()
    return output.toString()
}

fun noticeFileUpdate(message: String, file: File) {
    println("::notice file=${file.path}::$message")

}



fun deleteTempFiles() {
    // Step 1: Identify the temporary files
    val tempDirPath = ".github/workflows/.temp"
    val tempDir = File(tempDirPath)
    logHelper.run {
        startGroup("Deleting temporary files")
        if (tempDir.exists() && tempDir.isDirectory) {
            tempDir.deleteRecursively()
            logDebug("Temporary files deleted successfully.")
            updateRemote("Deleted temporary files")

        } else {
            logWarning("Temporary directory does not exist or is not a directory.")
        }
        endGroup()
    }
}

fun parseStringPages(bodyPages: String): List<NewsItem> {
    var newsItems = emptyList<NewsItem>()
    logHelper.startGroup("Parsing pages")
    newsItems = try {
        val pages =
            List(5) {
                if (it > 0) {
                    val page = bodyPages.getFieldForTag("pagina_$it")
                    logHelper.endGroup()
                    page?.let { it1 -> NewsItem("", it1, "") } ?: run {
                        logHelper.logError("Page $it not found")
                        null
                    }
                } else {
                    null
                }
            }.filterNotNull()
        pages
    } catch (e: Exception) {
        logHelper.logError("Error getting pages")
        emptyList()
    }
    logHelper.endGroup()
    return newsItems

}

fun fetchAuthorData(body: String): AuthorObject? {
    logHelper.startGroup("Fetching author data")
    return try {
        val author = body.getFieldForTag("author")
        logHelper.endGroup()
        val reference = body.getFieldForTag("link")
        logHelper.endGroup()
        safeLet(author, reference) { a, r ->
            logHelper.endGroup()
            AuthorObject(a, r)
        } ?: run {
            logHelper.endGroup()
            return null
        }

    } catch (e: Exception) {
        logHelper.logError("Error getting reference data => ${e.message}")
        logHelper.endGroup()
        return null
    }
}

fun <T1 : Any, T2 : Any, T3 : Any, T4 : Any, R : Any> safeLet(
    p1: T1?,
    p2: T2?,
    p3: T3?,
    p4: T4?,
    block: (T1, T2, T3, T4) -> R?,
): R? {
    if (p1 != null && p2 != null && p3 != null && p4 != null) return block(p1, p2, p3, p4)
    return null
}

fun <T1 : Any, T2 : Any, R : Any> safeLet(
    p1: T1?,
    p2: T2?,
    block: (T1, T2) -> R?,
): R? {
    if (p1 != null && p2 != null) return block(p1, p2)
    return null
}


fun String.getFieldForTag(field: String): String? {
    val tagRef = "### $field"
    val lineBreakTag = "#"
    logHelper.startGroup("Tag Map for $field")

    return try {
        logHelper.logDebug("Searching for tag $tagRef on { $this }")
        if (!this.contains(tagRef)) {
            logHelper.logWarning("tag $field not found")
            null
        } else {
            val start = this.indexOf(tagRef) + tagRef.length
            if (!this.contains(lineBreakTag)) {
                logHelper.logError("End tag not found, cant complete mapping.")
                null
            } else {
                val valueAfterTag = this.substring(start)
                var endIndex =
                    valueAfterTag.indexOf(lineBreakTag).takeIf { it >= 0 }
                        ?: valueAfterTag.indexOf(lineBreakTag).takeIf { it >= 0 }
                endIndex = endIndex ?: valueAfterTag.length // Use string length if no newline is found
                valueAfterTag.substring(0, endIndex)
                logHelper.logInfo("tag($field) value = $valueAfterTag")
                valueAfterTag.substring(0, valueAfterTag.indexOf(lineBreakTag))
            }
        }
    } catch (e: Exception) {
        logHelper.logError("error getting $field value => ${e.message}")
        null
    }

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