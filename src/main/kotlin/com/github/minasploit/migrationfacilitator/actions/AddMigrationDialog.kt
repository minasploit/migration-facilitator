package com.github.minasploit.migrationfacilitator.actions

import com.github.minasploit.migrationfacilitator.BaseDialogWrapper
import java.awt.Dimension
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField
import java.util.function.Consumer
import com.github.minasploit.migrationfacilitator.DATA_PROJECT
import com.github.minasploit.migrationfacilitator.STARTUP_PROJECT
import com.github.minasploit.migrationfacilitator.Util
import com.intellij.notification.NotificationType
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages

class AddMigrationDialog(private val project: Project) : BaseDialogWrapper(project, true) {

    private val useUnderscoreRatherThanCamelCase = false
    private val migrationNameInput = JTextField()

    override fun createCenterPanel(): JComponent {
        val dialogPanel = JPanel()
        dialogPanel.layout = BoxLayout(dialogPanel, BoxLayout.PAGE_AXIS)
        dialogPanel.preferredSize = Dimension(75, 0)

        val description = JLabel("This allows you to create a migration from the specified startup and data projects")

        val migrationNameInputLabel = JLabel("Migration Name", JLabel.TRAILING)

        migrationNameInput.preferredSize = Dimension(75, 25)
        migrationNameInputLabel.labelFor = migrationNameInput

        dialogPanel.add(description)
        dialogPanel.add(separator)
        dialogPanel.add(migrationNameInputLabel)
        dialogPanel.add(migrationNameInput)
        addDefaultUi(dialogPanel)

        return dialogPanel
    }

    override fun getPreferredFocusedComponent(): JComponent {
        return migrationNameInput
    }

    override fun doOKAction() {
        var migrationName = migrationNameInput.text

        if (migrationName.isBlank()) {
            Messages.showErrorDialog("Input the migration name", "Error")
            migrationNameInput.requestFocusInWindow()
            return
        }

        Util.disableAllButtons()

        if (useUnderscoreRatherThanCamelCase) {
            migrationName = migrationName.replace(" ", "_")
        } else {
            var fixedMigrationName = ""
            migrationName.split(" ").forEach(
                Consumer {
                    fixedMigrationName += it.capitalize()
                }
            )
            migrationName = fixedMigrationName
        }

        properties.setValue(STARTUP_PROJECT, startupProjectSelector.selectedItem as String)
        properties.setValue(DATA_PROJECT, dataProjectSelector.selectedItem as String)

        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Adding new migration...") {
            override fun run(indicator: ProgressIndicator) {
                val (success, output, errorMessage) = Util.runCommand(
                    project,
                    Util.buildDotnetCommand(
                        "migrations add \"$migrationName\"",
                        startupProjectSelector.selectedItem as String,
                        dataProjectSelector.selectedItem as String
                    )
                )
                if (success) {
                    Util.showNotification(
                        project,
                        "Migration created",
                        "Migration: '$migrationName' created in the project '${dataProjectSelector.selectedItem as String}'",
                        NotificationType.INFORMATION
                    )
                } else {
                    Util.showNotification(
                        project,
                        "Can't create migration",
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
        title = "Add A New Migration"
    }
}
