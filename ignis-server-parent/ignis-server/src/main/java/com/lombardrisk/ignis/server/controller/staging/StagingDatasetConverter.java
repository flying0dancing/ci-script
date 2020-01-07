package com.lombardrisk.ignis.server.controller.staging;

import com.lombardrisk.ignis.client.external.job.staging.DatasetState;
import com.lombardrisk.ignis.client.external.job.staging.StagingItemView;
import com.lombardrisk.ignis.client.external.job.staging.StagingSchemaView;
import com.lombardrisk.ignis.client.external.path.api;
import com.lombardrisk.ignis.server.job.staging.model.StagingDataset;
import io.vavr.Function0;
import io.vavr.Function1;

public class StagingDatasetConverter implements Function1<StagingDataset, StagingItemView> {

    private static final long serialVersionUID = -5349176082464062965L;

    private final String contextPath;
    private final Function0<String> incomingRequestUriSupplier;

    public StagingDatasetConverter(
            final String contextPath,
            final Function0<String> incomingRequestUriSupplier) {
        this.contextPath = contextPath;
        this.incomingRequestUriSupplier = incomingRequestUriSupplier;
    }

    @Override
    public StagingItemView apply(final StagingDataset stagingDataset) {
        Long id = stagingDataset.getId();
        return StagingItemView.builder()
                .id(id)
                .schema(StagingSchemaView.builder()
                        .name(stagingDataset.getTable())
                        .physicalName(stagingDataset.getDatasetName())
                        .build())
                .jobExecutionId(stagingDataset.getServiceRequestId())
                .status(DatasetState.valueOf(stagingDataset.getStatus().name()))
                .startTime(stagingDataset.getStartTime())
                .endTime(stagingDataset.getEndTime())
                .lastUpdateTime(stagingDataset.getLastUpdateTime())
                .message(stagingDataset.getMessage())
                .stagingFile(stagingDataset.getStagingFile())
                .stagingFileCopyLocation(stagingDataset.getStagingFileCopy())
                .validationErrorFile(stagingDataset.getValidationErrorFile())
                .validationErrorFileUrl(getStagingErrorFileDownloadUrl(id))
                .datasetId(stagingDataset.getDatasetId())
                .build();
    }

    private String getStagingErrorFileDownloadUrl(final Long datasetId) {
        return incomingRequestUriSupplier.get()
                + this.contextPath
                + api.external.v1.staging.byID.ValidationErrorFile
                .replace("{" + api.Params.ID + "}", String.valueOf(datasetId));
    }
}
