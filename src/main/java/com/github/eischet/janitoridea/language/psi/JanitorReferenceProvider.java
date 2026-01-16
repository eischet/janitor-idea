package com.github.eischet.janitoridea.language.psi;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.util.ProcessingContext;

public class JanitorReferenceProvider extends PsiReferenceProvider {
    @Override
    public PsiReference[] getReferencesByElement(PsiElement element, ProcessingContext context) {
        if (!JanitorTokenSets.isIdentifier(element.getNode() != null ? element.getNode().getElementType() : null)) {
            return PsiReference.EMPTY_ARRAY;
        }
        if (element.getParent() instanceof JanitorNamedElement) {
            return PsiReference.EMPTY_ARRAY;
        }
        return new PsiReference[] { new JanitorReference(element) };
    }

    private static final class JanitorReference extends PsiReferenceBase<PsiElement> {
        private JanitorReference(PsiElement element) {
            super(element, new TextRange(0, element.getTextLength()), false);
        }

        @Override
        public PsiElement resolve() {
            return JanitorScopeResolver.resolve(getElement());
        }

        @Override
        public PsiElement handleElementRename(String newElementName) {
            PsiElement replacement = JanitorElementFactory.createIdentifier(getElement().getProject(), newElementName);
            return getElement().replace(replacement);
        }

        @Override
        public Object[] getVariants() {
            return new Object[0];
        }
    }
}
