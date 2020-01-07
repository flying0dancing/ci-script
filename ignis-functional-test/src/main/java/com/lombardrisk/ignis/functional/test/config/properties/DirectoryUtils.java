package com.lombardrisk.ignis.functional.test.config.properties;

import lombok.experimental.UtilityClass;

import java.io.File;
import java.nio.file.Path;

@UtilityClass
public class DirectoryUtils {

    public static void makeDirectory(final Path directoryPath) {
        File directory = directoryPath.toFile();

        if (!directory.exists() || !directory.isDirectory()) {
            boolean madeDirectory = directory.mkdir();
            if (!madeDirectory) {
                throw new IllegalStateException(String.format(
                        "Unable to create directory [%s]", directory.getAbsolutePath()));
            }
        }
    }

}
