package com.github.eischet.janitoridea.highlighting

import com.github.eischet.janitoridea.grammar.JanitorLexer
import com.github.eischet.janitoridea.lexer.JanitorLexerAdapter
import com.github.eischet.janitoridea.lexer.JanitorTokenType
import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase

class JanitorSyntaxHighlighter : SyntaxHighlighterBase() {
    override fun getHighlightingLexer(): Lexer = JanitorLexerAdapter()

    override fun getTokenHighlights(tokenType: IElementType?): Array<TextAttributesKey> {
        if (tokenType == TokenType.BAD_CHARACTER) {
            return BAD_CHAR_KEYS
        }
        if (tokenType !is JanitorTokenType) {
            return EMPTY_KEYS
        }

        return when (tokenType.antlrTokenType) {
            JanitorLexer.COMMENT, JanitorLexer.LINE_COMMENT -> COMMENT_KEYS
            JanitorLexer.STRING_LITERAL_SINGLE,
            JanitorLexer.STRING_LITERAL_DOUBLE,
            JanitorLexer.STRING_LITERAL_TRIPLE_SINGLE,
            JanitorLexer.STRING_LITERAL_TRIPLE_DOUBLE -> STRING_KEYS
            JanitorLexer.REGEX_LITERAL -> REGEXP_KEYS
            JanitorLexer.DECIMAL_LITERAL,
            JanitorLexer.HEX_LITERAL,
            JanitorLexer.OCT_LITERAL,
            JanitorLexer.BINARY_LITERAL,
            JanitorLexer.FLOAT_LITERAL,
            JanitorLexer.DATE_LITERAL,
            JanitorLexer.DATE_TIME_LITERAL,
            JanitorLexer.YEARS_LITERAL,
            JanitorLexer.MONTHS_LITERAL,
            JanitorLexer.WEEKS_LITERAL,
            JanitorLexer.DAYS_LITERAL,
            JanitorLexer.HOURS_LITERAL,
            JanitorLexer.MINUTES_LITERAL,
            JanitorLexer.SECONDS_LITERAL,
            JanitorLexer.TODAY_LITERAL,
            JanitorLexer.NOW_LITERAL -> NUMBER_KEYS
            JanitorLexer.LPAREN, JanitorLexer.RPAREN -> PAREN_KEYS
            JanitorLexer.LBRACE, JanitorLexer.RBRACE -> BRACES_KEYS
            JanitorLexer.LBRACK, JanitorLexer.RBRACK -> BRACKETS_KEYS
            JanitorLexer.COMMA -> COMMA_KEYS
            JanitorLexer.SEMICOLON -> SEMICOLON_KEYS
            JanitorLexer.DOT, JanitorLexer.QDOT -> DOT_KEYS
            JanitorLexer.ASSIGN,
            JanitorLexer.PLUS_ASSIGN,
            JanitorLexer.MINUS_ASSIGN,
            JanitorLexer.MUL_ASSIGN,
            JanitorLexer.DIV_ASSIGN,
            JanitorLexer.MOD_ASSIGN,
            JanitorLexer.GT,
            JanitorLexer.LT,
            JanitorLexer.MATCH,
            JanitorLexer.MATCH_NOT,
            JanitorLexer.QUESTION,
            JanitorLexer.COLON,
            JanitorLexer.EQUAL,
            JanitorLexer.LE,
            JanitorLexer.GE,
            JanitorLexer.NOTEQUAL,
            JanitorLexer.ALT_NOTEQUAL,
            JanitorLexer.NOT,
            JanitorLexer.ALT_NOT,
            JanitorLexer.AND,
            JanitorLexer.CAND,
            JanitorLexer.OR,
            JanitorLexer.COR,
            JanitorLexer.INC,
            JanitorLexer.DEC,
            JanitorLexer.ADD,
            JanitorLexer.SUB,
            JanitorLexer.MUL,
            JanitorLexer.DOUBLE_STAR,
            JanitorLexer.DIV,
            JanitorLexer.MOD,
            JanitorLexer.ARROW -> OPERATION_KEYS
            JanitorLexer.FUNCTION,
            JanitorLexer.BREAK,
            JanitorLexer.CATCH,
            JanitorLexer.CONTINUE,
            JanitorLexer.DO,
            JanitorLexer.ELSE,
            JanitorLexer.FINALLY,
            JanitorLexer.FOR,
            JanitorLexer.IF,
            JanitorLexer.THEN,
            JanitorLexer.IMPORT,
            JanitorLexer.RETURN,
            JanitorLexer.THROW,
            JanitorLexer.TRY,
            JanitorLexer.WHILE,
            JanitorLexer.TRUE,
            JanitorLexer.FALSE,
            JanitorLexer.NULL,
            JanitorLexer.FROM,
            JanitorLexer.TO,
            JanitorLexer.IN -> KEYWORD_KEYS
            JanitorLexer.IDENTIFIER -> IDENTIFIER_KEYS
            else -> EMPTY_KEYS
        }
    }

