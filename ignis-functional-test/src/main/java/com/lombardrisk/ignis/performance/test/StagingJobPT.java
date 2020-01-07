package com.lombardrisk.ignis.performance.test;

import com.google.common.collect.ImmutableList;
import com.lombardrisk.ignis.client.external.job.staging.request.DataSource;
import com.lombardrisk.ignis.client.external.job.staging.request.DatasetMetadata;
import com.lombardrisk.ignis.client.external.job.staging.request.v1.StagingItemRequest;
import com.lombardrisk.ignis.client.external.job.staging.request.v1.StagingRequest;
import com.lombardrisk.ignis.client.external.productconfig.view.ProductConfigView;
import com.lombardrisk.ignis.functional.test.config.PerformanceTest;
import com.lombardrisk.ignis.functional.test.dsl.FcrEngine;
import com.lombardrisk.ignis.functional.test.dsl.FcrEngineTest;
import com.lombardrisk.ignis.functional.test.dsl.StagingJobContext;
import com.lombardrisk.ignis.performance.test.steps.ReportingSteps;
import com.lombardrisk.ignis.performance.test.steps.service.reporting.StagingJobData;
import com.lombardrisk.ignis.performance.test.steps.service.reporting.StagingJobDataService;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

@SuppressWarnings({ "squid:S109", "squid:S00100" })
@RunWith(SpringRunner.class)
@PerformanceTest
@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StagingJobPT {

    private static final String APRA_PRODUCT_CONFIG_PATH = "src/main/resources/products/APRA";

    @Autowired
    private ReportingSteps reportingSteps;
    @Autowired
    private StagingJobDataService stagingJobDataService;

    @Autowired
    private FcrEngine fcrEngine;

    private FcrEngineTest test;

    @Before
    public void setUp() {
        test = fcrEngine.newTest();
    }

    @After
    public void tearDown() {
        test.cleanup();
    }

    @Test
    public void stageInIncrements() throws IOException {
        List<StagingJobData> stagingJobData = new ArrayList<>();

        List<StagingRequest> stagingRequests =
                ImmutableList.of("250k", "500k", "1m", "2m", "4m", "5m", "6m", "12m", "24m", "48m").stream()
                        .map(this::buildStagingRequest)
                        .collect(toList());

        try {
            for (StagingRequest stagingRequest : stagingRequests) {
                ProductConfigView apraProduct = test
                        .importProductConfig(APRA_PRODUCT_CONFIG_PATH)
                        .waitForJobToSucceed();

                List<StagingJobData> stagingJobRecord =
                        stagingJobDataService.recordStagingJob(() -> stageDataset(apraProduct, stagingRequest));

                stagingJobData.addAll(stagingJobRecord);

                test.cleanup();
            }
        } finally {
            generateCsvReport("stageInIncrements", stagingJobData);
        }
    }

    @Test
    public void soakTest_250k() throws IOException {
        ProductConfigView apraProduct = test.importProductConfig(APRA_PRODUCT_CONFIG_PATH).waitForJobToSucceed();

        runSoakTest(apraProduct, "250k", "soakTest_250k", 50);
    }

    @Test
    public void soakTest_5m() throws IOException {
        ProductConfigView apraProduct = test.importProductConfig(APRA_PRODUCT_CONFIG_PATH).waitForJobToSucceed();

        runSoakTest(apraProduct, "5m", "soakTest_5m", 8);
    }

    private void runSoakTest(
            final ProductConfigView productConfig,
            final String request,
            final String reportName,
            final int runs) throws IOException {

        List<StagingJobData> stagingJobData = new ArrayList<>();

        StagingRequest stagingRequest = buildStagingRequest(request);
        try {
            for (int i = 0; i < runs; i++) {
                List<StagingJobData> stagingJobRecord =
                        stagingJobDataService.recordStagingJob(() -> stageDataset(productConfig, stagingRequest));

                stagingJobData.addAll(stagingJobRecord);
            }
        } finally {
            generateCsvReport(reportName, stagingJobData);
        }
    }

    private StagingRequest buildStagingRequest(final String request) {
        return StagingRequest.builder()
                .name(request)
                .items(newHashSet(
                        StagingItemRequest.builder()
                                .schema("FINANCIAL_PERFORMANCE")
                                .source(DataSource.builder()
                                        .filePath("FINANCIALPERFORMANCE_" + request + ".csv")
                                        .header(true)
                                        .build())
                                .dataset(DatasetMetadata.builder()
                                        .entityCode("APRA")
                                        .referenceDate("01/01/2001")
                                        .build())
                                .build()))
                .build();
    }

    private StagingJobContext stageDataset(
            final ProductConfigView productConfig, final StagingRequest stagingRequest) {

        return Try.of(() -> test.stageDatasets(productConfig, stagingRequest))
                .onFailure(throwable -> log.warn("Failed to run staging job for request {}, skipping...",
                        stagingRequest.getName(), throwable))
                .getOrElse(StagingJobContext.builder()
                        .stagingDatasets(emptyList())
                        .stagedDatasets(emptyList())
                        .success(false)
                        .build());
    }

    private void generateCsvReport(
            final String reportName,
            final List<StagingJobData> stagingJobData) throws IOException {

        reportingSteps.generateReports(reportName, stagingJobData);
    }
}
