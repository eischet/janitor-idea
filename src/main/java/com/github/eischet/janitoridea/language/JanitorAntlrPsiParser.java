package com.github.eischet.janitoridea.language;

import com.eischet.janitor.lang.JanitorBaseVisitor;
import com.eischet.janitor.lang.JanitorLexer;
import com.eischet.janitor.lang.JanitorParser;
import com.github.eischet.janitoridea.language.psi.JanitorElementTypes;
import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;
import com.intellij.psi.tree.IElementType;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JanitorAntlrPsiParser implements PsiParser {
    @Override
    public ASTNode parse(IElementType root, PsiBuilder builder) {
        PsiBuilder.Marker rootMarker = builder.mark();
        CharSequence text = builder.getOriginalText();

        JanitorLexer lexer = new JanitorLexer(CharStreams.fromString(text.toString()));
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        JanitorParser parser = new JanitorParser(tokenStream);
        ParseErrorListener errorListener = new ParseErrorListener(text.toString());
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);
        JanitorParser.ScriptContext script = parser.script();

        Map<Integer, IElementType> declarations = new HashMap<>();
        new DeclarationCollector(declarations).visit(script);
        List<ParseError> errors = errorListener.getErrors();
        int errorIndex = 0;

        while (!builder.eof()) {
            int tokenStart = builder.getCurrentOffset();
            while (errorIndex < errors.size() && errors.get(errorIndex).offset == tokenStart) {
                builder.error(errors.get(errorIndex).message);
                errorIndex++;
            }
            IElementType elementType = declarations.get(tokenStart);
            if (elementType != null) {
                PsiBuilder.Marker marker = builder.mark();
                builder.advanceLexer();
                marker.done(elementType);
            } else {
                builder.advanceLexer();
            }
        }
        while (errorIndex < errors.size()) {
            builder.error(errors.get(errorIndex).message);
            errorIndex++;
        }

        rootMarker.done(root);
        return builder.getTreeBuilt();
    }

    private static final class ParseError {
        private final int offset;
        private final String message;

        private ParseError(int offset, String message) {
            this.offset = offset;
            this.message = message;
        }
    }

    private static final class ParseErrorListener extends BaseErrorListener {
        private final List<ParseError> errors = new java.util.ArrayList<>();
        private final String source;
        private List<String> lines;

        private ParseErrorListener(String source) {
            this.source = source;
        }

        private List<String> splitSource() {
            return java.util.List.of(source.split("\r?\n\r?"));
        }

        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
                                String msg, RecognitionException e) {
            if (lines == null) {
                lines = splitSource();
            }

            // Copied from JanitorANTLRErrorListener (janitor-lang) so we can keep runtime/editor diagnostics aligned.
            if (offendingSymbol instanceof Token token) {
                int type = token.getType();
                if (type == JanitorLexer.RBRACE && msg != null) {
                    if (msg.contains("no viable alternative") || msg.contains("missing ';'") || msg.contains("mismatched input")) {
                        return;
                    }
                }
            }

            String improvedMessage = improveAntlrMessage(msg);
            if (improvedMessage == null) {
                return;
            }

            String errorLine = getLine(lines, line);
            String fullMessage = improvedMessage != null ? improvedMessage : msg;

            int offset = 0;
            if (offendingSymbol instanceof Token token) {
                offset = Math.max(0, token.getStartIndex());
            }
            errors.add(new ParseError(offset, fullMessage));
        }

        private List<ParseError> getErrors() {
            errors.sort(java.util.Comparator.comparingInt(error -> error.offset));
            return errors;
        }

        private String improveAntlrMessage(final String msg) {
            if (msg != null && msg.contains("failed predicate") && msg.contains("stmtTerminator")) {
                return null;
            }
            String improved = msg;
            if (improved != null) {
                if (improved.startsWith("token recognition error at: '\"") || improved.startsWith("token recognition error at: ''")) {
                    improved = improved.replaceFirst("token recognition error", "invalid string");
                }
                if (improved.contains("STMT_TERM")) {
                    improved = improved.replace("STMT_TERM", "';'");
                }
                if (improved.contains(("'<EOF>'"))) {
                    improved = improved.replace("'<EOF>'", "end of file");
                }
            }
            return improved;
        }

        private static String getLine(List<String> lines, int line) {
            int index = line - 1;
            if (index < 0 || index >= lines.size()) {
                return null;
            }
            return lines.get(index);
        }
    }

    private static final class DeclarationCollector extends JanitorBaseVisitor<Void> {
        private final Map<Integer, IElementType> declarations;

        private DeclarationCollector(Map<Integer, IElementType> declarations) {
            this.declarations = declarations;
        }

        @Override
        public Void visitFunctionDeclaration(JanitorParser.FunctionDeclarationContext ctx) {
            mark(ctx.validIdentifier(), JanitorElementTypes.FUNCTION_DECLARATION);
            return super.visitFunctionDeclaration(ctx);
        }

        @Override
        public Void visitImportAlias(JanitorParser.ImportAliasContext ctx) {
            mark(ctx.validIdentifier(), JanitorElementTypes.IMPORT_ALIAS);
            return super.visitImportAlias(ctx);
        }

        @Override
        public Void visitForStatement(JanitorParser.ForStatementContext ctx) {
            mark(ctx.validIdentifier(), JanitorElementTypes.LOOP_VARIABLE);
            return super.visitForStatement(ctx);
        }

        @Override
        public Void visitForRangeStatement(JanitorParser.ForRangeStatementContext ctx) {
            mark(ctx.validIdentifier(), JanitorElementTypes.LOOP_VARIABLE);
            return super.visitForRangeStatement(ctx);
        }

        @Override
        public Void visitCatchClause(JanitorParser.CatchClauseContext ctx) {
            mark(ctx.validIdentifier(), JanitorElementTypes.CATCH_PARAMETER);
            return super.visitCatchClause(ctx);
        }

        @Override
        public Void visitFormalParameter(JanitorParser.FormalParameterContext ctx) {
            mark(ctx.validIdentifier(), isLambdaParameter(ctx) ? JanitorElementTypes.LAMBDA_PARAMETER : JanitorElementTypes.FORMAL_PARAMETER);
            return super.visitFormalParameter(ctx);
        }

        @Override
        public Void visitFormalParameterWithDefault(JanitorParser.FormalParameterWithDefaultContext ctx) {
            mark(ctx.validIdentifier(), isLambdaParameter(ctx) ? JanitorElementTypes.LAMBDA_PARAMETER : JanitorElementTypes.FORMAL_PARAMETER);
            return super.visitFormalParameterWithDefault(ctx);
        }

        @Override
        public Void visitVarArgList(JanitorParser.VarArgListContext ctx) {
            mark(ctx.validIdentifier(), isLambdaParameter(ctx) ? JanitorElementTypes.LAMBDA_PARAMETER : JanitorElementTypes.FORMAL_PARAMETER);
            return super.visitVarArgList(ctx);
        }

        @Override
        public Void visitKwArgList(JanitorParser.KwArgListContext ctx) {
            mark(ctx.validIdentifier(), isLambdaParameter(ctx) ? JanitorElementTypes.LAMBDA_PARAMETER : JanitorElementTypes.FORMAL_PARAMETER);
            return super.visitKwArgList(ctx);
        }

        @Override
        public Void visitLambdaParameters(JanitorParser.LambdaParametersContext ctx) {
            List<JanitorParser.ValidIdentifierContext> identifiers = ctx.validIdentifier();
            if (identifiers != null && !identifiers.isEmpty()) {
                for (JanitorParser.ValidIdentifierContext identifier : identifiers) {
                    mark(identifier, JanitorElementTypes.LAMBDA_PARAMETER);
                }
            }
            return super.visitLambdaParameters(ctx);
        }

        @Override
        public Void visitAssignmentStatement(JanitorParser.AssignmentStatementContext ctx) {
            JanitorParser.ExpressionContext left = ctx.expression(0);
            if (left instanceof JanitorParser.IdentifierContext identifier) {
                mark(identifier.validIdentifier(), JanitorElementTypes.ASSIGNMENT_TARGET);
            }
            return super.visitAssignmentStatement(ctx);
        }

        private void mark(JanitorParser.ValidIdentifierContext ctx, IElementType type) {
            if (ctx == null) {
                return;
            }
            Token token = ctx.getStart();
            if (token != null && token.getStartIndex() >= 0) {
                declarations.put(token.getStartIndex(), type);
            }
        }

        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        private boolean isLambdaParameter(org.antlr.v4.runtime.ParserRuleContext ctx) {
            org.antlr.v4.runtime.ParserRuleContext current = ctx;
            while (current != null) {
                if (current instanceof JanitorParser.LambdaParametersContext) {
                    return true;
                }
                current = current.getParent();
            }
            return false;
        }
    }
}
