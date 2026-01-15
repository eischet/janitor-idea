package com.github.eischet.janitoridea.startup

import com.eischet.janitor.api.Janitor
import com.github.eischet.janitoridea.janitor.IdeScriptingEnvironment
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class MyProjectActivity : ProjectActivity {

    override suspend fun execute(project: Project) {
        // thisLogger().warn("DO YOU SEE THIS!?")

        // thisLogger().warn("user provider: " + Janitor.getUserProvider())

        // thisLogger().warn("Don't forget to remove all non-needed sample code files with their corresponding registration entries in `plugin.xml`.")
        Janitor.setUserProvider {
            IdeScriptingEnvironment(project) { message ->
                NotificationGroupManager.getInstance()
                    .getNotificationGroup("JanitorWarnings")
                    .createNotification(message, NotificationType.WARNING)
                    .notify(project)
            }
        }
        // thisLogger().warn("user provider: " + Janitor.getUserProvider())

        // Janitor.current().warn("foo")

    }
}