    companion object {
        private val KEYWORD = TextAttributesKey.createTextAttributesKey(
            "JANITOR_KEYWORD",
            DefaultLanguageHighlighterColors.KEYWORD
        )
        private val STRING = TextAttributesKey.createTextAttributesKey(
            "JANITOR_STRING",
            DefaultLanguageHighlighterColors.STRING
        )
        private val NUMBER = TextAttributesKey.createTextAttributesKey(
            "JANITOR_NUMBER",
            DefaultLanguageHighlighterColors.NUMBER
        )
        private val COMMENT = TextAttributesKey.createTextAttributesKey(
            "JANITOR_COMMENT",
            DefaultLanguageHighlighterColors.LINE_COMMENT
        )
        private val REGEXP = TextAttributesKey.createTextAttributesKey(
            "JANITOR_REGEXP",
            DefaultLanguageHighlighterColors.STRING
        )
        private val OPERATION = TextAttributesKey.createTextAttributesKey(
            "JANITOR_OPERATION",
            DefaultLanguageHighlighterColors.OPERATION_SIGN
        )
        private val BRACES = TextAttributesKey.createTextAttributesKey(
            "JANITOR_BRACES",
            DefaultLanguageHighlighterColors.BRACES
        )
        private val BRACKETS = TextAttributesKey.createTextAttributesKey(
            "JANITOR_BRACKETS",
            DefaultLanguageHighlighterColors.BRACKETS
        )
        private val PAREN = TextAttributesKey.createTextAttributesKey(
            "JANITOR_PAREN",
            DefaultLanguageHighlighterColors.PARENTHESES
        )
        private val COMMA = TextAttributesKey.createTextAttributesKey(
            "JANITOR_COMMA",
            DefaultLanguageHighlighterColors.COMMA
        )
        private val DOT = TextAttributesKey.createTextAttributesKey(
            "JANITOR_DOT",
            DefaultLanguageHighlighterColors.DOT
        )
        private val SEMICOLON = TextAttributesKey.createTextAttributesKey(
            "JANITOR_SEMICOLON",
            DefaultLanguageHighlighterColors.SEMICOLON
        )
        private val IDENTIFIER = TextAttributesKey.createTextAttributesKey(
            "JANITOR_IDENTIFIER",
            DefaultLanguageHighlighterColors.IDENTIFIER
        )
        private val BAD_CHAR = TextAttributesKey.createTextAttributesKey(
            "JANITOR_BAD_CHARACTER",
            HighlighterColors.BAD_CHARACTER
        )

        private val KEYWORD_KEYS = arrayOf(KEYWORD)
        private val STRING_KEYS = arrayOf(STRING)
        private val NUMBER_KEYS = arrayOf(NUMBER)
        private val COMMENT_KEYS = arrayOf(COMMENT)
        private val REGEXP_KEYS = arrayOf(REGEXP)
        private val OPERATION_KEYS = arrayOf(OPERATION)
        private val BRACES_KEYS = arrayOf(BRACES)
        private val BRACKETS_KEYS = arrayOf(BRACKETS)
        private val PAREN_KEYS = arrayOf(PAREN)
        private val COMMA_KEYS = arrayOf(COMMA)
        private val DOT_KEYS = arrayOf(DOT)
        private val SEMICOLON_KEYS = arrayOf(SEMICOLON)
        private val IDENTIFIER_KEYS = arrayOf(IDENTIFIER)
        private val BAD_CHAR_KEYS = arrayOf(BAD_CHAR)
        private val EMPTY_KEYS = emptyArray<TextAttributesKey>()
    }
}
