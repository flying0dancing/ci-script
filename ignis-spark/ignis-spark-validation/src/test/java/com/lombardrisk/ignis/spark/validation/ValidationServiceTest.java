package com.lombardrisk.ignis.spark.validation;

import com.lombardrisk.ignis.common.lang.ErrorMessage;
import com.lombardrisk.ignis.spark.api.fixture.Populated;
import com.lombardrisk.ignis.spark.api.validation.DatasetValidationRule;
import com.lombardrisk.ignis.spark.core.repository.DatasetRepository;
import com.lombardrisk.ignis.spark.core.schema.DatasetTableSchema;
import com.lombardrisk.ignis.spark.validation.function.UpdateRowValidationFunction;
import com.lombardrisk.ignis.spark.validation.function.ValidationResult;
import com.lombardrisk.ignis.spark.validation.transform.JexlFunction;
import io.vavr.control.Either;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static com.lombardrisk.ignis.api.rule.ValidationOutput.DATASET_ID;
import static com.lombardrisk.ignis.api.rule.ValidationOutput.DATASET_ROW_KEY;
import static com.lombardrisk.ignis.api.rule.ValidationOutput.VALIDATION_RULE_ID;
import static com.lombardrisk.ignis.pipeline.step.api.DatasetFieldName.ROW_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ValidationServiceTest {

    @InjectMocks
    private ValidationService validationService;

    @Mock
    private Dataset<Row> dataset;
    @Mock
    private Dataset<Row> mappedDataset;
    @Mock
    private Dataset<Row> filteredDataset;

    @Mock
    private DatasetRepository datasetRepository;

    @Captor
    private ArgumentCaptor<MapFunction<Row, ValidationResult>> mapFunctionCaptor;

    @Before
    public void setUp() {
        when(dataset.map(Mockito.<MapFunction<Row, Row>>any(), Mockito.any()))
                .thenReturn(dataset);
        when(dataset.filter(anyString()))
                .thenReturn(dataset);
        when(dataset.withColumn(anyString(), any(Column.class)))
                .thenReturn(dataset);
        when(dataset.withColumnRenamed(anyString(), anyString()))
                .thenReturn(dataset);
        when(datasetRepository.writeDataFrame(any(), any()))
                .thenReturn(Either.right(dataset));

        StructField name = DataTypes.createStructField("name", DataTypes.StringType, false);
        when(dataset.schema())
                .thenReturn(DataTypes.createStructType(Collections.singletonList(name)));
    }

    @Test
    public void executeRule_ValidationRule_FiltersDatasetForFailures() {
        when(dataset.filter(anyString()))
                .thenReturn(filteredDataset);

        validationService.executeRule(
                Populated.datasetValidationRule().build(), 5005L, dataset);

        verify(dataset).filter(eq("RESULT_TYPE == 'FAIL'"));
    }

    @Test
    public void executeRule_ValidationRule_FiltersDatasetForErrors() {
        when(dataset.filter(anyString()))
                .thenReturn(filteredDataset);

        validationService.executeRule(
                Populated.datasetValidationRule().build(), 5005L, dataset);

        verify(dataset).filter("RESULT_TYPE == 'ERROR'");
    }

    @Test
    public void executeRule_ValidationRule_SelectsColumns() {
        validationService.executeRule(
                Populated.datasetValidationRule().build(), 5005L, dataset);

        verify(dataset).select(DatasetTableSchema.validationResultsTableSchema().getColumns());
    }

    @Test
    public void executeRule_ValidationRule_MapsDataset() {
        validationService.executeRule(
                Populated.datasetValidationRule().build(), 5005L, dataset);

        verify(dataset, times(2)).map(mapFunctionCaptor.capture(), any());
        assertThat(mapFunctionCaptor.getAllValues().get(0))
                .isInstanceOf(JexlFunction.class);
    }

    @Test
    public void executeRule_ValidationRule_MapsDatasetWithRuleExpressionAndAliasForRowKey() {
        DatasetValidationRule validationRule = Populated.datasetValidationRule()
                .expression("well i never!")
                .build();

        validationService.executeRule(validationRule, 5005L, dataset);

        verify(dataset, times(2)).map(mapFunctionCaptor.capture(), any());
        JexlFunction jexlFunction = (JexlFunction) mapFunctionCaptor.getAllValues().get(0);

        assertThat(jexlFunction.getJexlTransformation().getName())
                .isEqualTo("well i never!");
    }

    @Test
    public void executeRule_ValidationRule_MapsDatasetWithValidationFunction() {
        DatasetValidationRule validationRule = Populated.datasetValidationRule()
                .build();

        validationService.executeRule(validationRule, 5005L, dataset);

        verify(dataset, times(2)).map(mapFunctionCaptor.capture(), any());
        MapFunction mapFunction = mapFunctionCaptor.getAllValues().get(1);

        assertThat(mapFunction)
                .isInstanceOf(UpdateRowValidationFunction.class);
    }

    @Test
    public void executeRule_ValidationRule_AddsTwoLiteralColumns() {
        DatasetValidationRule validationRule = Populated.datasetValidationRule()
                .build();

        validationService.executeRule(validationRule, 5005L, dataset);

        verify(dataset, times(2)).withColumn(anyString(), any(Column.class));
    }

    @Test
    public void executeRule_ValidationRule_AddsDatasetIdLiteralColumn() {
        DatasetValidationRule validationRule = Populated.datasetValidationRule()
                .build();

        validationService.executeRule(validationRule, 5005L, dataset);

        verify(dataset).withColumn(DATASET_ID, functions.lit(5005L));
    }

    @Test
    public void executeRule_ValidationRule_AddsRuleIdLiteralColumn() {
        DatasetValidationRule validationRule = Populated.datasetValidationRule()
                .id(100L)
                .build();

        validationService.executeRule(validationRule, 5005L, dataset);

        verify(dataset).withColumn(VALIDATION_RULE_ID, functions.lit(100L));
    }

    @Test
    public void executeRule_ValidationRule_RenamesRowKeyColumn() {
        DatasetValidationRule validationRule = Populated.datasetValidationRule()
                .id(100L)
                .build();

        validationService.executeRule(validationRule, 5005L, dataset);

        verify(dataset).withColumnRenamed(ROW_KEY.name(), DATASET_ROW_KEY);
    }

    @Test
    public void executeRule_ValidationRule_PersistsMappedDatasetToRepository() {
        when(dataset.map(Mockito.<MapFunction<Row, Row>>any(), Mockito.any()))
                .thenReturn(mappedDataset);
        when(mappedDataset.map(Mockito.<MapFunction<Row, Row>>any(), Mockito.any()))
                .thenReturn(mappedDataset);
        when(mappedDataset.withColumn(anyString(), any(Column.class)))
                .thenReturn(mappedDataset);
        when(mappedDataset.withColumnRenamed(anyString(), anyString()))
                .thenReturn(mappedDataset);
        when(mappedDataset.select(Mockito.<Column>any()))
                .thenReturn(mappedDataset);

        when(datasetRepository.writeDataFrame(any(), any()))
                .thenReturn(Either.right(mappedDataset));

        when(mappedDataset.filter(anyString()))
                .thenReturn(filteredDataset);
        when(filteredDataset.count())
                .thenReturn(100L);

        validationService.executeRule(Populated.datasetValidationRule().build(), 5005L, dataset);

        verify(datasetRepository)
                .writeDataFrame(mappedDataset, DatasetTableSchema.validationResultsTableSchema());
    }

    @Test
    public void executeRule_ValidationRule_ReturnsFailureCountFromRetrievedDataset() {
        when(dataset.count())
                .thenReturn(176L);

        Either<ErrorMessage, ValidationRuleStatistics> result =
                validationService.executeRule(Populated.datasetValidationRule().build(), 5005L, dataset);

        assertThat(result.get().getNumberOfFailures())
                .isEqualTo(176L);
    }

    @Test
    public void executeRule_ValidationRule_ReturnsErrorCountFromRetrievedDataset() {
        when(dataset.count())
                .thenReturn(189L);

        Either<ErrorMessage, ValidationRuleStatistics> result =
                validationService.executeRule(Populated.datasetValidationRule().build(), 5005L, dataset);

        assertThat(result.get().getNumberOfErrors())
                .isEqualTo(189L);
    }

    @Test
    public void executeRule_DatasetRepositoryReturnsError_ReturnsSameError() {
        when(datasetRepository.writeDataFrame(any(), any()))
                .thenReturn(Either.left(ErrorMessage.of("oops")));

        Either<ErrorMessage, ValidationRuleStatistics> result =
                validationService.executeRule(Populated.datasetValidationRule().build(), 5005L, dataset);

        assertThat(result.getLeft())
                .isEqualTo(ErrorMessage.of("oops"));
    }
}
