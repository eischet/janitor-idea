package com.github.eischet.janitoridea.language.psi;

import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;

public class JanitorReferenceContributor extends PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement().withLanguage(com.github.eischet.janitoridea.language.JanitorLanguage.INSTANCE),
            new JanitorReferenceProvider()
        );
    }
}
