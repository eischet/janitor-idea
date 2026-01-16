package com.eischet.janitor.idea;

import com.eischet.janitor.api.JanitorRuntime;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Scope;
import com.eischet.janitor.api.scopes.ScriptModule;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JMap;
import com.eischet.janitor.api.types.builtin.JNull;
import com.eischet.janitor.api.types.functions.JCallArgs;
import com.eischet.janitor.compiler.CompilerError;
import com.eischet.janitor.compiler.JanitorCompiler;
import com.eischet.janitor.compiler.ast.statement.Script;
import com.eischet.janitor.lang.JanitorParser;
import com.eischet.janitor.runtime.BaseRuntime;
import com.eischet.janitor.runtime.JanitorScript;
import com.eischet.janitor.runtime.RunningScriptProcess;
import com.github.eischet.janitoridea.janitor.IdeScriptingEnvironment;
import com.intellij.openapi.project.Project;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public final class JanitorManifestEvaluator {
    private JanitorManifestEvaluator() {
    }

    public static @NotNull JMap evaluateManifest(final @NotNull Project project,
                                                 final @Language("Janitor") @NotNull String source,
                                                 final @NotNull Consumer<Scope> prepareGlobals,
                                                 final @NotNull Consumer<String> warnSink)
        throws JanitorCompilerException, JanitorRuntimeException {

        final IdeScriptingEnvironment env = new IdeScriptingEnvironment(project, message -> {
            warnSink.accept(message);
            return kotlin.Unit.INSTANCE;
        });
        final JanitorRuntime runtime = new BaseRuntime(env) {
            @Override
            public JanitorObject print(JanitorScriptProcess process, JCallArgs args) {
                return JNull.NULL;
            }
        };

        final JanitorParser.ScriptContext script = JanitorScript.parseScript(source);
        final ScriptModule module = ScriptModule.unnamed(source);
        final Script scriptObject;
        try {
            scriptObject = JanitorCompiler.build(env, module, script, source);
        } catch (CompilerError e) {
            throw new JanitorCompilerException(e);
        }

        final Scope globalScope = Scope.createGlobalScope(runtime.getEnvironment(), module);
        prepareGlobals.accept(globalScope);
        final RunningScriptProcess process = new RunningScriptProcess(runtime, globalScope, "manifest", scriptObject);
        final JanitorObject result = process.run();
        if (result instanceof JMap map) {
            return map;
        }
        throw new IllegalStateException("Manifest script must return a map, got " + result.janitorClassName());
    }
}
