package com.lombardrisk.ignis.functional.test;

import com.google.common.collect.ImmutableMap;
import com.lombardrisk.ignis.client.core.page.request.PageRequest;
import com.lombardrisk.ignis.client.core.page.request.Sort;
import com.lombardrisk.ignis.client.external.job.pipeline.PipelineRequest;
import com.lombardrisk.ignis.client.external.job.staging.request.DataSource;
import com.lombardrisk.ignis.client.external.job.staging.request.DatasetMetadata;
import com.lombardrisk.ignis.client.external.job.staging.request.v1.StagingItemRequest;
import com.lombardrisk.ignis.client.external.job.staging.request.v1.StagingRequest;
import com.lombardrisk.ignis.client.external.job.staging.request.v2.StagingItemRequestV2;
import com.lombardrisk.ignis.client.external.job.staging.request.v2.StagingRequestV2;
import com.lombardrisk.ignis.client.external.pipeline.view.PipelineStepView;
import com.lombardrisk.ignis.client.external.pipeline.view.PipelineView;
import com.lombardrisk.ignis.client.external.productconfig.view.ProductConfigView;
import com.lombardrisk.ignis.feature.IgnisFeature;
import com.lombardrisk.ignis.functional.test.assertions.expected.ExpectedDataset;
import com.lombardrisk.ignis.functional.test.assertions.expected.ExpectedResultsDetails;
import com.lombardrisk.ignis.functional.test.assertions.expected.ExpectedRuleSummaries;
import com.lombardrisk.ignis.functional.test.config.FunctionalTest;
import com.lombardrisk.ignis.functional.test.dsl.FcrEngine;
import com.lombardrisk.ignis.functional.test.dsl.FcrEngineTest;
import com.lombardrisk.ignis.functional.test.dsl.PipelineJobContext;
import com.lombardrisk.ignis.functional.test.dsl.StagingJobContext;
import com.lombardrisk.ignis.functional.test.dsl.ValidationJobContext;
import com.lombardrisk.ignis.functional.test.feature.FeatureRule;
import com.lombardrisk.ignis.functional.test.feature.FeatureService;
import com.lombardrisk.ignis.functional.test.feature.RunWithFeature;
import com.lombardrisk.ignis.functional.test.junit.PassingTestsCleanupRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;

