package com.eischet.janitor.idea;

import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;

public class IdeaWrapper extends JanitorWrapper<Project> {
    private static final WrapperDispatchTable<Project> dispatcher = new WrapperDispatchTable<>();

    static {
        dispatcher.addVoidMethod("openSettings", (self, process, args) -> {
            Project project = self.janitorGetHostValue();
            ApplicationManager.getApplication().invokeLater(() -> {
                AnAction action = ActionManager.getInstance().getAction("ShowSettings");
                if (action == null) {
                    ShowSettingsUtil.getInstance().showSettingsDialog(project);
                    return;
                }
                DataContext dataContext = dataId -> {
                    if (CommonDataKeys.PROJECT.getName().equals(dataId)) {
                        return project;
                    }
                    return null;
                };
                AnActionEvent event = AnActionEvent.createFromAnAction(
                        action,
                        null,
                        ActionPlaces.UNKNOWN,
                        dataContext
                );
                action.actionPerformed(event);
            });
        });
    }

    public IdeaWrapper(Project project) { super(dispatcher, project); }
    public static IdeaWrapper of(Project project) { return new IdeaWrapper(project); }
}
