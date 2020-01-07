package com.lombardrisk.ignis.server.product.pipeline.model;

import com.lombardrisk.ignis.common.MapperUtils;
import com.lombardrisk.ignis.data.common.Identifiable;
import com.lombardrisk.ignis.pipeline.TransformationStep;
import com.lombardrisk.ignis.server.product.pipeline.details.SchemaDetails;
import com.lombardrisk.ignis.server.product.pipeline.select.PipelineFilter;
import com.lombardrisk.ignis.server.product.pipeline.select.Select;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@ToString(exclude = { "selects", "pipelineFilters" })
@EqualsAndHashCode(exclude = { "selects", "pipelineFilters" })
@Entity
@Table(name = "PIPELINE_STEP")
@AllArgsConstructor
@NoArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "TYPE")
public abstract class PipelineStep implements Identifiable, TransformationStep<SchemaDetails> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "stepIdGenerator")
    @SequenceGenerator(name = "stepIdGenerator", sequenceName = "STEP_ID_SEQUENCE", allocationSize = 30)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PIPELINE_ID", nullable = false)
    private Pipeline pipeline;

    @Column(name = "NAME")
    private String name;

    @Column(name = "DESCRIPTION")
    private String description;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true, mappedBy = "pipelineStep")
    @OrderBy("CALC_ORDER")
    private Set<Select> selects = new LinkedHashSet<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true, mappedBy = "pipelineStep")
    private Set<PipelineFilter> pipelineFilters = new LinkedHashSet<>();

    @Column(name = "TYPE", insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private TransformationType type;

    public Set<String> getFilters() {
        return MapperUtils.mapOrEmptySet(pipelineFilters, PipelineFilter::getFilter);
    }
}
