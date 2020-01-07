package com.lombardrisk.ignis.server.util;

import com.lombardrisk.ignis.server.config.IntegrationTestConfig;
import io.vavr.control.Try;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@IntegrationTestConfig
public class TempDirectoryIT {

    @Autowired
    private TempDirectory tempDirectory;

    @Test
    public void createDeleteDirectory() {
        Try<Path> created = tempDirectory.createDirectory("this-is-a-test-directory");

        assertThat(created.isSuccess())
                .isTrue();

        File createdDirectory = created.get().toFile();

        assertThat(createdDirectory)
                .exists();

        assertThat(createdDirectory)
                .isDirectory();

        Try<Boolean> deleted = tempDirectory.delete("this-is-a-test-directory");

        assertThat(deleted.isSuccess())
                .isTrue();

        assertThat(deleted.get())
                .isTrue();
    }

    @Test
    public void createDirectory_InvalidDirectory() {
        Try<Path> directory = tempDirectory.createDirectory("this/is/invalid");

        assertThat(directory.isFailure()).isTrue();

        assertThat(directory.getCause())
                .isInstanceOf(NoSuchFileException.class);
    }

    @Test
    public void delete_DeletesRecursively() throws IOException {
        Try<Path> created = tempDirectory.createDirectory("directory-with-files");

        assertThat(created.isSuccess())
                .isTrue();

        assertThat(created.get())
                .exists();

        writeToFile("this is file1", new File(created.get().toString(), "file1.txt"));
        writeToFile("this is file2", new File(created.get().toString(), "file2.txt"));
        writeToFile("this is file3", new File(created.get().toString(), "file3.txt"));

        assertThat(created.get().toFile().listFiles())
                .hasSize(3);

        Try<Boolean> deletedSingleFile = tempDirectory.delete("directory-with-files/file2.txt");

        assertThat(deletedSingleFile.isSuccess())
                .isTrue();

        assertThat(created.get().toFile().listFiles())
                .hasSize(2);

        Try<Boolean> deletedDirectory = tempDirectory.delete("directory-with-files");

        assertThat(deletedDirectory.isSuccess())
                .isTrue();

        assertThat(created.get())
                .doesNotExist();
    }

    @Test
    public void resolvePath() throws IOException {
        Path someFile = tempDirectory.resolvePath("some-file.txt");

        writeToFile("some text", someFile.toFile());

        assertThat(someFile)
                .endsWith(Paths.get("target/some-file.txt"));

        assertThat(someFile.toFile())
                .exists();

        Try<Boolean> deleted = tempDirectory.delete("some-file.txt");

        assertThat(deleted.isSuccess())
                .isTrue();

        assertThat(someFile.toFile())
                .doesNotExist();
    }

    private void writeToFile(final String text, final File file) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(text.getBytes());
            fos.flush();
        }
    }
}