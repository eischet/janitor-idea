package com.github.eischet.janitoridea.language.psi;

import com.github.eischet.janitoridea.language.JanitorLanguage;
import com.intellij.psi.tree.IElementType;

public class JanitorElementType extends IElementType {
    public JanitorElementType(String debugName) {
        super(debugName, JanitorLanguage.INSTANCE);
    }
}
