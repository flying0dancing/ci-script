package com.lombardrisk.ignis.server.dataset.view;

import com.lombardrisk.ignis.client.external.dataset.model.Dataset;
import com.lombardrisk.ignis.common.time.TimeSource;
import com.lombardrisk.ignis.server.dataset.model.DatasetOnly;
import io.vavr.Function1;

public class DatasetConverter implements Function1<DatasetOnly, Dataset> {

    private static final long serialVersionUID = 2563596802662186978L;
    private final transient TimeSource timeSource;

    public DatasetConverter(final TimeSource timeSource) {
        this.timeSource = timeSource;
    }

    @Override
    public Dataset apply(final DatasetOnly dataset) {
        return Dataset.builder()
                .id(dataset.getId())
                .name(dataset.getName())
                .rowKeySeed(dataset.getRowKeySeed())
                .predicate(dataset.getPredicate())
                .recordsCount(dataset.getRecordsCount())
                .createdTime(dataset.getCreatedTime())
                .lastUpdated(dataset.getLastUpdated())
                .table(dataset.getSchemaDisplayName())
                .tableId(dataset.getSchemaId())
                .validationJobId(dataset.getValidationJobId())
                .pipelineJobId(dataset.getPipelineJobId())
                .pipelineInvocationId(dataset.getPipelineInvocationId())
                .pipelineStepInvocationId(dataset.getPipelineStepInvocationId())
                .entityCode(dataset.getEntityCode())
                .localReferenceDate(dataset.getReferenceDate())
                .referenceDate(timeSource.fromLocalDate(dataset.getReferenceDate()))
                .validationStatus(dataset.getValidationStatus())
                .hasRules(dataset.hasRules())
                .runKey(dataset.getRunKey())
                .build();
    }
}
