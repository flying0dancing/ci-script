package com.lombardrisk.ignis.design.field.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "DATASET_SCHEMA_FIELD")
public class DatasetSchemaField {

    @Id
    @Column(name = "ID")
    private Long id;

    @Column(name = "DATASET_SCHEMA_ID")
    private Long datasetSchemaId;

    @Column(name = "FIELD_TYPE")
    private String type;

    @Column(name = "NAME")
    private String name;

    @Column(name = "NULLABLE")
    private Boolean nullable;

    // StringField
    @Column(name = "MAX_LENGTH")
    private Integer maxLength;

    @Column(name = "MIN_LENGTH")
    private Integer minLength;

    @Column(name = "REGULAR_EXPRESSION")
    private String regularExpression;

    // Date/TimestampField
    @Column(name = "DATE_FORMAT")
    private String format;

    // DecimalField
    @Column(name = "DEC_SCALE")
    private Integer scale;

    @Column(name = "DEC_PRECISION")
    private Integer precision;
}
