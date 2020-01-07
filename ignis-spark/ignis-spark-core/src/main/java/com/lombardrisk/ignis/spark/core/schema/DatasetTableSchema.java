package com.lombardrisk.ignis.spark.core.schema;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.lombardrisk.ignis.pipeline.step.api.DatasetFieldName;
import com.lombardrisk.ignis.spark.core.phoenix.PhoenixJdbcDialect;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.jdbc.JdbcType;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.MetadataBuilder;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import scala.collection.JavaConversions;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.lombardrisk.ignis.api.rule.ValidationOutput.DATASET_ID;
import static com.lombardrisk.ignis.api.rule.ValidationOutput.DATASET_ROW_KEY;
import static com.lombardrisk.ignis.api.rule.ValidationOutput.ERROR_MESSAGE;
import static com.lombardrisk.ignis.api.rule.ValidationOutput.VALIDATION_RESULT_TYPE;
import static com.lombardrisk.ignis.api.rule.ValidationOutput.VALIDATION_RULE_ID;
import static com.lombardrisk.ignis.api.rule.ValidationOutput.VALIDATION_RULE_RESULTS;
import static com.lombardrisk.ignis.pipeline.step.api.DatasetFieldName.ROW_KEY;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@Builder
@EqualsAndHashCode
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DatasetTableSchema {

    private static final Metadata ROW_KEY_METADATA = new MetadataBuilder()
            .putBoolean(DatasetFieldName.ROW_KEY.getName(), true)
            .build();

    private final StructType structType;
    private final String tableName;
    private final List<StructField> primaryKeys;
    private final Type type;

    public enum Type {
        DYNAMIC,
        FIXED
    }

    @VisibleForTesting
    DatasetTableSchema(
            final String tableName,
            final StructType structType,
            final Type type,
            final List<StructField> primaryKeys) {
        this.tableName = tableName;
        this.structType = structType;
        this.type = type;
        this.primaryKeys = primaryKeys;
    }

    public static DatasetTableSchema rowKeyRangedSchema(final String tableName, final StructType structType) {
        StructField rowKeyField = DataTypes.createStructField(
                ROW_KEY.getName(), DataTypes.LongType, false, ROW_KEY_METADATA);

        ImmutableList.Builder<StructField> structFields = ImmutableList.builder();

        if (!structType.getFieldIndex(ROW_KEY.getName()).isDefined()) {
            structFields.add(rowKeyField);
        }

        structFields.addAll(JavaConversions.asJavaIterator(structType.iterator()));

        return new DatasetTableSchema(
                tableName,
                DataTypes.createStructType(structFields.build()),
                Type.DYNAMIC,
                Collections.singletonList(rowKeyField));
    }

    public static DatasetTableSchema validationResultsTableSchema() {
        StructField datasetRowKey = DataTypes.createStructField(DATASET_ROW_KEY, DataTypes.LongType, false);
        StructField ruleId = DataTypes.createStructField(VALIDATION_RULE_ID, DataTypes.IntegerType, false);
        StructField datasetId = DataTypes.createStructField(DATASET_ID, DataTypes.LongType, false);
        StructField resultType = DataTypes.createStructField(VALIDATION_RESULT_TYPE, DataTypes.StringType, false);
        StructField errorMessage = DataTypes.createStructField(ERROR_MESSAGE, DataTypes.StringType, true);

        StructType structType =
                new StructType(new StructField[]{ datasetRowKey, ruleId, datasetId, resultType, errorMessage });

        return new DatasetTableSchema(
                VALIDATION_RULE_RESULTS, structType, Type.FIXED, ImmutableList.of(datasetRowKey, ruleId));
    }

    public StructType getStructType() {
        return structType;
    }

    public String getTableName() {
        return tableName;
    }

    public Type getType() {
        return type;
    }

    public Column[] getColumns() {
        return Stream.of(structType.fieldNames())
                .map(Column::new)
                .collect(toList())
                .toArray(new Column[]{});
    }

    boolean isPrimaryKey(final StructField structField) {
        return primaryKeys.contains(structField);
    }

    public String createTableDDL(final int saltBuckets) {
        String fields = Stream.of(structType.fields())
                .map(this::getFieldDeclaration)
                .collect(Collectors.joining(", "));

        return String.format(
                "CREATE TABLE %s (%s %s) SALT_BUCKETS=%d",
                tableName,
                fields,
                createConstraints(),
                saltBuckets);
    }

    private String createConstraints() {
        if (primaryKeys.isEmpty()) {
            return EMPTY;
        }

        String primaryKeyString = this.primaryKeys.stream()
                .map(StructField::name)
                .collect(Collectors.joining(", "));

        return String.format(", CONSTRAINT pk PRIMARY KEY (%s)", primaryKeyString);
    }

    private String getFieldDeclaration(final StructField field) {
        JdbcType jdbcType = PhoenixJdbcDialect.getJdbcType(field.dataType())
                .orElseThrow(() -> new IllegalArgumentException(String.format(
                        "Can not get jdbc type for data type %s",
                        field.dataType())));

        ImmutableList.Builder<String> fieldBuilder = ImmutableList.builder();
        fieldBuilder.add(field.name());
        fieldBuilder.add(jdbcType.databaseTypeDefinition());

        if (isPrimaryKey(field)) {
            fieldBuilder.add("NOT NULL");
        }
        return String.join(" ", fieldBuilder.build());
    }
}
