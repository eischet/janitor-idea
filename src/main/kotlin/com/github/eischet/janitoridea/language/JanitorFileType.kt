package com.github.eischet.janitoridea.language

import com.intellij.openapi.fileTypes.LanguageFileType
import com.github.eischet.janitoridea.JanitorIcons
import javax.swing.Icon

class JanitorFileType : LanguageFileType(JanitorLanguage) {
    override fun getName(): String = "Janitor"

    override fun getDescription(): String = "Janitor language file"

    override fun getDefaultExtension(): String = "jan"

    override fun getIcon(): Icon? = JanitorIcons.FILE

    companion object {
        val INSTANCE = JanitorFileType()
    }
}
