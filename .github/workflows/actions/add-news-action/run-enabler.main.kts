#!/usr/bin/env kotlinc -script

import java.io.File

fun main() {

    println("Enabling scripts")
    val rootPath = System.getProperty("user.dir")
    val rootFile = File(rootPath)
    println("Root => $rootPath")
    val folders = rootFile.listFiles()?.filter { it.isDirectory }?.joinToString { it.name }
    println("Project folders => $folders")
    val scriptsDir = File("$rootPath/.github/workflows/actions/add-news-action/helpers")
    if (!scriptsDir.exists()) {
        println("Folder not found")
        return
    }
    val scripts = scriptsDir.listFiles()?.filter { it.isFile && it.name.endsWith(".kts") }?.map { it.name } ?: emptyList()
    println("Scripts on folder $scriptsDir: $scripts")
    scripts.forEach {
        val file = File("$scriptsDir/$it")
        if (file.exists()) {
            println("Enabling $it")
            file.setExecutable(true)
        } else {
            println("File $it does not exist")
        }
    }
    println("Enabled ${scripts.size} scripts")

}

main()