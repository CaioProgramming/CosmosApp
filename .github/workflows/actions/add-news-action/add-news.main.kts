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
val issueNumber = args.first()
val branch = "news/$issueNumber"
main(args)

fun main(args: Array<String>) {
    println("Adding news to file")
    val argumentsList = args.joinToString().split(" , ")

    val issueBody = argumentsList.last()
    val authorData = fetchAuthorData(issueBody)
    println("Author data => $authorData")

    val pageData = parseStringPages(issueBody)
    println("Page data => $pageData")

    val thumbnail = issueBody.getFieldForTag("thumbnail")

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

            println("\n\nNew item => $newItem\n\n")
            val modifiedNews = newsJson.copy(news = newsJson.news.plus(newItem))

            val newJsonContent = json.encodeToString(modifiedNews)
            deleteTempFiles()
            it.writeText(newJsonContent)
            updateRemote("News added to ${it.path}")

        }

    }
}

fun searchForFile(dir: String = System.getProperty("user.dir"), filePath: String): File? {
    val rootPath = System.getProperty("user.dir")
    println("Root files => ${File(rootPath).listFiles()?.joinToString("\n -") {  it.name }}")
    val rootFile = File("$rootPath/$dir")
    if (rootFile.exists()) {
        val folders = rootFile.listFiles().joinToString("\n") { " - ${it.name}" }
        println("Current files on $dir => $folders")
        println("Searching for file $filePath in $rootPath")

        val requiredFile = rootFile.listFiles().find { it.name == filePath }
        return requiredFile
    }
    println("Cant find file $filePath on $dir")
    return null
}

fun pullBranch() {
    executeGitCommand(listOf("git", "pull", "--rebase", "origin", branch))
}

fun fetchBranch() {
    // Fetch changes from the remote repository
    executeGitCommand(listOf("git", "fetch", "origin", branch))

    // Merge the fetched changes into your local branch
    val mergeResult = executeGitCommand(listOf("git", "merge", "origin/$branch"))
    println(mergeResult)

    // Check if merge was successful or if there were conflicts
    if (mergeResult.contains("Automatic merge failed; fix conflicts and then commit the result.")) {
        println("Merge conflicts detected. Please resolve them before pushing.")
    } else {
        println("Update sucessful")
    }
}

fun updateRemote(message: String) {
    println(message)
    fetchBranch()
    executeGitCommand(listOf("git", "add", "."))
    executeGitCommand(listOf("git", "commit", "-m", message))
    pullBranch()
    executeGitCommand(listOf("git", "push", "--set-upstream", "origin", branch))

}

fun executeGitCommand(command: List<String>): String {
    val processBuilder = ProcessBuilder(command)
    processBuilder.redirectErrorStream(true)
    val process = processBuilder.start()

    val reader = BufferedReader(InputStreamReader(process.inputStream))
    var line: String?
    val output = StringBuilder()

    while (reader.readLine().also { line = it } != null) {
        println(line)
        output.append(line)
    }

    val exitCode = process.waitFor()
    if (exitCode != 0) {
        println("Error executing command: $command")
    }
    return output.toString()
}

fun deleteTempFiles() {
    // Step 1: Identify the temporary files
    val tempDirPath = ".github/workflows/.temp"
    val tempDir = File(tempDirPath)

    // Step 2: Delete the files
    if (tempDir.exists() && tempDir.isDirectory) {
        tempDir.deleteRecursively()
        println("Temporary files deleted successfully.")
    } else {
        println("Temporary directory does not exist or is not a directory.")
    }

    updateRemote("Deleted temporary files")
}

fun parseStringPages(bodyPages: String): List<NewsItem> {
    println("mapping pages for $bodyPages")
    try {
        val pages =
            List(5) {
                if (it > 0) {
                    val page = bodyPages.getFieldForTag("pagina_$it")
                    page?.let { it1 -> NewsItem("", it1, "") } ?: run {
                        null
                    }
                } else {
                    null
                }
            }.filterNotNull()
        return pages
    } catch (e: Exception) {
        println("Error getting pages")
        return emptyList()
    }
}

fun fetchAuthorData(body: String): AuthorObject? {
    return try {
        val author = body.getFieldForTag("author")
        val reference = body.getFieldForTag("link")
        safeLet(author, reference) { a, r ->
            AuthorObject(a, r)
        } ?: run {
            return null
        }
    } catch (e: Exception) {
        println("Error getting reference data => ${e.message}")
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
    println("getting value for tag { $field }")
    return try {
        if (!this.contains(tagRef)) {
            println("tag $field not found")
            null
        } else {
            val start = this.indexOf(tagRef) + tagRef.length

            fun String.getFieldForTag(field: String): String? {
                val tagRef = "###$field "
                return try {
                    if (!this.contains(tagRef)) {
                        null
                    } else {
                        if (!this.contains(lineBreakTag)) {
                            println("No line break found, cant map tag.")
                            return null
                        }

                        val start = this.indexOf(tagRef) + tagRef.length
                        val valueAfterTag = this.substring(start)
                        var endIndex =
                            valueAfterTag.indexOf(lineBreakTag).takeIf { it >= 0 }
                                ?: valueAfterTag.indexOf(lineBreakTag).takeIf { it >= 0 }
                        endIndex = endIndex ?: valueAfterTag.length // Use string length if no newline is found
                        valueAfterTag.substring(0, endIndex)
                    }
                } catch (e: Exception) {
                    null
                }
            }
            val valueAfterTag = this.substring(start)
            println("tag($field) value = $valueAfterTag")
            return valueAfterTag.substring(0, valueAfterTag.indexOf(lineBreakTag))
        }
    } catch (e: Exception) {
        println("error getting $field value => ${e.message}")
        null
    }
}
