package com.lombardrisk.ignis.server.product.table;

import com.lombardrisk.ignis.server.product.table.model.Table;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TableRepository extends JpaRepository<Table, Long> {

    /** @deprecated - used for creating a Dataset with single versioned schema (PRODUCT_VERSIONING turned off) */
    @Query("select a from Table a  left join fetch a.fields f where a.displayName = :displayName")
    Table findByDisplayName(
            @Param("displayName") final String displayName);

    @Query("select a.physicalTableName from Table a")
    List<String> getPhysicalTableNames();

    @Modifying
    @Query("update Table schema"
            + "  set schema.startDate = :startDate,"
            + "  schema.endDate = :endDate "
            + "where schema.id = :schemaId")
    int updatePeriodDates(
            @Param("schemaId") final Long schemaId,
            @Param("startDate") final LocalDate startDate,
            @Param("endDate") final LocalDate endDate);

    @Query("select schema from Table schema "
            + "where schema.displayName = :displayName "
            + "and schema.startDate <= :referenceDate "
            + "and ("
            + "  schema.endDate is null "
            + "  or schema.endDate >= :referenceDate"
            + ")"
    )
    Optional<Table> findByDisplayNameAndReferenceDate(
            @Param("displayName") final String displayName,
            @Param("referenceDate") final LocalDate referenceDate);

    @EntityGraph(attributePaths = { "fields", "validationRules" })
    @Query("select t from Table t where t.id in :ids")
    List<Table> findAllByIdsPreFetchingFieldsAndRules(@Param("ids") Iterable<Long> ids);
}
