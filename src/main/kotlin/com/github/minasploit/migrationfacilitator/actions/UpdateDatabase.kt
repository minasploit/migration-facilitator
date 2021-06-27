package com.github.minasploit.migrationfacilitator.actions

import com.github.minasploit.migrationfacilitator.BaseAction
import com.intellij.openapi.actionSystem.AnActionEvent

class UpdateDatabase : BaseAction() {

    companion object {
        var IsEnabled = true
    }

    override fun actionPerformed(e: AnActionEvent) {
        UpdateDatabaseDialog(e.project!!).show()
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = IsEnabled

        super.update(e)
    }
}
