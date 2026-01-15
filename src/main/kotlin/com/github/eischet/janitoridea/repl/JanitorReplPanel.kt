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
import java.util.Locale
import java.util.concurrent.ExecutorService
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.KeyStroke

class JanitorReplPanel(project: Project) : JPanel(BorderLayout()), Disposable {
    private val outputArea = JBTextArea()
    private val promptLabel = JBLabel("janitor> ")
    private val inputField = EditorTextField("", project, JanitorFileType.INSTANCE)
    private val executor: ExecutorService = AppExecutorUtil.createBoundedApplicationPoolExecutor("JanitorRepl", 1)
    private val submitAction = object : AnAction() {
        override fun actionPerformed(e: AnActionEvent) {
            submitCurrentText()
        }
    }

    private val io = IdeReplIO(outputArea)
    private val repl: JanitorRepl

    init {
        outputArea.isEditable = false
        outputArea.lineWrap = true
        outputArea.wrapStyleWord = true

        inputField.setOneLineMode(false)
        val scheme = EditorColorsManager.getInstance().globalScheme
        inputField.background = scheme.defaultBackground
        inputField.foreground = scheme.defaultForeground

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
        val inputScroll = JBScrollPane(
            inputField,
            JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JBScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        )
        inputScroll.border = JBUI.Borders.empty()
        inputField.preferredSize = Dimension(0, inputField.preferredSize.height * 3)
        inputPanel.add(inputScroll, BorderLayout.CENTER)
        inputPanel.add(JButton("Run").apply {
            toolTipText = "Run (Shift+Enter)"
            addActionListener { submitCurrentText() }
        }, BorderLayout.EAST)

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
