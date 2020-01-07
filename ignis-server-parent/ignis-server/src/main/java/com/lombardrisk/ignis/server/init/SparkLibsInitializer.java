package com.lombardrisk.ignis.server.init;

import com.lombardrisk.ignis.common.ZipUtils;
import com.lombardrisk.ignis.fs.FileSystemTemplate;
import com.lombardrisk.ignis.server.util.spark.SparkConfFactory;
import io.vavr.Tuple;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.google.common.base.Objects.equal;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.readAllBytes;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.lang3.SystemUtils.JAVA_IO_TMPDIR;

@Slf4j
public class SparkLibsInitializer {

    private static final String MD5_EXT = ".MD5";
    public static final String SPARK_LIBS_DIR = "__spark_libs__";

    private final String sparkLibsPath;
    private final boolean localSpark;
    private final boolean copySparkLibs;

    private final FileSystemTemplate fileSystemTemplate;
    private final String sparkYarnArchive;

    public SparkLibsInitializer(
            final String sparkLibsPath,
            final FileSystemTemplate fileSystemTemplate,
            final SparkConfFactory sparkConfFactory,
            final boolean localSpark,
            final boolean copySparkLibs) {
        this.sparkLibsPath = sparkLibsPath;
        this.fileSystemTemplate = fileSystemTemplate;
        this.sparkYarnArchive = sparkConfFactory.create().get("spark.yarn.archive");
        this.localSpark = localSpark;
        this.copySparkLibs = copySparkLibs;
    }

    @PostConstruct
    public void upload() throws IOException {
        if (localSpark) {
            unpackSparkLibs();
        } else if (copySparkLibs) {
            uploadSparkJars();
        }
    }

    private void unpackSparkLibs() throws IOException {
        File sparkLibsTempDir = createDirectories(Paths.get(JAVA_IO_TMPDIR, SPARK_LIBS_DIR)).toFile();

        File sparkLibsZip = new File(sparkLibsPath);
        if (hasMissingFiles(sparkLibsTempDir, sparkLibsZip)) {
            log.info("Setup local Spark libs dir in [{}]", sparkLibsTempDir);

            unpack(sparkLibsZip, sparkLibsTempDir);
        } else {
            log.info("Local Spark libs are already set up in [{}]", sparkLibsTempDir);
        }
    }

    private static boolean hasMissingFiles(final File sparkLibsTempDir, final File sparkLibsZip) throws IOException {
        try (ZipFile zipFile = new ZipFile(sparkLibsZip)) {
            return zipFile
                    .stream()
                    .map(ZipEntry::getName)
                    .map(sparkLibsTempDir.toPath()::resolve)
                    .anyMatch(sparkLib -> !sparkLib.toFile().canRead());
        }
    }

    private static void unpack(final File sparkLibsZipFile, final File sparkLibsTempDir) throws IOException {
        deleteQuietly(sparkLibsTempDir);
        createDirectories(sparkLibsTempDir.toPath());

        Map<String, byte[]> sparkLibsJarFiles = ZipUtils.readFiles(new FileInputStream(sparkLibsZipFile));

        sparkLibsJarFiles.entrySet().parallelStream()
                .map(fileNameAndContents -> Tuple.of(
                        new File(sparkLibsTempDir, fileNameAndContents.getKey()),
                        fileNameAndContents.getValue()))
                .forEach(fileNameAndContents ->
                        writeSparkLibFile(fileNameAndContents._1, fileNameAndContents._2));
    }

    private static void writeSparkLibFile(final File sparkLib, final byte[] contents) {
        log.trace("Write Spark lib [{}]", sparkLib);
        try {
            Files.write(sparkLib.toPath(), contents, CREATE, WRITE, TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void uploadSparkJars() throws IOException {
        File sparkLibsChecksum = new File(sparkLibsPath + MD5_EXT);
        String sparkYarnArchiveChecksum = sparkYarnArchive + MD5_EXT;

        if (hasDiffChecksum(sparkLibsChecksum, sparkYarnArchiveChecksum)
                || hasDiffLength(sparkLibsPath, sparkYarnArchive)) {
            deleteExistingSparkLibs(sparkYarnArchiveChecksum);

            uploadSparkLibs(sparkLibsChecksum, sparkYarnArchiveChecksum);
        } else {
            log.info("Remote and local Spark libs are the same");
            log.debug("[{}] and [{}] have the same checksum and length", sparkLibsPath, sparkYarnArchive);
        }
    }

    private void uploadSparkLibs(
            final File sparkLibsChecksum,
            final String sparkYarnArchiveChecksum) throws IOException {
        File sparkLibsZip = new File(sparkLibsPath);
        log.info("Upload spark libs and checksum from [{}] to [{}]", sparkLibsZip, sparkYarnArchive);

        fileSystemTemplate.copy(sparkLibsChecksum, sparkYarnArchiveChecksum);
        fileSystemTemplate.copy(sparkLibsZip, sparkYarnArchive);

        log.info("Spark libs uploaded");
    }

    private boolean hasDiffLength(
            final String sparkLibsPath,
            final String sparkYarnArchive) throws IOException {
        if (fileSystemTemplate.exists(sparkYarnArchive)) {
            long sparkYarnArchiveLength = fileSystemTemplate.getContentSummary(sparkYarnArchive).getLength();
            long sparkLibsLength = new File(sparkLibsPath).length();

            return sparkYarnArchiveLength != sparkLibsLength;
        }
        return true;
    }

    private boolean hasDiffChecksum(
            final File sparkLibsChecksumFile,
            final String sparkYarnArchiveChecksumFile) throws IOException {
        Optional<String> yarnArchiveChecksum = fileSystemTemplate.readSmallFile(sparkYarnArchiveChecksumFile);

        if (yarnArchiveChecksum.isPresent()) {
            String sparkLibsChecksum = new String(readAllBytes(sparkLibsChecksumFile.toPath()), UTF_8);

            return !equal(yarnArchiveChecksum.get().trim(), sparkLibsChecksum.trim());
        }
        return true;
    }

    private void deleteExistingSparkLibs(final String sparkYarnArchiveChecksum) throws IOException {
        if (fileSystemTemplate.exists(sparkYarnArchiveChecksum)) {
            fileSystemTemplate.delete(sparkYarnArchiveChecksum);
        }
        if (fileSystemTemplate.exists(sparkYarnArchive)) {
            fileSystemTemplate.delete(sparkYarnArchive);
        }
    }
}