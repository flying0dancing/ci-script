package com.lombardrisk.ignis.design.server.jpa;

import com.lombardrisk.ignis.design.server.productconfig.schema.Schema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SchemaJpaRepository extends JpaRepository<Schema, Long> {

    @Query("select distinct s from ProductConfig p left join p.tables s where p.id=:productId and s.id=:schemaId")
    Optional<Schema> findByIdAndProductId(@Param("productId") long productId, @Param("schemaId") long schemaId);

    @Query("select distinct s from Schema s where s.displayName = :schemaDisplayName and s.majorVersion = "
            + "(select max(s.majorVersion) from Schema s where s.displayName= :schemaDisplayName and latest = false)")
    Optional<Schema> findSecondLatestVersion(@Param("schemaDisplayName") String schemaDisplayName);

    @Query("select distinct s from Schema s where s.displayName = :schemaDisplayName and s.majorVersion = "
            + "(select max(s.majorVersion) from Schema s where s.displayName= :schemaDisplayName "
            + "and s.majorVersion < :majorVersion)")
    Optional<Schema> findPreviousVersion(
            @Param("schemaDisplayName") final String displayName,
            @Param("majorVersion") final int majorVersion);

    @Query("select distinct s from Schema s where s.displayName = :schemaDisplayName and s.majorVersion = "
            + "(select min(s.majorVersion) from Schema s where s.displayName= :schemaDisplayName "
            + "and s.majorVersion > :majorVersion)")
    Optional<Schema> findNextVersion(
            @Param("schemaDisplayName") final String displayName,
            @Param("majorVersion") final int majorVersion);

    Optional<Schema> findByPhysicalTableNameAndMajorVersion(String physicalTableName, Integer majorVersion);

    Optional<Schema> findByDisplayNameAndMajorVersion(String displayName, Integer majorVersion);

    @Query("select distinct s from Schema s where s.physicalTableName = :physicalTableName and s.majorVersion = "
            + "(select max(s.majorVersion) from Schema s where s.physicalTableName= :physicalTableName)")
    Optional<Schema> findMaxVersionByPhysicalTableName(@Param("physicalTableName") String physicalTableName);
}
