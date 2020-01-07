package com.lombardrisk.ignis.design.server.pipeline.test.model;

import com.lombardrisk.ignis.data.common.Identifiable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "STEP_TEST_ROW_CELL")
public class PipelineStepTestCell implements Identifiable {

    @Id
    @GeneratedValue(
            strategy = GenerationType.AUTO,
            generator = "cellIdGenerator")
    @SequenceGenerator(
            name = "cellIdGenerator",
            sequenceName = "CELL_ID_SEQUENCE")
    @Column(name = "ID")
    private Long id;

    @Column(name = "DATA")
    private String data;

    @Column(name = "FIELD_ID")
    private Long fieldId;
}
