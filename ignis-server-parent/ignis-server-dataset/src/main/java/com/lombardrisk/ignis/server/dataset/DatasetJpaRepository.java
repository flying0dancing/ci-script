package com.lombardrisk.ignis.server.dataset;

import com.lombardrisk.ignis.api.dataset.ValidationStatus;
import com.lombardrisk.ignis.server.dataset.model.Dataset;
import com.lombardrisk.ignis.server.dataset.model.DatasetOnly;
import com.lombardrisk.ignis.server.product.table.model.Table;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("squid:S1214")
public interface DatasetJpaRepository extends JpaRepository<Dataset, Long> {

    String FIND_DATASETS_ONLY_SQL = "select "
            + "dataset.id as id,"
            + "dataset.name as name,"
            + "dataset.rowKeySeed as rowKeySeed,"
            + "dataset.predicate as predicate,"
            + "dataset.recordsCount as recordsCount,"
            + "dataset.schema.displayName as schemaDisplayName,"
            + "dataset.schema.id as schemaId,"
            + "dataset.latestValidationJobId as validationJobId,"
            + "dataset.pipelineJobId as pipelineJobId,"
            + "dataset.pipelineInvocationId as pipelineInvocationId,"
            + "dataset.pipelineStepInvocationId as pipelineStepInvocationId,"
            + "dataset.createdTime as createdTime,"
            + "dataset.lastUpdated as lastUpdated,"
            + "dataset.validationStatus as validationStatus,"
            + "dataset.entityCode as entityCode,"
            + "dataset.referenceDate as referenceDate,"
            + "dataset.runKey as runKey,"
            + "case"
            + "  when dataset.schema.validationRules is not empty "
            + "    then TRUE "
            + "  else FALSE "
            + "end as rulesDefined "
            + "from Dataset as dataset";

    String FIND_DATASETS_ONLY_SQL_FILTERED_BY_NAME = FIND_DATASETS_ONLY_SQL
            + " where dataset.name = :name"
            + " and dataset.entityCode = :entityCode"
            + " and dataset.referenceDate = :referenceDate";

    String FIND_DATASETS_ONLY_SQL_FILTERED_BY_DISPLAY_NAME = FIND_DATASETS_ONLY_SQL
            + " where dataset.schema.displayName = :displayName"
            + " and dataset.entityCode = :entityCode"
            + " and dataset.referenceDate = :referenceDate";

    @Query("select d1 from Dataset d1 "
            + "where d1.name=:name "
            + "and d1.entityCode=:entityCode "
            + "and d1.referenceDate=:referenceDate "
            + "and d1.runKey = ("
            + "select max(d2.runKey) from Dataset d2 "
            + "where d2.name=:name and d2.entityCode=:entityCode and d2.referenceDate=:referenceDate)")
    Optional<Dataset> findLatestDataset(
            @Param("name") String table,
            @Param("entityCode") String entityCode,
            @Param("referenceDate") LocalDate referenceDate);

    List<Dataset> findBySchemaId(long id);

    @Query("select d from Dataset d where d.schema in (:tables)")
    List<Dataset> findBySchemas(@Param("tables") final List<Table> tables);

    List<Dataset> findByStagingJobsServiceRequestIdAndValidationStatus(
            long id,
            final ValidationStatus validationStatus);

    @Query(FIND_DATASETS_ONLY_SQL)
    Page<DatasetOnly> findAllDatasetsOnly(final Pageable pageable);

    @Query(FIND_DATASETS_ONLY_SQL_FILTERED_BY_NAME)
    Page<DatasetOnly> findAllDatasetsOnlyByName(
            @Param("name") final String name,
            @Param("entityCode") final String entityCode,
            @Param("referenceDate") final LocalDate referenceDate,
            final Pageable pageable);

    @Query(FIND_DATASETS_ONLY_SQL_FILTERED_BY_DISPLAY_NAME)
    Page<DatasetOnly> findAllDatasetsOnlyByDisplayName(
            @Param("displayName") final String name,
            @Param("entityCode") final String entityCode,
            @Param("referenceDate") final LocalDate referenceDate,
            final Pageable pageable);

    @Query("select d1 from Dataset d1 "
            + "where d1.schema.id=:schemaId "
            + "and d1.entityCode=:entityCode "
            + "and d1.referenceDate=:referenceDate "
            + "and d1.runKey = ("
            + "select max(d2.runKey) from Dataset d2 "
            + "where d2.schema.id=:schemaId and d2.entityCode=:entityCode and d2.referenceDate=:referenceDate)")
    Optional<Dataset> findLatestDatasetForSchemaId(
            @Param("schemaId") long schemaId,
            @Param("entityCode") String entityCode,
            @Param("referenceDate") LocalDate referenceDate);

    Optional<Dataset> findByPipelineInvocationIdAndSchemaId(
            long pipelineInvocationId, long schemaId);

    @Query("select d from Dataset d join fetch d.schema s join fetch s.validationRules where d.id = :datasetId")
    Optional<Dataset> findByIdPrefetchSchemaRules(@Param("datasetId") Long datasetId);

    Optional<Dataset> findByNameAndAndRunKeyAndAndReferenceDateAndEntityCode(
            String name,
            Long runKey,
            LocalDate referenceDate,
            String entityCode);
}
