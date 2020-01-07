package com.lombardrisk.ignis.server.util;

import io.vavr.control.Try;
import org.springframework.core.env.Environment;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TempDirectory {

    private final Path tmpPath;

    public TempDirectory(final Environment environment) {
        tmpPath = Paths.get(environment.getRequiredProperty("ignis.tmp.path", String.class));

        File tmpDir = tmpPath.toFile();
        Path tmpAbsolutePath = tmpPath.toAbsolutePath();

        if (!tmpDir.exists()) {
            throw new IllegalArgumentException(String.format("[%s] does not exist", tmpAbsolutePath));
        }
        if (!tmpDir.isDirectory()) {
            throw new IllegalArgumentException(String.format("[%s] is not a directory", tmpAbsolutePath));
        }
        if (!tmpDir.canWrite()) {
            throw new IllegalArgumentException(String.format("[%s] is not writable", tmpAbsolutePath));
        }
    }

    public Path resolvePath(final String path) {
        return tmpPath.resolve(path);
    }

    public Try<Path> createDirectory(final String directoryName) {
        return Try.of(() -> Files.createDirectory(tmpPath.resolve(directoryName)));
    }

    public Try<Boolean> delete(final String file) {
        return Try.of(() -> FileSystemUtils.deleteRecursively(tmpPath.resolve(file)));
    }
}
