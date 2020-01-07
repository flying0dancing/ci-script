package com.lombardrisk.ignis.performance.test.steps.service.reporting;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.function.Function.identity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

@SuppressWarnings("squid:S00100")
public class StatisticsAverageReportTest {

    @Test
    public void testReport_oddNumberOfItems() {
        ImmutableList<Long> data = ImmutableList.of(11L, 4L, 27L, 48L, 2L, 28L, 18L);

        StatisticsAverageReport report = StatisticsAverageReport.build(data, identity());

        assertThat(report.getTotal()).isEqualTo(7);
        assertThat(report.getMin()).isEqualTo(2L);
        assertThat(report.getMax()).isEqualTo(48L);
        assertThat(report.getMedian()).isEqualTo(18D);
        assertThat(report.getNinetyPercentile()).isEqualTo(36D);
    }

    @Test
    public void testReport_evenNumberOfItems() {
        ImmutableList<Long> data = ImmutableList.of(11L, 4L, 27L, 48L, 2L, 28L, 18L, 43L);

        StatisticsAverageReport report = StatisticsAverageReport.build(data, identity());

        assertThat(report.getTotal()).isEqualTo(8);
        assertThat(report.getMin()).isEqualTo(2L);
        assertThat(report.getMax()).isEqualTo(48L);
        assertThat(report.getMedian()).isEqualTo(22.5, offset(0D));
        assertThat(report.getNinetyPercentile()).isEqualTo(44.5D);
    }

    @Test
    public void testReport_singleItem() {
        List<Long> data = singletonList(11L);

        StatisticsAverageReport report = StatisticsAverageReport.build(data, identity());

        assertThat(report.getTotal()).isEqualTo(1);
        assertThat(report.getMin()).isEqualTo(11L);
        assertThat(report.getMax()).isEqualTo(11L);
        assertThat(report.getMedian()).isEqualTo(11);
        assertThat(report.getNinetyPercentile()).isEqualTo(11);
    }

    @Test
    public void testEmptyReport() {
        StatisticsAverageReport report = StatisticsAverageReport.build(emptyList(), identity());

        assertThat(report.getTotal()).isEqualTo(0);
        assertThat(report.getMin()).isEqualTo(0L);
        assertThat(report.getMax()).isEqualTo(0L);
        assertThat(report.getMedian()).isEqualTo(0D);
        assertThat(report.getNinetyPercentile()).isEqualTo(0D);
    }

    @Test
    public void testCsvOutput() throws JsonProcessingException {
        StatisticsAverageReport
                report = StatisticsAverageReport.build(ImmutableList.of(11L, 4L, 27L, 48L, 2L, 28L, 18L, 43L), identity());

        CsvMapper csvMapper = new CsvMapper();
        CsvSchema schema = csvMapper.schemaFor(StatisticsAverageReport.class).withHeader();
        String csvOutput = csvMapper.writer(schema).writeValueAsString(report);

        assertThat(csvOutput).isEqualTo(""
                + "90th,max,median,min\n"
                + "44.5,48,22.5,2\n");
    }
}