package com.lombardrisk.ignis.design.server.pipeline.model;

import com.lombardrisk.ignis.data.common.Identifiable;
import com.lombardrisk.ignis.pipeline.TransformationPipeline;
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
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import java.util.Set;

@Data
@Entity
@Table(name = "PIPELINE")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Pipeline implements Identifiable, TransformationPipeline<PipelineStep> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Long id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "PRODUCT_ID")
    private Long productId;

    @OrderBy("id ASC")
    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "PIPELINE_ID", nullable = false, updatable = false, insertable = false)
    private Set<PipelineStep> steps;
}
