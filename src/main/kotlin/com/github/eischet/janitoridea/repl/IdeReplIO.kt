package com.github.eischet.janitoridea.repl

import com.eischet.janitor.api.errors.runtime.JanitorNativeException
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException
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
        // A JanitorRuntimeException's message is already a formatted script-level traceback
        // (module/line/source line down to the error), so the Java stack trace under it is just
        // interpreter-internal noise -- except for JanitorNativeException, where the cause is a
        // real Java exception from host code and the stack trace is the only way to debug it.
        if (e !is JanitorRuntimeException || e is JanitorNativeException) {
            write(e.stackTraceToString() + "\n")
        }
    }

    override fun verbose(text: String?) {
        if (!verboseEnabled || text == null) {
            return
        }
        write(text + "\n")
    }

    fun clear() {
        if (ApplicationManager.getApplication().isDispatchThread) {
            outputArea.text = ""
        } else {
            ApplicationManager.getApplication().invokeLater {
                outputArea.text = ""
            }
        }
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
