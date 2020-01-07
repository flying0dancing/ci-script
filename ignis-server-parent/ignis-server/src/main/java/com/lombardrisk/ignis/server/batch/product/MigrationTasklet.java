package com.lombardrisk.ignis.server.batch.product;

import com.lombardrisk.ignis.server.batch.SparkJobExecutor;
import com.lombardrisk.ignis.server.batch.product.datasource.MigrationTableService;
import com.lombardrisk.ignis.server.job.servicerequest.ServiceRequest;
import com.lombardrisk.ignis.server.product.productconfig.ProductConfigService;
import com.lombardrisk.ignis.server.product.productconfig.model.ImportStatus;
import com.lombardrisk.ignis.server.product.productconfig.model.ProductImportContext;
import com.lombardrisk.ignis.server.product.table.TableService;
import com.lombardrisk.ignis.server.product.table.model.Field;
import com.lombardrisk.ignis.server.product.table.model.Table;
import com.lombardrisk.ignis.pipeline.step.api.DrillbackColumnLink;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static com.lombardrisk.ignis.common.json.MapperWrapper.MAPPER;
import static com.lombardrisk.ignis.server.job.servicerequest.ServiceRequest.PRODUCT_CONFIG_ID;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

@Slf4j
public class MigrationTasklet implements StoppableTasklet {

    private final SparkJobExecutor sparkJobExecutor;
    private final TableService tableService;
    private final ProductConfigService productConfigService;
    private final MigrationTableService migrationTableService;

    public MigrationTasklet(
            final SparkJobExecutor sparkJobExecutor,
            final TableService tableService,
            final ProductConfigService productConfigService,
            final MigrationTableService migrationTableService) {
        this.sparkJobExecutor = sparkJobExecutor;
        this.tableService = tableService;
        this.productConfigService = productConfigService;
        this.migrationTableService = migrationTableService;
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext) throws Exception {
        ServiceRequest serviceRequest = sparkJobExecutor.setupRequest(chunkContext);

        ProductImportContext productImportContext = MAPPER.readValue(
                serviceRequest.getRequestMessage(), ProductImportContext.class);

        if (!productImportContext.getNewSchemaNameToId().isEmpty()) {
            createNewPhysicalSchemas(productImportContext.getNewSchemaNameToId());
        }

        addNewPhysicalSchemaFields(productImportContext.getSchemaNameToNewFields());
        addDrillBackColumns(productImportContext.getSchemaNameToDrillBackColumns());

        setSuccessImportStatus(chunkContext);

        log.info(
                "Finished tasklet for job [{}] with id [{}]",
                serviceRequest.getName(),
                serviceRequest.getJobExecutionId());
        return RepeatStatus.FINISHED;
    }

    private void createNewPhysicalSchemas(final Map<String, Long> newSchemaNameToId) throws JobExecutionException {
        List<Table> newSchemas = tableService.findAllByIdsPreFetchingFieldsAndRules(newSchemaNameToId.values());
        List<String> newSchemaNames = newSchemas.stream()
                .map(Table::getPhysicalTableName)
                .collect(toList());

        validateNewSchemasDoNotExist(newSchemaNames);

        for (Table newSchema : newSchemas) {
            migrationTableService.createTable(newSchema);
        }
        log.info("Created physical schemas {}", newSchemaNames);
    }

    private void validateNewSchemasDoNotExist(final List<String> newSchemaNames) throws JobExecutionException {
        List<String> schemasThatShouldNotExist = newSchemaNames.stream()
                .filter(migrationTableService::isPhysicalSchemaPresent)
                .collect(toList());

        if (isNotEmpty(schemasThatShouldNotExist)) {
            String errorMsg =
                    "Cannot setup physical schemas " + schemasThatShouldNotExist + " because they already exist";

            log.error(errorMsg);
            throw new JobExecutionException(errorMsg);
        }
    }

