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
import org.apache.commons.lang.StringUtils
import java.awt.Dimension
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.JLabel
import javax.swing.JSeparator

class RemoveMigrationDialog(private val project: Project) : DialogWrapper(project, true) {
    private val properties: PropertiesComponent = PropertiesComponent.getInstance(project)
    private val startupProjectInput = JTextField(properties.getValue(STARTUP_PROJECT, project.name))
    private val dataProjectInput = JTextField(properties.getValue(DATA_PROJECT, project.name))

    override fun createCenterPanel(): JComponent {
        val dialogPanel = JPanel()
        dialogPanel.layout = BoxLayout(dialogPanel, BoxLayout.PAGE_AXIS)
        dialogPanel.preferredSize = Dimension(75, 0)

        val description = JLabel("This allows you to remove the last migration which isn't applied to the database.")
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

        // fetch all available migrations and add them to the selector

        dialogPanel.add(description)
        dialogPanel.add(separator)
        dialogPanel.add(startupProjectInputLabel)
        dialogPanel.add(startupProjectInput)
        dialogPanel.add(dataProjectInputLabel)
        dialogPanel.add(dataProjectInput)

        return dialogPanel
    }

    override fun doOKAction() {
        val confirmationResult = Messages.showConfirmationDialog(
            contentPanel,
            "Are you sure you want to remove the last migration?",
            "Confirmation",
            "Remove",
            "Cancel"
        )
        if (confirmationResult == Messages.YES) {
            properties.setValue(STARTUP_PROJECT, startupProjectInput.text)
            properties.setValue(DATA_PROJECT, dataProjectInput.text)

            ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Removing last migration...") {
                override fun run(indicator: ProgressIndicator) {
                    // start your process

                    val (success, output) = Util.runCommand(
                        project,
                        "dotnet ef migrations remove -s ${startupProjectInput.text} -p ${dataProjectInput.text}"
                    )
                    if (success) {
                        val migrationName = StringUtils.substringBetween(output, "'", "'")
                        Util.showNotification(
                            project,
                            "Migration removed",
                            "Last Migration: $migrationName removed from the project ${dataProjectInput.text}",
                            NotificationType.INFORMATION,
                            NotificationDisplayType.BALLOON,
                            Messages.getInformationIcon()
                        )
                    } else {
                        Util.showNotification(
                            project,
                            "Can't remove migration",
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
    }

    init {
        init()
        title = "Remove The Last Migration"
    }
}
