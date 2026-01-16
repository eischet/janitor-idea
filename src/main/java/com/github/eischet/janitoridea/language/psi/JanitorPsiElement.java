package com.github.eischet.janitoridea.language.psi;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;

public class JanitorPsiElement extends ASTWrapperPsiElement {
    public JanitorPsiElement(ASTNode node) {
        super(node);
    }
}
