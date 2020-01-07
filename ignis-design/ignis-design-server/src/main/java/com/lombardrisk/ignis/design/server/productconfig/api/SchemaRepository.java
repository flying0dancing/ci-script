package com.lombardrisk.ignis.design.server.productconfig.api;

import com.lombardrisk.ignis.design.server.productconfig.schema.Schema;
import io.vavr.control.Option;

import java.util.List;
import java.util.Optional;

public interface SchemaRepository {

    Optional<Schema> findByIdAndProductId(long productId, long schemaId);

    Optional<Schema> findSecondLatestVersion(String schemaDisplayName);

    Optional<Schema> findPreviousVersion(final String displayName, final int majorVersion);

    Optional<Schema> findNextVersion(final String displayName, final int majorVersion);

    Optional<Schema> findByPhysicalTableNameAndVersion(String physicalTableName, final Integer majorVersion);

    Optional<Schema> findByDisplayNameAndVersion(String displayName, final Integer majorVersion);

    List<Schema> findAllByIds(Iterable<Long> ids);

    Option<Schema> findById(long id);

    Schema saveSchema(Schema schema);

    List<Schema> findAll();

    Schema deleteSchema(Schema schema);

    Optional<Schema> findMaxVersion(final String physicalName);
}
