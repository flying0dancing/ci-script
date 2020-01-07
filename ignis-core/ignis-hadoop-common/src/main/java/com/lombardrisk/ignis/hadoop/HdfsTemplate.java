package com.lombardrisk.ignis.hadoop;

import com.lombardrisk.ignis.fs.FileSystemTemplate;
import io.vavr.CheckedFunction1;
import io.vavr.control.Try;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.ContentSummary;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.security.UserGroupInformation;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.PrivilegedAction;
import java.util.Optional;

import static java.lang.System.lineSeparator;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.joining;

@Slf4j
public class HdfsTemplate implements FileSystemTemplate {

    private static final long SMALL_FILE_MAX_LENGTH = FileUtils.ONE_MB;

    private final Configuration configuration;
    private final UserGroupInformation ugi;

    @Builder
    public HdfsTemplate(
            final @NonNull Configuration configuration,
            final @NonNull String hdfsUser) {
        this.configuration = configuration;
        this.ugi = UserGroupInformation.createRemoteUser(hdfsUser);
    }

    @Override
    public InputStream open(final String path) throws IOException {
        return execute(fileSystem -> fileSystem.open(new Path(path)));
    }

    @Override
    public boolean exists(final String path) throws IOException {
        return execute(fileSystem -> fileSystem.exists(new Path(path)));
    }

    @Override
    public boolean copy(final File source, final String target) throws IOException {
        return execute(fileSystem ->
                FileUtil.copy(source, fileSystem, new Path(target), false, configuration));
    }

    public boolean delete(final String path) throws IOException {
        return execute(fileSystem -> fileSystem.delete(new Path(path), true));
    }

    @Override
    public ContentSummary getContentSummary(final String path) throws IOException {
        return execute(fileSystem -> fileSystem.getContentSummary(new Path(path)));
    }

    private <T> T execute(final CheckedFunction1<FileSystem, T> mapper) throws IOException {
        return ugi
                .doAs(getTryPrivilegedAction(mapper))
                .getOrElseThrow(this::toIoException);
    }

    @Override
    public Optional<String> readSmallFile(final String path) throws IOException {
        if (!exists(path)) {
            return Optional.empty();
        }
        ContentSummary summary = getContentSummary(path);

        if (summary.getLength() <= SMALL_FILE_MAX_LENGTH) {
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(open(path), UTF_8))) {
                return Optional.of(
                        bufferedReader.lines()
                                .collect(joining(lineSeparator())));
            }
        }
        return Optional.empty();
    }

    private IOException toIoException(final Throwable e) {
        return e instanceof IOException
                ? (IOException) e
                : new IOException(e);
    }

    private <T> PrivilegedAction<Try<T>> getTryPrivilegedAction(final CheckedFunction1<FileSystem, T> mapper) {
        return () -> {
            Try<FileSystem> fileSystems = Try.of(() -> FileSystem.get(configuration));
            return fileSystems.mapTry(mapper);
        };
    }
}
