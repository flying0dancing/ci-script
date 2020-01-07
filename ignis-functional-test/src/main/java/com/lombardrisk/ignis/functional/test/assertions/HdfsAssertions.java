package com.lombardrisk.ignis.functional.test.assertions;

import com.lombardrisk.ignis.fs.FileSystemTemplate;
import org.assertj.core.api.SoftAssertions;

import java.io.IOException;
import java.nio.file.Path;

public class HdfsAssertions {

    private final SoftAssertions soft = new SoftAssertions();
    private final FileSystemTemplate fileSystemTemplate;

    public HdfsAssertions(final FileSystemTemplate fileSystemTemplate) {
        this.fileSystemTemplate = fileSystemTemplate;
    }

    public HdfsAssertions doesNotHaveFile(final Path filePath) throws IOException {
        soft.assertThat(fileSystemTemplate.exists(filePath.toString()))
                .describedAs("Expected file " + filePath.toString() + " to not exist")
                .isFalse();
        return this;
    }

    public void assertAll() {
        soft.assertAll();
    }
}
