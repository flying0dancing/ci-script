package com.lombardrisk.ignis.design.server.pipeline.model.select;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
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
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Long id;

    @Lob
    @Column(name = "SELECTS")
    private String select;

    @Column(name = "OUTPUT_FIELD_ID")
    private Long outputFieldId;

    @Column(name = "IS_INTERMEDIATE")
    private boolean isIntermediate;

    @Column(name = "CALC_ORDER")
    private Long order;

    @Column(name = "IS_WINDOW")
    private boolean isWindow;

    @Column(name = "IS_UNION")
    private boolean isUnion;

    private Window window;

    private Union union;

    public Long unionSchemaId() {
        return union == null ? null : union.getUnionSchemaId();
    }

    public Select copy() {
        return Select.builder()
                .select(select)
                .outputFieldId(outputFieldId)
                .order(order)
                .isIntermediate(isIntermediate)
                .isWindow(isWindow)
                .isUnion(isUnion)
                .union(union)
                .build();
    }

    public static Comparator<Select> selectOrder() {
        return Comparator.comparing(sel -> sel.getOrder() == null ? sel.getOutputFieldId() : sel.getOrder());
    }

}
