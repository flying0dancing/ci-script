package com.lombardrisk.ignis.hadoop;

import com.lombardrisk.ignis.fs.FileSystemTemplate;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.hadoop.fs.ContentSummary;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class LocalFileSystemTemplate implements FileSystemTemplate {

    @Override
    public InputStream open(final String path) throws IOException {
        return new FileInputStream(path);
    }

    @Override
    public boolean exists(final String path) {
        return new File(path).exists();
    }

    @Override
    public boolean copy(final File source, final String target) throws IOException {
        Path targetPath = Paths.get(target);
        Files.createDirectories(targetPath.getParent());

        Files.copy(source.toPath(), targetPath);
        return targetPath.toFile().exists();
    }

    @Override
    public boolean delete(final String path) {
        return Paths.get(path).toFile().delete();
    }

    @Override
    public ContentSummary getContentSummary(final String path) {
        throw new NotImplementedException("nope");
    }

    @Override
    public Optional<String> readSmallFile(final String path) {
        throw new NotImplementedException("nope");
    }
}
