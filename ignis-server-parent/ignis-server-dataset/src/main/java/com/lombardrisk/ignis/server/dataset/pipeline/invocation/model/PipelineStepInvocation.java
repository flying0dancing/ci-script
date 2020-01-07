package com.lombardrisk.ignis.server.dataset.pipeline.invocation.model;

import com.lombardrisk.ignis.data.common.Identifiable;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineStep;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "PIPELINE_STEP_INVOCATION")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PipelineStepInvocation implements Identifiable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Long id;

    @Column(name = "PIPELINE_STEP_ID")
    private Long pipelineStepId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "PIPELINE_STEP_INVC_DATASET", joinColumns = {
            @JoinColumn(name = "PIPELINE_STEP_INVOCATION_ID", referencedColumnName = "ID") })
    private Set<PipelineStepInvocationDataset> inputDatasets = new LinkedHashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "PIPELINE_STEP_INVC_DATASET", joinColumns = {
            @JoinColumn(name = "PIPELINE_STEP_INVOCATION_ID", referencedColumnName = "ID") })
    @Column(name = "INPUT_PIPELINE_STEP_ID")
    private Set<Long> inputPipelineStepIds = new LinkedHashSet<>();


    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "DATASET", joinColumns = {
            @JoinColumn(name = "PIPELINE_STEP_INVOCATION_ID", referencedColumnName = "ID") })
    @Column(name = "ID")
    private Set<Long> outputDatasetIds  = new LinkedHashSet<>();


    /**
     * Used only for retrieves, to perform the necessary join
     */
    @JoinColumn(name = "PIPELINE_STEP_ID", insertable = false, updatable = false)
    @ManyToOne(fetch = FetchType.EAGER)
    private PipelineStep pipelineStep;

    @Column(name = "STATUS")
    @Enumerated(EnumType.STRING)
    private PipelineStepStatus status;
}
