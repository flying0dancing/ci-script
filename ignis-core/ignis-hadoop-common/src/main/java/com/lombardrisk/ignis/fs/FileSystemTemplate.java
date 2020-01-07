package com.lombardrisk.ignis.fs;

import org.apache.hadoop.fs.ContentSummary;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public interface FileSystemTemplate {

    InputStream open(final String path) throws IOException;

    boolean exists(final String path) throws IOException;

    boolean copy(final File source, final String target) throws IOException;

    boolean delete(final String path) throws IOException;

    ContentSummary getContentSummary(final String path) throws IOException;

    Optional<String> readSmallFile(final String path) throws IOException;
}
