package com.lombardrisk.ignis.functional.test;

import com.lombardrisk.ignis.client.core.view.IdView;
import com.lombardrisk.ignis.client.design.pipeline.PipelineStepView;
import com.lombardrisk.ignis.client.design.pipeline.PipelineView;
import com.lombardrisk.ignis.client.design.pipeline.map.PipelineMapStepRequest;
import com.lombardrisk.ignis.client.design.pipeline.select.SelectRequest;
import com.lombardrisk.ignis.client.design.pipeline.test.StepTestStatus;
import com.lombardrisk.ignis.client.design.pipeline.test.view.StepRowOutputDataView;
import com.lombardrisk.ignis.client.design.pipeline.test.view.StepTestView;
import com.lombardrisk.ignis.client.design.productconfig.NewProductConfigRequest;
import com.lombardrisk.ignis.client.design.productconfig.ProductConfigDto;
import com.lombardrisk.ignis.client.design.schema.SchemaDto;
import com.lombardrisk.ignis.client.design.schema.field.FieldDto;
import com.lombardrisk.ignis.functional.test.assertions.PipelineStepTestAssertions;
import com.lombardrisk.ignis.functional.test.config.FunctionalTest;
import com.lombardrisk.ignis.functional.test.junit.PassingTestsCleanupRule;
import com.lombardrisk.ignis.functional.test.steps.DesignStudioSteps;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Sets.newHashSet;
import static com.lombardrisk.ignis.functional.test.assertions.PipelineStepTestAssertions.TestColumn.column;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the FCR specific import functionality, are jobs rolled back, are physical tables dropped/created etc
 */
@SuppressWarnings({
        "squid:S109", "squid:S00100", "findbugs:URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD", "squid:S00112"
})
@RunWith(SpringRunner.class)
@FunctionalTest
public class PipelineUnitTestFT {

    private static final String CHARACTER_DETAILS_SCHEMA_PATH =
            "design/pipeline-unit-tests/character-details.schema.json";
    private static final String CHARACTER_SUMMARY_SCHEMA_PATH =
            "design/pipeline-unit-tests/character-summary.schema.json";
    private static final String DECIMAL_DETAILS_SCHEMA_PATH =
            "design/pipeline-unit-tests/decimal-details.schema.json";
    private static final String DECIMAL_SUMMARY_SCHEMA_PATH =
            "design/pipeline-unit-tests/decimal-summary.schema.json";

    private static final String CHARACTER_DETAILS_INPUT_DATA_PATH =
            "design/pipeline-unit-tests/data/character-details-input-data.csv";
    private static final String CHARACTER_DETAILS_EXPECTED_FAIL_DATA_PATH =
            "design/pipeline-unit-tests/data/character-details-expected-fail-data.csv";
    private static final String CHARACTER_DETAILS_EXPECTED_SUCCESS_DATA_PATH =
            "design/pipeline-unit-tests/data/character-details-expected-success-data.csv";
    private static final String DECIMAL_DETAILS_INPUT_DATA_PATH =
            "design/pipeline-unit-tests/data/decimal-details-input-data.csv";
    private static final String DECIMAL_DETAILS_EXPECTED_FAIL_DATA_PATH =
            "design/pipeline-unit-tests/data/decimal-details-expected-fail-data.csv";
    private static final String DECIMAL_DETAILS_EXPECTED_SUCCESS_DATA_PATH =
            "design/pipeline-unit-tests/data/decimal-details-expected-success-data.csv";


    private ProductConfigDto designProduct;

    @Autowired
    private DesignStudioSteps designStudioSteps;

    @Before
    public void setUp() {
    }

    @Rule
    public PassingTestsCleanupRule passingTestsCleanupRule = PassingTestsCleanupRule.withSteps(() ->
            designStudioSteps.deleteProductConfig(designProduct.getId()));

