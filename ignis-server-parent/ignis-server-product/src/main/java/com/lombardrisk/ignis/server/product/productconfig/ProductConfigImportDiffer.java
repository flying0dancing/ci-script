package com.lombardrisk.ignis.server.product.productconfig;

import com.lombardrisk.ignis.client.external.productconfig.export.SchemaPeriod;
import com.lombardrisk.ignis.server.product.productconfig.model.ProductConfig;
import com.lombardrisk.ignis.server.product.productconfig.model.ProductImportDiff;
import com.lombardrisk.ignis.server.product.table.model.Table;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

public class ProductConfigImportDiffer {

    private final ProductConfigRepository productConfigRepository;

    public ProductConfigImportDiffer(final ProductConfigRepository productConfigRepository) {
        this.productConfigRepository = productConfigRepository;
    }

    public ProductImportDiff calculateDiff(final ProductConfig newProductConfig) {
        Set<String> schemaNames = extractSchemaNames(newProductConfig);

        if (schemaNames.isEmpty()) return null;

        List<Table> existingSchemas =
                productConfigRepository.findAllPreviousSchemas(newProductConfig.getName(), schemaNames);

        SchemaTypes schemaTypes;
        Map<Table, SchemaPeriod> newSchemaPeriods = new HashMap<>();

        if (isNotEmpty(existingSchemas)) {
            schemaTypes = findAllSchemaTypes(existingSchemas, newProductConfig);

            newSchemaPeriods = findNewSchemaPeriods(schemaTypes.existingSchemas, newProductConfig.getTables());
        } else {
            schemaTypes = findNewSchemaTypesOnly(newProductConfig);
        }
        return ProductImportDiff.builder()
                .productName(newProductConfig.getName())
                .productVersion(newProductConfig.getVersion())
                .newSchemas(schemaTypes.newSchemas)
                .existingSchemas(schemaTypes.existingSchemas)
                .newVersionedSchemas(schemaTypes.newVersionedSchemas)
                .existingSchemaToNewPeriod(newSchemaPeriods)
                .build();
    }

    private static Set<String> extractSchemaNames(final ProductConfig newProductConfig) {
        return newProductConfig.getTables()
                .stream()
                .map(Table::getPhysicalTableName)
                .collect(toSet());
    }

    private static SchemaTypes findAllSchemaTypes(
            final Collection<Table> existingTables,
            final ProductConfig newProductConfig) {
        SchemaTypes schemaTypes = new SchemaTypes();
        schemaTypes.existingSchemas.addAll(existingTables);

        Map<String, Table> previousSchemasByVersionedName =
                createPreviousTableLookup(existingTables);

        Set<Table> newSchemasAndNewVersionedSchemas =
                findAllNewSchemas(previousSchemasByVersionedName, newProductConfig);

        schemaTypes.newSchemas.addAll(
                findNewSchemasOnly(schemaTypes.existingSchemas, newSchemasAndNewVersionedSchemas));

        schemaTypes.newVersionedSchemas.addAll(
                findNewVersionsOfSchemasOnly(newSchemasAndNewVersionedSchemas, schemaTypes.newSchemas));

        return schemaTypes;
    }

    private static Map<String, Table> createPreviousTableLookup(final Collection<Table> existingTables) {
        return existingTables
                .stream()
                .collect(toMap(Table::getVersionedName, table -> table));
    }

    private static Set<Table> findNewVersionsOfSchemasOnly(
            final Set<Table> newSchemasAndNewVersionedSchemas, final Set<Table> newSchemas) {

        return newSchemasAndNewVersionedSchemas.stream()
                .filter(schema -> existsByVersionedName(newSchemas, schema))
                .collect(toSet());
    }

    private static boolean existsByVersionedName(final Set<Table> newSchemas, final Table schema) {
        return newSchemas.stream()
                .noneMatch(newSchema -> newSchema.isSameVersionAs(schema));
    }

    private static Map<Table, SchemaPeriod> findNewSchemaPeriods(
            final Set<Table> existingSchemas,
            final Set<Table> schemas) {
        HashMap<Table, SchemaPeriod> existingSchemaToNewPeriod = new HashMap<>();

        for (Table existingSchema : existingSchemas) {
            Optional<SchemaPeriod> optionalNewPeriod = schemas.stream()
                    .filter(schema -> schema.getVersionedName().equals(existingSchema.getVersionedName()))
                    .map(Table::getSchemaPeriod)
                    .filter(schemaPeriod -> schemaPeriod.isDifferent(existingSchema.getSchemaPeriod()))
                    .findFirst();
            if (optionalNewPeriod.isPresent()) {
                existingSchemaToNewPeriod.put(existingSchema, optionalNewPeriod.get());
            }
        }
        return existingSchemaToNewPeriod;
    }

    private static SchemaTypes findNewSchemaTypesOnly(final ProductConfig newProductConfig) {
        SchemaTypes schemaTypes = new SchemaTypes();

        schemaTypes.newSchemas.addAll(
                findNewSchemasOnly(new HashSet<>(), newProductConfig.getTables()));

        Set<Table> newSchemasAndNewVersionedSchemas = findAllNewSchemas(new HashMap<>(), newProductConfig);

        schemaTypes.newVersionedSchemas.addAll(
                findNewVersionsOfSchemasOnly(newSchemasAndNewVersionedSchemas, schemaTypes.newSchemas));

        return schemaTypes;
    }

    private static Collection<Table> findNewSchemasOnly(
            final Set<Table> existingSchemas,
            final Set<Table> newAndNewVersionSchemas) {
        List<Table> sortedNewSchemas = newAndNewVersionSchemas.stream()
                .sorted(comparing(Table::getVersionedName).reversed())
                .collect(toList());

        Map<String, Table> distinctNewSchemaByNames = new HashMap<>();

        for (Table schema : sortedNewSchemas) {
            if (doesNotContainSchemaWithName(existingSchemas, schema.getName())) {
                distinctNewSchemaByNames.put(schema.getName(), schema);
            }
        }
        return distinctNewSchemaByNames.values();
    }

    private static boolean doesNotContainSchemaWithName(final Set<Table> existingSchemas, final String schemaName) {
        return existingSchemas.stream()
                .noneMatch(existingSchema -> existingSchema.getName().equals(schemaName));
    }

    private static Set<Table> findAllNewSchemas(
            final Map<String, Table> previousSchemasByVersionedName,
            final ProductConfig nextProduct) {

        return nextProduct.getTables().stream()
                .filter(table -> isNewTable(previousSchemasByVersionedName, table))
                .collect(toSet());
    }

    private static boolean isNewTable(
            final Map<String, Table> previousSchemasByVersionedName,
            final Table table) {

        Table previousTable = previousSchemasByVersionedName.get(table.getVersionedName());
        return previousTable == null;
    }

    private static class SchemaTypes {

        private Set<Table> existingSchemas = new HashSet<>();
        private Set<Table> newSchemas = new HashSet<>();
        private Set<Table> newVersionedSchemas = new HashSet<>();
    }
}
