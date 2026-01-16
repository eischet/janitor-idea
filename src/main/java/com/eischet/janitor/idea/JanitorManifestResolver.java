package com.eischet.janitor.idea;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JMap;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class JanitorManifestResolver {
    private static final Logger LOG = Logger.getInstance(JanitorManifestResolver.class);
    private static final String INDEX_NAME = "index.d.jan";

    private JanitorManifestResolver() {
    }

    public static @NotNull JMap resolveForFile(final @NotNull Project project,
                                               final @Nullable VirtualFile scriptFile,
                                               final @NotNull Consumer<String> warnSink) {
        Path basePath = projectBasePath(project);
        if (basePath == null) {
            return Janitor.map();
        }

        Path targetDir = basePath;
        if (scriptFile != null) {
            Path scriptPath = Paths.get(scriptFile.getPath()).normalize();
            Path parent = scriptPath.getParent();
            if (parent != null && parent.startsWith(basePath)) {
                targetDir = parent;
            }
        }

        List<Path> manifests = collectManifestFiles(basePath, targetDir);
        if (manifests.isEmpty()) {
            return Janitor.map();
        }

        JMap merged = Janitor.map();
        for (Path manifest : manifests) {
            String source = readFile(manifest);
            if (source == null) {
                continue;
            }
            try {
                JMap map = JanitorManifestEvaluator.evaluateManifest(
                    project,
                    source,
                    scope -> {},
                    warnSink
                );
                mergeMaps(merged, map);
            } catch (JanitorCompilerException | JanitorRuntimeException e) {
                LOG.warn("Failed to evaluate Janitor manifest: " + manifest, e);
            } catch (RuntimeException e) {
                LOG.warn("Failed to merge Janitor manifest: " + manifest, e);
            }
        }
        return merged;
    }

    private static @Nullable Path projectBasePath(Project project) {
        String basePath = project.getBasePath();
        if (basePath == null) {
            return null;
        }
        return Paths.get(basePath).normalize();
    }

    private static List<Path> collectManifestFiles(Path basePath, Path targetDir) {
        List<Path> directories = new ArrayList<>();
        Path current = targetDir;
        while (current != null && current.startsWith(basePath)) {
            directories.add(current);
            if (current.equals(basePath)) {
                break;
            }
            current = current.getParent();
        }

        java.util.Collections.reverse(directories);
        List<Path> ordered = new ArrayList<>();
        for (Path dir : directories) {
            Path index = dir.resolve(INDEX_NAME);
            if (Files.isRegularFile(index)) {
                ordered.add(index);
            }
            ordered.addAll(findAdditionalManifests(dir));
        }
        return ordered;
    }

    private static List<Path> findAdditionalManifests(Path dir) {
        List<Path> result = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.d.jan")) {
            for (Path entry : stream) {
                if (Files.isRegularFile(entry) && !INDEX_NAME.equals(entry.getFileName().toString())) {
                    result.add(entry);
                }
            }
        } catch (IOException e) {
            LOG.warn("Failed to scan manifest directory: " + dir, e);
        }
        result.sort(Comparator.comparing(path -> path.getFileName().toString()));
        return result;
    }

    private static @Nullable String readFile(Path path) {
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOG.warn("Failed to read manifest: " + path, e);
            return null;
        }
    }

    private static void mergeMaps(JMap target, JMap source) {
        Map<JanitorObject, JanitorObject> sourceMap = source.janitorGetHostValue();
        for (Map.Entry<JanitorObject, JanitorObject> entry : sourceMap.entrySet()) {
            String key = entry.getKey().janitorToString();
            JanitorObject value = entry.getValue();
            JanitorObject existing = target.janitorGetHostValue().get(Janitor.string(key));
            if (existing instanceof JMap existingMap && value instanceof JMap valueMap) {
                mergeMaps(existingMap, valueMap);
                target.put(key, existingMap);
            } else {
                target.put(key, value);
            }
        }
    }
}
