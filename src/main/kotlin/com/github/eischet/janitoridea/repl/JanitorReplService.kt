package com.github.eischet.janitoridea.repl

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager

@Service(Service.Level.PROJECT)
class JanitorReplService(private val project: Project) {
    @Volatile
    private var panel: JanitorReplPanel? = null

    fun registerPanel(panel: JanitorReplPanel) {
        this.panel = panel
    }

    fun runScript(text: String, sourceName: String?) {
        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("JanitorRepl")
        toolWindow?.show()
        panel?.runScript(text, sourceName)
    }
}