    @Test
    public void createPipelineStep_RunTests_FailsAndPasses() throws IOException {
        String productConfigName = "SIMPSONS_TEST_" + randomAlphanumeric(30);
        NewProductConfigRequest productConfigRequest = NewProductConfigRequest.builder()
                .name(productConfigName)
                .version("1")
                .build();

        designProduct = designStudioSteps.createProductConfig(productConfigRequest);

        SchemaDto characterDetailsTable = designStudioSteps.createTable(
                designProduct.getId(),
                CHARACTER_DETAILS_SCHEMA_PATH);

        SchemaDto characterSummaryTable = designStudioSteps.createTable(
                designProduct.getId(),
                CHARACTER_SUMMARY_SCHEMA_PATH);
        FieldDto outputField = characterSummaryTable.getFields().iterator().next();

        PipelineView characterPipeline = designStudioSteps.createPipeline(designProduct.getId(), "Character Pipeline");

        PipelineStepView pipelineStep =
                designStudioSteps.createPipelineMapStep(
                        characterPipeline.getId(),
                        PipelineMapStepRequest.builder()
                                .name("Summary calculator")
                                .schemaInId(characterDetailsTable.getId())
                                .schemaOutId(characterSummaryTable.getId())
                                .selects(newHashSet(
                                        SelectRequest.builder()
                                                .select("concat(FIRST_NAME, ' ', LAST_NAME)")
                                                .outputFieldId(characterSummaryTable.getFields().get(0).getId())
                                                .order(0L)
                                                .build()))
                                .filters(emptyList())
                                .build());

        IdView pipelineStepTestId =
                designStudioSteps.createPipelineStepTest(pipelineStep.getId(), "Test Homer's Friends");

        StepTestView pipelineStepTest =
                designStudioSteps.updatePipelineStepTest(pipelineStepTestId.getId(),
                        "Test Homer's Updated Friends", "Test Homer's Updated Friends Description");

        List<Map<Long, String>> inputRow =
                designStudioSteps.readCsvData(characterDetailsTable, CHARACTER_DETAILS_INPUT_DATA_PATH);

        designStudioSteps.createInputDataRowAndUpdateCells(characterDetailsTable.getId(),
                pipelineStepTest.getId(), inputRow);

        List<Map<Long, String>> expectedFailData =
                designStudioSteps.readCsvData(characterSummaryTable, CHARACTER_DETAILS_EXPECTED_FAIL_DATA_PATH);

        List<Long> expectedDataRowIds =
                designStudioSteps.createExpectedDataRowAndUpdateCells(characterSummaryTable.getId(),
                        pipelineStepTest.getId(), expectedFailData);

        StepTestStatus stepTestStatus = designStudioSteps.runTest(pipelineStepTest.getId());

        assertThat(stepTestStatus).isEqualTo(StepTestStatus.FAIL);

        List<StepRowOutputDataView> pipelineStepTestOutputRows =
                designStudioSteps.getPipelineStepTestOutputRows(pipelineStepTest.getId());

        PipelineStepTestAssertions.assertThat(pipelineStepTestOutputRows)
                .hasNotFoundRow(singletonList(column(outputField.getId(), "HOMER JAY SIMPSON")))
                .hasUnexpectedRow(singletonList(column(outputField.getId(), "HOMER SIMPSON")))
                .hasNoMatches();

        for (Long expectedDataRowId : expectedDataRowIds) {
            designStudioSteps.deletePipelineTestExpectedDataRow(pipelineStepTest.getId(), expectedDataRowId);
        }

        List<Map<Long, String>> expectedSuccessData =
                designStudioSteps.readCsvData(characterSummaryTable, CHARACTER_DETAILS_EXPECTED_SUCCESS_DATA_PATH);

        designStudioSteps.createExpectedDataRowAndUpdateCells(characterSummaryTable.getId(),
                        pipelineStepTest.getId(), expectedSuccessData);

        stepTestStatus = designStudioSteps.runTest(pipelineStepTest.getId());

        assertThat(stepTestStatus).isEqualTo(StepTestStatus.PASS);

        List<StepRowOutputDataView> updatedPipelineTestOutputRows =
                designStudioSteps.getPipelineStepTestOutputRows(pipelineStepTest.getId());

        PipelineStepTestAssertions.assertThat(updatedPipelineTestOutputRows)
                .hasNoNotFound()
                .hasNoUnexpected()
                .hasMatchedRow(singletonList(column(outputField.getId(), "HOMER SIMPSON")));

        designStudioSteps.deletePipelineStepTest(pipelineStepTest.getId());
    }

