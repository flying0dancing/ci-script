package com.lombardrisk.ignis.server.batch.product;

import com.lombardrisk.ignis.server.batch.product.datasource.MigrationTableService;
import com.lombardrisk.ignis.server.product.productconfig.model.ProductImportContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toMap;

@Slf4j
public class RollbackMigration {

    private final JdbcTemplate phoenixJdbcTemplate;
    private final MigrationTableService migrationTableService;

    public RollbackMigration(
            final MigrationTableService migrationTableService,
            final JdbcTemplate phoenixJdbcTemplate) {
        this.migrationTableService = migrationTableService;
        this.phoenixJdbcTemplate = phoenixJdbcTemplate;
    }

    Optional<String> execute(final ProductImportContext productImportContext) {
        Optional<String> optionalError = rollbackNewSchemas(productImportContext.getNewSchemaNameToId());
        if (optionalError.isPresent()) {
            return optionalError;
        }
        return rollbackNewFields(productImportContext);
    }

    private Optional<String> rollbackNewSchemas(final Map<String, Long> newSchemaNameToId) {
        Set<String> newSchemaNames = newSchemaNameToId.keySet();
        List<String> droppedTableNames = new ArrayList<>();

        for (String physicalSchemaName : newSchemaNames) {
            if (migrationTableService.isPhysicalSchemaPresent(physicalSchemaName)) {
                List<Map<String, Object>> datasetData =
                        phoenixJdbcTemplate.queryForList("select * from " + physicalSchemaName + " limit 1");

                if (datasetData.isEmpty()) {
                    phoenixJdbcTemplate.execute("drop table " + physicalSchemaName);
                    droppedTableNames.add(physicalSchemaName);
                } else {
                    return Optional.of("Cannot remove physical schema '" + physicalSchemaName + "'"
                            + " because it is not empty");
                }
            }
        }
        log.info("Dropped physical schemas {}", droppedTableNames);

        return Optional.empty();
    }

    private Optional<String> rollbackNewFields(final ProductImportContext productImportContext) {
        Map<String, Map<String, Long>> schemaNameToNewFields = findNewFieldsFromExistingSchemas(productImportContext);

        if (schemaNameToNewFields.isEmpty()) {
            return Optional.empty();
        }
        List<String> missingSchemas = new ArrayList<>();
        for (Entry<String, Map<String, Long>> newFieldByName : schemaNameToNewFields.entrySet()) {

            rollbackFields(newFieldByName)
                    .ifPresent(missingSchemas::add);
        }
        String missingSchemasMsg = "Could not rollback fields because the following physical schemas were missing: "
                + String.join(", ", missingSchemas);

        return missingSchemas.isEmpty()
                ? Optional.empty()
                : Optional.of(missingSchemasMsg);
    }

    private static Map<String, Map<String, Long>> findNewFieldsFromExistingSchemas(
            final ProductImportContext productImportContext) {
        Set<String> newPhysicalSchemaNames = productImportContext.getNewSchemaNameToId().keySet();

        return productImportContext.getSchemaNameToNewFields().entrySet()
                .stream()
                .filter(entry -> isNotRollbackedAsNewSchema(newPhysicalSchemaNames, entry))
                .collect(toMap(Entry::getKey, Entry::getValue));
    }

    private static boolean isNotRollbackedAsNewSchema(
            final Set<String> newPhysicalSchemaNames,
            final Entry<String, Map<String, Long>> entry) {
        return newPhysicalSchemaNames
                .stream()
                .noneMatch(newSchemaName -> entry.getKey().equals(newSchemaName));
    }

    private Optional<String> rollbackFields(final Entry<String, Map<String, Long>> newFieldByName) {
        String physicalSchemaName = newFieldByName.getKey();

        if (migrationTableService.isPhysicalSchemaMissing(physicalSchemaName)) {
            return Optional.of(physicalSchemaName);
        }
        phoenixJdbcTemplate.execute("ALTER TABLE " + physicalSchemaName + " DROP COLUMN "
                + String.join(",", newFieldByName.getValue().keySet()));
        return Optional.empty();
    }
}
