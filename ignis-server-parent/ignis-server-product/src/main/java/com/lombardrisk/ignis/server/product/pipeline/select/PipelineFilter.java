package com.lombardrisk.ignis.server.product.pipeline.select;

import com.lombardrisk.ignis.server.product.pipeline.model.PipelineStep;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "PIPELINE_STEP_FILTER")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class PipelineFilter {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PIPELINE_STEP_ID", nullable = false)
    private PipelineStep pipelineStep;

    @Lob
    @Column(name = "FILTER")
    private String filter;

    @Column(name = "UNION_INPUT_SCHEMA_ID")
    private Long unionSchemaId;
}
