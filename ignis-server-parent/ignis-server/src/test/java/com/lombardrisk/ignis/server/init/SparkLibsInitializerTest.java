package com.lombardrisk.ignis.server.init;

import com.lombardrisk.ignis.hadoop.HdfsTemplate;
import com.lombardrisk.ignis.server.util.spark.SparkConfFactory;
import org.apache.hadoop.fs.ContentSummary;
import org.apache.spark.SparkConf;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.nio.file.Files;
import java.util.Optional;

import static java.nio.file.Files.createFile;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SparkLibsInitializerTest {

    private static final boolean MISSING_CHECKSUM = false;
    private static final boolean EXISTING_ARCHIVE = true;

    @Mock
    private HdfsTemplate fileSystemTemplate;
    @Mock
    private SparkConfFactory sparkConfFactory;
    @Mock
    private SparkConf sparkConf;

    private SparkLibsInitializer sparkLibsInitializer;

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Before
    public void setup() {
        when(sparkConfFactory.create())
                .thenReturn(sparkConf);

        when(sparkConf.get("spark.yarn.archive"))
                .thenReturn("testhdfs://__spark_libs__.zip");

        sparkLibsInitializer =
                new SparkLibsInitializer("spark/sparklibs.zip", fileSystemTemplate, sparkConfFactory, false, true);
    }

    @Test
    public void run_CopySparkLibsTurnedOff_SkippsSparkLibsUpload() throws Exception {
        sparkLibsInitializer =
                new SparkLibsInitializer("spark/sparklibs.zip", fileSystemTemplate, sparkConfFactory, false, false);

        sparkLibsInitializer.upload();

        verifyZeroInteractions(fileSystemTemplate);
    }

    @Test
    public void run_MissingChecksumAndArchive_DoesNotRemoveArchiveAndChecksum() throws Exception {
        when(fileSystemTemplate.readSmallFile(any()))
                .thenReturn(Optional.empty());

        when(fileSystemTemplate.exists(any()))
                .thenReturn(false);

        sparkLibsInitializer.upload();

        verify(fileSystemTemplate, never())
                .delete(anyString());
    }

    @Test
    public void run_MissingChecksumExistingArchive_RemovesArchive() throws Exception {
        when(fileSystemTemplate.readSmallFile(any()))
                .thenReturn(Optional.empty());

        when(fileSystemTemplate.exists(any()))
                .thenReturn(MISSING_CHECKSUM, EXISTING_ARCHIVE);

        sparkLibsInitializer.upload();

        verify(fileSystemTemplate)
                .delete(anyString());
    }

    @Test
    public void run_ExistingChecksumExistingArchive_RemovesArchiveAndChecksum() throws Exception {
        when(fileSystemTemplate.readSmallFile(any()))
                .thenReturn(Optional.empty());

        when(fileSystemTemplate.exists(any()))
                .thenReturn(true);

        sparkLibsInitializer.upload();

        verify(fileSystemTemplate, times(2))
                .delete(anyString());
    }

    @Test
    public void run_SameSparkLibsAndArchive_DoesNotUploadSparkLibs() throws Exception {
        File libsDir = temp.newFolder();
        File sparkLibsZip = createFile(libsDir.toPath().resolve("__spark_libs.zip")).toFile();
        File sparkLibsChecksum = createFile(libsDir.toPath().resolve("__spark_libs.zip.MD5")).toFile();
        Files.write(sparkLibsChecksum.toPath(), "1\r\n".getBytes());

        when(fileSystemTemplate.exists(any()))
                .thenReturn(true);
        when(fileSystemTemplate.readSmallFile(any()))
                .thenReturn(Optional.of("1\n"));
        when(fileSystemTemplate.getContentSummary(any()))
                .thenReturn(new ContentSummary(sparkLibsZip.length(), 0, 0));

        sparkLibsInitializer =
                new SparkLibsInitializer(
                        sparkLibsZip.getAbsolutePath(),
                        fileSystemTemplate,
                        sparkConfFactory,
                        false,
                        true);
        sparkLibsInitializer.upload();

        verify(fileSystemTemplate)
                .readSmallFile(any());
        verify(fileSystemTemplate)
                .getContentSummary(any());
        verify(fileSystemTemplate)
                .exists(any());
        verify(fileSystemTemplate, never())
                .copy(any(), any());
    }

    @Test
    public void run_SameChecksumDifferentSparkLibsAndArchive_UploadsSparkLibs() throws Exception {
        File libsDir = temp.newFolder();
        File sparkLibsZip = createFile(libsDir.toPath().resolve("__spark_libs.zip")).toFile();
        File sparkLibsChecksum = createFile(libsDir.toPath().resolve("__spark_libs.zip.MD5")).toFile();
        Files.write(sparkLibsChecksum.toPath(), "1".getBytes());

        when(fileSystemTemplate.exists(any()))
                .thenReturn(true);
        when(fileSystemTemplate.readSmallFile(any()))
                .thenReturn(Optional.of("1"));
        when(fileSystemTemplate.getContentSummary(any()))
                .thenReturn(new ContentSummary(-123, 0, 0));

        sparkLibsInitializer =
                new SparkLibsInitializer(
                        sparkLibsZip.getAbsolutePath(),
                        fileSystemTemplate,
                        sparkConfFactory,
                        false,
                        true);
        sparkLibsInitializer.upload();

        verify(fileSystemTemplate)
                .readSmallFile(any());
        verify(fileSystemTemplate)
                .getContentSummary(any());
        verify(fileSystemTemplate, times(3))
                .exists(any());
        verify(fileSystemTemplate, times(2))
                .copy(any(), any());
    }

    @Test
    public void run_DifferentSparkLibsAndArchive_UploadsSparkLibs() throws Exception {
        File libsDir = temp.newFolder();
        File sparkLibsZip = createFile(libsDir.toPath().resolve("__spark_libs.zip")).toFile();
        File sparkLibsChecksum = createFile(libsDir.toPath().resolve("__spark_libs.zip.MD5")).toFile();
        Files.write(sparkLibsChecksum.toPath(), "1".getBytes());

        when(fileSystemTemplate.exists(any()))
                .thenReturn(true);

        when(fileSystemTemplate.readSmallFile(any()))
                .thenReturn(Optional.of("2"));

        sparkLibsInitializer =
                new SparkLibsInitializer(
                        sparkLibsZip.getAbsolutePath(),
                        fileSystemTemplate,
                        sparkConfFactory,
                        false,
                        true);
        sparkLibsInitializer.upload();

        verify(fileSystemTemplate, times(2))
                .exists(any());
        verify(fileSystemTemplate, times(2))
                .copy(any(), any());
    }

    @Test
    public void run_MissingChecksumExistingArchive_UploadsSparkLibs() throws Exception {
        when(fileSystemTemplate.exists(any()))
                .thenReturn(MISSING_CHECKSUM, EXISTING_ARCHIVE);

        when(fileSystemTemplate.readSmallFile(any()))
                .thenReturn(Optional.empty());

        sparkLibsInitializer.upload();

        verify(fileSystemTemplate, times(2))
                .exists(any());
        verify(fileSystemTemplate, times(2))
                .copy(any(), any());
    }
}