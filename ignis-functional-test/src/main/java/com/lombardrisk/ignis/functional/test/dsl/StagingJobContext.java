package com.lombardrisk.ignis.functional.test.dsl;

import com.lombardrisk.ignis.client.external.dataset.model.Dataset;
import com.lombardrisk.ignis.client.external.job.staging.StagingItemView;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("ALL")
@Getter
@Builder
public class StagingJobContext {
    private final Long jobId;
    private final List<StagingItemView> stagingDatasets;
    private final List<Dataset> stagedDatasets;
    private boolean success;

    StagingItemView findStagingItemBySchemaName(final String schemaName) {
        Optional<StagingItemView> stagingItemView = stagingDatasets.stream()
                .filter(item -> item.getSchema().getPhysicalName().startsWith(schemaName))
                .findFirst();

        assertThat(stagingItemView)
                .describedAs("Staging dataset with schema name [%s] not found", schemaName)
                .isPresent();

        return stagingItemView.get();
    }

    Dataset findStagedDatasetByName(final String datasetName) {
        Optional<Dataset> stagedDataset = stagedDatasets.stream()
                .filter(dataset -> dataset.getName().startsWith(datasetName))
                .findFirst();

        assertThat(stagedDataset)
                .describedAs("Dataset with name [%s] not staged", datasetName)
                .isPresent();

        return stagedDataset.get();
    }
}
