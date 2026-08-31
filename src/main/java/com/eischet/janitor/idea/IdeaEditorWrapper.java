package com.eischet.janitor.idea;

import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * Wraps an {@link Editor}, giving Janitor scripts access to the document text, the
 * current selection and the caret of a text editor open in the IDE.
 */
public class IdeaEditorWrapper extends JanitorWrapper<Editor> {

    private static final WrapperDispatchTable<Editor> dispatcher = new WrapperDispatchTable<>();

    static {
        dispatcher.addStringProperty("text",
                self -> ReadAction.compute(() -> self.janitorGetHostValue().getDocument().getText()),
                (self, value) -> {
                    final Editor editor = self.janitorGetHostValue();
                    final Document document = editor.getDocument();
                    final String newText = value == null ? "" : value;
                    IdeaSync.runOnEdt(() -> WriteCommandAction.runWriteCommandAction(
                            editor.getProject(), "Janitor: Edit Document", null, () -> document.setText(newText)));
                });

        dispatcher.addStringProperty("selectedText", self -> self.janitorGetHostValue().getSelectionModel().getSelectedText());

        dispatcher.addIntegerProperty("caretOffset",
                self -> self.janitorGetHostValue().getCaretModel().getOffset(),
                (self, value) -> self.janitorGetHostValue().getCaretModel().moveToOffset(value));

        dispatcher.addIntegerProperty("caretLine", self -> self.janitorGetHostValue().getCaretModel().getLogicalPosition().line);
        dispatcher.addIntegerProperty("caretColumn", self -> self.janitorGetHostValue().getCaretModel().getLogicalPosition().column);

        dispatcher.addObjectProperty("file", self -> {
            final VirtualFile file = FileDocumentManager.getInstance().getFile(self.janitorGetHostValue().getDocument());
            return file == null ? null : IdeaFileWrapper.of(file);
        });

        dispatcher.addVoidMethod("insertText", (self, process, args) -> {
            args.require(1);
            final String text = args.getRequiredStringValue(0);
            final Editor editor = self.janitorGetHostValue();
            final Document document = editor.getDocument();
            final int offset = editor.getCaretModel().getOffset();
            IdeaSync.runOnEdt(() -> WriteCommandAction.runWriteCommandAction(
                    editor.getProject(), "Janitor: Insert Text", null, () -> document.insertString(offset, text)));
        });
    }

    public IdeaEditorWrapper(final Editor editor) {
        super(dispatcher, editor);
    }

    public static IdeaEditorWrapper of(final Editor editor) {
        return new IdeaEditorWrapper(editor);
    }

}
