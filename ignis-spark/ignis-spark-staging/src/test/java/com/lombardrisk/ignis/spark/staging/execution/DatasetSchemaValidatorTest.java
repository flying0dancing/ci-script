package com.lombardrisk.ignis.spark.staging.execution;

import com.google.common.collect.ImmutableList;
import com.lombardrisk.ignis.spark.api.staging.StagingSchemaValidation;
import com.lombardrisk.ignis.spark.api.staging.field.DecimalFieldValidation;
import com.lombardrisk.ignis.spark.api.staging.field.StringFieldValidation;
import com.lombardrisk.ignis.spark.staging.datafields.DataRow;
import com.lombardrisk.ignis.spark.staging.datafields.IndexedDataField;
import org.assertj.core.api.Condition;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class DatasetSchemaValidatorTest {

    @Test
    public void call_DecimalNotOfCorrectPrecision_ReturnsFailure() {
        StagingSchemaValidation table = StagingSchemaValidation.builder().physicalTableName("employee")
                .fields(new LinkedHashSet<>(ImmutableList.of(
                        DecimalFieldValidation.builder()
                                .name("first")
                                .precision(6)
                                .scale(5)
                                .build()
                ))).build();

        DatasetSchemaValidator datasetSchemaValidator = new DatasetSchemaValidator(table);
        DataRow dataRow = new DataRow(ImmutableList.of(new IndexedDataField(0, "9000.1")));
        Optional<String> result = datasetSchemaValidator.call(dataRow);

        assertThat(result)
                .hasValueSatisfying(new Condition<>(
                        string -> string.endsWith("will not fit the precision (6 < 9)\""), "Ends with message"));
    }

    @Test
    public void call_DecimalCorrectPrecision_ReturnsEmpty() {
        StagingSchemaValidation table = StagingSchemaValidation.builder().physicalTableName("employee")
                .fields(new LinkedHashSet<>(Collections.singletonList(
                        DecimalFieldValidation.builder()
                                .name("first")
                                .precision(6)
                                .scale(3)
                                .build()
                ))).build();

        DatasetSchemaValidator datasetSchemaValidator = new DatasetSchemaValidator(table);

        DataRow dataRow = new DataRow(ImmutableList.of(new IndexedDataField(0, "900.123")));
        Optional<String> result = datasetSchemaValidator.call(dataRow);

        assertThat(result).isEmpty();
    }

    @Test
    public void call_MultipleFieldsInCorrectOrder_ReturnsNoErrors() {
        StagingSchemaValidation table = StagingSchemaValidation.builder().physicalTableName("employee")
                .fields(new LinkedHashSet<>(asList(
                        DecimalFieldValidation.builder()
                                .name("first")
                                .precision(6)
                                .scale(3)
                                .build(),
                        aStringField("second"),
                        aStringField("third")
                ))).build();

        DatasetSchemaValidator datasetSchemaValidator = new DatasetSchemaValidator(table);

        DataRow dataRow = new DataRow(ImmutableList.of(
                new IndexedDataField(0, "900.123"),
                new IndexedDataField(1, "hello"),
                new IndexedDataField(2, "there")
        ));

        Optional<String> result = datasetSchemaValidator.call(dataRow);

        assertThat(result).isEmpty();
    }

    @Test
    public void call_MultipleFieldsInIncorrectOrder_ReturnsErrors() {
        StagingSchemaValidation table = StagingSchemaValidation.builder().physicalTableName("employee")
                .fields(new LinkedHashSet<>(asList(
                        DecimalFieldValidation.builder()
                                .name("first")
                                .precision(6)
                                .scale(3)
                                .build(),
                        aStringField("second"),
                        aStringField("third")
                ))).build();

        DatasetSchemaValidator datasetSchemaValidator = new DatasetSchemaValidator(table);

        DataRow dataRow = new DataRow(ImmutableList.of(
                new IndexedDataField(0, "hello"),
                new IndexedDataField(1, "900.123"),
                new IndexedDataField(2, "there")
        ));

        Optional<String> result = datasetSchemaValidator.call(dataRow);

        assertThat(result).hasValueSatisfying(new Condition<>(
                string -> string.endsWith("\"Field [first] expected to be have a decimal type, but was [hello]\""),
                "Validation errors must end with correct value"));
    }

    @Test
    public void call_NullableValueForNullableField_ReturnsNoErrors() {
        StringFieldValidation nullable = aStringField("nullable");
        nullable.setNullable(true);

        StagingSchemaValidation table = StagingSchemaValidation.builder().physicalTableName("employee")
                .fields(new LinkedHashSet<>(asList(
                        nullable,
                        aStringField("notNullable")
                ))).build();

        DatasetSchemaValidator datasetSchemaValidator = new DatasetSchemaValidator(table);

        DataRow dataRow = new DataRow(ImmutableList.of(
                new IndexedDataField(0, ""),
                new IndexedDataField(1, "there")
        ));

        Optional<String> result = datasetSchemaValidator.call(dataRow);

        assertThat(result).isEmpty();
    }

    @Test
    public void call_NullableValueForNonNullableField_ReturnsErrors() {
        StringFieldValidation notNullable = aStringField("notNullable");
        notNullable.setNullable(false);

        StagingSchemaValidation table = StagingSchemaValidation.builder().physicalTableName("employee")
                .fields(new LinkedHashSet<>(Collections.singletonList(notNullable))).build();

        DatasetSchemaValidator datasetSchemaValidator = new DatasetSchemaValidator(table);

        DataRow dataRow = new DataRow(ImmutableList.of(new IndexedDataField(0, "")));

        Optional<String> result = datasetSchemaValidator.call(dataRow);

        assertThat(result).hasValue("<NULL>,Field [notNullable] is not nullable");
    }

    @Test
    public void call_FieldWithIndexNotFound_ReturnsError() {
        StagingSchemaValidation table = StagingSchemaValidation.builder().physicalTableName("employee")
                .fields(new LinkedHashSet<>(asList(
                        aStringField("one"),
                        aStringField("two")
                ))).build();

        DatasetSchemaValidator datasetSchemaValidator = new DatasetSchemaValidator(table);

        DataRow dataRow = new DataRow(ImmutableList.of(
                new IndexedDataField(0, "one"),
                new IndexedDataField(1, "two"),
                new IndexedDataField(2, "three")
        ));
        Optional<String> result = datasetSchemaValidator.call(dataRow);

        assertThat(result)
                .hasValue("one,two,three,Expected 2 fields - actual: 3");
    }

    @Test
    public void call_FieldWithIndexValueWrong_ReturnsError() {
        StagingSchemaValidation table = StagingSchemaValidation.builder().physicalTableName("employee")
                .fields(new LinkedHashSet<>(asList(
                        aStringField("one"),
                        aStringField("two")
                ))).build();

        DatasetSchemaValidator datasetSchemaValidator = new DatasetSchemaValidator(table);

        DataRow dataRow = new DataRow(ImmutableList.of(
                new IndexedDataField(0, "one"),
                new IndexedDataField(2, "three")
        ));
        Optional<String> result = datasetSchemaValidator.call(dataRow);

        assertThat(result)
                .hasValue("one,three,The field with index 2 is undefined, please check schema.");
    }

    @Test
    public void parse_FieldNotFound_ReturnsNull() {
        StagingSchemaValidation table = StagingSchemaValidation.builder().physicalTableName("employee")
                .fields(new LinkedHashSet<>(asList(
                        aStringField("one"),
                        aStringField("two")
                ))).build();

        DatasetSchemaValidator datasetSchemaValidator = new DatasetSchemaValidator(table);

        Object parsed = datasetSchemaValidator.parseField(new IndexedDataField(3, ""));

        assertThat(parsed).isNull();
    }

    @Test
    public void parse_FieldFound_ReturnsField() {
        StagingSchemaValidation table = StagingSchemaValidation.builder().physicalTableName("employee")
                .fields(new LinkedHashSet<>(asList(
                        aStringField("one"),
                        aStringField("two")
                ))).build();

        DatasetSchemaValidator datasetSchemaValidator = new DatasetSchemaValidator(table);

        Object parsed = datasetSchemaValidator.parseField(new IndexedDataField(0, "hello"));

        assertThat(parsed).isEqualTo("hello");
    }

    private static StringFieldValidation aStringField(final String name) {
        StringFieldValidation stringField = new StringFieldValidation();
        stringField.setName(name);
        return stringField;
    }
}