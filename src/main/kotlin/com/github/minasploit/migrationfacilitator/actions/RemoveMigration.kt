package com.github.minasploit.migrationfacilitator.actions

import com.github.minasploit.migrationfacilitator.BaseAction
import com.intellij.openapi.actionSystem.AnActionEvent

class RemoveMigration : BaseAction() {

    companion object {
        var IsEnabled = true
    }

    override fun actionPerformed(e: AnActionEvent) {
        RemoveMigrationDialog(e.project!!).show()
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = AddMigration.IsEnabled

        super.update(e)
    }
}
