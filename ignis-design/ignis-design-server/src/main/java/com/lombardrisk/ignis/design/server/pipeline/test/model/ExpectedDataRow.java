package com.lombardrisk.ignis.design.server.pipeline.test.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.Set;

@Entity
@DiscriminatorValue(value = "EXPECTED")
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
public class ExpectedDataRow extends PipelineStepTestRow {

    @Column(name = "STATUS")
    @Enumerated(EnumType.STRING)
    private Status status;

    public enum Status {
        MATCHED,
        NOT_FOUND,
        UNEXPECTED
    }

    @Builder
    public ExpectedDataRow(
            final Long id,
            final Long pipelineStepTestId,
            final Long schemaId,
            final boolean isRun,
            final Status status,
            final Set<PipelineStepTestCell> cells) {
        super(id, pipelineStepTestId, schemaId, isRun, Type.EXPECTED, cells);
        this.status = status;
    }

}
