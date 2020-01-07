package com.lombardrisk.ignis.design.server.pipeline.model;

import com.lombardrisk.ignis.client.external.pipeline.export.TransformationType;
import com.lombardrisk.ignis.common.MapperUtils;
import com.lombardrisk.ignis.data.common.Identifiable;
import com.lombardrisk.ignis.design.server.pipeline.model.select.PipelineFilter;
import com.lombardrisk.ignis.design.server.pipeline.model.select.Select;
import com.lombardrisk.ignis.pipeline.TransformationStep;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "PIPELINE_STEP")
@AllArgsConstructor
@NoArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "TYPE")
public abstract class PipelineStep implements Identifiable, TransformationStep<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Long id;

    @Column(name = "PIPELINE_ID")
    private Long pipelineId;

    @Column(name = "NAME")
    private String name;

    @Column(name = "DESCRIPTION")
    private String description;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "PIPELINE_STEP_ID", nullable = false)
    @OrderBy("IS_INTERMEDIATE DESC, CALC_ORDER ASC NULLS LAST, OUTPUT_FIELD_ID ASC")
    private Set<Select> selects = new LinkedHashSet<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "PIPELINE_STEP_ID", nullable = false)
    private Set<PipelineFilter> pipelineFilters
            = new LinkedHashSet<>();

    @Column(name = "TYPE", insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private TransformationType type;

    public Set<String> getFilters() {
        return MapperUtils.mapSet(pipelineFilters, PipelineFilter::getFilter);
    }
}
