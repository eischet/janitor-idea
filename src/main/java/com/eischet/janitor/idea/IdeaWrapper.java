package com.eischet.janitor.idea;

import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;

public class IdeaWrapper extends JanitorWrapper<Project> {
    private static final WrapperDispatchTable<Project> dispatcher = new WrapperDispatchTable<>();

    static {
        dispatcher.addVoidMethod("openSettings", (self, process, args) -> {
            Project project = self.janitorGetHostValue();
            ApplicationManager.getApplication().invokeLater(() ->
                    ShowSettingsUtil.getInstance().showSettingsDialog(project));
        });
    }

    public IdeaWrapper(Project project) { super(dispatcher, project); }
    public static IdeaWrapper of(Project project) { return new IdeaWrapper(project); }
}
