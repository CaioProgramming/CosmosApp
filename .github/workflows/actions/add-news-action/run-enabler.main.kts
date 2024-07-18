#!/usr/bin/env kotlinc -script

import java.io.File

val logHelper = LogHelper()
main()
fun main() {
    logHelper.startGroup("Enabling scripts")
    val rootPath = System.getProperty("user.dir")
    val rootFile = File(rootPath)
    logHelper.logDebug("Root => $rootPath")
    val folders = rootFile.listFiles()?.filter { it.isDirectory }?.joinToString { it.name }
    logHelper.logDebug("Project folders => $folders")
    val scriptsDir = File(".github/workflows/actions/add-news-action")
    if (!scriptsDir.exists()) {
        logHelper.logError("$scriptsDir directory not found")
        return
    }
    val scripts = scriptsDir.listFiles()?.filter { it.isFile && it.name.endsWith(".kts") }?.map { it.name } ?: emptyList()
    logHelper.logDebug("Scripts on $scriptsDir: $scripts")
    scripts.forEach {
        val file = File("$scriptsDir/$it")
        if (file.exists()) {
            logHelper.logNotice("Enabling $it")
            file.setExecutable(true)
        } else {
            logHelper.logError("File $it does not exist")
        }
    }
    logHelper.logDebug("Enabled ${scripts.size} scripts")
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