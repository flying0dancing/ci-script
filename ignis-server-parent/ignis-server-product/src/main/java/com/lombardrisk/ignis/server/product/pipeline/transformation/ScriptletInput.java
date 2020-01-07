package com.lombardrisk.ignis.server.product.pipeline.transformation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lombardrisk.ignis.server.product.pipeline.details.SchemaDetails;
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
import javax.persistence.ManyToOne;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SCHEMA_IN_ID", insertable = false, updatable = false)
    @JsonIgnore
    private SchemaDetails schemaIn;
}
