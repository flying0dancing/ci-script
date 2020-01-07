package com.lombardrisk.ignis.hadoop;

import com.lombardrisk.ignis.fs.FileSystemTemplate;
import org.apache.commons.lang3.SystemUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.ContentSummary;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

import static org.apache.commons.io.FileUtils.ONE_MB;
import static org.apache.commons.lang3.StringUtils.repeat;
import static org.assertj.core.api.Assertions.assertThat;

public class FileSystemTemplateTest {

    private static final String FILE_NOT_EXISTS = "fileNotExists";

    private FileSystemTemplate fileSystemTemplate;

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Before
    public void setup() {
        fileSystemTemplate = HdfsTemplate.builder()
                .hdfsUser("hdfs")
                .configuration(new Configuration())
                .build();

        if (SystemUtils.IS_OS_WINDOWS) {
            System.setProperty(
                    "hadoop.home.dir",
                    Paths.get("../../ignis-core/ignis-common/src/test/resources/hadoop").toAbsolutePath().toString());
        }
    }

    @Test
    public void open_ReturnsInputStream() throws IOException {
        File file = Files.write(temp.newFile().toPath(), "1".getBytes()).toFile();

        InputStream inputStream = fileSystemTemplate.open(file.getAbsolutePath());

        assertThat(inputStream).hasContent("1");
    }

    @Test
    public void exists_NonExistentFile_ReturnsFalse() throws IOException {
        assertThat(
                fileSystemTemplate.exists(FILE_NOT_EXISTS)
        ).isFalse();
    }

    @Test
    public void exists_ExistentFile_ReturnsTrue() throws IOException {
        assertThat(
                fileSystemTemplate.exists(temp.newFile().getAbsolutePath())
        ).isTrue();
    }

    @Test
    public void copy_File_CopiesFile() throws IOException {
        File input = temp.newFile();
        String destination = temp.newFolder().getAbsolutePath();

        fileSystemTemplate.copy(input, destination);

        assertThat(new File(destination, input.getName())).exists();
    }

    @Test
    public void delete_DeletesFile() throws IOException {
        File input = temp.newFile();

        fileSystemTemplate.delete(input.getAbsolutePath());

        assertThat(input).doesNotExist();
    }

    @Test
    public void readSmallFile_ReturnsContents() throws IOException {
        File checksum = Files.write(temp.newFile("checksum").toPath(), "0".getBytes()).toFile();
        Optional<String> contents = fileSystemTemplate.readSmallFile(checksum.getAbsolutePath());

        assertThat(contents)
                .isPresent()
                .contains("0");
    }

    @Test
    public void readSmallFile_MissingFile_ReturnsEmpty() throws IOException {
        Optional<String> contents = fileSystemTemplate.readSmallFile(FILE_NOT_EXISTS);

        assertThat(contents).isEmpty();
    }

    @Test
    public void readSmallFile_FileTooLarge_ReturnsEmpty() throws IOException {
        File checksum = temp.newFile("checksum");
        Files.write(checksum.toPath(), repeat("0", Long.valueOf(ONE_MB + 1).intValue()).getBytes());

        Optional<String> contents = fileSystemTemplate.readSmallFile(checksum.getAbsolutePath());

        assertThat(contents).isEmpty();
    }

    @Test
    public void getContentSummary_ReturnsFileContentSummary() throws IOException {
        File file = temp.newFile();
        Files.write(file.toPath(), "0".getBytes());

        ContentSummary contentSummary = fileSystemTemplate.getContentSummary(file.getAbsolutePath());

        assertThat(contentSummary.getLength())
                .isEqualTo(1L);
    }
}