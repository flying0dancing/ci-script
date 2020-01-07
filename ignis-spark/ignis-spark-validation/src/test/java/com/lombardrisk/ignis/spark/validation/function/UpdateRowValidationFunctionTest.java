package com.lombardrisk.ignis.spark.validation.function;

import org.apache.spark.sql.Row;
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.junit.Test;
import scala.Tuple2;
import scala.collection.JavaConversions;

import static com.lombardrisk.ignis.api.rule.ValidationOutput.ERROR_MESSAGE;
import static com.lombardrisk.ignis.api.rule.ValidationOutput.VALIDATION_RESULT_TYPE;
import static com.lombardrisk.ignis.pipeline.step.api.DatasetFieldName.ROW_KEY;
import static org.assertj.core.api.Assertions.assertThat;

public class UpdateRowValidationFunctionTest {

    private final Object[] rowValues = new Object[]{ 1238573L, "Matthew", "Sibson" };
    private final StructType structType = new StructType(new StructField[]{
            new StructField(ROW_KEY.name(), DataTypes.LongType, false, Metadata.empty()),
            new StructField("Forename", DataTypes.StringType, false, Metadata.empty()),
            new StructField("Surname", DataTypes.StringType, false, Metadata.empty())
    });

    @Test
    public void call_ResultContainsNoErrors_AddsNewValuesFields() {

        Tuple2<ValidationResult, Row> mapResult = Tuple2.apply(
                ValidationResult.of(true, null),
                new GenericRowWithSchema(rowValues, structType));

        Row newRow = new UpdateRowValidationFunction().call(mapResult);

        assertThat(JavaConversions.seqAsJavaList(newRow.toSeq()))
                .containsExactly(1238573L, "Matthew", "Sibson", "SUCCESS", "");
    }

    @Test
    public void call_ResultContainsNoErrors_AddsNewSchemaFields() {

        Tuple2<ValidationResult, Row> mapResult = Tuple2.apply(
                ValidationResult.of(false, null),
                new GenericRowWithSchema(rowValues, structType));

        Row newRow = new UpdateRowValidationFunction().call(mapResult);

        assertThat(newRow.schema().fields())
                .containsExactly(
                        new StructField(ROW_KEY.name(), DataTypes.LongType, false, Metadata.empty()),
                        new StructField("Forename", DataTypes.StringType, false, Metadata.empty()),
                        new StructField("Surname", DataTypes.StringType, false, Metadata.empty()),
                        new StructField(VALIDATION_RESULT_TYPE, DataTypes.StringType, false, Metadata.empty()),
                        new StructField(ERROR_MESSAGE, DataTypes.StringType, true, Metadata.empty())
                );
    }

    @Test
    public void call_ResultSuccess_AddsSuccessToRow() {
        Tuple2<ValidationResult, Row> mapResult = Tuple2.apply(
                ValidationResult.of(true, null),
                new GenericRowWithSchema(rowValues, structType));

        Row newRow = new UpdateRowValidationFunction().call(mapResult);

        assertThat((String) newRow.getAs(VALIDATION_RESULT_TYPE))
                .isEqualTo("SUCCESS");
    }

    @Test
    public void call_ResultSuccess_AddsFailedToRow() {

        Tuple2<ValidationResult, Row> mapResult = Tuple2.apply(
                ValidationResult.of(false, null),
                new GenericRowWithSchema(rowValues, structType));

        Row newRow = new UpdateRowValidationFunction().call(mapResult);

        assertThat((String) newRow.getAs(VALIDATION_RESULT_TYPE))
                .isEqualTo("FAIL");
    }

    @Test
    public void call_ResultError_AddsErrorStatusToRow() {
        Tuple2<ValidationResult, Row> mapResult = Tuple2.apply(
                ValidationResult.of(false, "Whoopy Goldberg"),
                new GenericRowWithSchema(rowValues, structType));

        Row newRow = new UpdateRowValidationFunction().call(mapResult);

        assertThat((String) newRow.getAs(VALIDATION_RESULT_TYPE))
                .isEqualTo("ERROR");
    }

    @Test
    public void call_ResultError_AddsErrorMessageToRow() {

        Tuple2<ValidationResult, Row> mapResult = Tuple2.apply(
                ValidationResult.of(false, "Whoopy Goldberg"),
                new GenericRowWithSchema(rowValues, structType));

        Row newRow = new UpdateRowValidationFunction().call(mapResult);

        assertThat((String) newRow.getAs(ERROR_MESSAGE))
                .isEqualTo("Whoopy Goldberg");
    }
}
