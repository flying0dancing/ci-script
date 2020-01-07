package com.lombardrisk.ignis.spark.staging.execution.load;

import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.supercsv.io.CsvListReader;
import org.supercsv.prefs.CsvPreference;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class CsvLineParser {

    private final String line;

    @Builder
    public CsvLineParser(final String line) {
        this.line = line;
    }

    public List<String> parse(final boolean trimField) throws IOException {
        if (StringUtils.isBlank(line)) {
            return Collections.emptyList();
        }

        try (Reader in = new InputStreamReader(
                new BufferedInputStream(new ByteArrayInputStream(line.getBytes())),
                Charset.forName("UTF-8"))) {

            CsvListReader reader = new CsvListReader(in, CsvPreference.STANDARD_PREFERENCE);
            if (trimField) {
                return reader.read().stream()
                        .map(StringUtils::trim)
                        .collect(Collectors.toList());
            }

            return reader.read();
        }
    }

    public List<String> parse() throws IOException {
        return parse(false);
    }
}