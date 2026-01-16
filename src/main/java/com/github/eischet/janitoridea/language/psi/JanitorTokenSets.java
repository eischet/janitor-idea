package com.github.eischet.janitoridea.language.psi;

import com.eischet.janitor.lang.JanitorLexer;
import com.github.eischet.janitoridea.lexer.JanitorTokenTypes;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;

public final class JanitorTokenSets {
    public static final TokenSet IDENTIFIERS = TokenSet.create(
        JanitorTokenTypes.INSTANCE.get(JanitorLexer.IDENTIFIER),
        JanitorTokenTypes.INSTANCE.get(JanitorLexer.FROM),
        JanitorTokenTypes.INSTANCE.get(JanitorLexer.TO),
        JanitorTokenTypes.INSTANCE.get(JanitorLexer.IN)
    );
    public static final TokenSet COMMENTS = TokenSet.create(
        JanitorTokenTypes.INSTANCE.get(JanitorLexer.COMMENT),
        JanitorTokenTypes.INSTANCE.get(JanitorLexer.LINE_COMMENT)
    );
    public static final TokenSet STRINGS = TokenSet.create(
        JanitorTokenTypes.INSTANCE.get(JanitorLexer.STRING_LITERAL_SINGLE),
        JanitorTokenTypes.INSTANCE.get(JanitorLexer.STRING_LITERAL_DOUBLE),
        JanitorTokenTypes.INSTANCE.get(JanitorLexer.STRING_LITERAL_TRIPLE_SINGLE),
        JanitorTokenTypes.INSTANCE.get(JanitorLexer.STRING_LITERAL_TRIPLE_DOUBLE)
    );

    private JanitorTokenSets() {
    }

    public static boolean isIdentifier(IElementType type) {
        return type != null && IDENTIFIERS.contains(type);
    }
}
