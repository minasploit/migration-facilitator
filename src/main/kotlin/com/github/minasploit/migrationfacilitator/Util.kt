package com.github.minasploit.migrationfacilitator

import com.intellij.notification.NotificationType
import com.intellij.notification.Notification
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager
import org.apache.commons.lang.StringUtils
import java.io.File
import java.util.Scanner

class Util private constructor() {

    companion object {
        fun showNotification(
            project: Project,
            title: String,
            message: String,
            notificationType: NotificationType
        ) {
            val notification = Notification(NOTIFICATION_GROUP_ID, title, message, notificationType)
            notification.notify(project)
        }

        fun runCommand(
            project: Project,
            command: String,
            skipLines: Int = 2,
            dir: String = project.basePath!!
        ): Triple<Boolean, String, String> {
            val process = Runtime.getRuntime().exec(command, null, File(dir))
            val successful = process.waitFor() == 0

            val scanner = Scanner(process.inputStream)

            var output = ""
            var outputTrimmed = ""
            var line = 0
            while (scanner.hasNextLine()) {
                val lineString = scanner.nextLine()

                output += "$lineString\n"

                if (line < skipLines) {
                    line++
                    continue
                }

                outputTrimmed += "$lineString\n"

                line++
            }

            val errorMessage: String
            when {
                output.contains("Project file does not exist") -> {
                    val errorProject = StringUtils.substringBetween(output, "Switch: ", "\n")

                    errorMessage =
                        "The project '$errorProject' doesn't exist or is not an SDK-style project. Please make sure it's a valid project type."
                }
                output.contains("Unable to create an object of type") -> {
                    errorMessage =
                        "Unable to locate a valid DbContext. Please make sure you've selected a valid startup project and that Program.cs and Startup.cs are configured properly."
                }
                output.contains("Could not load assembly") -> {
                    val dataProject = StringUtils.substringBetween(output, "assembly '", "'")
                    val startupProject = StringUtils.substringBetween(output, "project '", "'")

                    errorMessage =
                        "Could not load '$dataProject' as a data project. Ensure it is referenced by the startup project '$startupProject'"
                }
                else -> {
                    errorMessage = ""
                }
            }

            if (successful)
                VirtualFileManager.getInstance().refreshWithoutFileWatcher(true)

            return Triple(successful, outputTrimmed, errorMessage)
        }

        fun buildDotnetCommand(commandSection: String, startupProject: String, dataProject: String): String {
            return "dotnet ef $commandSection -s \"$startupProject\" -p \"$dataProject\""
        }
    }
}
