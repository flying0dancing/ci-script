package com.lombardrisk.ignis.design.server.pipeline.validator.transformation.dataframe;

import com.google.common.collect.Sets;
import com.lombardrisk.ignis.common.MapperUtils;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.design.field.model.Field;
import com.lombardrisk.ignis.design.field.model.LongField;
import com.lombardrisk.ignis.design.server.pipeline.StructFieldIgnoringNullable;
import com.lombardrisk.ignis.design.server.pipeline.converter.SchemaStructTypeConverter;
import io.vavr.control.Validation;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.AnalysisException;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.catalyst.parser.ParseException;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import scala.collection.JavaConversions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.collect.Sets.newHashSet;
import static com.lombardrisk.ignis.pipeline.step.api.DatasetFieldName.ROW_KEY;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toSet;

@Slf4j
@UtilityClass
public class SparkTransformUtils {

    public static <T> Validation<List<ErrorResponse>, T> handleException(final String stepInfo, final Exception e) {
        log.error(
                "Caught [{}] while trying to execute spark transformation [{}],",
                e.getClass().getName(),
                stepInfo,
                e);

        if (e instanceof ParseException) {
            return Validation.invalid(singletonList(
                    ErrorResponse.valueOf(e.getMessage(), "SQL_PARSE_ERROR")));
        }

        if (e instanceof AnalysisException) {
            return Validation.invalid(singletonList(
                    ErrorResponse.valueOf(((AnalysisException) e).getSimpleMessage(), "SQL_ANALYSIS_ERROR")));
        }

        return Validation.invalid(
                singletonList(ErrorResponse.valueOf(e.getMessage(), "UNKNOWN_ERROR")));
    }

    public static Validation<List<ErrorResponse>, Dataset<Row>> validateFields(
            final Dataset<Row> outputData, final StructType schemaOutStructType) {

        List<ErrorResponse> errors = new ArrayList<>();

        Set<StructFieldIgnoringNullable> inputFields = Arrays.stream(schemaOutStructType.fields())
                .filter(structField -> allowedField(structField.name()))
                .map(StructFieldIgnoringNullable::new)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Set<StructFieldIgnoringNullable> outputFields = newHashSet(outputData.schema().fields())
                .stream()
                .filter(structField -> allowedField(structField.name()))
                .map(StructFieldIgnoringNullable::new)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Set<String> fieldsInInputNotExpected = Sets.difference(outputFields, inputFields).stream()
                .map(StructFieldIgnoringNullable::getStructField)
                .map(SparkTransformUtils::toDisplayName)
                .collect(toSet());

        Set<String> fieldsInOutputNotFound = Sets.difference(inputFields, outputFields).stream()
                .map(StructFieldIgnoringNullable::getStructField)
                .map(SparkTransformUtils::toDisplayName)
                .collect(toSet());

        if (!fieldsInOutputNotFound.isEmpty()) {
            log.warn("Output fields required the following fields that were not found {}", fieldsInOutputNotFound);

            errors.add(ErrorResponse.valueOf(
                    "Output fields required the following fields that were not found " + fieldsInOutputNotFound,
                    "UNMAPPED_OUTPUT_FIELDS"));
        }

        if (!fieldsInInputNotExpected.isEmpty()) {
            log.warn(
                    "The following fields in the transformation were not mapped to any fields in the output {}",
                    fieldsInInputNotExpected);
            errors.add(ErrorResponse.valueOf(
                    "The following fields in the transformation were not mapped to any fields in the output "
                            + fieldsInInputNotExpected,
                    "UNMAPPED_INPUT_FIELDS"));
        }

        List<Column> outputColumns = MapperUtils.map(inputFields, str -> new Column(str.getStructField().name()));

        return errors.isEmpty()
                ? Validation.valid(outputData.select(JavaConversions.asScalaBuffer(outputColumns)))
                : Validation.invalid(errors);
    }

    private static boolean allowedField(final String name) {
        return !name.equals(ROW_KEY.name()) && !name.startsWith("FCR_SYS__");
    }

    public static boolean outputContainsExpectedField(final Dataset<Row> output, final Field outputField) {
        HashSet<StructField> structFields = newHashSet(output.schema().fields());
        StructFieldIgnoringNullable expected =
                new StructFieldIgnoringNullable(SchemaStructTypeConverter.fieldToStructField(outputField));

        if (MapperUtils.mapSet(structFields, StructFieldIgnoringNullable::new).contains(expected)) {
            return true;
        } else {
            if (expected.getStructField().nullable()) {
                StructFieldIgnoringNullable nullableExpectedStructField =
                        new StructFieldIgnoringNullable(new StructField(expected.getStructField().name(),
                                DataTypes.NullType,
                                true,
                                expected.getStructField().metadata()));
                return MapperUtils.mapSet(structFields, StructFieldIgnoringNullable::new)
                        .contains(nullableExpectedStructField);
            }
        }

        return false;
    }

    public static String toDisplayName(final StructField field) {
        return field.name() + "(type=" + field.dataType() + ")";
    }

    public static Row generateDummyRow(final Set<Field> fields) {

        LongField rowKeyField = LongField.builder().name(ROW_KEY.name())
                .id(-1L)
                .build();

        Object[] values = Stream.concat(Stream.of(rowKeyField), fields.stream())
                .sorted(Comparator.comparing(Field::getId))
                .map((Function<Field, Comparable>) Field::generateData)
                .toArray(Object[]::new);

        return RowFactory.create(values);
    }
}