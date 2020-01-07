package com.lombardrisk.ignis.data.common.utils;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import io.vavr.control.Validation;
import lombok.Data;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@UtilityClass
public class CsvUtils {

    private static final String CSV_DELIMITER = ",";

    public static Validation<CRUDFailure, List<Map<String, String>>> readCsv(
            final Set<String> schemaFields,
            final InputStream csvInputStream) {
        List<Map<String, String>> csvContent = new ArrayList<>();

        try {
            CsvMapper mapper = new CsvMapper();
            CsvSchema schema = CsvSchema.emptySchema().withHeader();

            MappingIterator<Map<String, String>> it = mapper.readerFor(Map.class)
                    .with(schema)
                    .readValues(csvInputStream);

            while (it.hasNext()) {
                Map<String, String> csvRow = it.next();
                if (csvRow.entrySet().size() != schemaFields.size()) {
                    return Validation.invalid(CRUDFailure.constraintFailure(
                            "Invalid CSV row columns comparing to expected schema fields number"));
                }

                csvContent.add(csvRow);
            }
        } catch (Exception ex) {
            log.info("Error when reading CSV content", ex);
            return Validation.invalid(CRUDFailure.constraintFailure("Error when reading CSV content"));
        }

        if (csvContent.isEmpty()) {
            return Validation.invalid(CRUDFailure.constraintFailure("Empty CSV content"));
        }

        if (!compareHeaderWithExpectedFields(schemaFields, csvContent.get(0).keySet())) {
            return Validation.invalid(CRUDFailure.constraintFailure(
                    "Invalid CSV header comparing to expected schema fields"));
        }

        return Validation.valid(csvContent);
    }

    public <T extends OutputStream> Validation<CRUDFailure, CsvUtils.CsvOutputStream<T>> writeCsv(
            final T outputStream,
            final String filename,
            final List<List<String>> csvContent) {

        if (csvContent == null) {
            return Validation.invalid(CRUDFailure.constraintFailure(
                    "Invalid content to write in output stream"));
        }

        try {
            outputStream.write(csvContent.stream()
                    .map(line -> formatCsvRowLine(line, CSV_DELIMITER))
                    .collect(Collectors.joining("\n"))
                    .getBytes(Charset.forName("UTF-8")));

            return Validation.valid(
                    new CsvUtils.CsvOutputStream<>(filename, outputStream));
        } catch (IOException e) {
            log.error("Could not write csv content to output stream", e);
            return Validation.invalid(CRUDFailure.constraintFailure(
                    "Could not write csv content to output stream"));
        }
    }

    private static String formatCsvRowLine(final Collection<String> values, final CharSequence delimiter) {
        return values.stream()
                .collect(Collectors.joining(delimiter));
    }

    @Data
    public static final class CsvOutputStream<T extends OutputStream> {

        private final String filename;
        private final T outputStream;

        public CsvOutputStream(final String filename, final T outputStream) {
            this.filename = filename + ".csv";
            this.outputStream = outputStream;
        }
    }

    private boolean compareHeaderWithExpectedFields(Set<String> schemaFields, Set<String> csvHeaders) {
        if (schemaFields.size() != csvHeaders.size()) {
            return false;
        }

        for (String csvHeader : csvHeaders) {
            if (!schemaFields.contains(csvHeader)) {
                return false;
            }
        }
        return true;
    }
}