    private void addNewPhysicalSchemaFields(final Map<String, Map<String, Long>> schemaNameToNewFields)
            throws JobExecutionException {
        Set<String> existingPhysicalSchemas = schemaNameToNewFields.keySet();

        validatePhysicalSchemasExist(existingPhysicalSchemas);

        for (Entry<String, Map<String, Long>> newFieldBySchemaName : schemaNameToNewFields.entrySet()) {
            addNewFields(newFieldBySchemaName);
        }
    }

    private void addDrillBackColumns(final Map<String, Set<DrillbackColumnLink>> schemaNameToNewColumns)
            throws JobExecutionException {

        Set<String> existingPhysicalSchemas = schemaNameToNewColumns.keySet();
        validatePhysicalSchemasExist(existingPhysicalSchemas);

        for (Entry<String, Set<DrillbackColumnLink>> newFieldBySchemaName : schemaNameToNewColumns.entrySet()) {

            String physicalSchemaName = newFieldBySchemaName.getKey();
            Set<String> existingColumns = migrationTableService.findPhysicalColumns(physicalSchemaName);

            List<String> newFieldSqlDefinitions = new ArrayList<>();
            for (DrillbackColumnLink columnToAdd : newFieldBySchemaName.getValue()) {

                if (!existingColumns.contains(columnToAdd.toDrillbackColumn())) {
                    newFieldSqlDefinitions.add("\"" + columnToAdd.toDrillbackColumn() + "\""
                            + " "
                            + columnToAdd.getInputColumnSqlType());
                } else {
                    log.info(
                            "Physical table {} already contains drillback column {}",
                            physicalSchemaName,
                            columnToAdd.getInputColumn());
                }
            }

            String newColumnSql = String.join(", ", newFieldSqlDefinitions);

            if (!newColumnSql.isEmpty()) {
                migrationTableService.addColumnsToTable(physicalSchemaName, newFieldSqlDefinitions);
                log.info("Added fields {} to physical schema [{}]", newColumnSql, physicalSchemaName);
            }
        }
    }

    private void validatePhysicalSchemasExist(final Set<String> existingPhysicalSchemas) throws JobExecutionException {
        List<String> nonExistentPhysicalSchemas =
                existingPhysicalSchemas.stream()
                        .map(String::toUpperCase)
                        .filter(migrationTableService::isPhysicalSchemaMissing)
                        .collect(toList());

        if (isNotEmpty(nonExistentPhysicalSchemas)) {
            String errorMsg = "Cannot setup new fields because the following physical schemas do not exist "
                    + nonExistentPhysicalSchemas;

            log.error(errorMsg);
            throw new JobExecutionException(errorMsg);
        }
    }

    private void addNewFields(final Entry<String, Map<String, Long>> newFieldBySchemaName) {
        Collection<Long> newFieldIds = newFieldBySchemaName.getValue().values();
        Set<Field> newFields = newHashSet(tableService.findAllFields(newFieldIds));

        String physicalSchemaName = newFieldBySchemaName.getKey();

        migrationTableService.addColumnsToTable(physicalSchemaName, newFields.stream()
                .map(Field::toColumnDef)
                .collect(toList()));

        Set<String> newFieldNames = newFieldBySchemaName.getValue().keySet();
        log.info("Added fields {} to physical schema [{}]", newFieldNames, physicalSchemaName);
    }

    private static String toColumnsDefSql(final Set<Field> fields) {
        return fields.stream()
                .map(Field::toColumnDef)
                .collect(joining(","));
    }

    private void setSuccessImportStatus(final ChunkContext chunkContext) {
        Long productConfigId = Long.valueOf((
                (String) chunkContext.getStepContext().getJobParameters().get(PRODUCT_CONFIG_ID)));

        productConfigService.updateImportStatus(productConfigId, ImportStatus.SUCCESS);
    }

    @Override
    public void stop() {
        log.warn("Stopping");
    }
}
