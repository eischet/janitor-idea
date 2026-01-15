package com.github.eischet.janitoridea.repl

import com.github.eischet.janitoridea.language.JanitorFileType
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.icons.AllIcons
import com.intellij.psi.PsiElement

class JanitorRunLineMarkerProvider : LineMarkerProvider {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? = null

    override fun collectSlowLineMarkers(
        elements: MutableList<out PsiElement>,
        result: MutableCollection<in LineMarkerInfo<*>>
    ) {
        val file = elements.firstOrNull()?.containingFile ?: return
        if (file.fileType != JanitorFileType.INSTANCE) {
            return
        }
        val target = elements.firstOrNull { it.textRange.startOffset == 0 } ?: file.firstChild ?: return

        val handler = GutterIconNavigationHandler<PsiElement> { _, elt ->
            val psiFile = elt.containingFile ?: return@GutterIconNavigationHandler
            val vFile = psiFile.virtualFile ?: return@GutterIconNavigationHandler
            val document = FileDocumentManager.getInstance().getDocument(vFile)
            val text = document?.text ?: runCatching { VfsUtilCore.loadText(vFile) }.getOrNull() ?: return@GutterIconNavigationHandler
            psiFile.project.service<JanitorReplService>().runScript(text, vFile.name)
        }

        result.add(
            LineMarkerInfo(
                target,
                target.textRange,
                AllIcons.Actions.Execute,
                { "Run Janitor script" },
                handler,
                GutterIconRenderer.Alignment.LEFT,
                { "Run Janitor script" }
            )
        )
    }
}
