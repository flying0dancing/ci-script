package com.lombardrisk.ignis.data.common.utils;

import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import io.vavr.control.Validation;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.lombardrisk.ignis.data.common.utils.CsvUtils.readCsv;
import static com.lombardrisk.ignis.data.common.utils.CsvUtils.writeCsv;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class CsvUtilsTest {

    @Test
    public void readCsv_validCsvContent_returnsCsvContent() {
        final ByteArrayInputStream byteArrayInputStream =
                new ByteArrayInputStream("Name,desc\nmatt,java dev".getBytes());

        Set<String> csvFields = new HashSet<>();
        csvFields.add("Name");
        csvFields.add("desc");

        Validation<CRUDFailure, List<Map<String, String>>> csvReadContentResult =
                readCsv(csvFields, byteArrayInputStream);

        List<Map<String, String>> csvContent = csvReadContentResult.get();

        assertThat(csvContent.size()).isEqualTo(1);
        assertThat(csvContent.get(0)).isInstanceOf(Map.class);
        assertThat(csvContent.get(0)).containsKey("Name");
        assertThat(csvContent.get(0).get("Name")).isEqualTo("matt");
        assertThat(csvContent.get(0)).containsKey("desc");
        assertThat(csvContent.get(0).get("desc")).isEqualTo("java dev");
    }

    @Test
    public void readCsv_emptyCsvContent_returnsCrudFailure() {
        final ByteArrayInputStream byteArrayInputStream =
                new ByteArrayInputStream(new byte[]{});

        Set<String> csvFields = new HashSet<>();
        csvFields.add("Name");
        csvFields.add("desc");

        Validation<CRUDFailure, List<Map<String, String>>> csvReadContentResult =
                readCsv(csvFields, byteArrayInputStream);

        assertThat(csvReadContentResult.getError())
                .isEqualTo(CRUDFailure.constraintFailure("Error when reading CSV content"));
    }

    @Test
    public void readCsv_csvContentWithWithOnlyHeaderLine_returnsCrudFailure() {
        final ByteArrayInputStream byteArrayInputStream =
                new ByteArrayInputStream("Name,desc".getBytes());

        Set<String> csvFields = new HashSet<>();
        csvFields.add("Name");
        csvFields.add("desc");

        Validation<CRUDFailure, List<Map<String, String>>> csvReadContentResult =
                readCsv(csvFields, byteArrayInputStream);

        assertThat(csvReadContentResult.getError())
                .isEqualTo(CRUDFailure.constraintFailure("Empty CSV content"));
    }

    @Test
    public void readCsv_aCsvRowColumnSizeDifferentThanTheExpectedHeaderEntries_returnsCrudFailure() {
        final ByteArrayInputStream byteArrayInputStream =
                new ByteArrayInputStream(("Name,desc,email\n"
                        + "matt,java dev\n"
                        + "Mohamed,technical architect,motounsi@vermeg.com").getBytes());

        Set<String> csvFields = new HashSet<>();
        csvFields.add("Name");
        csvFields.add("desc");
        csvFields.add("email");

        Validation<CRUDFailure, List<Map<String, String>>> csvReadContentResult =
                readCsv(csvFields, byteArrayInputStream);

        assertThat(csvReadContentResult.getError())
                .isEqualTo(CRUDFailure.constraintFailure(
                        "Invalid CSV row columns comparing to expected schema fields number"));
    }

    @Test
    public void readCsv_aCsvRowWithTooManyEntriesThanTheExpectedHeaderEntries_returnsCrudFailure() {
        final ByteArrayInputStream byteArrayInputStream =
                new ByteArrayInputStream(("Name,desc\n"
                        + "matt,java dev\n"
                        + "Mohamed,technical architect,motounsi@vermeg.com").getBytes());

        Set<String> csvFields = new HashSet<>();
        csvFields.add("Name");
        csvFields.add("desc");

        Validation<CRUDFailure, List<Map<String, String>>> csvReadContentResult =
                readCsv(csvFields, byteArrayInputStream);

        assertThat(csvReadContentResult.getError())
                .isEqualTo(CRUDFailure.constraintFailure("Error when reading CSV content"));
    }

    @Test
    public void readCsv_expectedColumnsDifferentThanCsvHeader_returnsCrudFailure() {
        final ByteArrayInputStream byteArrayInputStream =
                new ByteArrayInputStream(("Name,desc,email\n"
                        + "matt,java dev,matthew@vermeg.com\n"
                        + "Mohamed,technical architect,motounsi@vermeg.com").getBytes());

        Set<String> csvFields = new HashSet<>();
        csvFields.add("Name");
        csvFields.add("Login");
        csvFields.add("email");

        Validation<CRUDFailure, List<Map<String, String>>> csvReadContentResult =
                readCsv(csvFields, byteArrayInputStream);

        assertThat(csvReadContentResult.getError())
                .isEqualTo(CRUDFailure.constraintFailure("Invalid CSV header comparing to expected schema fields"));
    }

    @Test
    public void writeCsv_validContent_addContentToOutputStream() {
        List<String> header = new ArrayList<>();
        header.addAll(asList("c1", "c2"));

        List<String> line = new ArrayList<>();
        line.addAll(asList("ab", "cd"));

        List<List<String>> csvContent = new ArrayList<>();
        csvContent.addAll(asList(header, line));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        writeCsv(outputStream, "file.txt", csvContent);
        assertThat(new String(outputStream.toByteArray()))
                .isEqualTo("c1,c2\nab,cd");
    }

    @Test
    public void writeCsv_contentIsNull_returnsCrudFailure() {
        List<String> header = new ArrayList<>();
        header.addAll(asList("c1", "c2"));

        List<String> line = new ArrayList<>();
        line.addAll(asList("ab", "cd"));

        List<List<String>> csvContent = new ArrayList<>();
        csvContent.addAll(asList(header, line));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Validation<CRUDFailure, CsvUtils.CsvOutputStream<ByteArrayOutputStream>> csvWriteContentResult =
                writeCsv(outputStream, "file.txt", null);

        assertThat(csvWriteContentResult.getError())
                .isEqualTo(CRUDFailure.constraintFailure("Invalid content to write in output stream"));
    }
}