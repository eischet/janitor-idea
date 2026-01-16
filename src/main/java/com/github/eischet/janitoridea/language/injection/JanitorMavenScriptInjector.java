package com.github.eischet.janitoridea.language.injection;

import com.github.eischet.janitoridea.language.JanitorLanguage;
import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlText;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class JanitorMavenScriptInjector implements MultiHostInjector {
    private static final String GROUP_ID = "com.eischet.janitor";
    private static final String ARTIFACT_ID = "janitor-maven-plugin";

    @Override
    public void getLanguagesToInject(@NotNull MultiHostRegistrar registrar, @NotNull PsiElement context) {
        if (!(context instanceof PsiLanguageInjectionHost host)) {
            return;
        }
        XmlTag tag = PsiTreeUtil.getParentOfType(context, XmlTag.class);
        if (tag == null || !"script".equals(tag.getName())) {
            return;
        }
        if (!isPomXml(tag) || !hasAncestorTag(tag, "configuration") || !isJanitorPlugin(tag)) {
            return;
        }

        registrar.startInjecting(JanitorLanguage.INSTANCE);
        registrar.addPlace(null, null, host, TextRange.create(0, host.getTextLength()));
        registrar.doneInjecting();
    }

    @Override
    public @NotNull List<Class<? extends PsiElement>> elementsToInjectIn() {
        return List.of(XmlText.class);
    }

    private static boolean isPomXml(XmlTag tag) {
        if (!(tag.getContainingFile() instanceof XmlFile xmlFile)) {
            return false;
        }
        return "pom.xml".equalsIgnoreCase(xmlFile.getName());
    }

    private static boolean hasAncestorTag(XmlTag tag, String name) {
        XmlTag current = tag.getParentTag();
        while (current != null) {
            if (name.equals(current.getName())) {
                return true;
            }
            current = current.getParentTag();
        }
        return false;
    }

    private static boolean isJanitorPlugin(XmlTag tag) {
        XmlTag current = tag;
        while (current != null) {
            if ("plugin".equals(current.getName())) {
                String groupId = current.getSubTagText("groupId");
                String artifactId = current.getSubTagText("artifactId");
                return GROUP_ID.equals(groupId) && ARTIFACT_ID.equals(artifactId);
            }
            current = current.getParentTag();
        }
        return false;
    }
}