    @Test
    public void createPipelineStep_RunTestsDecimal_FailsAndPasses() throws IOException {
        String productConfigName = "DECIMAL_TEST_" + randomAlphanumeric(30);
        NewProductConfigRequest productConfigRequest = NewProductConfigRequest.builder()
                .name(productConfigName)
                .version("1")
                .build();

        designProduct = designStudioSteps.createProductConfig(productConfigRequest);

        SchemaDto decimalDetailsTable = designStudioSteps.createTable(
                designProduct.getId(),
                DECIMAL_DETAILS_SCHEMA_PATH);

        SchemaDto decimalSummaryTable = designStudioSteps.createTable(
                designProduct.getId(),
                DECIMAL_SUMMARY_SCHEMA_PATH);
        FieldDto outputField = decimalSummaryTable.getFields().iterator().next();

        PipelineView decimalPipeline = designStudioSteps.createPipeline(designProduct.getId(), "Decimal Pipeline");

        PipelineStepView pipelineStep =
                designStudioSteps.createPipelineMapStep(
                        decimalPipeline.getId(),
                        PipelineMapStepRequest.builder()
                                .name("Decimal calculator")
                                .schemaInId(decimalDetailsTable.getId())
                                .schemaOutId(decimalSummaryTable.getId())
                                .selects(newHashSet(
                                        SelectRequest.builder()
                                                .select("floor(DECIMAL_VALUE)")
                                                .outputFieldId(decimalSummaryTable.getFields().get(0).getId())
                                                .order(0L)
                                                .build()))
                                .filters(emptyList())
                                .build());

        IdView pipelineStepTestId =
                designStudioSteps.createPipelineStepTest(pipelineStep.getId(), "Test Decimal Step");

        List<Map<Long, String>> inputRow =
                designStudioSteps.readCsvData(decimalDetailsTable, DECIMAL_DETAILS_INPUT_DATA_PATH);

        designStudioSteps.createInputDataRowAndUpdateCells(decimalDetailsTable.getId(),
                pipelineStepTestId.getId(), inputRow);

        List<Map<Long, String>> expectedFailData =
                designStudioSteps.readCsvData(decimalSummaryTable, DECIMAL_DETAILS_EXPECTED_FAIL_DATA_PATH);

        List<Long> expectedDataRowIds =
                designStudioSteps.createExpectedDataRowAndUpdateCells(decimalSummaryTable.getId(),
                        pipelineStepTestId.getId(), expectedFailData);

        StepTestStatus stepTestStatus = designStudioSteps.runTest(pipelineStepTestId.getId());

        assertThat(stepTestStatus).isEqualTo(StepTestStatus.FAIL);

        List<StepRowOutputDataView> pipelineStepTestOutputRows =
                designStudioSteps.getPipelineStepTestOutputRows(pipelineStepTestId.getId());

        PipelineStepTestAssertions.assertThat(pipelineStepTestOutputRows)
                .hasNotFoundRow(singletonList(column(outputField.getId(), "-8.00")))
                .hasUnexpectedRow(singletonList(column(outputField.getId(), "-13")))
                .hasNoMatches();

        for (Long expectedDataRowId : expectedDataRowIds) {
            designStudioSteps.deletePipelineTestExpectedDataRow(pipelineStepTestId.getId(), expectedDataRowId);
        }

        List<Map<Long, String>> expectedSuccessData =
                designStudioSteps.readCsvData(decimalSummaryTable, DECIMAL_DETAILS_EXPECTED_SUCCESS_DATA_PATH);

        designStudioSteps.createExpectedDataRowAndUpdateCells(decimalSummaryTable.getId(),
                pipelineStepTestId.getId(), expectedSuccessData);

        stepTestStatus = designStudioSteps.runTest(pipelineStepTestId.getId());

        assertThat(stepTestStatus).isEqualTo(StepTestStatus.PASS);

        List<StepRowOutputDataView> updatedPipelineTestOutputRows =
                designStudioSteps.getPipelineStepTestOutputRows(pipelineStepTestId.getId());

        PipelineStepTestAssertions.assertThat(updatedPipelineTestOutputRows)
                .hasNoNotFound()
                .hasNoUnexpected()
                .hasMatchedRow(singletonList(column(outputField.getId(), "-13")));
    }
}
