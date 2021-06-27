package com.github.minasploit.migrationfacilitator

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages

abstract class BaseAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        if (e.project == null) {
            return Messages.showMessageDialog(
                "A project needs to be loaded to use this action",
                "Error",
                Messages.getErrorIcon()
            )
        }
    }
}
