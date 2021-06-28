package com.github.minasploit.migrationfacilitator.extensions

import com.github.minasploit.migrationfacilitator.Util
import com.intellij.notification.NotificationType
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task.Backgroundable
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity

class StartupListener : StartupActivity {
    override fun runActivity(project: Project) {
        Util.disableAllButtons()

        ProgressManager.getInstance()
            .run(object : Backgroundable(project, "Checking if EF Command line tool is installed...") {
                override fun run(indicator: ProgressIndicator) {
                    indicator.isIndeterminate = false
                    indicator.fraction = 0.33

                    val (success) = Util.runCommand(project, "dotnet ef", 2, project.basePath!!, false)
                    if (!success) {
                        indicator.fraction = 0.66
                        indicator.text = "Installing EF Command line tool..."

                        val (installSuccess) = Util.runCommand(
                            project,
                            "dotnet tool install --global dotnet-ef",
                            2, project.basePath!!, false
                        )

                        if (installSuccess) {
                            Util.showNotification(
                                project,
                                "Dotnet ef installed",
                                "Entity Framework Core .NET Command-line Tool installed successfully",
                                NotificationType.INFORMATION
                            )

                            Util.enableAllButtons()
                        } else {
                            Util.showNotification(
                                project,
                                "Installation failed",
                                "Couldn't install Entity Framework Core .NET Command-line Tool. Please install it manually and restart the IDE.",
                                NotificationType.ERROR
                            )
                        }
                    } else {
                        Util.enableAllButtons()
                    }

                    indicator.fraction = 1.0
                }
            })
    }
}
