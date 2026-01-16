package com.github.eischet.janitoridea.language.psi;

import com.github.eischet.janitoridea.language.JanitorFileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.SyntaxTraverser;

public final class JanitorElementFactory {
    private JanitorElementFactory() {
    }

    public static PsiElement createIdentifier(Project project, String name) {
        PsiFile file = PsiFileFactory.getInstance(project).createFileFromText(
            "dummy.jan",
            JanitorFileType.Companion.getINSTANCE(),
            "function " + name + "() {}"
        );
        for (PsiElement element : SyntaxTraverser.psiTraverser(file)) {
            if (JanitorTokenSets.isIdentifier(element.getNode() != null ? element.getNode().getElementType() : null)) {
                return element;
            }
        }
        throw new IllegalStateException("Failed to create identifier for " + name);
    }
}
