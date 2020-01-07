package com.lombardrisk.ignis.common;

import lombok.experimental.UtilityClass;
import org.apache.commons.compress.utils.IOUtils;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@UtilityClass
public class ZipUtils {

    public static Map<String, byte[]> readFiles(final InputStream inputStream) throws IOException {
        Map<String, byte[]> fileContentByFileName = new HashMap<>();

        try (ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                if (!zipEntry.isDirectory()) {
                    byte[] zipEntryBytes = IOUtils.toByteArray(zipInputStream);
                    fileContentByFileName.put(zipEntry.getName(), zipEntryBytes);
                    zipInputStream.closeEntry();
                }
            }
        }
        return fileContentByFileName;
    }

    public static <T extends OutputStream> T writeFiles(
            final Map<String, byte[]> filenamesToFiles, final T outputStream) throws IOException {

        try (ZipOutputStream zip = new ZipOutputStream(new BufferedOutputStream(outputStream))) {
            for (Map.Entry<String, byte[]> fileEntry : filenamesToFiles.entrySet()) {
                zip.putNextEntry(new ZipEntry(fileEntry.getKey()));
                zip.write(fileEntry.getValue());
                zip.closeEntry();
            }
        }

        return outputStream;
    }
}
