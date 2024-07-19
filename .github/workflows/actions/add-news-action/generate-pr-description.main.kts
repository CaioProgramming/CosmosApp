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
    val argument = args.first().substring(1, args.first().length - 1)
    val issueData: Map<String, String> = json.decodeFromString(argument)
    logHelper.logDebug("Issue data: $issueData")
    
    val issueFile = File(".github/workflows/.temp/issue.json")
    issueFile.writeText(json.encodeToString(issueData))
    
    logHelper.logNotice("Issue file created")

    logHelper.endGroup()
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
