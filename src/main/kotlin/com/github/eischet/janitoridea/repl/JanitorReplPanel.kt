package com.github.eischet.janitoridea.repl

import com.eischet.janitor.api.JanitorRuntime
import com.eischet.janitor.api.JanitorScriptProcess
import com.eischet.janitor.api.types.JanitorObject
import com.eischet.janitor.api.types.builtin.JNull
import com.eischet.janitor.api.types.functions.JCallArgs
import com.eischet.janitor.repl.JanitorRepl
import com.eischet.janitor.runtime.BaseRuntime
import com.github.eischet.janitoridea.janitor.IdeScriptingEnvironment
import com.github.eischet.janitoridea.language.JanitorFileType
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.project.Project
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CustomShortcutSet
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.ui.EditorTextField
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.OnePixelSplitter
import com.intellij.util.concurrency.AppExecutorUtil
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.event.KeyEvent
import java.awt.Dimension
import java.util.concurrent.ExecutorService
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.KeyStroke

class JanitorReplPanel(private val project: Project) : JPanel(BorderLayout()), Disposable {
    private val outputArea = JBTextArea()
    private val promptLabel = JBLabel(">")
    private val inputField = EditorTextField("", project, JanitorFileType.INSTANCE)
    private var executor: ExecutorService = AppExecutorUtil.createBoundedApplicationPoolExecutor("JanitorRepl", 1)
    private val submitAction = object : AnAction() {
        override fun actionPerformed(e: AnActionEvent) {
            submitCurrentText()
        }
    }

    private val io = IdeReplIO(outputArea)
    private var repl: JanitorRepl = createRepl()

    init {
        outputArea.isEditable = false
        outputArea.lineWrap = true
        outputArea.wrapStyleWord = true

        inputField.setOneLineMode(false)
        val scheme = EditorColorsManager.getInstance().globalScheme
        inputField.background = scheme.defaultBackground
        inputField.foreground = scheme.defaultForeground

        repl.getLogo()?.let { logo ->
            io.println(logo)
        }
        updatePrompt()

        val inputPanel = JPanel(BorderLayout())
        inputPanel.add(promptLabel, BorderLayout.WEST)
        val inputScroll = JBScrollPane(
            inputField,
            JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JBScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        )
        inputScroll.border = JBUI.Borders.empty()
        inputField.preferredSize = Dimension(0, inputField.preferredSize.height * 3)
        inputPanel.add(inputScroll, BorderLayout.CENTER)
        val buttonPanel = JPanel(BorderLayout())
        buttonPanel.add(JButton("Run").apply {
            toolTipText = "Run (Shift+Enter)"
            addActionListener { submitCurrentText() }
        }, BorderLayout.CENTER)
        buttonPanel.add(JButton("Restart").apply {
            toolTipText = "Restart the REPL: discards all variables and any pending/stuck input"
            addActionListener { restart() }
        }, BorderLayout.EAST)
        inputPanel.add(buttonPanel, BorderLayout.EAST)

        val outputScroll = JBScrollPane(outputArea)
        val splitter = OnePixelSplitter(true, 0.8f)
        splitter.firstComponent = outputScroll
        splitter.secondComponent = inputPanel
        add(splitter, BorderLayout.CENTER)

        inputField.addSettingsProvider { editor ->
            editor.colorsScheme = scheme
            editor.backgroundColor = scheme.defaultBackground
            (editor as? EditorEx)?.let {
                it.setVerticalScrollbarVisible(true)
                it.setHorizontalScrollbarVisible(true)
            }
            submitAction.registerCustomShortcutSet(
                CustomShortcutSet(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.SHIFT_DOWN_MASK)),
                editor.contentComponent
            )
        }
    }

    private fun submitCurrentText() {
        val text = inputField.text
        inputField.text = ""
        echoInput(text)
        submit(text)
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

    fun runScript(text: String, sourceName: String?) {
        val header = sourceName?.let { "-- running $it --" }
        if (header != null) {
            if (ApplicationManager.getApplication().isDispatchThread) {
                outputArea.append(header + "\n")
            } else {
                ApplicationManager.getApplication().invokeLater { outputArea.append(header + "\n") }
            }
        }
        submit(text)
    }

    private fun updatePrompt() {
        if (ApplicationManager.getApplication().isDispatchThread) {
            promptLabel.text = repl.prompt
        } else {
            ApplicationManager.getApplication().invokeLater { promptLabel.text = repl.prompt }
        }
    }

    private fun createRepl(): JanitorRepl {
        val env = IdeScriptingEnvironment(project) { message ->
            io.error(message)
        }
        val runtime: JanitorRuntime = object : BaseRuntime(env) {
            override fun print(process: JanitorScriptProcess, args: JCallArgs): JanitorObject {
                for (janitorObject in args.list) {
                    io.print(janitorObject.janitorToString())
                    io.print(" ")
                }
                io.println("")
                return JNull.NULL
            }
        }
        return JanitorRepl(runtime, io)
    }

    /**
     * Discards the current REPL state (global scope, pending/incomplete input, and any
     * script possibly still stuck in a background call) and starts over with a fresh one.
     * The old executor is shut down forcibly, so this also recovers from a REPL that is
     * hung inside a script call, not just from a parser stuck in a "..." continuation.
     */
    private fun restart() {
        val oldExecutor = executor
        executor = AppExecutorUtil.createBoundedApplicationPoolExecutor("JanitorRepl", 1)
        oldExecutor.shutdownNow()
        repl = createRepl()
        io.clear()
        io.println("-- REPL restarted --")
        updatePrompt()
    }

    override fun dispose() {
        executor.shutdownNow()
    }
}
