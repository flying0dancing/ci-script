package com.lombardrisk.ignis.server.product.pipeline.model;

import com.lombardrisk.ignis.data.common.Identifiable;
import com.lombardrisk.ignis.pipeline.TransformationPipeline;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.validation.Valid;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@ToString(exclude = "steps")
@EqualsAndHashCode(exclude = "steps")
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

    @Valid
    @OrderBy("id ASC")
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true, mappedBy = "pipeline")
    private Set<PipelineStep> steps = new LinkedHashSet<>();
}
