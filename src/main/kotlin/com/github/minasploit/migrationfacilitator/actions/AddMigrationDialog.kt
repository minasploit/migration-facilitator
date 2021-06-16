package com.github.minasploit.migrationfacilitator.actions

import java.awt.Dimension
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JSeparator
import javax.swing.JTextField
import java.util.function.Consumer
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

class AddMigrationDialog(private val project: Project) : DialogWrapper(project, true) {

    private val useUnderscoreRatherThanCamelCase = false
    private val properties: PropertiesComponent = PropertiesComponent.getInstance(project)
    private val migrationNameInput = JTextField()
    private val startupProjectInput = JTextField(properties.getValue(STARTUP_PROJECT, project.name))
    private val dataProjectInput = JTextField(properties.getValue(DATA_PROJECT, project.name))

    override fun createCenterPanel(): JComponent {
        val dialogPanel = JPanel()
        dialogPanel.layout = BoxLayout(dialogPanel, BoxLayout.PAGE_AXIS)
        dialogPanel.preferredSize = Dimension(75, 0)

        val description = JLabel("This allows you to create a migration from the specified startup and data projects")
//        description.preferredSize = Dimension()

        val migrationNameInputLabel = JLabel("Migration Name", JLabel.TRAILING)
//        migrationNameInputLabel.preferredSize = Dimension()

        val startupProjectInputLabel = JLabel("Startup Project. Ex: Solution.StartupProject", JLabel.TRAILING)
//        startupProjectInputLabel.preferredSize = Dimension()

        val dataProjectInputLabel =
            JLabel("Data Project, the project where you store the DbContext. Ex: Solution.DataProject", JLabel.TRAILING)
//        dataProjectInputLabel.preferredSize = Dimension()

        val separator = JSeparator()
        separator.size = Dimension(10, 20)

        migrationNameInput.preferredSize = Dimension(75, 25)
        migrationNameInputLabel.labelFor = migrationNameInput

        startupProjectInput.preferredSize = Dimension(75, 25)
        startupProjectInputLabel.labelFor = startupProjectInput

        dataProjectInput.preferredSize = Dimension(75, 25)
        dataProjectInputLabel.labelFor = dataProjectInput

        dialogPanel.add(description)
        dialogPanel.add(separator)
        dialogPanel.add(migrationNameInputLabel)
        dialogPanel.add(migrationNameInput)
        dialogPanel.add(startupProjectInputLabel)
        dialogPanel.add(startupProjectInput)
        dialogPanel.add(dataProjectInputLabel)
        dialogPanel.add(dataProjectInput)

        return dialogPanel
    }

    override fun getPreferredFocusedComponent(): JComponent? {
        return migrationNameInput
    }

    override fun doOKAction() {
        var migrationName = migrationNameInput.text

        if (migrationName.isNullOrBlank()) {
            Messages.showErrorDialog("Input the migration name", "Error")
            migrationNameInput.requestFocusInWindow()
            return
        }

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

        properties.setValue(STARTUP_PROJECT, startupProjectInput.text)
        properties.setValue(DATA_PROJECT, dataProjectInput.text)

        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Adding new migration...") {
            override fun run(indicator: ProgressIndicator) {
                // start your process

                val (success, output, errorMessage) = Util.runCommand(
                    project,
                    "dotnet ef migrations add $migrationName -s ${startupProjectInput.text} -p ${dataProjectInput.text}"
                )
                if (success) {
                    Util.showNotification(
                        project,
                        "Migration created",
                        "Migration: '$migrationName' created in the project ${dataProjectInput.text}",
                        NotificationType.INFORMATION,
                        NotificationDisplayType.BALLOON,
                        Messages.getInformationIcon()
                    )
                } else {
                    Util.showNotification(
                        project,
                        "Can't create migration",
                        if (errorMessage != "") errorMessage else output,
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
        title = "Add A New Migration"
    }
}
