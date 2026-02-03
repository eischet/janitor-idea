package com.github.eischet.janitoridea.language.doc;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JMap;
import com.eischet.janitor.api.types.builtin.JNull;
import com.eischet.janitor.api.types.builtin.JString;
import com.eischet.janitor.idea.JanitorManifestResolver;
import com.github.eischet.janitoridea.language.psi.JanitorNamedElement;
import com.intellij.lang.documentation.DocumentationProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.editor.Editor;
import org.jetbrains.annotations.Nullable;

public class JanitorDocumentationProvider implements DocumentationProvider {
    @Override
    public @Nullable String generateDoc(PsiElement element, @Nullable PsiElement originalElement) {
        // Debug logs disabled; re-enable if documentation lookup needs tracing.
        if (element == null) {
            return null;
        }
        PsiFile file = element.getContainingFile();
        if (file == null) {
            return null;
        }
        Project project = element.getProject();
        VirtualFile virtualFile = file.getVirtualFile();
        String name = element instanceof JanitorNamedElement named ? named.getName() : element.getText();
        if (name == null || name.isBlank()) {
            return null;
        }

        JMap manifest = JanitorManifestResolver.resolveForFile(project, virtualFile, message -> {
            com.intellij.openapi.diagnostic.Logger.getInstance(JanitorDocumentationProvider.class).info(message);
        });
        JanitorObject globalsObj = manifest.get(Janitor.string("globals"));
        if (!(globalsObj instanceof JMap globals)) {
            return null;
        }
        JanitorObject entryObj = globals.get(Janitor.string(name));
        if (!(entryObj instanceof JMap entry)) {
            return null;
        }
        JanitorObject docObj = entry.get(Janitor.string("doc"));
        if (docObj == null || docObj instanceof JNull) {
            return null;
        }
        if (docObj instanceof JString) {
            return docObj.janitorToString();
        }
        return docObj.janitorToString();
    }

    @Override
    public @Nullable String getQuickNavigateInfo(PsiElement element, PsiElement originalElement) {
        // Debug logs disabled; re-enable if documentation lookup needs tracing.
        return null;
    }

    @Override
    public @Nullable PsiElement getCustomDocumentationElement(Editor editor, PsiFile file, PsiElement contextElement, int targetOffset) {
        // Debug logs disabled; re-enable if documentation lookup needs tracing.
        return contextElement;
    }
}
