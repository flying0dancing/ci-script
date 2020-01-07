package com.lombardrisk.ignis.server.dataset.pipeline.invocation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Table;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
@Table(name = "PIPELINE_STEP_INVC_DATASET")
public class PipelineStepInvocationDataset {

    @Column(name = "DATASET_ID")
    private Long datasetId;

    @Column(name = "DATASET_RUN_KEY")
    private Long datasetRunKey;
}