import static com.google.common.collect.Sets.newHashSet;
import static com.lombardrisk.ignis.client.core.page.request.Sort.Direction.ASC;
import static com.lombardrisk.ignis.client.external.productconfig.export.FieldExport.DecimalFieldExport;
import static com.lombardrisk.ignis.client.external.productconfig.export.FieldExport.IntegerFieldExport;
import static com.lombardrisk.ignis.client.external.productconfig.export.FieldExport.StringFieldExport;
import static com.lombardrisk.ignis.client.external.rule.ValidationRuleSummaryView.SummaryStatus.FAIL;
import static com.lombardrisk.ignis.functional.test.assertions.ValidationRuleSummaryViewsAssertions.summary;
import static com.lombardrisk.ignis.functional.test.assertions.expected.ExpectedResultDetailsData.expectedData;
import static com.lombardrisk.ignis.functional.test.constants.TestConstants.QUOTES_NAMES_DISPLAY;
import static com.lombardrisk.ignis.functional.test.constants.TestConstants.QUOTES_NAMES_PHYSICAL;
import static com.lombardrisk.ignis.functional.test.constants.TestConstants.STOCKS_NAMES_DISPLAY;
import static com.lombardrisk.ignis.functional.test.constants.TestConstants.STOCKS_NAMES_PHYSICAL;
import static com.lombardrisk.ignis.functional.test.constants.TestConstants.TRADERS_DISPLAY;
import static com.lombardrisk.ignis.functional.test.constants.TestConstants.TRADERS_PHYSICAL;
import static com.lombardrisk.ignis.functional.test.constants.TestConstants.TRADES;
import static com.lombardrisk.ignis.functional.test.constants.TestConstants.TRADES_BY_TRADERS_PHYSICAL;
import static com.lombardrisk.ignis.functional.test.constants.TestConstants.TRADES_JOINED_PHYSICAL;
import static com.lombardrisk.ignis.functional.test.constants.TestConstants.TRADES_PIPELINE_AGGREGATED_PHYSICAL;
import static com.lombardrisk.ignis.functional.test.constants.TestConstants.TRADES_PIPELINE_OUT_PHYSICAL;
import static com.lombardrisk.ignis.functional.test.constants.TestConstants.TRADES_PIPELINE_PRODUCT_CONFIG;
import static com.lombardrisk.ignis.functional.test.constants.TestConstants.TRADES_PIPELINE_WINDOWED_PHYSICAL;
import static com.lombardrisk.ignis.functional.test.constants.TestConstants.TRADES_PRODUCT_CONFIG;
import static com.lombardrisk.ignis.functional.test.constants.TestConstants.TRADES_RAW_EXTERNAL_DISPLAY;
import static com.lombardrisk.ignis.functional.test.constants.TestConstants.TRADES_RAW_EXTERNAL_PHYSICAL;
import static com.lombardrisk.ignis.functional.test.constants.TestConstants.TRADES_RAW_INTERNAL_DISPLAY;
import static com.lombardrisk.ignis.functional.test.constants.TestConstants.TRADES_RAW_INTERNAL_PHYSICAL;
import static com.lombardrisk.ignis.functional.test.constants.TestConstants.TRADES_SCHEMA;
import static com.lombardrisk.ignis.functional.test.constants.TestConstants.TRADES_UNION_RAW_PHYSICAL;
import static com.lombardrisk.ignis.functional.test.utils.RowBuilder.fields;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings({ "squid:S109", "squid:S1192", "squid:S00100", "findbugs:URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD" })
@RunWith(SpringRunner.class)
@FunctionalTest
public class PipelineFT {

    @Autowired
    private FcrEngine fcrEngine;

    @Autowired
    private FeatureService featureService;

    private FcrEngineTest test;

    @Before
    public void setUp() {
        test = fcrEngine.newTest();
    }

    @Rule
    public PassingTestsCleanupRule passingTestsCleanupRule = PassingTestsCleanupRule.withSteps(() -> test.cleanup());

    @Rule
    public FeatureRule featureRule = new FeatureRule(() -> featureService);

