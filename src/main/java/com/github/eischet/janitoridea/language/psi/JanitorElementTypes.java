package com.github.eischet.janitoridea.language.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;

public final class JanitorElementTypes {
    public static final IElementType FUNCTION_DECLARATION = new JanitorElementType("FUNCTION_DECLARATION");
    public static final IElementType IMPORT_ALIAS = new JanitorElementType("IMPORT_ALIAS");
    public static final IElementType FORMAL_PARAMETER = new JanitorElementType("FORMAL_PARAMETER");
    public static final IElementType LOOP_VARIABLE = new JanitorElementType("LOOP_VARIABLE");
    public static final IElementType CATCH_PARAMETER = new JanitorElementType("CATCH_PARAMETER");
    public static final IElementType LAMBDA_PARAMETER = new JanitorElementType("LAMBDA_PARAMETER");
    public static final IElementType ASSIGNMENT_TARGET = new JanitorElementType("ASSIGNMENT_TARGET");

    private JanitorElementTypes() {
    }

    public static PsiElement createPsiElement(ASTNode node) {
        IElementType type = node.getElementType();
        if (type == FUNCTION_DECLARATION) {
            return new JanitorFunctionDeclaration(node);
        }
        if (type == IMPORT_ALIAS) {
            return new JanitorImportAlias(node);
        }
        if (type == FORMAL_PARAMETER) {
            return new JanitorFormalParameter(node);
        }
        if (type == LOOP_VARIABLE) {
            return new JanitorLoopVariable(node);
        }
        if (type == CATCH_PARAMETER) {
            return new JanitorCatchParameter(node);
        }
        if (type == LAMBDA_PARAMETER) {
            return new JanitorLambdaParameter(node);
        }
        if (type == ASSIGNMENT_TARGET) {
            return new JanitorAssignmentTarget(node);
        }
        return new JanitorPsiElement(node);
    }
}
