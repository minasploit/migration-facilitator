package com.github.minasploit.migrationfacilitator

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JSeparator

abstract class BaseDialogWrapper(project: Project, canBeParent: Boolean) : DialogWrapper(project, canBeParent) {
    val properties: PropertiesComponent = PropertiesComponent.getInstance(project)

    val startupProjectInputLabel = JLabel("Startup Project. Ex: Solution.StartupProject", JLabel.TRAILING)

    val dataProjectInputLabel =
        JLabel("Data Project, the project where you store the DbContext. Ex: Solution.DataProject", JLabel.TRAILING)

    val separator = JSeparator()

    val startupProjectSelector = com.intellij.openapi.ui.ComboBox<String>()
    val dataProjectSelector = com.intellij.openapi.ui.ComboBox<String>()

    init {
        Util.getProjectsInSolutionByCommand(project).forEach {
            startupProjectSelector.addItem(it)
            dataProjectSelector.addItem(it)
        }
        startupProjectSelector.selectedItem = properties.getValue(STARTUP_PROJECT, project.name)
        dataProjectSelector.selectedItem = properties.getValue(DATA_PROJECT, project.name)

        startupProjectSelector.isEditable = true
        dataProjectSelector.isEditable = true

        separator.size = Dimension(10, 20)
    }

    fun addDefaultUi(panel: JComponent) {
        panel.add(startupProjectInputLabel)
        panel.add(startupProjectSelector)
        panel.add(dataProjectInputLabel)
        panel.add(dataProjectSelector)
    }
}
