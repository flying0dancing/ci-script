package com.lombardrisk.ignis.common.zip;

import com.google.common.collect.ImmutableMap;
import com.lombardrisk.ignis.common.ZipUtils;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

public class ZipUtilsTest {

    @Test
    public void readFiles_EmptyZip_ReturnsEmptyMap() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
        zipOutputStream.close();
        outputStream.close();

        Map<String, byte[]> files = ZipUtils.readFiles(new ByteArrayInputStream(outputStream.toByteArray()));

        assertThat(files).isEmpty();
    }

    @Test
    public void readFiles_InvalidZip_ReturnsEmptyMap() throws Exception {
        Map<String, byte[]> files = ZipUtils.readFiles(new ByteArrayInputStream("something random".getBytes()));

        assertThat(files).isEmpty();
    }

    @Test
    public void readFiles_ZipWithFiles_ReturnsMapOfFilesToContent() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);

        zipOutputStream.putNextEntry(new ZipEntry("file1.txt"));
        zipOutputStream.write("this is one file".getBytes());

        zipOutputStream.putNextEntry(new ZipEntry("file2.txt"));
        zipOutputStream.write("this is another file".getBytes());

        zipOutputStream.close();
        outputStream.close();

        Map<String, byte[]> files = ZipUtils.readFiles(new ByteArrayInputStream(outputStream.toByteArray()));

        assertThat(files).hasSize(2);

        byte[] file1 = files.get("file1.txt");
        byte[] file2 = files.get("file2.txt");
        assertThat(new String(file1)).isEqualTo("this is one file");
        assertThat(new String(file2)).isEqualTo("this is another file");
    }

    @Test
    public void readFiles_ZipWithFilesInsideDirectories_ReturnsMapOfFilesOnlyToContent() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);

        zipOutputStream.putNextEntry(new ZipEntry("directory/"));

        zipOutputStream.putNextEntry(new ZipEntry("directory/file1.txt"));
        zipOutputStream.write("this is inside directory".getBytes());

        zipOutputStream.putNextEntry(new ZipEntry("directory/file2.txt"));
        zipOutputStream.write("this is also inside directory".getBytes());

        zipOutputStream.putNextEntry(new ZipEntry("file3.txt"));
        zipOutputStream.write("this is outside of directory".getBytes());

        zipOutputStream.close();
        outputStream.close();

        Map<String, byte[]> files = ZipUtils.readFiles(new ByteArrayInputStream(outputStream.toByteArray()));

        assertThat(files).hasSize(3);

        byte[] file1 = files.get("directory/file1.txt");
        byte[] file2 = files.get("directory/file2.txt");
        byte[] file3 = files.get("file3.txt");
        assertThat(new String(file1)).isEqualTo("this is inside directory");
        assertThat(new String(file2)).isEqualTo("this is also inside directory");
        assertThat(new String(file3)).isEqualTo("this is outside of directory");
    }

    @Test
    public void writeFiles_EmptyCollection_WritesEmptyZip() throws IOException {
        Map<String, byte[]> inputFiles = ImmutableMap.of();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZipUtils.writeFiles(inputFiles, outputStream);

        Map<String, byte[]> outputFiles =
                ZipUtils.readFiles(new ByteArrayInputStream(outputStream.toByteArray()));

        assertThat(outputFiles).hasSize(0);
    }

    @Test
    public void writeFiles_CollectionOfFile_WritesFilesToZip() throws IOException {
        Map<String, byte[]> inputFiles = ImmutableMap.of(
                "file1.txt", "first file".getBytes(),
                "file2.json", "{\"second\":\"file\"}".getBytes(),
                "file3.doc", "third file".getBytes()
        );

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZipUtils.writeFiles(inputFiles, outputStream);

        Map<String, byte[]> outputFiles =
                ZipUtils.readFiles(new ByteArrayInputStream(outputStream.toByteArray()));

        assertThat(outputFiles).hasSize(3);
        assertThat(outputFiles).containsAllEntriesOf(inputFiles);
    }
}