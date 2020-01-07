package com.lombardrisk.ignis.spark.staging.execution;

import com.lombardrisk.ignis.spark.api.staging.StagingDatasetConfig;
import com.lombardrisk.ignis.spark.core.repository.RowKeyedDatasetRepository;
import com.lombardrisk.ignis.spark.staging.exception.DatasetStoreException;
import com.lombardrisk.ignis.spark.staging.execution.error.ErrorFileService;
import io.vavr.control.Either;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Slf4j
@Component
public class DatasetRegister implements Serializable {

    private static final long serialVersionUID = 8667442008765594090L;

    private final transient StagingListener stagingListener;
    private final transient RowKeyedDatasetRepository datasetRepository;
    private final ErrorFileService errorFileService;

    public DatasetRegister(
            final StagingListener stagingListener,
            final RowKeyedDatasetRepository datasetRepository,
            final ErrorFileService errorFileService) {
        this.stagingListener = stagingListener;
        this.datasetRepository = datasetRepository;
        this.errorFileService = errorFileService;
    }

    void register(
            final long rowKeySeed,
            final StagingDatasetConfig item,
            final Either<JavaRDD<String>, Dataset<Row>> validateResult) {

        String physicalTableName = item.getStagingSchemaValidation().getPhysicalTableName();

        if (validateResult.isLeft()) {
            stagingListener.onValidationFailed(item.getId());
            errorFileService.saveErrorFile(validateResult.getLeft(), item.getStagingErrorOutput());

            log.info("Finished storing invalid records for dataset for schema [{}]", physicalTableName);
            return;
        }

        stagingListener.onValidationFinished(item.getId());

        storeValidDataset(rowKeySeed, item, validateResult.get());
        log.info("Finished storing valid records for dataset for schema [{}]", physicalTableName);
    }

    private void storeValidDataset(
            final long rowKeySeed,
            final StagingDatasetConfig stagingDatasetConfig,
            final Dataset<Row> validDF) {

        try {
            stagingListener.onRegisterStart(stagingDatasetConfig.getId());

            datasetRepository.generateRowKeyAndSaveDataset(rowKeySeed, validDF, stagingDatasetConfig);

            stagingListener.onRegisterFinished(stagingDatasetConfig.getId());
        } catch (final Exception e) {
            stagingListener.onRegisterFailed(stagingDatasetConfig.getId());
            log.error(e.getMessage(), e);

            throw new DatasetStoreException("An error occurred while registering the dataset", e);
        }
    }
}