package com.lombardrisk.ignis.spark.core.hive;

import com.lombardrisk.ignis.client.internal.InternalDatasetClient;
import com.lombardrisk.ignis.spark.api.DatasetTableLookup;
import com.lombardrisk.ignis.spark.api.pipeline.PipelineAppConfig;
import com.lombardrisk.ignis.spark.api.pipeline.PipelineStepAppConfig;
import com.lombardrisk.ignis.spark.api.staging.StagingDatasetConfig;
import com.lombardrisk.ignis.spark.core.phoenix.ranged.PhoenixTableRowKey;
import com.lombardrisk.ignis.spark.core.repository.RowKeyedDatasetRepository;
import com.lombardrisk.ignis.spark.core.server.DatasetEntityRepository;
import io.vavr.Tuple2;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;
import scala.collection.Seq;

import static com.lombardrisk.ignis.pipeline.step.api.DatasetFieldName.ROW_KEY;
import static com.lombardrisk.ignis.spark.core.schema.DatasetTableSchema.rowKeyRangedSchema;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Using by unit test.
 *
 * Used in previous version, now using Phoenix
 */
@Slf4j
public abstract class InMemoryRowKeyedDatasetRepository implements RowKeyedDatasetRepository {

    private final SparkSession sparkSession;
    private final InternalDatasetClient datasetClient;
    private final DatasetEntityRepository datasetEntityRepository;

    public InMemoryRowKeyedDatasetRepository(
            final InternalDatasetClient datasetClient,
            final SparkSession sparkSession,
            final DatasetEntityRepository datasetEntityRepository) {

        this.sparkSession = sparkSession;
        this.datasetClient = datasetClient;
        this.datasetEntityRepository = datasetEntityRepository;
    }

    @Override
    public Dataset<Row> readDataFrame(final DatasetTableLookup datasetTableLookup) {
        return readDataFrame(datasetTableLookup.getDatasetName(), datasetTableLookup.getPredicate());
    }

    public String buildPredicate(final long seed) {
        io.vavr.Tuple2<Long, Long> range = rowKeyRange(seed);

        return String.format("%s >= %d and %s <= %d",
                ROW_KEY.name(), range._1,
                ROW_KEY.name(), range._2);
    }

    @Override
    public DatasetTableLookup generateRowKeyAndSaveDataset(
            final long rowKeySeed,
            final Dataset<Row> dataset,
            final StagingDatasetConfig stagingDatasetConfig) {
        String schemaName = stagingDatasetConfig.getStagingSchemaValidation().getPhysicalTableName();

        saveDataFrame(dataset, schemaName, rowKeySeed);

        String rowKeyPredicate = buildPredicate(rowKeySeed);
        long recordsCount = dataset.count();

        Long datasetId = datasetEntityRepository.createStagingDataset(
                recordsCount,
                rowKeySeed,
                rowKeyPredicate,
                stagingDatasetConfig);

        return DatasetTableLookup.builder()
                .datasetId(datasetId)
                .datasetName(schemaName)
                .predicate(rowKeyPredicate)
                .rowKeySeed(rowKeySeed)
                .recordsCount(recordsCount)
                .build();
    }

    @Override
    public DatasetTableLookup generateRowKeyAndSaveDataset(
            final long rowKeySeed,
            final Dataset<Row> dataset,
            final PipelineAppConfig pipelineAppConfig,
            final PipelineStepAppConfig pipelineStepAppConfig) {

        String schemaName =
                pipelineStepAppConfig.getOutputDataset().getStagingSchemaValidation().getPhysicalTableName();

        saveDataFrame(dataset, schemaName, rowKeySeed);

        String rowKeyPredicate = buildPredicate(rowKeySeed);
        long recordsCount = dataset.count();

        Long datasetId = datasetEntityRepository.createPipelineDataset(
                recordsCount, rowKeySeed, rowKeyPredicate, pipelineAppConfig, pipelineStepAppConfig);

        return DatasetTableLookup.builder()
                .datasetId(datasetId)
                .datasetName(schemaName)
                .predicate(rowKeyPredicate)
                .rowKeySeed(rowKeySeed)
                .recordsCount(recordsCount)
                .build();
    }

    public InternalDatasetClient getDatasetClient() {
        return this.datasetClient;
    }

    protected Dataset<Row> saveDataFrame(
            final Dataset<Row> dataFrame, final String schemaName, final long rowKeySeed) {

        Dataset<Row> datasetWithRowKey = addRowKeyField(dataFrame, schemaName, rowKeySeed);

        sparkSession.sqlContext()
                .registerDataFrameAsTable(datasetWithRowKey, schemaName);

        return datasetWithRowKey;
    }

    private Dataset<Row> readDataFrame(final String datasetName, final String predicate) {
        String whereClause = EMPTY;
        if (isNotBlank(predicate)) {
            whereClause = " where " + predicate;
        }
        return sparkSession.sql("select * from " + datasetName + whereClause);
    }

    private Dataset<Row> addRowKeyField(final Dataset<Row> dataFrame, final String tableName, final long rowKeySeed) {

        Tuple2<Long, Long> rowKeyRange = rowKeyRange(rowKeySeed);
        Long startRowKey = rowKeyRange._1;

        Dataset<Row> datasetWithRowKeyColumn = dataFrame
                .withColumn(ROW_KEY.getName(), functions.lit(startRowKey))
                .select(rowKeyRangedSchema(tableName, dataFrame.schema()).getColumns());

        JavaRDD<Row> rdd = datasetWithRowKeyColumn.javaRDD()
                .zipWithIndex()
                .map(InMemoryRowKeyedDatasetRepository::toRowWithRowKey);

        return sparkSession.createDataFrame(rdd, datasetWithRowKeyColumn.schema());
    }

    private static Row toRowWithRowKey(final scala.Tuple2<Row, Long> entry) {
        Seq<Object> values = entry._1.toSeq();
        int rowValueSize = values.size();

        Object[] valueArray = new Object[rowValueSize];
        values.copyToArray(valueArray);

        valueArray[0] = ((Long) valueArray[0]) + entry._2;

        return RowFactory.create(valueArray);
    }

    private static Tuple2<Long, Long> rowKeyRange(final long seed) {
        return PhoenixTableRowKey.of(seed).getRowKeyRange();
    }
}
