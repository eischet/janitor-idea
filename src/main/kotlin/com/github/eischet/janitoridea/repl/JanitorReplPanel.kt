package com.github.eischet.janitoridea.repl

import com.eischet.janitor.api.JanitorEnvironment
import com.eischet.janitor.api.JanitorRuntime
import com.eischet.janitor.api.JanitorScriptProcess
import com.eischet.janitor.api.types.JanitorObject
import com.eischet.janitor.api.types.builtin.JNull
import com.eischet.janitor.api.types.functions.JCallArgs
import com.eischet.janitor.env.JanitorDefaultEnvironment
import com.eischet.janitor.repl.JanitorRepl
import com.eischet.janitor.runtime.BaseRuntime
import com.eischet.janitor.runtime.JanitorFormattingLocale
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.intellij.util.concurrency.AppExecutorUtil
import java.awt.BorderLayout
import java.util.Locale
import java.util.concurrent.ExecutorService
import javax.swing.JPanel
import javax.swing.JTextField

class JanitorReplPanel(project: Project) : JPanel(BorderLayout()), Disposable {
    private val outputArea = JBTextArea()
    private val promptLabel = JBLabel("janitor> ")
    private val inputField = JTextField()
    private val executor: ExecutorService = AppExecutorUtil.createBoundedApplicationPoolExecutor("JanitorRepl", 1)

    private val io = IdeReplIO(outputArea)
    private val repl: JanitorRepl

    init {
        outputArea.isEditable = false
        outputArea.lineWrap = true
        outputArea.wrapStyleWord = true

        val env: JanitorEnvironment = object : JanitorDefaultEnvironment(JanitorFormattingLocale(Locale.getDefault())) {
            override fun warn(message: String) {
                io.error(message)
            }
        }
        val runtime: JanitorRuntime = object : BaseRuntime(env) {
            override fun print(process: JanitorScriptProcess, args: JCallArgs): JanitorObject {
                for (janitorObject in args.list) {
                    io.print(janitorObject.janitorToString())
                }
                io.println("")
                return JNull.NULL
            }
        }

        repl = JanitorRepl(runtime, io)
        repl.getLogo()?.let { logo ->
            io.println(logo)
        }
        updatePrompt()

        val inputPanel = JPanel(BorderLayout())
        inputPanel.add(promptLabel, BorderLayout.WEST)
        inputPanel.add(inputField, BorderLayout.CENTER)

        add(JBScrollPane(outputArea), BorderLayout.CENTER)
        add(inputPanel, BorderLayout.SOUTH)

        inputField.addActionListener {
            val text = inputField.text
            inputField.text = ""
            echoInput(text)
            submit(text)
        }
    }

    private fun echoInput(text: String) {
        val prompt = repl.prompt
        outputArea.append(prompt + text + "\n")
    }

    private fun submit(text: String) {
        executor.submit {
            repl.acceptText(text)
            updatePrompt()
        }
    }

    private fun updatePrompt() {
        if (ApplicationManager.getApplication().isDispatchThread) {
            promptLabel.text = repl.prompt
        } else {
            ApplicationManager.getApplication().invokeLater { promptLabel.text = repl.prompt }
        }
    }

    override fun dispose() {
        executor.shutdownNow()
    }
}
