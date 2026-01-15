package com.github.eischet.janitoridea.startup

import com.eischet.janitor.api.Janitor
import com.eischet.janitor.env.JanitorDefaultEnvironment
import com.eischet.janitor.runtime.JanitorFormattingGerman
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class MyProjectActivity : ProjectActivity {

    override suspend fun execute(project: Project) {
        // thisLogger().warn("Don't forget to remove all non-needed sample code files with their corresponding registration entries in `plugin.xml`.")
        Janitor.setUserProvider { IdeScripting(project) }
    }
}

class IdeScripting(private val project: Project) : JanitorDefaultEnvironment(JanitorFormattingGerman()) {
    override fun warn(message: String?) {
        if (message.isNullOrBlank()) {
            return
        }
        NotificationGroupManager.getInstance()
            .getNotificationGroup("JanitorWarnings")
            .createNotification(message, NotificationType.WARNING)
            .notify(project)
    }
}
