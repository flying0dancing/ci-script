package com.lombardrisk.ignis.spark.core.phoenix.ranged;

import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import scala.Tuple2;
import scala.collection.JavaConversions;
import scala.collection.Seq;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.lombardrisk.ignis.pipeline.step.api.DatasetFieldName.ROW_KEY;
import static com.lombardrisk.ignis.spark.core.phoenix.PhoenixJdbcDialect.ROW_KEY_METADATA;

@Slf4j
public class PhoenixRowKeyDataFrame {

    private final SparkSession spark;
    private final Dataset<Row> dataFrame;

    public PhoenixRowKeyDataFrame(final SparkSession spark, final Dataset<Row> dataFrame) {
        this.spark = spark;
        this.dataFrame = dataFrame;
    }

    public Dataset<Row> withRowKey(final Long seed) {
        StructType structType = schemaWithRowKey(dataFrame);

        JavaRDD<Row> withRowKeyRdd = dataWithRowKey(seed, 0L);

        return spark.createDataFrame(withRowKeyRdd, structType);
    }

    public Dataset<Row> withRowKeyStartingFrom(final Long offset, final Long seed) {
        StructType structType = schemaWithRowKey(dataFrame);

        JavaRDD<Row> withRowKeyRdd = dataWithRowKey(seed, offset);

        return spark.createDataFrame(withRowKeyRdd, structType);
    }

    private JavaRDD<Row> dataWithRowKey(final Long seed, final Long offset) {
        log.debug("Generate and insert row keys into dataset, starting from seed [{}], offset [{}] ", seed, offset);
        // our row key field always locates in the beginning
        boolean hasRowKeyField =
                Arrays.stream(dataFrame.schema().fieldNames())
                        .anyMatch(ROW_KEY.getName()::endsWith);

        JavaPairRDD<Row, Long> rdd = dataFrame.javaRDD().zipWithIndex();

        return rdd.map(tuple -> toRow(seed, offset, hasRowKeyField, tuple));
    }

    private static Row toRow(
            final Long seed,
            final Long offset,
            final boolean hasRowKeyField,
            final Tuple2<Row, Long> tuple) {

        Long rowKey = PhoenixTableRowKey.of(seed).buildRowKey(tuple._2() + offset);
        Seq<Object> rowAsSequence = tuple._1().toSeq();

        List<Object> rowWithRowKey = new ArrayList<>();
        rowWithRowKey.add(rowKey);

        rowWithRowKey.addAll(hasRowKeyField
                ? JavaConversions.seqAsJavaList(rowAsSequence.tail().toSeq())
                : JavaConversions.seqAsJavaList(rowAsSequence));

        return RowFactory.create(rowWithRowKey.toArray());
    }

    private static StructType schemaWithRowKey(final Dataset<Row> dataFrame) {
        StructType oldSchema = dataFrame.schema();
        String rowKeyName = ROW_KEY.getName();

        boolean rowKeyFieldNameExists =
                Arrays.stream(oldSchema.fieldNames())
                        .anyMatch(f -> StringUtils.equals(rowKeyName, f));

        if (rowKeyFieldNameExists) {
            String errorMessage = String.format(
                    "Dataset field cannot have the same name as the index column [%s]", rowKeyName);

            log.error(errorMessage);
            throw new IllegalStateException(errorMessage);
        }

        StructField rowKeyField = new StructField(rowKeyName, DataTypes.LongType, false, ROW_KEY_METADATA);
        List<StructField> newFields = ImmutableList.<StructField>builder()
                .add(rowKeyField)
                .addAll(ImmutableList.copyOf(oldSchema.fields()))
                .build();

        log.debug("Append {} as {} to current schema {}", ROW_KEY.name(), rowKeyField, oldSchema);
        return new StructType(newFields.toArray(new StructField[]{}));
    }
}
