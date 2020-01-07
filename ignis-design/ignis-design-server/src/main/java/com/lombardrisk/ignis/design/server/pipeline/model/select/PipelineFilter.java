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

    @Lob
    @Column(name = "FILTER")
    private String filter;

    @Column(name = "UNION_INPUT_SCHEMA_ID")
    private Long unionSchemaId;
}
