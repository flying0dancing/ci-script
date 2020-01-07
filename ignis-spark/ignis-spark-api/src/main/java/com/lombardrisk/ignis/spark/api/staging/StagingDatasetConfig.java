package com.lombardrisk.ignis.spark.api.staging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.lombardrisk.ignis.spark.api.DatasetTableLookup;
import com.lombardrisk.ignis.spark.api.staging.datasource.DataSource;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static java.util.Objects.requireNonNull;

@Data
@Builder
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StagingDatasetConfig {

    private long id;
    private DataSource source;
    private DatasetProperties datasetProperties;
    private StagingSchemaValidation stagingSchemaValidation;
    private boolean autoValidate;
    private StagingErrorOutput stagingErrorOutput;
    private DatasetTableLookup appendToDataset;

    @Builder
    public StagingDatasetConfig(
            final long id,
            final DataSource source,
            final DatasetProperties datasetProperties,
            final StagingSchemaValidation stagingSchemaValidation,
            final boolean autoValidate,
            final StagingErrorOutput stagingErrorOutput,
            final DatasetTableLookup appendToDataset) {

        this.id = id;
        this.source = source;
        this.datasetProperties = requireNonNull(datasetProperties, "datasetProperties cannot be null");
        this.stagingSchemaValidation = stagingSchemaValidation;
        this.autoValidate = autoValidate;
        this.stagingErrorOutput = stagingErrorOutput;
        this.appendToDataset = appendToDataset;
    }
}
