package com.github.eischet.janitoridea.language;

import com.github.eischet.janitoridea.language.psi.JanitorNamedElement;
import com.github.eischet.janitoridea.language.psi.JanitorTokenSets;
import com.github.eischet.janitoridea.lexer.JanitorLexerAdapter;
import com.intellij.lang.cacheBuilder.DefaultWordsScanner;
import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.psi.PsiElement;

public class JanitorFindUsagesProvider implements FindUsagesProvider {
    @Override
    public WordsScanner getWordsScanner() {
        return new DefaultWordsScanner(new JanitorLexerAdapter(), JanitorTokenSets.IDENTIFIERS, JanitorTokenSets.COMMENTS, JanitorTokenSets.STRINGS);
    }

    @Override
    public boolean canFindUsagesFor(PsiElement psiElement) {
        return psiElement instanceof JanitorNamedElement;
    }

    @Override
    public String getHelpId(PsiElement psiElement) {
        return null;
    }

    @Override
    public String getType(PsiElement element) {
        return "symbol";
    }

    @Override
    public String getDescriptiveName(PsiElement element) {
        if (element instanceof JanitorNamedElement namedElement && namedElement.getName() != null) {
            return namedElement.getName();
        }
        return element.getText();
    }

    @Override
    public String getNodeText(PsiElement element, boolean useFullName) {
        return element.getText();
    }
}
