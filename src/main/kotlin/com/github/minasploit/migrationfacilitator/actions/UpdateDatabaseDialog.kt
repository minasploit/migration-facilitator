package com.github.minasploit.migrationfacilitator.actions

import com.github.minasploit.migrationfacilitator.BaseDialogWrapper
import com.github.minasploit.migrationfacilitator.REMOVE_ALL_MIGRATIONS
import com.github.minasploit.migrationfacilitator.DATA_PROJECT
import com.github.minasploit.migrationfacilitator.STARTUP_PROJECT
import com.github.minasploit.migrationfacilitator.Util
import com.intellij.notification.NotificationType
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import java.awt.Dimension
import java.awt.event.ActionEvent
import javax.swing.AbstractAction
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

class UpdateDatabaseDialog(private val project: Project) : BaseDialogWrapper(project, true) {
    private val migrationSelector = com.intellij.openapi.ui.ComboBox<String>()
    private val refreshMigrationButton = JButton()

    override fun createCenterPanel(): JComponent {
        val dialogPanel = JPanel()
        dialogPanel.layout = BoxLayout(dialogPanel, BoxLayout.PAGE_AXIS)
        dialogPanel.preferredSize = Dimension(75, 0)

        val description = JLabel("This allows you to update the database to a specific migration")

        refreshMigrationButton.action = object : AbstractAction() {
            override fun actionPerformed(e: ActionEvent?) {
                refreshMigrationList()
            }
        }
        refreshMigrationButton.text = "Refresh Migrations"
        refreshMigrationButton.isVisible = false

        refreshMigrationList()

        dialogPanel.add(description)
        dialogPanel.add(separator)
        dialogPanel.add(migrationSelector)
        addDefaultUi(dialogPanel)
        dialogPanel.add(refreshMigrationButton)

        return dialogPanel
    }

    // fetch all available migrations and add them to the selector
    fun refreshMigrationList() {
        Util.disableAllButtons()

        ProgressManager.getInstance().runProcessWithProgressSynchronously(
            {
                val (success, output, errorMessage) = Util.runCommand(
                    project,
                    Util.buildDotnetCommand(
                        "migrations list",
                        startupProjectSelector.item,
                        dataProjectSelector.item,
                        false
                    ),
                    2, project.basePath!!, false
                )

                if (success) {
                    var migrationItems = mutableListOf<String>()
                    migrationItems.add(REMOVE_ALL_MIGRATIONS)
                    migrationItems.addAll(
                        output.split("\n")
                    )

                    if (errorMessage.contains("An error occurred while accessing the database")) {
                        // remove the error message from the migration list

                        // 0 -> Remove All Migrations
                        // 1 -> An error occurred while accessing the database...
                        // 2... -> Other migrations

                        migrationItems.removeAt(1)
                    } else if (errorMessage == "No Migrations found.") {
                        // no migrations in the project

                        migrationItems = migrationItems.dropLast(1).toMutableList()
                    }

                    migrationItems.forEach {
                        migrationSelector.addItem(it)
                    }

                    migrationSelector.selectedIndex = migrationSelector.itemCount - 1

                    properties.setValue(STARTUP_PROJECT, startupProjectSelector.item)
                    properties.setValue(DATA_PROJECT, dataProjectSelector.item)

                    refreshMigrationButton.isVisible = false
                } else {
                    Util.showNotification(
                        project,
                        "Failed to load migrations",
                        if (errorMessage != "") errorMessage else output,
                        NotificationType.ERROR
                    )

                    refreshMigrationButton.isVisible = true
                }

                Util.enableAllButtons()
            },
            "Loading existing migrations...", false, project, contentPanel
        )
    }

    override fun doOKAction() {
        val item = migrationSelector.selectedItem as String

        val migrationName =
            if (item == REMOVE_ALL_MIGRATIONS) "0" else item.split(" ")[0]

        if (migrationName == "0") {
            val stopOperation = Messages.showOkCancelDialog(
                contentPanel,
                "Are you sure you want to reset the database?",
                "Confirmation",
                "Continue",
                "Cancel",
                Messages.getQuestionIcon()
            ) != Messages.YES

            if (stopOperation)
                return
        }

        Util.disableAllButtons()

        properties.setValue(STARTUP_PROJECT, startupProjectSelector.item)
        properties.setValue(DATA_PROJECT, dataProjectSelector.item)

        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Updating database...") {
            override fun run(indicator: ProgressIndicator) {
                // start your process
                val (success, output, errorMessage) = Util.runCommand(
                    project,
                    Util.buildDotnetCommand(
                        "database update \"$migrationName\"",
                        startupProjectSelector.item,
                        dataProjectSelector.item
                    ),
                    0
                )
                if (success) {
                    Util.showNotification(
                        project,
                        "Database updated",
                        if (migrationName == "0") "All migrations have been removed from the database."
                        else "Migration: '$migrationName' applied to database referenced in the project '${dataProjectSelector.item}'",
                        NotificationType.INFORMATION
                    )
                } else {
                    Util.showNotification(
                        project,
                        "Can't update database",
                        if (errorMessage != "") errorMessage else output,
                        NotificationType.ERROR
                    )
                }

                Util.enableAllButtons()
            }
        })

        super.doOKAction()
    }

    init {
        init()
        title = "Update Database"
    }
}
