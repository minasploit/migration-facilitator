package com.github.minasploit.migrationfacilitator.actions

import com.github.minasploit.migrationfacilitator.BaseDialogWrapper
import com.github.minasploit.migrationfacilitator.DATA_PROJECT
import com.github.minasploit.migrationfacilitator.STARTUP_PROJECT
import com.github.minasploit.migrationfacilitator.Util
import com.intellij.notification.NotificationType
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import org.apache.commons.lang.StringUtils
import java.awt.Dimension
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JLabel

class RemoveMigrationDialog(private val project: Project) : BaseDialogWrapper(project, true) {

    override fun createCenterPanel(): JComponent {
        val dialogPanel = JPanel()
        dialogPanel.layout = BoxLayout(dialogPanel, BoxLayout.PAGE_AXIS)
        dialogPanel.preferredSize = Dimension(75, 0)

        val description = JLabel("This allows you to remove the last migration which isn't applied to the database.")

        dialogPanel.add(description)
        dialogPanel.add(separator)
        addDefaultUi(dialogPanel)

        return dialogPanel
    }

    override fun doOKAction() {
        val confirmationResult = Messages.showOkCancelDialog(
            contentPanel,
            "Are you sure you want to remove the last migration?",
            "Confirmation",
            "Remove",
            "Cancel",
            Messages.getQuestionIcon()
        )
        if (confirmationResult != Messages.YES) return

        Util.disableAllButtons()

        properties.setValue(STARTUP_PROJECT, startupProjectSelector.selectedItem as String)
        properties.setValue(DATA_PROJECT, dataProjectSelector.selectedItem as String)

        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Removing last migration...") {
            override fun run(indicator: ProgressIndicator) {
                val (success, output, errorMessage) = Util.runCommand(
                    project,
                    Util.buildDotnetCommand(
                        "migrations remove",
                        startupProjectSelector.selectedItem as String,
                        dataProjectSelector.selectedItem as String,
                        false
                    )
                )
                if (success) {
                    val migrationName = StringUtils.substringBetween(output, "'", "'")
                    Util.showNotification(
                        project,
                        "Migration removed",
                        "Last Migration: $migrationName removed from the project '${dataProjectSelector.selectedItem as String}'",
                        NotificationType.INFORMATION
                    )
                } else {
                    Util.showNotification(
                        project,
                        "Can't remove migration",
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
        title = "Remove The Last Migration"
    }
}
