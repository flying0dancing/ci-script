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
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.util.Comparator;

@Data
@Entity
@Table(name = "PIPELINE_STEP_SELECT")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Select {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "selectIdGenerator")
    @SequenceGenerator(name = "selectIdGenerator", sequenceName = "STEP_SELECT_ID_SEQUENCE")
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PIPELINE_STEP_ID", nullable = false)
    private PipelineStep pipelineStep;

    @Lob
    @Column(name = "SELECTS")
    private String select;

    @Column(name = "OUTPUT_FIELD_ID")
    private Long outputFieldId;

    @Column(name = "CALC_ORDER")
    private Long order;

    @Column(name = "IS_INTERMEDIATE")
    private boolean isIntermediateResult;

    @Column(name = "IS_WINDOW")
    private boolean isWindow;

    @Column(name = "IS_UNION")
    private boolean isUnion;

    private Window window;
    private Union selectUnion;

    public static Comparator<Select> sortOrder() {
        return Comparator.comparing(select -> select.getOrder() == null
                ? select.getOutputFieldId()
                : select.getOrder());
    }
}
