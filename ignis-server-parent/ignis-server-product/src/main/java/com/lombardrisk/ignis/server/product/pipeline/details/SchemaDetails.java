package com.lombardrisk.ignis.server.product.pipeline.details;

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

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Entity
@Table(name = "DATASET_SCHEMA")
public class SchemaDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Long id;

    @Column(name = "DISPLAY_NAME")
    private String displayName;

    @Column(name = "PHYSICAL_TABLE_NAME")
    private String physicalTableName;

    @Column(name = "VERSION")
    private Integer version;
}
