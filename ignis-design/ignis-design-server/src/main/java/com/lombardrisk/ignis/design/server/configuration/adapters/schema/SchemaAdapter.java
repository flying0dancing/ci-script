package com.lombardrisk.ignis.design.server.configuration.adapters.schema;

import com.lombardrisk.ignis.design.server.jpa.SchemaJpaRepository;
import com.lombardrisk.ignis.design.server.productconfig.api.SchemaRepository;
import com.lombardrisk.ignis.design.server.productconfig.schema.Schema;
import io.vavr.control.Option;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public class SchemaAdapter implements SchemaRepository {

    private final SchemaJpaRepository schemaRepository;

    public SchemaAdapter(final SchemaJpaRepository schemaRepository) {
        this.schemaRepository = schemaRepository;
    }

    @Override
    public Optional<Schema> findByIdAndProductId(
            final long productId, final long schemaId) {
        return schemaRepository.findByIdAndProductId(productId, schemaId);
    }

    @Override
    public Optional<Schema> findSecondLatestVersion(final String schemaDisplayName) {
        return schemaRepository.findSecondLatestVersion(schemaDisplayName);
    }

    @Override
    public Optional<Schema> findPreviousVersion(final String displayName, final int majorVersion) {
        return schemaRepository.findPreviousVersion(displayName, majorVersion);
    }

    @Override
    public Optional<Schema> findNextVersion(final String displayName, final int majorVersion) {
        return schemaRepository.findNextVersion(displayName, majorVersion);
    }

    @Override
    public Optional<Schema> findByPhysicalTableNameAndVersion(
            final String physicalTableName,
            final Integer majorVersion) {
        return schemaRepository.findByPhysicalTableNameAndMajorVersion(physicalTableName, majorVersion);
    }

    @Override
    public Optional<Schema> findByDisplayNameAndVersion(final String displayName, final Integer majorVersion) {
        return schemaRepository.findByDisplayNameAndMajorVersion(displayName, majorVersion);
    }

    @Override
    public List<Schema> findAllByIds(final Iterable<Long> ids) {
        return schemaRepository.findAllById(ids);
    }

    @Override
    public Option<Schema> findById(final long id) {
        return Option.ofOptional(schemaRepository.findById(id));
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Schema saveSchema(final Schema schema) {
        return schemaRepository.save(schema);
    }

    @Override
    public List<Schema> findAll() {
        return schemaRepository.findAll();
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Schema deleteSchema(final Schema schema) {
        schemaRepository.delete(schema);
        return schema;
    }

    @Override
    public Optional<Schema> findMaxVersion(final String physicalName) {
        return schemaRepository.findMaxVersionByPhysicalTableName(physicalName);
    }
}
