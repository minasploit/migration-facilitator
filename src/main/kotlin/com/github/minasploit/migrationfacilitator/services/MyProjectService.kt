package com.github.minasploit.migrationfacilitator.services

import com.github.minasploit.migrationfacilitator.MyBundle
import com.intellij.openapi.project.Project

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
