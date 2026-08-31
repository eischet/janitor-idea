package com.eischet.janitor.idea;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.builtin.JNull;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

import java.nio.file.Path;
import java.util.Arrays;

public class IdeaWrapper extends JanitorWrapper<Project> {
    private static final WrapperDispatchTable<Project> dispatcher = new WrapperDispatchTable<>();

    static {
        dispatcher.addStringProperty("name", self -> self.janitorGetHostValue().getName());
        dispatcher.addStringProperty("basePath", self -> self.janitorGetHostValue().getBasePath());

        dispatcher.addVoidMethod("openSettings", (self, process, args) -> {
            Project project = self.janitorGetHostValue();
            ApplicationManager.getApplication().invokeLater(() ->
                    ShowSettingsUtil.getInstance().showSettingsDialog(project));
        });

        dispatcher.addVoidMethod("notify", (self, process, args) -> {
            args.require(1, 2);
            final String message = args.getRequiredStringValue(0);
            final String kind = args.getOptionalStringValue(1, "info");
            final NotificationType type = switch (kind.toLowerCase()) {
                case "warning", "warn" -> NotificationType.WARNING;
                case "error" -> NotificationType.ERROR;
                default -> NotificationType.INFORMATION;
            };
            NotificationGroupManager.getInstance()
                    .getNotificationGroup("Janitor")
                    .createNotification(message, type)
                    .notify(self.janitorGetHostValue());
        });

        dispatcher.addVoidMethod("showMessage", (self, process, args) -> {
            args.require(1, 2);
            final String message = args.getRequiredStringValue(0);
            final String title = args.getOptionalStringValue(1, "Janitor");
            final Project project = self.janitorGetHostValue();
            IdeaSync.runOnEdt(() -> Messages.showInfoMessage(project, message, title));
        });

        dispatcher.addVoidMethod("showError", (self, process, args) -> {
            args.require(1, 2);
            final String message = args.getRequiredStringValue(0);
            final String title = args.getOptionalStringValue(1, "Janitor");
            final Project project = self.janitorGetHostValue();
            IdeaSync.runOnEdt(() -> Messages.showErrorDialog(project, message, title));
        });

        dispatcher.addMethod("confirm", (self, process, args) -> {
            args.require(1, 2);
            final String message = args.getRequiredStringValue(0);
            final String title = args.getOptionalStringValue(1, "Janitor");
            final Project project = self.janitorGetHostValue();
            final int result = IdeaSync.computeOnEdt(() -> Messages.showYesNoDialog(project, message, title, null));
            return result == Messages.YES ? Janitor.TRUE : Janitor.FALSE;
        });

        dispatcher.addMethod("askInput", (self, process, args) -> {
            args.require(1, 3);
            final String message = args.getRequiredStringValue(0);
            final String title = args.getOptionalStringValue(1, "Janitor");
            final String initialValue = args.getOptionalStringValue(2, "");
            final Project project = self.janitorGetHostValue();
            final String result = IdeaSync.computeOnEdt(() ->
                    Messages.showInputDialog(project, message, title, null, initialValue, null));
            return result == null ? JNull.NULL : Janitor.string(result);
        });

        dispatcher.addMethod("currentFile", (self, process, args) -> {
            args.require(0);
            final Project project = self.janitorGetHostValue();
            final VirtualFile[] files = IdeaSync.computeOnEdt(() -> FileEditorManager.getInstance(project).getSelectedFiles());
            return files.length == 0 ? JNull.NULL : IdeaFileWrapper.of(files[0]);
        });

        dispatcher.addMethod("currentEditor", (self, process, args) -> {
            args.require(0);
            final Project project = self.janitorGetHostValue();
            final Editor editor = IdeaSync.computeOnEdt(() -> FileEditorManager.getInstance(project).getSelectedTextEditor());
            return editor == null ? JNull.NULL : IdeaEditorWrapper.of(editor);
        });

        dispatcher.addMethod("openFiles", (self, process, args) -> {
            args.require(0);
            final Project project = self.janitorGetHostValue();
            final VirtualFile[] files = IdeaSync.computeOnEdt(() -> FileEditorManager.getInstance(project).getOpenFiles());
            return Janitor.list(Arrays.stream(files).map(IdeaFileWrapper::of));
        });

        dispatcher.addMethod("findFile", (self, process, args) -> {
            args.require(1);
            final String path = args.getRequiredStringValue(0);
            final Project project = self.janitorGetHostValue();
            final String basePath = project.getBasePath();
            final Path requested = Path.of(path);
            final Path resolved = requested.isAbsolute() || basePath == null
                    ? requested
                    : Path.of(basePath).resolve(path);
            final VirtualFile file = LocalFileSystem.getInstance().findFileByNioFile(resolved);
            return file == null ? JNull.NULL : IdeaFileWrapper.of(file);
        });
    }

    public IdeaWrapper(Project project) { super(dispatcher, project); }
    public static IdeaWrapper of(Project project) { return new IdeaWrapper(project); }
}
