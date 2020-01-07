package com.lombardrisk.ignis.spark.validation;

import com.lombardrisk.ignis.common.lang.ErrorMessage;
import com.lombardrisk.ignis.spark.api.validation.DatasetValidationRule;
import com.lombardrisk.ignis.spark.core.repository.DatasetRepository;
import com.lombardrisk.ignis.spark.core.schema.DatasetTableSchema;
import com.lombardrisk.ignis.spark.validation.function.UpdateRowValidationFunction;
import com.lombardrisk.ignis.spark.validation.function.ValidationFunction;
import com.lombardrisk.ignis.spark.validation.function.ValidationResult;
import com.lombardrisk.ignis.spark.validation.transform.FieldType;
import com.lombardrisk.ignis.spark.validation.transform.JexlTransformation;
import io.vavr.control.Either;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoder;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.catalyst.encoders.ExpressionEncoder;
import org.apache.spark.sql.catalyst.encoders.RowEncoder;
import org.apache.spark.sql.functions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import scala.Tuple2;

import static com.lombardrisk.ignis.api.rule.ValidationOutput.DATASET_ID;
import static com.lombardrisk.ignis.api.rule.ValidationOutput.DATASET_ROW_KEY;
import static com.lombardrisk.ignis.api.rule.ValidationOutput.ResultType.ERROR;
import static com.lombardrisk.ignis.api.rule.ValidationOutput.ResultType.FAIL;
import static com.lombardrisk.ignis.api.rule.ValidationOutput.VALIDATION_RESULT_TYPE;
import static com.lombardrisk.ignis.api.rule.ValidationOutput.VALIDATION_RULE_ID;
import static com.lombardrisk.ignis.pipeline.step.api.DatasetFieldName.ROW_KEY;
import static com.lombardrisk.ignis.spark.validation.function.UpdateRowValidationFunction.addAdditionalValidationColumns;

@Component
public class ValidationService {

    private final DatasetRepository datasetRepository;
    private final DatasetTableSchema validationResultsTableSchema = DatasetTableSchema.validationResultsTableSchema();

    @Autowired
    public ValidationService(final DatasetRepository datasetRepository) {
        this.datasetRepository = datasetRepository;
    }

    public Either<ErrorMessage, ValidationRuleStatistics> executeRule(
            final DatasetValidationRule validationRule,
            final long datasetId,
            final Dataset<Row> dataset) {

        Dataset<Row> newDataFrame = dataset
                .map(toValidationResult(validationRule), createResultEncoder(dataset))
                .map(toValidationResultTableRow(), createResultRowEncoder(dataset))
                .withColumnRenamed(ROW_KEY.name(), DATASET_ROW_KEY)
                .withColumn(DATASET_ID, functions.lit(datasetId))
                .withColumn(VALIDATION_RULE_ID, functions.lit(validationRule.getId()))
                .select(validationResultsTableSchema.getColumns());

        return datasetRepository.writeDataFrame(newDataFrame, validationResultsTableSchema)
                .map(this::generateStatistics);
    }

    private ExpressionEncoder<Row> createResultRowEncoder(final Dataset<Row> dataset) {
        return RowEncoder.apply(addAdditionalValidationColumns(dataset.schema()));
    }

    private UpdateRowValidationFunction toValidationResultTableRow() {
        return new UpdateRowValidationFunction();
    }

    private Encoder<Tuple2<ValidationResult, Row>> createResultEncoder(final Dataset<Row> dataset) {
        return Encoders.tuple(Encoders.bean(ValidationResult.class), RowEncoder.apply(dataset.schema()));
    }

    private ValidationFunction toValidationResult(final DatasetValidationRule validationRule) {
        return new ValidationFunction(validationPredicate(validationRule.getExpression()));
    }

    private ValidationRuleStatistics generateStatistics(final Dataset<Row> transformedDataset) {
        long numberOfFailures = transformedDataset
                .filter(VALIDATION_RESULT_TYPE + " == '" + FAIL + "'")
                .count();

        long numberOfErrors = transformedDataset
                .filter(VALIDATION_RESULT_TYPE + " == '" + ERROR + "'")
                .count();

        return ValidationRuleStatistics.builder()
                .numberOfFailures(numberOfFailures)
                .numberOfErrors(numberOfErrors)
                .build();
    }

    private static JexlTransformation<Boolean> validationPredicate(final String expression) {
        return JexlTransformation.<Boolean>builder()
                .name(expression)
                .fieldType(FieldType.BOOLEAN)
                .javaParseResultFunction(Boolean.class::cast)
                .build();
    }
}
