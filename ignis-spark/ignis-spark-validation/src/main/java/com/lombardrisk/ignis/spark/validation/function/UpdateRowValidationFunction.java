package com.lombardrisk.ignis.spark.validation.function;

import com.lombardrisk.ignis.api.rule.ValidationOutput;
import com.lombardrisk.ignis.common.lang.ErrorMessage;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;
import scala.Tuple2;
import scala.collection.JavaConversions;

import java.util.ArrayList;
import java.util.List;

import static com.lombardrisk.ignis.api.rule.ValidationOutput.ERROR_MESSAGE;
import static com.lombardrisk.ignis.api.rule.ValidationOutput.ResultType.ERROR;
import static com.lombardrisk.ignis.api.rule.ValidationOutput.ResultType.FAIL;
import static com.lombardrisk.ignis.api.rule.ValidationOutput.ResultType.SUCCESS;
import static com.lombardrisk.ignis.api.rule.ValidationOutput.VALIDATION_RESULT_TYPE;

public class UpdateRowValidationFunction implements MapFunction<Tuple2<ValidationResult, Row>, Row> {

    private static final int EXTRA_COLUMNS_ADDED = 2;

    @Override
    public Row call(final Tuple2<ValidationResult, Row> rowMapResult) {
        Row row = rowMapResult._2();
        ValidationResult validationResult = rowMapResult._1();

        return validationResult.toEither()
                .fold(
                        errorMessage -> handleError(row, errorMessage),
                        value -> handleSuccess(row, value));
    }

    private Row handleError(final Row row, final ErrorMessage error) {
        return createRow(row, ERROR, error.getMessage());
    }

    private Row handleSuccess(final Row row, final boolean result) {
        return createRow(row, result ? SUCCESS : FAIL, "");
    }

    private Row createRow(final Row row, final ValidationOutput.ResultType resultType, final String errorMessage) {
        List<Object> fieldValues = new ArrayList<>(row.size() + EXTRA_COLUMNS_ADDED);
        fieldValues.addAll(JavaConversions.seqAsJavaList(row.toSeq()));
        fieldValues.add(resultType.name());
        fieldValues.add(errorMessage);

        return new GenericRowWithSchema(fieldValues.toArray(), addAdditionalValidationColumns(row.schema()));
    }

    public static StructType addAdditionalValidationColumns(final StructType inputSchema) {
        return inputSchema
                .add(VALIDATION_RESULT_TYPE, DataTypes.StringType, false)
                .add(ERROR_MESSAGE, DataTypes.StringType, true);
    }
}
