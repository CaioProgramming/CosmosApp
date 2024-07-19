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
var branch: String? = null
main(args)


fun getIssueFile(): File = File(".github/workflows/.temp/issue.json")

fun parseBody(issueNumber: String, issueTitle: String, body: String) : NewsObject {
    logHelper.startGroup("Parsing body data")
    val authorData = fetchAuthorData(body)

    var pageData = parseStringPages(body)
    

    pageData = pageData.toMutableList().apply {
        this[0] = this.first().copy(title = issueTitle)
    }

    val thumbnail = body.getFieldForTag("thumbnail")

    thumbnail?.let {
        val newPages = pageData.toMutableList()
        newPages[0] = newPages.first().copy(thumbnailURL = it)
        pageData = newPages
    } ?: kotlin.run {
        logHelper.logWarning("Thumbnail not found")
    }

    return NewsObject(issueNumber, pageData, authorData)
}

fun updateData(newItem: NewsObject) {
    logHelper.startGroup("Updating news data")
    searchForFile("resources", "news.json")?.let {
        logHelper.logDebug("Including $newItem")
        val jsonContent = it.readText()
        val newsJson = json.decodeFromString<NewsResponse>(jsonContent)

        logHelper.logDebug("Current news => ${newsJson.news.size}")

        val modifiedNews = newsJson.copy(news = newsJson.news.plus(newItem))

        logHelper.logDebug("Updated news => ${modifiedNews.news.size}")

        val newJsonContent = json.encodeToString(modifiedNews)

        val rootPath = System.getProperty("user.dir")
        val relativeFile = File(it.path.replace("rootPath/", ""))

        relativeFile.writeText(newJsonContent)
        noticeFileUpdate("News ${newItem.id} added to ${relativeFile.path}", it)
        deleteTempFiles()
        updateRemote("News added to news.json", "news/${newItem.id}")

    } ?: run {
        logHelper.logError("File news.json not found")
    }
}


fun main(args: Array<String>) {

    val argsMap = json.decodeFromString<Map<String, String>>(getIssueFile().readText())

    logHelper.startGroup("Issue temp data")
    val issueData = argsMap
    logHelper.logDebug("Issue data: $issueData")

    val issueBody = issueData["body"]
    val issueTitle = issueData["title"]
    val issueNumber = issueData["number"]

    safeLet(issueNumber, issueBody, issueTitle) { number, body, title ->
        val data = parseBody(number, title, body)
        updateData(data)
    } ?: kotlin.run {
        logHelper.logError("Issue data not found")
    }

}

fun searchForFile(
    dir: String = System.getProperty("user.dir"),
    fileName: String,
): File? {
    val rootPath = System.getProperty("user.dir")
    logHelper.startGroup("File search")
    logHelper.logDebug("Root files =>\n${File(rootPath).listFiles()?.joinToString("\n -") { it.name }}")
    logHelper.logInfo("Requested directory => $dir")
    val rootFile = File("$rootPath/$dir")
    return if (rootFile.exists()) {
        val folders = rootFile.listFiles().joinToString("\n") { " - ${it.name}" }
        logHelper.logInfo("Current files on ${rootFile.path} => $folders")
        logHelper.logDebug("Searching for file $fileName in ${rootFile.path}")
        rootFile.listFiles().find { it.name == fileName }
    } else {
        logHelper.logError("Cant find file $fileName on $dir")
        null
    }
}


fun pullBranch() {
    executeGitCommand(listOf("git", "pull", "--rebase", "origin"))
}



fun fetchBranch() {
    // Fetch changes from the remote repository
    executeGitCommand(listOf("git", "fetch", "origin"))

    // Merge the fetched changes into your local branch
    val mergeResult = executeGitCommand(listOf("git", "merge"))

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

fun updateRemote(message: String, branch: String) {
    logHelper.run {
        executeGitCommand(listOf("git", "push", "--set-upstream", "origin", branch))

        startGroup("Updating remote")
        logInfo(message)

        executeGitCommand(listOf("git", "add", "."))

        executeGitCommand(listOf("git", "status"))
        
        executeGitCommand(listOf("git", "commit","-m", message))


       //executeGitCommand(listOf("git", "reflog", "show", "--no-abbrev-commit"))
    }
}

fun executeGitCommand(command: List<String>): String {
    
    val output = StringBuilder()
    logHelper.startGroup("Executing ${command.size} git commands")
    logHelper.logDebug("Requested commands ${command.joinToString("\n")}")
    val processBuilder = ProcessBuilder(command)
    processBuilder.redirectErrorStream(true)
    val process = processBuilder.start()

    val reader = BufferedReader(InputStreamReader(process.inputStream))
    var line: String?

    while (reader.readLine().also { line = it } != null) {
        logHelper.logDebug("- Command output: $line")
        output.append(line)
    }

    val exitCode = process.waitFor()
    if (exitCode != 0) {
        logHelper.logError("Error executing command: $command with exit code $exitCode and output: $output")
    }
    return output.toString()
}

fun noticeFileUpdate(
    message: String,
    file: File,
) {
    println("::notice file=${file.name}::$message")
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
        } else {
            logWarning("Temporary directory does not exist or is not a directory.")
        }
    }
}

fun parseStringPages(bodyPages: String): List<NewsItem> {
    logHelper.startGroup("Parsing pages")
    return try {
                List(5) {
                    if (it > 0) {
                        val page = bodyPages.getFieldForTag("pagina_$it")
                        page?.let { it1 -> NewsItem("", it1, "") } ?: run {
                            logHelper.logWarning("Page $it not found")
                            null
                        }
                    } else {
                        null
                    }
                }.filterNotNull()
        } catch (e: Exception) {
            logHelper.logError("Error getting pages")
            emptyList()
        }
}

fun fetchAuthorData(body: String): AuthorObject? {
    logHelper.startGroup("Fetching author data")
    return try {
        val author = body.getFieldForTag("author")
        val reference = body.getFieldForTag("link")
        safeLet(author, reference) { a, r ->
            AuthorObject(a, r)
        } ?: run {
            return null
        }
    } catch (e: Exception) {
        logHelper.logError("Error getting reference data => ${e.message}")
        return null
    }
}

fun <T1 : Any, T2 : Any, T3 : Any, R : Any> safeLet(
    p1: T1?,
    p2: T2?,
    p3: T3?,
    block: (T1, T2, T3) -> R?,
): R? {
    if (p1 != null && p2 != null && p3 != null) return block(p1, p2, p3)
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
    logHelper.logDebug("Searching for tag $tagRef on { ${this.substring(0, 20)} }")

    return try {
        if (!this.contains(tagRef)) {
            logHelper.logWarning("tag $field not found")
            null
        } else {
            val start = this.indexOf(tagRef) + tagRef.length
            if (!this.contains(lineBreakTag)) {
                logHelper.logWarning("End tag not found, cant complete mapping.")
                null
            } else {
                val valueAfterTag = this.substring(start)
                var endIndex =
                    valueAfterTag.indexOf(lineBreakTag).takeIf { it >= 0 }
                        ?: valueAfterTag.indexOf(lineBreakTag).takeIf { it >= 0 }
                endIndex = endIndex ?: valueAfterTag.length // Use string length if no newline is found
                valueAfterTag.substring(0, endIndex)
                logHelper.logInfo("tag($field) value = ${valueAfterTag.substring(0, 20)}")
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
        endGroup()
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

    private fun endGroup() {
        println("::endgroup::")
    }

    fun maskLog(message: String) {
        println("::add-mask::$message")
    }
}
