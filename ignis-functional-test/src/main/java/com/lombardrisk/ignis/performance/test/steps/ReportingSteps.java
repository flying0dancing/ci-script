package com.lombardrisk.ignis.performance.test.steps;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.lombardrisk.ignis.performance.test.config.properties.PerformanceTestProperties;
import com.lombardrisk.ignis.performance.test.steps.service.reporting.NumberUtils;
import com.lombardrisk.ignis.performance.test.steps.service.reporting.StagingJobData;
import com.lombardrisk.ignis.performance.test.steps.service.reporting.StatisticsAverageReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

public class ReportingSteps {

    private static final Logger log = LoggerFactory.getLogger(ReportingSteps.class);

    private final CsvMapper csvMapper;
    private final PerformanceTestProperties performanceTestProperties;

    public ReportingSteps(
            final CsvMapper csvMapper,
            final PerformanceTestProperties performanceTestProperties) {
        this.csvMapper = csvMapper;
        this.performanceTestProperties = performanceTestProperties;
    }

    public void generateReports(final String reportName, final List<StagingJobData> stagingJobData) throws IOException {
        log.info("Generating report [{}]", reportName);

        List<StagingJobData> reports = stagingJobData.stream()
                .sorted(comparing(StagingJobData::getJobStartTime))
                .collect(toList());

        writeStagingJobDataCsv(reportName, reports);
        writeSoakTestAverageCsv(reportName, reports);
        writeRowToDurationCsv(reportName, reports);
    }

    private void writeStagingJobDataCsv(final String reportName, final List<StagingJobData> data) throws IOException {
        File csvFile = performanceTestProperties.getReportsPath().resolve(reportName + ".csv").toFile();

        CsvSchema schema = csvMapper.schemaFor(StagingJobData.class).withHeader();
        csvMapper.writer(schema).writeValue(csvFile, data);
    }

    private void writeSoakTestAverageCsv(final String reportName, final List<StagingJobData> data) throws IOException {
        File csvFile = performanceTestProperties.getTrendReportsPath().resolve(reportName + "-average.csv").toFile();

        CsvSchema schema = csvMapper.schemaFor(StatisticsAverageReport.class).withHeader();
        csvMapper.writer(schema).writeValue(csvFile, StatisticsAverageReport.build(data, StagingJobData::getDuration));
    }

    private void writeRowToDurationCsv(final String reportName, final List<StagingJobData> data) throws IOException {
        File csvFile = performanceTestProperties.getTrendReportsPath().resolve(reportName + "-plot.csv").toFile();

        Object[] csvHeaders = data.stream()
                .map(StagingJobData::getDatasetRows)
                .map(NumberUtils::scaleNumber)
                .toArray();

        Object[] csvRows = data.stream()
                .map(StagingJobData::getDuration)
                .toArray();

        csvMapper.writerFor(Object[].class).writeValue(csvFile, new Object[]{ csvHeaders, csvRows });
    }
}
