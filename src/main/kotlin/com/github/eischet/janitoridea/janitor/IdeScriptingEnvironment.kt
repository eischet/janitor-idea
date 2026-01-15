package com.github.eischet.janitoridea.janitor

import com.eischet.janitor.env.JanitorDefaultEnvironment
import com.eischet.janitor.idea.IdeaWrapper
import com.eischet.janitor.runtime.JanitorFormattingGerman
import com.intellij.openapi.project.Project

class IdeScriptingEnvironment(
    private val project: Project,
    private val warnSink: (String) -> Unit
) : JanitorDefaultEnvironment(JanitorFormattingGerman()) {

    init {
        builtinScope.bind("ide", IdeaWrapper.of(project))
    }

    override fun warn(message: String?) {
        if (message.isNullOrBlank()) {
            return
        }
        warnSink(message)
    }
}
