package com.lombardrisk.ignis.spark.validation.job;

import com.google.common.collect.ImmutableList;
import com.lombardrisk.ignis.api.rule.SummaryStatus;
import com.lombardrisk.ignis.common.lang.ErrorMessage;
import com.lombardrisk.ignis.spark.api.DatasetTableLookup;
import com.lombardrisk.ignis.spark.api.validation.DatasetValidationJobRequest;
import com.lombardrisk.ignis.spark.api.validation.DatasetValidationRule;
import com.lombardrisk.ignis.spark.core.phoenix.PhoenixTableService;
import com.lombardrisk.ignis.spark.core.repository.RowKeyedDatasetRepository;
import com.lombardrisk.ignis.spark.core.schema.DatasetTableSchema;
import com.lombardrisk.ignis.spark.validation.DatasetRuleSummary;
import com.lombardrisk.ignis.spark.validation.ValidationRuleSummaryService;
import com.lombardrisk.ignis.spark.validation.ValidationService;
import com.lombardrisk.ignis.spark.validation.exception.ValidationJobOperatorException;
import com.lombardrisk.ignis.spark.validation.fixture.Populated;
import io.vavr.control.Either;
import io.vavr.control.Option;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.ResourceLoader;

import java.io.File;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ValidationJobOperatorTest {

    private static final String JOB_FILE_STRING = "JOB_FILE";

    private static final File VALID_JOB_REQUEST =
            new File("src/test/resources/validationJobRequest_valid.json");

    private static final File INVALID_JOB_REQUEST =
            new File("src/test/resources/validationJobRequest_invalid.json");

    @Mock
    private ResourceLoader resourceLoader;

    @Mock
    private ValidationService validationService;

    @Mock
    private RowKeyedDatasetRepository dataFrameIo;

    @Mock
    private ValidationRuleSummaryService validationRuleSummaryService;

    @Mock
    private PhoenixTableService phoenixTableService;

    @Mock
    private Dataset<Row> dataset;

    @Captor
    private ArgumentCaptor<DatasetValidationRule> ruleCaptor;

    @Captor
    private ArgumentCaptor<List<DatasetRuleSummary>> summariesCaptor;

    private ValidationJobOperator validationJobOperator;

    @Before
    public void setUp() {
        validationJobOperator = new ValidationJobOperator(
                validationService,
                dataFrameIo,
                validationRuleSummaryService,
                phoenixTableService, Populated.datasetValidationJobRequest().build());

        when(validationService.executeRule(any(), anyLong(), any()))
                .thenReturn(Either.right(Populated.validationRuleStatistics().build()));
        when(validationRuleSummaryService.createValidationResultsSummary(anyLong(), any()))
                .thenReturn(Option.none());
        when(dataFrameIo.readDataFrame(any(DatasetTableLookup.class)))
                .thenReturn(dataset);
        when(phoenixTableService.createTableIfNotFound(any()))
                .thenReturn(Option.none());
    }

    @Test
    public void runJob_CallsPhoenixTableServiceToCreateRuleResultsTable() {
        validationJobOperator.runJob();

        verify(phoenixTableService).createTableIfNotFound(DatasetTableSchema.validationResultsTableSchema());
    }

    @Test
    public void runJob_FailedToCreateValidationRuleResultsTable_ThrowsException() {
        when(phoenixTableService.createTableIfNotFound(any()))
                .thenReturn(Option.of(ErrorMessage.of("Could not create validation rule results table")));

        assertThatThrownBy(() -> validationJobOperator.runJob())
                .isInstanceOf(ValidationJobOperatorException.class)
                .hasMessageContaining("Could not create validation rule results table");
    }

    @Test
    public void runJob_validValidationJobRequest_CallsValidationServiceWithRuleRequest() {
        validationJobOperator = new ValidationJobOperator(
                validationService, dataFrameIo, validationRuleSummaryService,
                phoenixTableService, DatasetValidationJobRequest.builder()
                        .datasetValidationRule(
                                Populated.datasetValidationRule()
                                        .id(1)
                                        .name("ValidationRule1")
                                        .expression("Expressionless")
                                        .build())
                        .datasetValidationRule(
                                Populated.datasetValidationRule()
                                        .id(2)
                                        .name("ValidationRule2")
                                        .expression("Expression")
                                        .build())
                        .build());

        validationJobOperator.runJob();

        verify(validationService, times(2))
                .executeRule(ruleCaptor.capture(), anyLong(), any());

        assertThat(ruleCaptor.getAllValues())
                .containsExactly(
                        DatasetValidationRule.builder()
                                .id(1)
                                .name("ValidationRule1")
                                .expression("Expressionless")
                                .build(),
                        DatasetValidationRule.builder()
                                .id(2)
                                .name("ValidationRule2")
                                .expression("Expression")
                                .build());
    }

    @Test
    public void runJob_validDatabaseLookup_CallsDataFrameIoWithLookup() {
        validationJobOperator = new ValidationJobOperator(
                validationService, dataFrameIo, validationRuleSummaryService,
                phoenixTableService, DatasetValidationJobRequest.builder()
                        .datasetTableLookup(
                                DatasetTableLookup.builder()
                                        .datasetName("datasetName")
                                        .predicate("ROW_KEY > 1000 AND ROW_KEY < 2000")
                                        .build())
                        .build());

        validationJobOperator.runJob();

        verify(dataFrameIo).readDataFrame(
                DatasetTableLookup.builder()
                        .datasetName("datasetName")
                        .predicate("ROW_KEY > 1000 AND ROW_KEY < 2000")
                        .build());
    }

    @Test
    public void runJob_DatasetFound_ValidationServiceWithDataset() {
        validationJobOperator = new ValidationJobOperator(
                validationService, dataFrameIo, validationRuleSummaryService,
                phoenixTableService, DatasetValidationJobRequest.builder()
                        .datasetTableLookup(Populated.datasetTableLookup().build())
                        .datasetValidationRules(ImmutableList.of(
                                Populated.datasetValidationRule().build(),
                                Populated.datasetValidationRule().build()))
                        .build());

        when(dataFrameIo.readDataFrame(any(DatasetTableLookup.class)))
                .thenReturn(dataset);

        validationJobOperator.runJob();

        verify(validationService, times(2)).executeRule(any(), anyLong(), eq(dataset));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void runJob_rulesExecutedSuccessfully_callsSummaryServiceWithDatasetRuleSummaries() {
        validationJobOperator = new ValidationJobOperator(
                validationService, dataFrameIo, validationRuleSummaryService,
                phoenixTableService, DatasetValidationJobRequest.builder()
                        .datasetValidationRule(Populated.datasetValidationRule().id(1).build())
                        .datasetValidationRule(Populated.datasetValidationRule().id(2).build())
                        .build());

        when(validationService.executeRule(any(), anyLong(), any()))
                .thenReturn(
                        Either.right(Populated.validationRuleStatistics()
                                .numberOfErrors(100L)
                                .numberOfFailures(10)
                                .build()),
                        Either.right(Populated.validationRuleStatistics()
                                .numberOfFailures(102L)
                                .numberOfErrors(0)
                                .build()));

        validationJobOperator.runJob();

        verify(validationRuleSummaryService).createValidationResultsSummary(anyLong(), summariesCaptor.capture());

        assertThat(summariesCaptor.getValue())
                .containsExactly(
                        DatasetRuleSummary.builder()
                                .ruleId(1)
                                .status(SummaryStatus.ERROR)
                                .validationRuleStatistics(Populated.validationRuleStatistics()
                                        .numberOfErrors(100L)
                                        .numberOfFailures(10)
                                        .build())
                                .build(),
                        DatasetRuleSummary.builder()
                                .ruleId(2)
                                .status(SummaryStatus.FAIL)
                                .validationRuleStatistics(Populated.validationRuleStatistics()
                                        .numberOfFailures(102L)
                                        .numberOfErrors(0)
                                        .build())
                                .build());
    }

    @Test
    public void runJob_SendingOfRulesFail_ThrowsException() {
        when(validationRuleSummaryService.createValidationResultsSummary(anyLong(), any()))
                .thenReturn(Option.of(ErrorMessage.of("WHOOPS")));

        assertThatThrownBy(() -> validationJobOperator.runJob())
                .isInstanceOf(ValidationJobOperatorException.class)
                .hasMessage("WHOOPS");
    }
}