    @Test
    public void import_Stage_Validate() {
        ProductConfigView importedProduct = test.importProductConfig(TRADES_PRODUCT_CONFIG).waitForJobToSucceed();

        StagingJobContext stagingJob = test.stageDatasets(
                importedProduct,
                StagingRequest.builder()
                        .name("Trades Staging for Validation Details Feature")
                        .items(newHashSet(StagingItemRequest.builder()
                                .schema(TRADES_SCHEMA)
                                .source(DataSource.builder()
                                        .header(true)
                                        .filePath("FT_validation_details_trades.csv")
                                        .build())
                                .dataset(DatasetMetadata.builder()
                                        .entityCode("ENTITY-21")
                                        .referenceDate("01/01/2016")
                                        .build())
                                .build()))
                        .build());

        test.assertStagingDatasetsExist(importedProduct, stagingJob,
                ExpectedDataset.expected()
                        .schema(TRADES)
                        .entityCode("ENTITY-21")
                        .referenceDate(LocalDate.of(2016, 1, 1))
                        .numberOfRows(4)
                        .build());

        ValidationJobContext validationJob = test.validateDataset(stagingJob, TRADES);

        test.assertValidationRuleSummaries(
                validationJob,
                ExpectedRuleSummaries.expected()
                        .totalNumberOfRecords(4)
                        .ruleSummaries(singletonList(
                                summary().ruleId("GONDOR_TRADE_POLICY")
                                        .numberOfFailures(1L)
                                        .numberOfErrors(0L)
                                        .status(FAIL)))
                        .build());

        test.assertValidationResultsDetails(
                validationJob,
                PageRequest.builder()
                        .size(4).page(0)
                        .sort(Sort.builder().field("Trade_Date").direction(ASC).build())
                        .build(),
                ExpectedResultsDetails.expected()
                        .schema(ImmutableMap.<String, Class<?>>builder()
                                .put("Trade_Date", IntegerFieldExport.class)
                                .put("Settlement_Date", IntegerFieldExport.class)
                                .put("Stock", StringFieldExport.class)
                                .put("Trade_Type", StringFieldExport.class)
                                .put("Rate", DecimalFieldExport.class)
                                .put("Amount", DecimalFieldExport.class)
                                .put("Calculated_Value", DecimalFieldExport.class)
                                .put("RESULT_TYPE", StringFieldExport.class)
                                .build())
                        .data(expectedData()
                                .headers("Trade_Date", "Settlement_Date", "Stock", "Trade_Type", "Rate",
                                        "Amount", "Calculated_Value", "RESULT_TYPE")
                                .row(1, 1492121606, "MORDOR INC", "SELL", 97.2, 94.54, 9189.26, "FAIL")
                                .row(2, 1497892373, "Resolute Energy", "ERRM?", 6.28, 30.94, 194.16, "SUCCESS")
                                .row(3, 1504786478, "Zoetis Inc.", "BUY", 67.91, 99.21, 6737.58, "SUCCESS")
                                .row(4, 1515092271, "REGENXBIO Inc.", "BUY", 87.06, 15.55, 1353.82, "SUCCESS")
                                .build())
                        .build());
    }

    @Test
    @RunWithFeature(feature = IgnisFeature.APPEND_DATASETS, active = true)
    public void import_Stage_Append() {
        ProductConfigView importedProduct = test.importProductConfig(TRADES_PRODUCT_CONFIG).waitForJobToSucceed();

        StagingJobContext stagingJob1 = test.stageDatasets(
                importedProduct,
                StagingRequestV2.builder()
                        .name("Append Trades Part 1")
                        .metadata(DatasetMetadata.builder()
                                .entityCode("ENTITY-21")
                                .referenceDate("01/01/2016")
                                .build())
                        .items(newHashSet(StagingItemRequestV2.builder()
                                .schema(TRADES_SCHEMA)
                                .source(DataSource.builder()
                                        .header(true)
                                        .filePath("TRADES_JAN_1.csv")
                                        .build())
                                .build()))
                        .build());

        StagingJobContext stagingJob2 = test.stageDatasets(
                importedProduct,
                StagingRequestV2.builder()
                        .name("Append Trades Part 2")
                        .metadata(DatasetMetadata.builder()
                                .entityCode("ENTITY-21")
                                .referenceDate("01/01/2016")
                                .build())
                        .items(newHashSet(StagingItemRequestV2.builder()
                                .schema(TRADES_SCHEMA)
                                .source(DataSource.builder()
                                        .header(true)
                                        .filePath("TRADES_FEB.csv")
                                        .build())
                                .appendToDatasetId(stagingJob1.getStagedDatasets().get(0).getId())
                                .build()))
                        .build());

        test.assertStagingDatasetsExist(importedProduct, stagingJob2,
                ExpectedDataset.expected()
                        .schema(TRADES)
                        .entityCode("ENTITY-21")
                        .referenceDate(LocalDate.of(2016, 1, 1))
                        .numberOfRows(8)
                        .build());
    }

