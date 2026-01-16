package com.github.eischet.janitoridea.language.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

public class JanitorNamedElementImpl extends JanitorPsiElement implements JanitorNamedElement {
    public JanitorNamedElementImpl(ASTNode node) {
        super(node);
    }

    @Override
    public String getName() {
        PsiElement identifier = getNameIdentifier();
        return identifier != null ? identifier.getText() : null;
    }

    @Override
    public @Nullable PsiElement getNameIdentifier() {
        ASTNode identifierNode = getNode().findChildByType(JanitorTokenSets.IDENTIFIERS);
        return identifierNode != null ? identifierNode.getPsi() : null;
    }

    @Override
    public PsiElement setName(String name) {
        PsiElement identifier = getNameIdentifier();
        if (identifier == null) {
            return this;
        }
        PsiElement replacement = JanitorElementFactory.createIdentifier(getProject(), name);
        identifier.replace(replacement);
        return this;
    }
}
