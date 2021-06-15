package com.github.minasploit.migrationfacilitator

import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager
import java.io.File
import java.util.Scanner
import javax.swing.Icon

class Util private constructor() {

    companion object {
        fun showNotification(
            project: Project,
            title: String,
            message: String,
            notificationType: NotificationType,
            notificationDisplayType: NotificationDisplayType,
            notificationGroupIcon: Icon? = null
        ) {
            val notificationGroup =
                NotificationGroup(NOTIFICATION_GROUP_ID, notificationDisplayType, true, null, notificationGroupIcon)
            val notification = notificationGroup.createNotification(title, message, notificationType)
            Notifications.Bus.notify(notification, project)
        }

        fun runCommand(
            project: Project,
            command: String,
            skipLines: Int = 2,
            dir: String = project.basePath!!
        ): Pair<Boolean, String> {
            val process = Runtime.getRuntime().exec(command, null, File(dir))

            val success = process.onExit().thenApply { p1: Process -> p1.exitValue() == 0 }

            val scanner = Scanner(process.inputStream)

            var output = ""
            var line = 0
            while (scanner.hasNextLine()) {
                if (line < skipLines) {
                    line++
                    scanner.nextLine()
                    continue
                }

                output += scanner.nextLine() + "\n"

                line++
            }

            val successful = success.get()
            if (successful)
                VirtualFileManager.getInstance().refreshWithoutFileWatcher(true)

            return Pair(successful, output)
        }
    }
}
