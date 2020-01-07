package com.lombardrisk.ignis.server.product.productconfig;

import com.lombardrisk.ignis.server.product.productconfig.model.ImportStatus;
import com.lombardrisk.ignis.server.product.productconfig.model.ProductConfig;
import com.lombardrisk.ignis.server.product.productconfig.model.ProductSchemaPipeline;
import com.lombardrisk.ignis.server.product.productconfig.model.ProductSchemaDetailsOnly;
import com.lombardrisk.ignis.server.product.table.model.Table;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@SuppressWarnings({ "SpringDataRepositoryMethodReturnTypeInspection", "SpringDataRepositoryMethodParametersInspection" })
public interface ProductConfigRepository extends JpaRepository<ProductConfig, Long> {

    Optional<ProductConfig> findByName(final String name);

    @Modifying
    @Query("update ProductConfig pc"
            + " set pc.importStatus = :importStatus"
            + " where pc.id = :id")
    void updateImportStatus(
            @Param("id") final Long id,
            @Param("importStatus") final ImportStatus importStatus);

    Optional<ProductConfig> findFirstByNameOrderByCreatedTimeDesc(final String name);

    boolean existsByNameAndVersion(final String name, final String version);

    int countByTablesId(final Long tableId);

    @Query("select "
            + "    schema.physicalTableName as schemaPhysicalName, "
            + "    schema.version as schemaVersion, "
            + "    prod.name as productName, "
            + "    prod.version as productVersion "
            + "from ProductConfig prod "
            + "left outer join prod.tables schema "
            + "where"
            + "    prod.name <> :notProductName "
            + "and schema.version = :schemaVersion "
            + "and schema.physicalTableName = :schemaPhysicalName")
    List<ProductSchemaDetailsOnly> findSchemasByPhysicalNameNotInProduct(
            @Param("schemaPhysicalName") final String schemaPhysicalName,
            @Param("schemaVersion") final int schemaVersion,
            @Param("notProductName") final String notProductName);

    @Query("select "
            + "    schema.displayName as schemaDisplayName, "
            + "    schema.version as schemaVersion, "
            + "    prod.name as productName, "
            + "    prod.version as productVersion "
            + "from ProductConfig prod "
            + "left outer join prod.tables schema "
            + "where"
            + "    prod.name <> :notProductName "
            + "and schema.version = :schemaVersion "
            + "and schema.displayName = :schemaDisplayName")
    List<ProductSchemaDetailsOnly> findSchemasByDisplayNameNotInProduct(
            @Param("schemaDisplayName") final String schemaDisplayName,
            @Param("schemaVersion") final int schemaVersion,
            @Param("notProductName") final String notProductName);

    @Transactional
    @Modifying
    @Query("update ProductConfig pc "
            + "set pc.importRequestId = :importRequestId "
            + "where pc.id = :id")
    void updateImportRequestId(
            @Param("id") final Long productConfigId,
            @Param("importRequestId") final Long importRequestId);

    @Query("select schemas from ProductConfig pc "
            + "join pc.tables schemas "
            + "where "
            + "    pc.name = :productName "
            + "and schemas.physicalTableName in :schemaPhysicalNames")
    List<Table> findAllPreviousSchemas(
            @Param("productName") final String productName,
            @Param("schemaPhysicalNames") final Set<String> schemaPhysicalNames);

    @Query("select "
            + "    prod.id as id, "
            + "    prod.name as name, "
            + "    prod.version as version, "
            + "    prod.createdTime as createdTime, "
            + "    prod.importStatus as importStatus, "
            + "    prod.importRequestId as importRequestId, "
            + "    schema.id as schemaId, "
            + "    schema.physicalTableName as schemaPhysicalTableName, "
            + "    schema.displayName as schemaDisplayName, "
            + "    schema.version as schemaVersion, "
            + "    schema.createdTime as schemaCreatedTime, "
            + "    schema.startDate as schemaStartDate, "
            + "    schema.endDate as schemaEndDate, "
            + "    schema.createdBy as schemaCreatedBy, "
            + "    schema.hasDatasets as schemaHasDatasets, "
            + "    pipeline.id as pipelineId, "
            + "    pipeline.name as pipelineName "
            + "from ProductConfig prod "
            + "join prod.tables schema "
            + "join prod.pipelines pipeline ")
    List<ProductSchemaPipeline> findAllWithoutFieldsRulesAndSteps();
}
