package com.github.eischet.janitoridea.lexer

import com.github.eischet.janitoridea.grammar.JanitorLexer
import com.intellij.lexer.LexerBase
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.Token

class JanitorLexerAdapter : LexerBase() {
    private var buffer: CharSequence = ""
    private var startOffset = 0
    private var endOffset = 0
    private var tokens: List<HighlightToken> = emptyList()
    private var tokenIndex = 0

    private var tokenType: IElementType? = null
    private var tokenStart = 0
    private var tokenEnd = 0

    private data class HighlightToken(val start: Int, val end: Int, val type: IElementType)

    override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
        this.buffer = buffer
        this.startOffset = startOffset
        this.endOffset = endOffset

        val text = buffer.subSequence(startOffset, endOffset).toString()
        val lexer = JanitorLexer(CharStreams.fromString(text))
        lexer.removeErrorListeners()

        val collected = ArrayList<HighlightToken>()
        var cursor = 0
        while (true) {
            val token = lexer.nextToken()
            if (token.type == Token.EOF) {
                break
            }
            val start = token.startIndex
            val end = token.stopIndex + 1
            if (start > cursor) {
                collected.add(HighlightToken(cursor, start, TokenType.BAD_CHARACTER))
            }
            if (start >= 0 && end > start) {
                collected.add(HighlightToken(start, end, JanitorTokenTypes.get(token.type)))
                cursor = end
            }
        }

        if (cursor < text.length) {
            collected.add(HighlightToken(cursor, text.length, TokenType.BAD_CHARACTER))
        }

        tokens = collected
        tokenIndex = 0
        advance()
    }

    override fun advance() {
        if (tokenIndex >= tokens.size) {
            tokenType = null
            tokenStart = endOffset
            tokenEnd = endOffset
            return
        }

        val token = tokens[tokenIndex++]
        tokenStart = startOffset + token.start
        tokenEnd = startOffset + token.end
        tokenType = token.type
    }

    override fun getState(): Int = 0

    override fun getTokenType(): IElementType? = tokenType

    override fun getTokenStart(): Int = tokenStart

    override fun getTokenEnd(): Int = tokenEnd

    override fun getBufferSequence(): CharSequence = buffer

    override fun getBufferEnd(): Int = endOffset
}