    //When pipeline feature is removed, validation should be merged with this test
    @Test
    public void import_Stage_Pipeline() {
        ProductConfigView importedProduct = test.importProductConfig(TRADES_PIPELINE_PRODUCT_CONFIG)
                .waitForJobToSucceed();

        assertThat(importedProduct.getPipelines())
                .extracting(PipelineView::getName)
                .describedAs("Imported product should contain single pipeline")
                .hasSize(1);

        PipelineView pipeline = importedProduct.getPipelines().get(0);

        DatasetMetadata datasetMetadata = DatasetMetadata.builder()
                .entityCode("ENTWISTLE")
                .referenceDate("01/02/2018")
                .build();

        StagingJobContext stagingJob = stageDatasetsForPipeline(importedProduct, datasetMetadata);
        assertFiveDatasetsCreated(importedProduct, stagingJob);

        PipelineJobContext pipelineJob = runPipelineJob(pipeline);

        assertPipelineDatasetsExist(importedProduct, pipelineJob);

        PipelineStepView joinStep = findStep(pipeline, "Join step");
        PipelineStepView mapStep = findStep(pipeline, "Map step");
        PipelineStepView aggregationStep = findStep(pipeline, "Aggregation step");
        PipelineStepView windowStep = findStep(pipeline, "Window step");

        assertJoinDrillBack(pipelineJob, pipeline.getId(), joinStep);
        assertMapDrillBack(pipelineJob, pipeline.getId(), mapStep);
        assertAggregationDrillBack(pipelineJob, pipeline.getId(), aggregationStep);
        assertWindowDrillBack(pipelineJob, pipeline.getId(), windowStep);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private PipelineStepView findStep(final PipelineView pipeline, final String stepName) {
        return pipeline.getSteps().stream()
                .filter(step -> step.getName().equals(stepName))
                .findFirst()
                .get();
    }

    private StagingJobContext stageDatasetsForPipeline(
            final ProductConfigView importedProduct,
            final DatasetMetadata datasetMetadata) {

        return test.stageDatasets(
                importedProduct,
                StagingRequestV2.builder()
                        .name("Transformation Pipeline Feature - Staging")
                        .metadata(datasetMetadata)
                        .items(newHashSet(
                                StagingItemRequestV2.builder()
                                        .schema(STOCKS_NAMES_DISPLAY)
                                        .autoValidate(false)
                                        .source(DataSource.builder()
                                                .header(true)
                                                .filePath("stocks.csv")
                                                .build())
                                        .build(),
                                StagingItemRequestV2.builder()
                                        .schema(QUOTES_NAMES_DISPLAY)
                                        .autoValidate(false)
                                        .source(DataSource.builder()
                                                .header(true)
                                                .filePath("quotes.csv")
                                                .build())
                                        .build(),
                                StagingItemRequestV2.builder()
                                        .schema(TRADES_RAW_INTERNAL_DISPLAY)
                                        .autoValidate(false)
                                        .source(DataSource.builder()
                                                .header(true)
                                                .filePath("trades_raw.csv")
                                                .build())
                                        .build(),
                                StagingItemRequestV2.builder()
                                        .schema(TRADES_RAW_EXTERNAL_DISPLAY)
                                        .autoValidate(false)
                                        .source(DataSource.builder()
                                                .header(true)
                                                .filePath("trades_raw_external.csv")
                                                .build())
                                        .build(),
                                StagingItemRequestV2.builder()
                                        .schema(TRADERS_DISPLAY)
                                        .autoValidate(false)
                                        .source(DataSource.builder()
                                                .header(true)
                                                .filePath("traders.csv")
                                                .build())
                                        .build()))
                        .build());
    }

    private void assertFiveDatasetsCreated(
            final ProductConfigView importedProduct,
            final StagingJobContext stagingJob) {
        test.assertStagingDatasetsExist(importedProduct, stagingJob,
                ExpectedDataset.expected()
                        .schema(QUOTES_NAMES_PHYSICAL)
                        .entityCode("ENTWISTLE")
                        .referenceDate(LocalDate.of(2018, 2, 1))
                        .numberOfRows(9)
                        .build(),
                ExpectedDataset.expected()
                        .schema(STOCKS_NAMES_PHYSICAL)
                        .entityCode("ENTWISTLE")
                        .referenceDate(LocalDate.of(2018, 2, 1))
                        .numberOfRows(6)
                        .build(),
                ExpectedDataset.expected()
                        .schema(TRADES_RAW_INTERNAL_PHYSICAL)
                        .entityCode("ENTWISTLE")
                        .referenceDate(LocalDate.of(2018, 2, 1))
                        .numberOfRows(5)
                        .build(),
                ExpectedDataset.expected()
                        .schema(TRADES_RAW_EXTERNAL_PHYSICAL)
                        .entityCode("ENTWISTLE")
                        .referenceDate(LocalDate.of(2018, 2, 1))
                        .numberOfRows(4)
                        .build(),
                ExpectedDataset.expected()
                        .schema(TRADERS_PHYSICAL)
                        .entityCode("ENTWISTLE")
                        .referenceDate(LocalDate.of(2018, 2, 1))
                        .numberOfRows(9)
                        .build());
    }

    private PipelineJobContext runPipelineJob(final PipelineView pipeline) {
        return test.runPipelineJob(PipelineRequest.builder()
                .name("Transformation Pipeline Feature - Pipeline")
                .entityCode("ENTWISTLE")
                .referenceDate(LocalDate.of(2018, 2, 1))
                .pipelineId(pipeline.getId())
                .build());
    }

    private void assertPipelineDatasetsExist(
            final ProductConfigView importedProduct,
            final PipelineJobContext pipelineJob) {

        test.assertPipelineDatasetsExist(importedProduct, pipelineJob,
                ExpectedDataset.expected()
                        .schema(TRADES_UNION_RAW_PHYSICAL)
                        .entityCode("ENTWISTLE")
                        .referenceDate(LocalDate.of(2018, 2, 1))
                        .numberOfRows(9)
                        .build(),
                ExpectedDataset.expected()
                        .schema(TRADES_JOINED_PHYSICAL)
                        .entityCode("ENTWISTLE")
                        .referenceDate(LocalDate.of(2018, 2, 1))
                        .numberOfRows(9)
                        .build(),
                ExpectedDataset.expected()
                        .schema(TRADES_PIPELINE_OUT_PHYSICAL)
                        .entityCode("ENTWISTLE")
                        .referenceDate(LocalDate.of(2018, 2, 1))
                        .numberOfRows(9)
                        .build(),
                ExpectedDataset.expected()
                        .schema(TRADES_PIPELINE_AGGREGATED_PHYSICAL)
                        .entityCode("ENTWISTLE")
                        .referenceDate(LocalDate.of(2018, 2, 1))
                        .numberOfRows(2)
                        .build(),
                ExpectedDataset.expected()
                        .schema(TRADES_PIPELINE_WINDOWED_PHYSICAL)
                        .entityCode("ENTWISTLE")
                        .referenceDate(LocalDate.of(2018, 2, 1))
                        .numberOfRows(9)
                        .build(),
                ExpectedDataset.expected()
                        .schema(TRADES_BY_TRADERS_PHYSICAL)
                        .entityCode("ENTWISTLE")
                        .referenceDate(LocalDate.of(2018, 2, 1))
                        .numberOfRows(7)
                        .build());
    }

    private void assertJoinDrillBack(
            final PipelineJobContext pipelineJob,
            final Long pipelineId,
            final PipelineStepView step) {
        test.assertDrillBackRowData(pipelineJob, TRADES_JOINED_PHYSICAL, pipelineId, step.getId(),
                fields("TRADE_DATE", "SETTLEMENT_DATE", "STOCK", "TradeType", "RATE", "AMOUNT")
                        .row(1, 1492121606, "MORDOR INC.", "SELL", 501.23, 4.10)
                        .row(2, 1497892373, "GONDOR CO.", "BUY", 19.80, 1.10)
                        .row(3, 1504786478, "REGENXBIO", "BUY", 102.88, 10.00)
                        .row(4, 1515092271, "IsenGuard Security", "BUY", 85.05, 10.0)
                        .row(5, 1515092271, "GONDOR CO.", "SELL", 23.34, 10.0)
                        .row(6, 1515092271, "GONDOR CO.", "BUY", 656.23, 10.0)
                        .row(7, 1515092271, "MORDOR INC.", "SELL", 123.45, 11.1)
                        .row(8, 1515092271, "MORDOR INC.", "BUY", 656.23, 8.4)
                        .row(9, 1515092271, "MORDOR INC.", "BUY", 123.45, 3.2)
                        .toRows());
    }

    private void assertMapDrillBack(
            final PipelineJobContext pipelineJob,
            final Long pipelineId,
            final PipelineStepView step) {
        test.assertDrillBackRowData(pipelineJob, TRADES_PIPELINE_OUT_PHYSICAL, pipelineId, step.getId(),
                fields(
                        "TRADE_DATE",
                        "SETTLEMENT_DATE",
                        "DAYS_TO_SETTLE_BEFORE_REF_DATE",
                        "STOCK",
                        "TradeType",
                        "RATE",
                        "AMOUNT",
                        "CALCULATED_VALUE")
                        .row(1, "1970-01-18", 12534, "MORDOR INC.", "SELL", 501.23, 4.10, 2055.04)
                        .row(2, "1970-01-18", 12534, "GONDOR CO.", "BUY", 19.80, 1.10, 21.78)
                        .row(3, "1970-01-18", 12534, "REGENXBIO", "BUY", 102.88, 10.00, 1028.8)
                        .row(4, "1970-01-18", 12534, "IsenGuard Security", "BUY", 85.05, 10.0, 850.5)
                        .row(5, "1970-01-18", 12534, "GONDOR CO.", "SELL", 23.34, 10.0, 233.4)
                        .row(6, "1970-01-18", 12534, "GONDOR CO.", "BUY", 656.23, 10.0, 6562.3)
                        .row(7, "1970-01-18", 12534, "MORDOR INC.", "SELL", 123.45, 11.1, 1370.3)
                        .row(8, "1970-01-18", 12534, "MORDOR INC.", "BUY", 656.23, 8.4, 5512.33)
                        .row(9, "1970-01-18", 12534, "MORDOR INC.", "BUY", 123.45, 3.2, 395.04)
                        .toRows());
    }

    private void assertAggregationDrillBack(
            final PipelineJobContext pipelineJob,
            final Long pipelineId,
            final PipelineStepView step) {
        test.assertDrillBackRowData(pipelineJob, TRADES_PIPELINE_AGGREGATED_PHYSICAL, pipelineId, step.getId(),
                fields("TradeType", "TOTAL")
                        .row("BUY", 6)
                        .row("SELL", 3)
                        .toRows());
    }

    private void assertWindowDrillBack(
            final PipelineJobContext pipelineJob,
            final Long pipelineId,
            final PipelineStepView step) {
        test.assertDrillBackRowData(pipelineJob, TRADES_PIPELINE_WINDOWED_PHYSICAL, pipelineId, step.getId(),
                fields("STOCK", "TradeType", "RATE", "AMOUNT", "RANK")
                        .row("MORDOR INC.", "BUY", 656.23, 8.4, 1)
                        .row("MORDOR INC.", "BUY", 123.45, 3.2, 2)
                        .row("MORDOR INC.", "SELL", 123.45, 11.1, 1)
                        .row("MORDOR INC.", "SELL", 501.23, 4.10, 2)
                        .row("GONDOR CO.", "SELL", 23.34, 10.0, 1)
                        .row("GONDOR CO.", "BUY", 656.23, 10.0, 1)
                        .row("GONDOR CO.", "BUY", 19.80, 1.10, 2)
                        .row("REGENXBIO", "BUY", 102.88, 10.00, 1)
                        .row("IsenGuard Security", "BUY", 85.05, 10.0, 1)
                        .toRows());
    }
}
