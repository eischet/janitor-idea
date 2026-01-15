package com.github.eischet.janitoridea.actions

import com.github.eischet.janitoridea.language.JanitorFileType
import com.github.eischet.janitoridea.repl.JanitorReplService
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.vfs.VfsUtilCore

class JanitorRunFileAction : AnAction() {
    init {
        templatePresentation.icon = AllIcons.Actions.Execute
        templatePresentation.text = "Run Janitor Script"
        templatePresentation.description = "Run current Janitor file in the REPL"
    }

    override fun update(e: AnActionEvent) {
        val vFile = e.getData(CommonDataKeys.VIRTUAL_FILE)
        val enabled = vFile != null && vFile.fileType == JanitorFileType.INSTANCE
        e.presentation.isEnabledAndVisible = enabled
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val psiFile = e.getData(CommonDataKeys.PSI_FILE) ?: return
        if (psiFile.fileType != JanitorFileType.INSTANCE) {
            return
        }
        val vFile = psiFile.virtualFile ?: return
        val document = FileDocumentManager.getInstance().getDocument(vFile)
        val text = document?.text ?: runCatching { VfsUtilCore.loadText(vFile) }.getOrNull() ?: return
        project.service<JanitorReplService>().runScript(text, vFile.name)
    }
}
