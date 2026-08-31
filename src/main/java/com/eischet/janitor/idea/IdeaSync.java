package com.eischet.janitor.idea;

import com.intellij.openapi.application.ApplicationManager;

import java.util.function.Supplier;

/**
 * Janitor scripts run on a background executor, but a lot of IntelliJ platform APIs
 * (UI dialogs, document edits, {@link com.intellij.openapi.fileEditor.FileEditorManager} lookups)
 * are only safe to call from the event dispatch thread. These helpers hop over to the EDT
 * and block the calling script thread until the result is available, which keeps the
 * wrapper methods simple synchronous calls from the script's point of view.
 */
final class IdeaSync {

    private IdeaSync() {
    }

    static <T> T computeOnEdt(final Supplier<T> supplier) {
        final Object[] box = new Object[1];
        ApplicationManager.getApplication().invokeAndWait(() -> box[0] = supplier.get());
        //noinspection unchecked
        return (T) box[0];
    }

    static void runOnEdt(final Runnable runnable) {
        ApplicationManager.getApplication().invokeAndWait(runnable);
    }

}
