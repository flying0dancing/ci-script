package com.lombardrisk.ignis.design.server.pipeline.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "PIPELINE_STEP_SCRIPTLET_INPUT")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScriptletInput {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Long id;

    @Column(name = "INPUT_NAME")
    private String inputName;

    @Column(name = "SCHEMA_IN_ID")
    private Long schemaInId;

}