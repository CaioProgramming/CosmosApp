#!/usr/bin/env kotlinc -script -Xplugin=.github/workflows/.temp/kotlinx-serialization-compiler-1.9.22-plugin.jar
@file:Repository("https://repo1.maven.org/maven2")
@file:DependsOn("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

val logHelper = LogHelper()
val json = Json { prettyPrint = true
    ignoreUnknownKeys = true
}

fun main(args: Array<String>) {
    
    logHelper.startGroup("Parsing issue data to json")
    logHelper.logDebug("Arguments: ${args.joinToString()}")
    
    val issueData: Map<String, String> = json.decodeFromString(args.first().substring(1, args.first().length - 1)
    logHelper.logDebug("Issue data: $issueData")
    
    val issueFile = File(".github/workflows/.temp/issue.json")
    issueFile.writeText(json.encodeToString(issueData))
    
    executeGitCommand(listOf("git", "add", ".github/workflows/.temp/issue.json"))
    logHelper.logNotice("Issue file created")

    executeGitCommand(listOf("git", "commit", "-m", "Adds issue temp file"))
    logHelper.endGroup()
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
