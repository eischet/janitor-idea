package com.github.eischet.janitoridea.lexer

import com.github.eischet.janitoridea.language.JanitorLanguage
import com.eischet.janitor.lang.JanitorLexer
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import org.antlr.v4.runtime.Token

class JanitorTokenType(debugName: String, val antlrTokenType: Int) : IElementType(debugName, JanitorLanguage)

object JanitorTokenTypes {
    private val cache = HashMap<Int, IElementType>()

    fun get(tokenType: Int): IElementType {
        if (tokenType == Token.INVALID_TYPE) {
            return TokenType.BAD_CHARACTER
        }
        if (tokenType == JanitorLexer.WS || tokenType == JanitorLexer.NEWLINE) {
            return TokenType.WHITE_SPACE
        }
        return cache.getOrPut(tokenType) {
            val name = JanitorLexer.VOCABULARY.getSymbolicName(tokenType) ?: "TOKEN_$tokenType"
            JanitorTokenType(name, tokenType)
        }
    }
}
