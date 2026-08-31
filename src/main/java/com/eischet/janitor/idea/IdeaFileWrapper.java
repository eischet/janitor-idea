package com.eischet.janitor.idea;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.errors.runtime.JanitorNativeException;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.IOException;
import java.util.Arrays;

/**
 * Wraps a {@link VirtualFile}, giving Janitor scripts read/write access to files and
 * directories reachable from the IDE project.
 */
public class IdeaFileWrapper extends JanitorWrapper<VirtualFile> {

    private static final WrapperDispatchTable<VirtualFile> dispatcher = new WrapperDispatchTable<>();

    static {
        dispatcher.addStringProperty("name", self -> self.janitorGetHostValue().getName());
        dispatcher.addStringProperty("path", self -> self.janitorGetHostValue().getPath());
        dispatcher.addStringProperty("extension", self -> self.janitorGetHostValue().getExtension());
        dispatcher.addBooleanProperty("exists", self -> self.janitorGetHostValue().exists());
        dispatcher.addBooleanProperty("isDirectory", self -> self.janitorGetHostValue().isDirectory());
        dispatcher.addBooleanProperty("isWritable", self -> self.janitorGetHostValue().isWritable());

        dispatcher.addObjectProperty("parent", self -> {
            final VirtualFile parent = self.janitorGetHostValue().getParent();
            return parent == null ? null : IdeaFileWrapper.of(parent);
        });

        dispatcher.addMethod("readText", (self, process, args) -> {
            args.require(0);
            final VirtualFile file = self.janitorGetHostValue();
            try {
                return Janitor.string(ReadAction.compute(() -> VfsUtil.loadText(file)));
            } catch (IOException e) {
                throw new JanitorNativeException(process, "error reading file " + file.getPath(), e);
            }
        });

        dispatcher.addVoidMethod("writeText", (self, process, args) -> {
            args.require(1);
            final String text = args.getRequiredStringValue(0);
            final VirtualFile file = self.janitorGetHostValue();
            final IOException[] error = new IOException[1];
            IdeaSync.runOnEdt(() -> {
                try {
                    WriteAction.run(() -> VfsUtil.saveText(file, text));
                } catch (IOException e) {
                    error[0] = e;
                }
            });
            if (error[0] != null) {
                throw new JanitorNativeException(process, "error writing file " + file.getPath(), error[0]);
            }
        });

        dispatcher.addMethod("children", (self, process, args) -> {
            args.require(0);
            final VirtualFile[] children = ReadAction.compute(self.janitorGetHostValue()::getChildren);
            return Janitor.list(Arrays.stream(children).map(IdeaFileWrapper::of));
        });

        dispatcher.addMethod("child", (self, process, args) -> {
            args.require(1);
            final String name = args.getRequiredStringValue(0);
            final VirtualFile child = ReadAction.compute(() -> self.janitorGetHostValue().findChild(name));
            return child == null ? com.eischet.janitor.api.types.builtin.JNull.NULL : IdeaFileWrapper.of(child);
        });
    }

    public IdeaFileWrapper(final VirtualFile file) {
        super(dispatcher, file);
    }

    public static IdeaFileWrapper of(final VirtualFile file) {
        return new IdeaFileWrapper(file);
    }

}
