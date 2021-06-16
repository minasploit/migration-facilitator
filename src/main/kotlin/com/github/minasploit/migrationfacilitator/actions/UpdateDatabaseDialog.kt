package com.github.minasploit.migrationfacilitator.actions

import com.github.minasploit.migrationfacilitator.DATA_PROJECT
import com.github.minasploit.migrationfacilitator.STARTUP_PROJECT
import com.github.minasploit.migrationfacilitator.Util
import com.intellij.ide.util.PropertiesComponent
import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationType
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import java.awt.Dimension
import java.awt.event.ActionEvent
import javax.swing.AbstractAction
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JSeparator
import javax.swing.JTextField
import javax.swing.SwingUtilities

class UpdateDatabaseDialog(private val project: Project) : DialogWrapper(project, true) {
    private val properties: PropertiesComponent = PropertiesComponent.getInstance(project)
    private val migrationSelector = com.intellij.openapi.ui.ComboBox<String>()
    private val startupProjectInput = JTextField(properties.getValue(STARTUP_PROJECT, project.name))
    private val dataProjectInput = JTextField(properties.getValue(DATA_PROJECT, project.name))
    private val refreshMigrationButton = JButton()

    override fun createCenterPanel(): JComponent {
        val dialogPanel = JPanel()
        dialogPanel.layout = BoxLayout(dialogPanel, BoxLayout.PAGE_AXIS)
        dialogPanel.preferredSize = Dimension(75, 0)

        val description = JLabel("This allows you to update the database to a specific migration")
//        description.preferredSize = Dimension()

        val startupProjectInputLabel = JLabel("Startup Project. Ex: Solution.StartupProject", JLabel.TRAILING)
//        startupProjectInputLabel.preferredSize = Dimension()

        val dataProjectInputLabel =
            JLabel("Data Project, the project where you store the DbContext. Ex: Solution.DataProject", JLabel.TRAILING)
//        dataProjectInputLabel.preferredSize = Dimension()

        val separator = JSeparator()
        separator.size = Dimension(10, 20)

        startupProjectInput.preferredSize = Dimension(75, 25)
        startupProjectInputLabel.labelFor = startupProjectInput

        dataProjectInput.preferredSize = Dimension(75, 25)
        dataProjectInputLabel.labelFor = dataProjectInput

        refreshMigrationButton.action = object : AbstractAction() {
            override fun actionPerformed(e: ActionEvent?) {
                refreshMigrationList()
            }
        }
        refreshMigrationButton.text = "Refresh Migrations"
        refreshMigrationButton.isVisible = false

        SwingUtilities.invokeLater {
            refreshMigrationList()
        }

        dialogPanel.add(description)
        dialogPanel.add(separator)
        dialogPanel.add(migrationSelector)
        dialogPanel.add(startupProjectInputLabel)
        dialogPanel.add(startupProjectInput)
        dialogPanel.add(dataProjectInputLabel)
        dialogPanel.add(dataProjectInput)
        dialogPanel.add(refreshMigrationButton)

        return dialogPanel
    }

    // fetch all available migrations and add them to the selector
    fun refreshMigrationList() {
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Loading existing migrations...") {
            override fun run(indicator: ProgressIndicator) {
                // start your process

                val (success, output, errorMessage) = Util.runCommand(
                    project,
                    "dotnet ef migrations list -s ${startupProjectInput.text} -p ${dataProjectInput.text}"
                )

                if (success) {
                    val migrationItems = mutableListOf<String>()
                    migrationItems.add("REMOVE ALL APPLIED MIGRATIONS")
                    migrationItems.addAll(
                        output.split("\n")
                            .dropLast(1)
                    )

                    migrationItems.forEach {
                        migrationSelector.addItem(it)
                    }

                    migrationSelector.selectedIndex = migrationSelector.itemCount - 1

                    properties.setValue(STARTUP_PROJECT, startupProjectInput.text)
                    properties.setValue(DATA_PROJECT, dataProjectInput.text)

                    refreshMigrationButton.isVisible = false
                } else {
                    Util.showNotification(
                        project,
                        "Failed to load migrations",
                        if (errorMessage != "") errorMessage else output,
                        NotificationType.ERROR,
                        NotificationDisplayType.BALLOON,
                        Messages.getErrorIcon()
                    )

                    refreshMigrationButton.isVisible = true
                }
            }
        })
    }

    override fun doOKAction() {
        val item = migrationSelector.selectedItem as String

        val migrationName =
            if (item == "REMOVE ALL APPLIED MIGRATIONS") "0" else item.split(" ")[0]

        properties.setValue(STARTUP_PROJECT, startupProjectInput.text)
        properties.setValue(DATA_PROJECT, dataProjectInput.text)

        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Updating database...") {
            override fun run(indicator: ProgressIndicator) {
                // start your process
                val (success, output) = Util.runCommand(
                    project,
                    "dotnet ef database update $migrationName -s ${startupProjectInput.text} -p ${dataProjectInput.text}",
                    0
                )
                if (success) {
                    Util.showNotification(
                        project,
                        "Database updated",
                        if (migrationName == "0") "All migrations have been removed and the database has been reset."
                        else "Migration: '$migrationName' applied to database referenced in the project ${dataProjectInput.text}",
                        NotificationType.INFORMATION,
                        NotificationDisplayType.BALLOON,
                        Messages.getInformationIcon()
                    )
                } else {
                    Util.showNotification(
                        project,
                        "Can't update database",
                        output,
                        NotificationType.ERROR,
                        NotificationDisplayType.BALLOON,
                        Messages.getErrorIcon()
                    )
                }
            }
        })

        super.doOKAction()
    }

    init {
        init()
        title = "Update Database"
    }
}