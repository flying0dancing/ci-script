package com.lombardrisk.ignis.server.product.productconfig.model;

import java.time.LocalDate;
import java.util.Date;

public interface ProductSchemaPipeline {

    Long getId();

    String getName();

    String getVersion();

    Date getCreatedTime();

    String getImportStatus();

    Long getSchemaId();

    String getSchemaPhysicalTableName();

    String getSchemaDisplayName();

    Integer getSchemaVersion();

    Date getSchemaCreatedTime();

    LocalDate getSchemaStartDate();

    LocalDate getSchemaEndDate();

    String getSchemaCreatedBy();

    Boolean getSchemaHasDatasets();

    Long getPipelineId();

    String getPipelineName();
}
