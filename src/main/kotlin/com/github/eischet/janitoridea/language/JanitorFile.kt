package com.github.eischet.janitoridea.language

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.psi.FileViewProvider
import com.intellij.openapi.fileTypes.FileType

class JanitorFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, JanitorLanguage) {
    override fun getFileType(): FileType = JanitorFileType.INSTANCE

    override fun toString(): String = "Janitor File"
}
