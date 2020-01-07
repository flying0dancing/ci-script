package com.lombardrisk.ignis.design.server.pipeline.test.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.Set;

@Entity
@DiscriminatorValue(value = "INPUT")
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
public class InputDataRow extends PipelineStepTestRow {

    @Builder
    public InputDataRow(
            final Long id,
            final Long pipelineStepTestId,
            final Long schemaId,
            final boolean isRun,
            final Set<PipelineStepTestCell> cells) {
        super(id, pipelineStepTestId, schemaId, isRun, Type.INPUT, cells);
    }
}
