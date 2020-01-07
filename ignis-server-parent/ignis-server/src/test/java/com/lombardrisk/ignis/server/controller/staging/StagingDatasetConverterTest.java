package com.lombardrisk.ignis.server.controller.staging;

import com.lombardrisk.ignis.api.dataset.DatasetState;
import com.lombardrisk.ignis.client.external.job.staging.StagingItemView;
import com.lombardrisk.ignis.server.job.fixture.JobPopulated;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;

import static com.lombardrisk.ignis.common.fixtures.PopulatedDates.toDateTime;
import static org.assertj.core.api.Assertions.assertThat;

public class StagingDatasetConverterTest {

    private final SoftAssertions soft = new SoftAssertions();
    private final StagingDatasetConverter converter =
            new StagingDatasetConverter("/fcrengine/", () -> "http://localhost:8080");

    @Test
    public void setsId() {
        StagingItemView resource = converter.apply(JobPopulated.stagingDataset()
                .id(1002L)
                .build());

        assertThat(resource.getId())
                .isEqualTo(1002L);
    }

    @Test
    public void setsJobExecutionId() {
        StagingItemView resource = converter.apply(JobPopulated.stagingDataset()
                .serviceRequestId(902782L)
                .build());

        assertThat(resource.getJobExecutionId())
                .isEqualTo(902782L);
    }

    @Test
    public void setsStartTime() {
        StagingItemView resource = converter.apply(JobPopulated.stagingDataset()
                .startTime(toDateTime("2018-01-01T00:00:02"))
                .build());

        assertThat(resource.getStartTime())
                .isEqualTo(toDateTime("2018-01-01T00:00:02"));
    }

    @Test
    public void setsEndTime() {
        StagingItemView resource = converter.apply(JobPopulated.stagingDataset()
                .endTime(toDateTime("2018-01-02T01:00:02"))
                .build());

        assertThat(resource.getEndTime())
                .isEqualTo(toDateTime("2018-01-02T01:00:02"));
    }

    @Test
    public void setsLastUpdateTime() {
        StagingItemView resource = converter.apply(JobPopulated.stagingDataset()
                .lastUpdateTime(toDateTime("2018-01-02T01:10:02"))
                .build());

        assertThat(resource.getLastUpdateTime())
                .isEqualTo(toDateTime("2018-01-02T01:10:02"));
    }

    @Test
    public void setsMessage() {
        StagingItemView resource = converter.apply(JobPopulated.stagingDataset()
                .message("hey there cowboy")
                .build());

        assertThat(resource.getMessage())
                .isEqualTo("hey there cowboy");
    }

    @Test
    public void setsStagingFile() {
        StagingItemView resource = converter.apply(JobPopulated.stagingDataset()
                .stagingFile("htap/ot/elif")
                .build());

        assertThat(resource.getStagingFile())
                .isEqualTo("htap/ot/elif");
    }

    @Test
    public void setsStagingFileCopyLocation() {
        StagingItemView resource = converter.apply(JobPopulated.stagingDataset()
                .stagingFileCopy("hdfs://root/0/vsc.elif")
                .build());

        assertThat(resource.getStagingFileCopyLocation())
                .hasValue("hdfs://root/0/vsc.elif");
    }

    @Test
    public void setsSchemaName() {
        StagingItemView resource = converter.apply(JobPopulated.stagingDataset()
                .table("hardwood finish, sturdy 4 legs")
                .build());

        assertThat(resource.getSchema().getName())
                .isEqualTo("hardwood finish, sturdy 4 legs");
    }

    @Test
    public void setsPhysicalName() {
        StagingItemView resource = converter.apply(JobPopulated.stagingDataset()
                .datasetName("HRDWD4")
                .build());

        assertThat(resource.getSchema().getPhysicalName())
                .isEqualTo("HRDWD4");
    }

    @Test
    public void setsStatus() {
        StagingItemView resource = converter.apply(JobPopulated.stagingDataset()
                .status(DatasetState.UPLOADED)
                .build());

        assertThat(resource.getStatus())
                .isEqualTo(com.lombardrisk.ignis.client.external.job.staging.DatasetState.UPLOADED);
    }

    @Test
    public void handlesAllStatuses() {
        for (final DatasetState state : DatasetState.values()) {
            StagingItemView resource = converter.apply(JobPopulated.stagingDataset()
                    .status(state)
                    .build());

            soft.assertThat(resource.getStatus().name())
                    .isEqualTo(state.name());
        }
        soft.assertAll();
    }

    @Test
    public void setsValidationErrorFile() {
        StagingItemView resource = converter.apply(JobPopulated.stagingDataset()
                .validationErrorFile("hdfs://error")
                .build());

        assertThat(resource.getValidationErrorFile())
                .isEqualTo("hdfs://error");
    }

    @Test
    public void setsValidationErrorFileUrl() {
        StagingDatasetConverter converter = new StagingDatasetConverter("/fcrengine/", () -> "http://localhost:90210");
        StagingItemView resource = converter.apply(JobPopulated.stagingDataset()
                .id(10282L)
                .build());

        assertThat(resource.getValidationErrorFileUrl())
                .isEqualTo("http://localhost:90210/fcrengine/api/v1/stagingItems/10282/validationError");
    }
}
