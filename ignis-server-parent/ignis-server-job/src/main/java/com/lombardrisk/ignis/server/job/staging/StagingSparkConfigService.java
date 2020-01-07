package com.lombardrisk.ignis.server.job.staging;

import com.lombardrisk.ignis.common.MapperUtils;
import com.lombardrisk.ignis.server.dataset.model.Dataset;
import com.lombardrisk.ignis.server.job.staging.file.DataSourceService;
import com.lombardrisk.ignis.server.job.staging.file.ErrorFileService;
import com.lombardrisk.ignis.server.job.staging.model.FieldConverter;
import com.lombardrisk.ignis.server.job.staging.model.StagingDataset;
import com.lombardrisk.ignis.server.job.staging.model.StagingDatasetInstruction;
import com.lombardrisk.ignis.server.product.table.model.Table;
import com.lombardrisk.ignis.spark.api.DatasetTableLookup;
import com.lombardrisk.ignis.spark.api.staging.DatasetProperties;
import com.lombardrisk.ignis.spark.api.staging.StagingDatasetConfig;
import com.lombardrisk.ignis.spark.api.staging.StagingSchemaValidation;
import com.lombardrisk.ignis.spark.api.staging.datasource.DataSource;

import java.util.LinkedHashSet;

public class StagingSparkConfigService {

    private final FieldConverter fieldConverter = new FieldConverter();
    private final DataSourceService dataSourceService;
    private final ErrorFileService errorFileService;

    public StagingSparkConfigService(
            final DataSourceService dataSourceService,
            final ErrorFileService errorFileService) {
        this.dataSourceService = dataSourceService;
        this.errorFileService = errorFileService;
    }

    public StagingDatasetConfig createDatasetAppConfig(
            final StagingDataset stagingDataset,
            final StagingDatasetInstruction datasetInstruction) {

        Table schema = datasetInstruction.getSchema();

        boolean header = datasetInstruction.isHeader();

        DataSource sparkDataSource = dataSourceService.createSparkDataSource(stagingDataset, header);

        StagingDatasetConfig.StagingDatasetConfigBuilder stagingConfig = StagingDatasetConfig.builder()
                .id(stagingDataset.getId())
                .stagingSchemaValidation(StagingSchemaValidation.builder()
                        .schemaId(schema.getId())
                        .displayName(schema.getDisplayName())
                        .physicalTableName(schema.getPhysicalTableName())
                        .fields(MapperUtils.mapCollection(schema.getFields(), fieldConverter, LinkedHashSet::new))
                        .build())
                .source(sparkDataSource)
                .datasetProperties(DatasetProperties.builder()
                        .referenceDate(datasetInstruction.getReferenceDate())
                        .entityCode(datasetInstruction.getEntityCode())
                        .build())
                .stagingErrorOutput(errorFileService.errorOutput(stagingDataset))
                .autoValidate(datasetInstruction.isAutoValidate());

        if (datasetInstruction.getAppendToDataset() != null) {
            Dataset appendToDataset = datasetInstruction.getAppendToDataset();
            stagingConfig.appendToDataset(DatasetTableLookup.builder()
                    .datasetId(appendToDataset.getId())
                    .datasetName(appendToDataset.getName())
                    .predicate(appendToDataset.getPredicate())
                    .rowKeySeed(appendToDataset.getRowKeySeed())
                    .recordsCount(appendToDataset.getRecordsCount())
                    .build());
        }

        return stagingConfig.build();
    }
}
