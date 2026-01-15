package com.github.eischet.janitoridea.language

import com.github.eischet.janitoridea.grammar.JanitorLexer
import com.github.eischet.janitoridea.lexer.JanitorLexerAdapter
import com.github.eischet.janitoridea.lexer.JanitorTokenTypes
import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet

class JanitorParserDefinition : ParserDefinition {
    override fun createLexer(project: Project?) = JanitorLexerAdapter()

    override fun createParser(project: Project?): PsiParser = JanitorPsiParser

    override fun getFileNodeType(): IFileElementType = FILE

    override fun getWhitespaceTokens(): TokenSet = WHITE_SPACES

    override fun getCommentTokens(): TokenSet = COMMENTS

    override fun getStringLiteralElements(): TokenSet = STRINGS

    override fun createElement(node: ASTNode): PsiElement = node.psi

    override fun createFile(viewProvider: FileViewProvider): PsiFile = JanitorFile(viewProvider)

    companion object {
        private val FILE = IFileElementType(JanitorLanguage)
        private val WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE)
        private val COMMENTS = TokenSet.create(
            JanitorTokenTypes.get(JanitorLexer.COMMENT),
            JanitorTokenTypes.get(JanitorLexer.LINE_COMMENT)
        )
        private val STRINGS = TokenSet.create(
            JanitorTokenTypes.get(JanitorLexer.STRING_LITERAL_SINGLE),
            JanitorTokenTypes.get(JanitorLexer.STRING_LITERAL_DOUBLE),
            JanitorTokenTypes.get(JanitorLexer.STRING_LITERAL_TRIPLE_SINGLE),
            JanitorTokenTypes.get(JanitorLexer.STRING_LITERAL_TRIPLE_DOUBLE)
        )
    }
}

private object JanitorPsiParser : PsiParser {
    override fun parse(root: com.intellij.psi.tree.IElementType, builder: PsiBuilder): ASTNode {
        val marker = builder.mark()
        while (!builder.eof()) {
            builder.advanceLexer()
        }
        marker.done(root)
        return builder.treeBuilt
    }
}
