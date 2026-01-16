package com.github.eischet.janitoridea.language.psi;

import com.eischet.janitor.lang.JanitorBaseVisitor;
import com.eischet.janitor.lang.JanitorLexer;
import com.eischet.janitor.lang.JanitorParser;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class JanitorScopeResolver {
    private JanitorScopeResolver() {
    }

    public static PsiElement resolve(PsiElement element) {
        PsiElement file = element.getContainingFile();
        if (file == null) {
            return null;
        }
        String name = element.getText();
        int offset = element.getTextOffset();
        ScopeIndex scopeIndex = ScopeIndex.build(file.getText());
        Scope scope = scopeIndex.findScope(offset);
        if (scope == null) {
            return fallbackResolve(file, name, offset);
        }
        scopeIndex.populateDeclarations(file);
        PsiElement resolved = scope.resolve(name, offset);
        return resolved != null ? resolved : fallbackResolve(file, name, offset);
    }

    private static PsiElement fallbackResolve(PsiElement file, String name, int offset) {
        List<JanitorNamedElement> namedElements = new ArrayList<>(PsiTreeUtil.collectElementsOfType(file, JanitorNamedElement.class));
        namedElements.sort(Comparator.comparingInt(PsiElement::getTextOffset));
        JanitorNamedElement best = null;
        for (JanitorNamedElement candidate : namedElements) {
            if (name.equals(candidate.getName()) && candidate.getTextOffset() <= offset) {
                best = candidate;
            }
        }
        if (best != null) {
            return best;
        }
        for (JanitorNamedElement candidate : namedElements) {
            if (name.equals(candidate.getName())) {
                return candidate;
            }
        }
        return null;
    }

    private enum ScopeKind {
        FILE,
        FUNCTION,
        LAMBDA,
        BLOCK,
        LOOP,
        CATCH
    }

    private static final class Scope {
        private final ScopeKind kind;
        private final int startOffset;
        private final int endOffset;
        private Scope parent;
        private final List<Scope> children = new ArrayList<>();
        private final Map<String, List<JanitorNamedElement>> declarations = new HashMap<>();

        private Scope(ScopeKind kind, int startOffset, int endOffset) {
            this.kind = kind;
            this.startOffset = startOffset;
            this.endOffset = endOffset;
        }

        private boolean contains(int offset) {
            return offset >= startOffset && offset < endOffset;
        }

        private void addDeclaration(JanitorNamedElement element) {
            String name = element.getName();
            if (name == null) {
                return;
            }
            declarations.computeIfAbsent(name, key -> new ArrayList<>()).add(element);
        }

        private PsiElement resolve(String name, int offset) {
            Scope current = this;
            while (current != null) {
                List<JanitorNamedElement> candidates = current.declarations.get(name);
                if (candidates != null && !candidates.isEmpty()) {
                    JanitorNamedElement prior = null;
                    for (JanitorNamedElement candidate : candidates) {
                        if (candidate.getTextOffset() <= offset) {
                            prior = candidate;
                        } else {
                            break;
                        }
                    }
                    if (prior != null) {
                        return prior;
                    }
                }
                current = current.parent;
            }
            return null;
        }
    }

    private static final class ScopeIndex {
        private final Scope root;
        private final List<Scope> scopes;
        private boolean populated;

        private ScopeIndex(Scope root, List<Scope> scopes) {
            this.root = root;
            this.scopes = scopes;
        }

        private static ScopeIndex build(String source) {
            JanitorLexer lexer = new JanitorLexer(CharStreams.fromString(source));
            CommonTokenStream tokenStream = new CommonTokenStream(lexer);
            JanitorParser parser = new JanitorParser(tokenStream);
            JanitorParser.ScriptContext script = parser.script();

            List<Scope> scopes = new ArrayList<>();
            Scope root = new Scope(ScopeKind.FILE, 0, source.length());
            scopes.add(root);

            new ScopeCollector(scopes).visit(script);
            buildHierarchy(scopes);

            return new ScopeIndex(root, scopes);
        }

        private static void buildHierarchy(List<Scope> scopes) {
            scopes.sort(Comparator.<Scope>comparingInt(scope -> scope.startOffset)
                .thenComparingInt(scope -> -(scope.endOffset - scope.startOffset)));
            for (Scope scope : scopes) {
                if (scope.kind == ScopeKind.FILE) {
                    continue;
                }
                Scope parent = findParent(scopes, scope);
                scope.parent = parent;
                if (parent != null) {
                    parent.children.add(scope);
                }
            }
        }

        private static Scope findParent(List<Scope> scopes, Scope child) {
            Scope candidate = null;
            for (Scope scope : scopes) {
                if (scope == child) {
                    continue;
                }
                if (scope.startOffset <= child.startOffset && scope.endOffset >= child.endOffset) {
                    if (candidate == null || (scope.endOffset - scope.startOffset) < (candidate.endOffset - candidate.startOffset)) {
                        candidate = scope;
                    }
                }
            }
            return candidate;
        }

        private Scope findScope(int offset) {
            return findScope(root, offset);
        }

        private Scope findScope(Scope scope, int offset) {
            if (!scope.contains(offset)) {
                return null;
            }
            for (Scope child : scope.children) {
                Scope found = findScope(child, offset);
                if (found != null) {
                    return found;
                }
            }
            return scope;
        }

        private void populateDeclarations(PsiElement file) {
            if (populated) {
                return;
            }
            populated = true;
            List<JanitorNamedElement> namedElements = new ArrayList<>(PsiTreeUtil.collectElementsOfType(file, JanitorNamedElement.class));
            namedElements.sort(Comparator.comparingInt(PsiElement::getTextOffset));
            for (JanitorNamedElement element : namedElements) {
                Scope scope = findScope(element.getTextOffset());
                if (scope == null) {
                    continue;
                }
                if (element instanceof JanitorFunctionDeclaration) {
                    Scope target = scope.parent != null ? scope.parent : scope;
                    target.addDeclaration(element);
                    continue;
                }
                if (element instanceof JanitorAssignmentTarget) {
                    Scope target = findAssignmentScope(scope, element.getName(), element.getTextOffset());
                    target.addDeclaration(element);
                    continue;
                }
                scope.addDeclaration(element);
            }
        }

        private Scope findAssignmentScope(Scope scope, String name, int offset) {
            Scope current = scope;
            while (current != null) {
                List<JanitorNamedElement> candidates = current.declarations.get(name);
                if (candidates != null) {
                    for (JanitorNamedElement candidate : candidates) {
                        if (candidate.getTextOffset() <= offset) {
                            return current;
                        }
                    }
                }
                current = current.parent;
            }
            return scope;
        }
    }

    private static final class ScopeCollector extends JanitorBaseVisitor<Void> {
        private final List<Scope> scopes;

        private ScopeCollector(List<Scope> scopes) {
            this.scopes = scopes;
        }

        @Override
        public Void visitFunctionDeclaration(JanitorParser.FunctionDeclarationContext ctx) {
            addScope(ScopeKind.FUNCTION, ctx.start, ctx.block().stop);
            return super.visitFunctionDeclaration(ctx);
        }

        @Override
        public Void visitLambdaExpression(JanitorParser.LambdaExpressionContext ctx) {
            addScope(ScopeKind.LAMBDA, ctx.start, ctx.lambdaBody().stop);
            return super.visitLambdaExpression(ctx);
        }

        @Override
        public Void visitBlock(JanitorParser.BlockContext ctx) {
            addScope(ScopeKind.BLOCK, ctx.start, ctx.stop);
            return super.visitBlock(ctx);
        }

        @Override
        public Void visitForStatement(JanitorParser.ForStatementContext ctx) {
            addScope(ScopeKind.LOOP, ctx.start, ctx.block().stop);
            return super.visitForStatement(ctx);
        }

        @Override
        public Void visitForRangeStatement(JanitorParser.ForRangeStatementContext ctx) {
            addScope(ScopeKind.LOOP, ctx.start, ctx.block().stop);
            return super.visitForRangeStatement(ctx);
        }

        @Override
        public Void visitCatchClause(JanitorParser.CatchClauseContext ctx) {
            addScope(ScopeKind.CATCH, ctx.start, ctx.block().stop);
            return super.visitCatchClause(ctx);
        }

        private void addScope(ScopeKind kind, Token startToken, Token stopToken) {
            if (startToken == null || stopToken == null) {
                return;
            }
            int start = Math.max(0, startToken.getStartIndex());
            int end = stopToken.getStopIndex() + 1;
            scopes.add(new Scope(kind, start, end));
        }
    }
}
