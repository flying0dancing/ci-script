package com.lombardrisk.ignis.spark.staging.execution;

import com.lombardrisk.ignis.spark.staging.execution.load.CsvLineParser;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class CsvLineParserTest {

    @Test
    public void testParse() throws Exception {
        CsvLineParser csvLineParser = CsvLineParser.builder()
                .line("a,b,c,d").build();

        assertThat(csvLineParser.parse().size()).isEqualTo(4);
        assertThat(csvLineParser.parse()).containsExactly("a", "b", "c", "d");
    }

    @Test
    public void emptyLine() throws IOException {
        CsvLineParser parser = CsvLineParser.builder().line("").build();
        assertThat(parser.parse()).isEmpty();
    }
}