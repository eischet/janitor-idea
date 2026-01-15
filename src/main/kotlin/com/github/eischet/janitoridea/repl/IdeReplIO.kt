package com.github.eischet.janitoridea.repl

import com.eischet.janitor.repl.ReplIO
import com.intellij.openapi.application.ApplicationManager
import java.io.IOException
import javax.swing.JTextArea

class IdeReplIO(
    private val outputArea: JTextArea,
    private val verboseEnabled: Boolean = false
) : ReplIO {
    override fun readLine(prompt: String): String {
        throw IOException("readLine is not supported in IDE-driven REPL")
    }

    override fun print(text: String) {
        write(text)
    }

    override fun println(text: String) {
        write(text + "\n")
    }

    override fun error(text: String) {
        write(text + "\n")
    }

    override fun exception(e: Exception) {
        write("Error: ${e.message}\n")
        write(e.stackTraceToString() + "\n")
    }

    override fun verbose(text: String) {
        if (!verboseEnabled) {
            return
        }
        write(text + "\n")
    }

    private fun write(text: String) {
        if (ApplicationManager.getApplication().isDispatchThread) {
            outputArea.append(text)
        } else {
            ApplicationManager.getApplication().invokeLater {
                outputArea.append(text)
            }
        }
    }
}
