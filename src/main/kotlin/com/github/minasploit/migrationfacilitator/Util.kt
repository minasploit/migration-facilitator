package com.github.minasploit.migrationfacilitator

import com.github.minasploit.migrationfacilitator.actions.AddMigration
import com.github.minasploit.migrationfacilitator.actions.RemoveMigration
import com.github.minasploit.migrationfacilitator.actions.UpdateDatabase
import com.intellij.notification.NotificationType
import com.intellij.notification.Notification
import com.intellij.openapi.fileEditor.impl.LoadTextUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
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
            dir: String = project.basePath!!,
            shouldRefreshFileManager: Boolean = true
        ): Triple<Boolean, String, String> {
            val process: Process
            val successful: Boolean
            try {
                process = Runtime.getRuntime().exec(command, null, File(dir))
                successful = process.waitFor() == 0
            } catch (ex: Exception) {
                return Triple(false, "", "")
            }

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
                        "The project '$errorProject' doesn't exist. Please make sure it's a valid project type."
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
                output.contains("doesn't match your migrations assembly") -> {
                    val dataProject = StringUtils.substringBetween(output, "assembly '", "'")
                    val userDataProject = StringUtils.substringBetween(output, "project '", "'")

                    errorMessage =
                        "The specified data project '$userDataProject' doesn't match the set up migration project '$dataProject'. Please use '$dataProject' as the data project."
                }
                output.contains("An error occurred while accessing the database.") -> {
                    errorMessage =
                        "An error occurred while accessing the database. Make sure you can access the database to ensure correct results."
                }
                output.toLowerCase().contains("no migrations were found") -> {
                    errorMessage = "No Migrations found."
                }
                else -> {
                    errorMessage = "Unknown error occurred. Try selecting the correct startup and data projects."
                }
            }

            if (successful && shouldRefreshFileManager)
                VirtualFileManager.getInstance().refreshWithoutFileWatcher(true)

            return Triple(successful, outputTrimmed.trim(), errorMessage.trim())
        }

        fun buildDotnetCommand(
            commandSection: String,
            startupProject: String,
            dataProject: String,
            build: Boolean = true
        ): String {
            var command = "dotnet ef $commandSection -s \"$startupProject\" -p \"$dataProject\""
            if (!build)
                command += " -- no-build"

            return command
        }

        fun enableAllButtons() {
            changeButtonsStatus(true)
        }

        fun disableAllButtons() {
            changeButtonsStatus(false)
        }

        private fun changeButtonsStatus(enable: Boolean) {
            AddMigration.IsEnabled = enable
            RemoveMigration.IsEnabled = enable
            UpdateDatabase.IsEnabled = enable
        }

        fun getProjectsInSolution(project: Project): List<String> {
            return try {
                val solutionDirectoryVirtualFile = LocalFileSystem.getInstance().findFileByPath(project.basePath!!)!!
                val solutionVirtualFile = solutionDirectoryVirtualFile.children.first { it.name.contains(".sln") }
                val solutionContent = LoadTextUtil.loadText(solutionVirtualFile).toString()
                val projectsDetail = StringUtils.substringsBetween(solutionContent, "Project", "EndProject")
                projectsDetail.map {
                    StringUtils.substringBetween(it, "= \"", "\"")
                }
            } catch (ex: Exception) {
                arrayListOf(project.name)
            }
        }
    }
}
