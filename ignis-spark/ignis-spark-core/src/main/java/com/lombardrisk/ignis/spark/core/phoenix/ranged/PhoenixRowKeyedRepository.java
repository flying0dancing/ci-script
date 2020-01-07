package com.lombardrisk.ignis.spark.core.phoenix.ranged;

import com.lombardrisk.ignis.spark.api.DatasetTableLookup;
import com.lombardrisk.ignis.spark.api.JobRequest;
import com.lombardrisk.ignis.spark.api.pipeline.PipelineAppConfig;
import com.lombardrisk.ignis.spark.api.pipeline.PipelineStepAppConfig;
import com.lombardrisk.ignis.spark.api.staging.StagingDatasetConfig;
import com.lombardrisk.ignis.spark.core.JobOperatorException;
import com.lombardrisk.ignis.spark.core.repository.DatasetRepository;
import com.lombardrisk.ignis.spark.core.repository.RowKeyedDatasetRepository;
import com.lombardrisk.ignis.spark.core.schema.DatasetTableSchema;
import com.lombardrisk.ignis.spark.core.server.DatasetEntityRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

@Slf4j
public class PhoenixRowKeyedRepository implements RowKeyedDatasetRepository {

    private final DatasetRepository datasetRepository;
    private final SparkSession sparkSession;
    private final JobRequest jobRequest;
    private final DatasetEntityRepository datasetEntityRepository;

    public PhoenixRowKeyedRepository(
            final SparkSession sparkSession,
            final JobRequest jobRequest,
            final DatasetRepository datasetRepository,
            final DatasetEntityRepository datasetEntityRepository) {

        this.jobRequest = jobRequest;
        this.datasetRepository = datasetRepository;
        this.sparkSession = sparkSession;
        this.datasetEntityRepository = datasetEntityRepository;
    }

    @Override
    public Dataset<Row> readDataFrame(final DatasetTableLookup datasetTableLookup) {
        log.debug("Reading dataframe for " + datasetTableLookup);
        return datasetRepository.readDataFrame(datasetTableLookup);
    }

    @Override
    public DatasetTableLookup generateRowKeyAndSaveDataset(
            final long rowKeySeed,
            final Dataset<Row> datasetWithoutRowKeys,
            final StagingDatasetConfig config) {

        if (config.getAppendToDataset() != null) {
            return appendAndSaveToDataset(config.getAppendToDataset(), datasetWithoutRowKeys, config);
        }

        return saveNewDataset(rowKeySeed, datasetWithoutRowKeys, config);
    }

    @Override
    public DatasetTableLookup generateRowKeyAndSaveDataset(
            final long rowKeySeed,
            final Dataset<Row> datasetWithoutRowKeys,
            final PipelineAppConfig pipelineAppConfig,
            final PipelineStepAppConfig pipelineStepAppConfig) {

        StagingDatasetConfig datasetConfig = pipelineStepAppConfig.getOutputDataset();

        Dataset<Row> datasetWithRowKeys = new PhoenixRowKeyDataFrame(sparkSession, datasetWithoutRowKeys)
                .withRowKey(rowKeySeed);

        String rowKeyPredicate = createRowKeyPredicate(rowKeySeed);

        Long recordsCount = saveRowKeyedDataset(datasetWithRowKeys, datasetConfig);

        Long datasetId = datasetEntityRepository.createPipelineDataset(
                recordsCount, rowKeySeed, rowKeyPredicate, pipelineAppConfig, pipelineStepAppConfig);

        return DatasetTableLookup.builder()
                .datasetId(datasetId)
                .datasetName(datasetConfig.getStagingSchemaValidation().getPhysicalTableName())
                .predicate(rowKeyPredicate)
                .rowKeySeed(rowKeySeed)
                .recordsCount(recordsCount)
                .build();
    }

    private DatasetTableLookup saveNewDataset(
            final long rowKeySeed,
            final Dataset<Row> dataset,
            final StagingDatasetConfig config) {

        Dataset<Row> datasetWithRowKeys = new PhoenixRowKeyDataFrame(sparkSession, dataset)
                .withRowKey(rowKeySeed);

        String rowKeyPredicate = createRowKeyPredicate(rowKeySeed);

        Long recordsCount = saveRowKeyedDataset(datasetWithRowKeys, config);

        Long datasetId = datasetEntityRepository.createStagingDataset(
                recordsCount, rowKeySeed, rowKeyPredicate, config);

        return DatasetTableLookup.builder()
                .datasetId(datasetId)
                .datasetName(config.getStagingSchemaValidation().getPhysicalTableName())
                .predicate(rowKeyPredicate)
                .rowKeySeed(rowKeySeed)
                .recordsCount(recordsCount)
                .build();
    }

    private DatasetTableLookup appendAndSaveToDataset(
            final DatasetTableLookup datasetTableLookup,
            final Dataset<Row> datasetWithoutRowKeys,
            final StagingDatasetConfig config) {

        long offset = datasetTableLookup.getRecordsCount();

        Dataset<Row> datasetWithRowKeys = new PhoenixRowKeyDataFrame(sparkSession, datasetWithoutRowKeys)
                .withRowKeyStartingFrom(offset, datasetTableLookup.getRowKeySeed());

        Long appendedDatasetRecordCount = saveRowKeyedDataset(datasetWithRowKeys, config);

        Long datasetId = datasetTableLookup.getDatasetId();
        long stagingDatasetId = config.getId();
        long updatedRecordsCount = appendedDatasetRecordCount + offset;

        datasetEntityRepository.updateStagingDataset(datasetId, stagingDatasetId, updatedRecordsCount);

        return datasetTableLookup.copy()
                .recordsCount(updatedRecordsCount)
                .build();
    }

    private Long saveRowKeyedDataset(
            final Dataset<Row> datasetWithRowKeys,
            final StagingDatasetConfig datasetConfig) {

        String schemaName = datasetConfig.getStagingSchemaValidation().getPhysicalTableName();
        long recordsCount = datasetWithRowKeys.count();
        logDataset(datasetConfig, schemaName, recordsCount);

        DatasetTableSchema phoenixTableSchema =
                DatasetTableSchema.rowKeyRangedSchema(schemaName, datasetWithRowKeys.schema());

        datasetRepository.writeDataFrame(datasetWithRowKeys, phoenixTableSchema)
                .getOrElseThrow(errorMessage -> new JobOperatorException(errorMessage.getMessage()));

        return recordsCount;
    }

    private void logDataset(final StagingDatasetConfig config, final String schemaName, final long recordsCount) {
        log.info(
                "Save [{}] records in dataset with physical schema [{}] entity code [{}] reference date [{}] job [{}]",
                recordsCount,
                schemaName,
                config.getDatasetProperties().getEntityCode(),
                config.getDatasetProperties().getReferenceDate(),
                jobRequest.getServiceRequestId());
    }

    private String createRowKeyPredicate(final long rowKeySeed) {
        return PhoenixTableRowKey.of(rowKeySeed).toPredicate();
    }
}