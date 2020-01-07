package com.lombardrisk.ignis.spark.validation.transform.parser.map;

import com.google.common.collect.ImmutableList;
import com.lombardrisk.ignis.spark.validation.transform.FieldType;
import com.lombardrisk.ignis.spark.validation.transform.JexlFunction;
import com.lombardrisk.ignis.spark.validation.transform.JexlTransformation;
import com.lombardrisk.ignis.spark.validation.transform.Result;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.junit.Test;
import scala.Tuple2;

import static org.assertj.core.api.Assertions.assertThat;

public class JexlFunctionTest {

    private final StructType structType = new StructType(new StructField[]{
            new StructField("Title", DataTypes.StringType, false, Metadata.empty()),
            new StructField("Forename", DataTypes.StringType, false, Metadata.empty()),
            new StructField("Surname", DataTypes.StringType, false, Metadata.empty())
    });

    @Test
    public void apply_ConcatFields_ReturnsRowWithSameSchema() {
        Object[] values = new Object[]{ "Mr", "Matthew", "Sibson" };

        StructType outputStructType = DataTypes.createStructType(ImmutableList.of(
                DataTypes.createStructField("Title", DataTypes.StringType, false),
                DataTypes.createStructField("Forename", DataTypes.StringType, false),
                DataTypes.createStructField("Surname", DataTypes.StringType, false)
        ));

        GenericRowWithSchema row = new GenericRowWithSchema(values, structType);

        JexlTransformation<String> transformation = JexlTransformation.<String>builder()
                .name("Title + Forename + Surname")
                .as("Full name")
                .fieldType(FieldType.STRING)
                .javaParseResultFunction(Object::toString)
                .build();

        Tuple2<TestResult, Row> transformed = new JexlFunction<>(transformation, TestResult::new)
                .call(row);

        assertThat(transformed._2().schema())
                .isEqualTo(outputStructType);
    }

    @Test
    public void apply_ConcatFields_ReturnsResult() {
        Object[] values = new Object[]{ "Mr", "Matthew", "Sibson" };
        GenericRowWithSchema row = new GenericRowWithSchema(values, structType);

        JexlTransformation<String> jexlTransformation = JexlTransformation.<String>builder()
                .name("Title + Forename + Surname")
                .as("Full name")
                .fieldType(FieldType.STRING)
                .javaParseResultFunction(Object::toString)
                .build();

        Tuple2<TestResult, Row> transformed = new JexlFunction<>(jexlTransformation, TestResult::new)
                .call(row);

        assertThat(transformed._1().value)
                .isEqualTo("MrMatthewSibson");
    }

    @Test
    public void apply_ConcatFields_ReturnsResultWithNoErrorMessage() {
        Object[] values = new Object[]{ "Mr", "Matthew", "Sibson" };
        GenericRowWithSchema row = new GenericRowWithSchema(values, structType);

        JexlTransformation<String> transformation = JexlTransformation.<String>builder()
                .name("Title + Forename + Surname")
                .as("Full name")
                .fieldType(FieldType.STRING)
                .javaParseResultFunction(Object::toString)
                .build();

        Tuple2<TestResult, Row> transformed = new JexlFunction<>(transformation, TestResult::new)
                .call(row);

        assertThat(transformed._1().error)
                .isNull();
    }

    @Test
    public void apply_ConcatFields_OriginalFieldsRemain() {
        Object[] values = new Object[]{ "Mr", "Matthew", "Sibson" };

        GenericRowWithSchema rowWithSchema = new GenericRowWithSchema(values, structType);

        JexlTransformation<String> transformation = JexlTransformation.<String>builder()
                .name("Title + Forename + Surname")
                .as("Full name")
                .fieldType(FieldType.STRING)
                .javaParseResultFunction(Object::toString)
                .build();

        Tuple2<TestResult, Row> transformed = new JexlFunction<>(transformation, TestResult::new)
                .call(rowWithSchema);

        assertThat(transformed._2())
                .extracting(row -> row.getAs("Title"), row -> row.getAs("Forename"), row -> row.getAs("Surname"))
                .containsExactly("Mr", "Matthew", "Sibson");
    }

    @Test
    public void apply_JexlExpressionFails_OriginalFieldsRemain() {
        Object[] values = new Object[]{ "Mr", "Matthew", "Sibson" };

        GenericRowWithSchema rowWithSchema = new GenericRowWithSchema(values, structType);

        JexlTransformation<String> jexlTransformation = JexlTransformation.<String>builder()
                .name("Oops + Thats + Not + Right")
                .as("Full name")
                .fieldType(FieldType.STRING)
                .javaParseResultFunction(Object::toString)
                .build();

        Tuple2<TestResult, Row> transformed = new JexlFunction<>(jexlTransformation, TestResult::new)
                .call(rowWithSchema);

        assertThat(transformed._2())
                .extracting(row -> row.getAs("Title"), row -> row.getAs("Forename"), row -> row.getAs("Surname"))
                .containsExactly("Mr", "Matthew", "Sibson");
    }

    @Test
    public void apply_FieldInExpressionNotDefined_ReturnsFailedResultType() {
        Object[] values = new Object[]{ "Matthew" };

        StructType structType = new StructType(new StructField[]{
                new StructField("Forename", DataTypes.StringType, false, Metadata.empty()),
        });

        GenericRowWithSchema row = new GenericRowWithSchema(values, structType);

        JexlTransformation<String> mapField = JexlTransformation.<String>builder()
                .name("Forename + Surname")
                .as("Full name")
                .fieldType(FieldType.STRING)
                .javaParseResultFunction(Object::toString)
                .build();

        Tuple2<TestResult, Row> mapResult = new JexlFunction<>(mapField, TestResult::new)
                .call(row);

        assertThat(mapResult._1().error)
                .isEqualTo("org.apache.commons.jexl3.JexlException$Variable: @1:12 undefined variable Surname");
    }

    @Test
    public void apply_FieldInExpressionNotDefined_ReturnsNoResult() {
        Object[] values = new Object[]{ "Matthew" };

        StructType structType = new StructType(new StructField[]{
                new StructField("Forename", DataTypes.StringType, false, Metadata.empty()),
        });

        GenericRowWithSchema row = new GenericRowWithSchema(values, structType);

        JexlTransformation<String> mapField = JexlTransformation.<String>builder()
                .name("Forename + Surname")
                .as("Full name")
                .fieldType(FieldType.STRING)
                .javaParseResultFunction(Object::toString)
                .build();

        Tuple2<TestResult, Row> mapResult = new JexlFunction<>(mapField, TestResult::new)
                .call(row);

        assertThat(mapResult._2())
                .isEqualTo(row);
    }

    @AllArgsConstructor
    @NoArgsConstructor
    private static class TestResult implements Result<String> {

        private String value;
        private String error;

        @Override
        public String value() {
            return value;
        }

        @Override
        public String errorMessage() {
            return error;
        }
    }
}




