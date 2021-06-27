package com.github.minasploit.migrationfacilitator.actions

import com.github.minasploit.migrationfacilitator.BaseAction
import com.intellij.openapi.actionSystem.AnActionEvent

class VersionAction : BaseAction() {
    override fun actionPerformed(e: AnActionEvent) {
        // do nothing
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = false

        super.update(e)
    }
}
