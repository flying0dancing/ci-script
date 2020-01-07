package com.lombardrisk.ignis.functional.test.steps;

import com.lombardrisk.ignis.client.external.dataset.model.Dataset;
import com.lombardrisk.ignis.client.external.pipeline.view.JoinView;
import com.lombardrisk.ignis.client.external.pipeline.view.PipelineStepView;
import com.lombardrisk.ignis.client.external.pipeline.view.PipelineView;
import com.lombardrisk.ignis.client.external.productconfig.view.ProductConfigView;
import com.lombardrisk.ignis.client.external.productconfig.view.SchemaView;
import com.lombardrisk.ignis.client.external.rule.ValidationRuleSummaryView;
import com.lombardrisk.ignis.fs.FileSystemTemplate;
import com.lombardrisk.ignis.functional.test.config.data.IgnisServer;
import com.lombardrisk.ignis.functional.test.config.data.Phoenix;
import com.lombardrisk.ignis.functional.test.config.properties.NameNodeProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Collections.singletonList;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;

@SuppressWarnings({ "UnusedReturnValue", "squid:S1192" })
public class CleanupSteps {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanupSteps.class);
    private static final Pattern STAGING_DIRECTORY_PATTERN = Pattern.compile("^(.*)/.*");
    private static final Pattern YARN_APPLICATION_ID_PATTERN = Pattern.compile(".*/(application_.*)$");

    @IgnisServer
    private final JdbcTemplate ignisServerTemplate;
    @Phoenix
    private final JdbcTemplate phoenixTemplate;
    private final FileSystemTemplate fileSystemTemplate;
    private final NameNodeProperties nameNodeProperties;

    public CleanupSteps(
            final JdbcTemplate ignisServerTemplate,
            final JdbcTemplate phoenixTemplate,
            final FileSystemTemplate fileSystemTemplate,
            final NameNodeProperties nameNodeProperties) {
        this.ignisServerTemplate = ignisServerTemplate;
        this.phoenixTemplate = phoenixTemplate;
        this.fileSystemTemplate = fileSystemTemplate;
        this.nameNodeProperties = nameNodeProperties;
    }

    public CleanupSteps deleteProductConfig(final ProductConfigView productConfig) {
        if (productConfig == null) {
            return this;
        }

        productConfig.getPipelines()
                .forEach(this::deletePipeline);
        productConfig.getSchemas()
                .forEach(this::deleteTable);

        removeFromIgnis(singletonList(productConfig), ProductConfigView::getId,
                "PRODUCT_CONFIG", "ID");
        return this;
    }

    private void deleteTable(final SchemaView table) {
        if (table != null) {

            removeValidationRules(table.getId());

            removeFromIgnis(
                    newArrayList(Arrays.asList(table)),
                    SchemaView::getId,
                    "DATASET_SCHEMA_FIELD",
                    "DATASET_SCHEMA_ID");

            removeFromIgnis(
                    singletonList(table),
                    SchemaView::getId,
                    "DATASET_SCHEMA",
                    "ID");

            dropPhoenixTable(table.getPhysicalTableName());

            LOGGER.info(
                    "Removed schema with id [{}] name [{}] version [{}]",
                    table.getId(), table.getPhysicalTableName(), table.getVersion());
        }
    }

    private void deletePipeline(final PipelineView pipeline) {
        if (pipeline != null) {
            List<PipelineStepView> steps = newArrayList(pipeline.getSteps());

            deleteStepSelects(steps);

            deleteSteps(steps);

            removeFromIgnis(singletonList(pipeline), PipelineView::getId, "PIPELINE", "ID");

            LOGGER.info(
                    "Removed pipeline with id [{}] name [{}]",
                    pipeline.getId(), pipeline.getName());
        }
    }

    private void deleteStepSelects(final List<PipelineStepView> steps) {
        if (!steps.isEmpty()) {
            String stepIds = steps.stream()
                    .map(PipelineStepView::getId)
                    .map(String::valueOf)
                    .collect(joining(","));

            String stepSelects =
                    String.format("SELECT ID FROM PIPELINE_STEP_SELECT WHERE PIPELINE_STEP_ID IN (%s)", stepIds);

            List<Long> selectIds = ignisServerTemplate.queryForList(stepSelects, Long.class);

            removeFromIgnis(selectIds, identity(), "PIPELINE_STEP_WINDOW_PARTITION", "PIPELINE_STEP_SELECT_ID");
            removeFromIgnis(selectIds, identity(), "PIPELINE_STEP_WINDOW_ORDER", "PIPELINE_STEP_SELECT_ID");
            removeFromIgnis(selectIds, identity(), "PIPELINE_STEP_SELECT", "ID");
        }
    }

    private void deleteSteps(final List<PipelineStepView> steps) {
        removeFromIgnis(steps, PipelineStepView::getId, "PIPELINE_STEP_FILTER", "PIPELINE_STEP_ID");
        removeFromIgnis(steps, PipelineStepView::getId, "PIPELINE_STEP_GROUPING", "PIPELINE_STEP_ID");
        removeFromIgnis(steps, PipelineStepView::getId, "PIPELINE_MAP_STEP", "ID");
        removeFromIgnis(steps, PipelineStepView::getId, "PIPELINE_AGGREGATION_STEP", "ID");

        removeFromIgnis(findJoins(steps), identity(), "PIPELINE_JOIN_FIELD", "PIPELINE_JOIN_ID");
        removeFromIgnis(steps, PipelineStepView::getId, "PIPELINE_STEP_JOIN", "PIPELINE_STEP_ID");
        removeFromIgnis(steps, PipelineStepView::getId, "PIPELINE_JOIN_STEP", "ID");
        removeFromIgnis(steps, PipelineStepView::getId, "PIPELINE_WINDOW_STEP", "ID");
        removeFromIgnis(steps, PipelineStepView::getId, "PIPELINE_STEP_UNION", "PIPELINE_STEP_ID");
        removeFromIgnis(steps, PipelineStepView::getId, "PIPELINE_UNION_STEP", "ID");

        removeFromIgnis(steps, PipelineStepView::getId, "PIPELINE_STEP_SCRIPTLET_INPUT", "PIPELINE_STEP_ID");
        removeFromIgnis(steps, PipelineStepView::getId, "PIPELINE_SCRIPTLET_STEP", "ID");

        removeFromIgnis(steps, PipelineStepView::getId, "PIPELINE_STEP", "ID");
    }

    private List<Long> findJoins(final List<PipelineStepView> steps) {
        return steps.stream().map(PipelineStepView::getJoins)
                .filter(Objects::nonNull)
                .flatMap(joinViews -> joinViews.stream()
                        .map(JoinView::getId))
                .collect(toList());
    }

    private void removeValidationRules(final Long schemaId) {
        if (schemaId > 0L) {
            ignisServerTemplate.update(
                    "DELETE FROM VALIDATION_RULE_EXAMPLE_FIELD"
                            + " WHERE VALIDATION_RULE_EXAMPLE_ID IN ("
                            + "   SELECT ID FROM VALIDATION_RULE_EXAMPLE"
                            + "   WHERE VALIDATION_RULE_ID IN ("
                            + "      SELECT ID FROM VALIDATION_RULE "
                            + "      WHERE DATASET_SCHEMA_ID = " + schemaId + "))");

            ignisServerTemplate.update(
                    "DELETE FROM VALIDATION_RULE_EXAMPLE WHERE VALIDATION_RULE_ID IN ("
                            + "      SELECT ID FROM VALIDATION_RULE "
                            + "      WHERE DATASET_SCHEMA_ID = " + schemaId + ")");

            ignisServerTemplate.update(
                    "DELETE FROM VALIDATION_RULE_SCHEMA_FIELD WHERE VALIDATION_RULE_ID IN ("
                            + "      SELECT ID FROM VALIDATION_RULE "
                            + "      WHERE DATASET_SCHEMA_ID = " + schemaId + ")");

            ignisServerTemplate.update(
                    "DELETE FROM VALIDATION_RULE WHERE ID IN ("
                            + "      SELECT ID FROM VALIDATION_RULE "
                            + "      WHERE DATASET_SCHEMA_ID = " + schemaId + ")");
        }
    }

    public void deleteDatasets(final List<Dataset> datasets) {
        if (isNotEmpty(datasets)) {
            removeFromIgnis(
                    datasets,
                    Dataset::getPipelineStepInvocationId,
                    "PIPELINE_STEP_INVC_DATASET",
                    "PIPELINE_STEP_INVOCATION_ID");

            removeFromIgnis(datasets, Dataset::getId, "DATASET", "ID");
        }
    }

    public void deletePipelineInvocationsForDatasets(final List<Dataset> datasets) {
        Set<Dataset> pipelineDatasets = datasets.stream()
                .filter(dataset -> dataset.getPipelineInvocationId() != null)
                .collect(toSet());

        if (isNotEmpty(pipelineDatasets)) {
            removeFromIgnis(
                    pipelineDatasets,
                    Dataset::getPipelineInvocationId,
                    "PIPELINE_STEP_INVOCATION",
                    "PIPELINE_INVOCATION_ID");

            removeFromIgnis(pipelineDatasets,
                    Dataset::getPipelineInvocationId,
                    "PIPELINE_INVOCATION", "ID");
        }
    }

    public void deleteServiceRequests(final Collection<Long> serviceRequestIds) {
        if (isNotEmpty(serviceRequestIds)) {
            removeStagingDirectoriesFromHdfs(serviceRequestIds);

            removeFromIgnis(serviceRequestIds, id -> id, "STAGING_DATA_SET", "SERVICE_REQUEST_ID");

            removeFromIgnis(serviceRequestIds, id -> id, "SVC_SERVICE_REQUEST", "ID");
        }
    }

    public void deleteSummaryViews(final List<ValidationRuleSummaryView> summaryViews) {
        removeFromIgnis(
                summaryViews, ValidationRuleSummaryView::getId,
                "VALIDATION_RESULTS_SUMMARY", "ID");

        removeFromTable(
                phoenixTemplate, summaryViews, ValidationRuleSummaryView::getDatasetId,
                "VALIDATION_RULE_RESULTS", "DATASET_ID");
    }

    @SuppressWarnings("SameParameterValue")
    private <T> void removeFromIgnis(
            final Collection<T> rows,
            final Function<T, Long> rowToId,
            final String tableName,
            final String filterColumn) {

        List<T> distinctRows = rows.stream().distinct().collect(toList());
        removeFromTableAndLog(ignisServerTemplate, distinctRows, rowToId, tableName, filterColumn);
    }

    private <T> void removeFromTableAndLog(
            final JdbcTemplate template,
            final List<T> rows,
            final Function<T, Long> rowToId,
            final String tableName,
            final String filterColumn) {

        if (isNotEmpty(rows)) {
            int deletedRows = removeFromTable(template, rows, rowToId, tableName, filterColumn);

            if (deletedRows != rows.size()) {
                LOGGER.error("Unexpected delete count: deleted {} {} rows", deletedRows, tableName);
            }
        }
    }

    private <T> int removeFromTable(
            final JdbcTemplate template,
            final List<T> rows,
            final Function<T, Long> rowToId,
            final String tableName,
            final String filterColumn) {

        if (isEmpty(rows)) {
            return 0;
        }
        String ids = rows.stream()
                .map(rowToId)
                .map(Objects::toString)
                .collect(joining(", "));

        int deletedCount = template.update(
                String.format("DELETE FROM %s WHERE %s IN (%s)", tableName, filterColumn, ids));

        LOGGER.info("Removed {} {} rows with ids [{}]", deletedCount, tableName, ids);

        return deletedCount;
    }

    private void dropPhoenixTable(final String datasetName) {
        if (isBlank(datasetName)) {
            return;
        }
        String dropDatasetSqls = String.format("DROP TABLE IF EXISTS %s", datasetName);

        try {
            int droppedCount = phoenixTemplate.batchUpdate(dropDatasetSqls).length;
            if (droppedCount == 1) {
                LOGGER.info("Dropped {} tables with names {}", droppedCount, datasetName);
            } else {
                LOGGER.error(
                        "Unexpected drop count: dropped {} datasets for names {}", droppedCount, datasetName);
            }
        } catch (Exception e) {
            LOGGER.error("Error while removing datasets", e);
        }
    }

    private void removeStagingDirectoriesFromHdfs(final Collection<Long> serviceRequestIds) {
        String query = "SELECT STAGING_FILE, YARN_APPLICATION_TRACKING_URL "
                + "FROM STAGING_DATA_SET "
                + "INNER JOIN SVC_SERVICE_REQUEST ON STAGING_DATA_SET.SERVICE_REQUEST_ID = SVC_SERVICE_REQUEST.ID "
                + "WHERE STAGING_DATA_SET.SERVICE_REQUEST_ID IN (%s)";

        String ids = serviceRequestIds.stream().map(Objects::toString).collect(joining(","));

        List<Map<String, Object>> resultSet = ignisServerTemplate.queryForList(String.format(query, ids));

        for (Map<String, Object> result : resultSet) {
            String stagingFile = (String) result.get("STAGING_FILE");
            String yarnApplicationUrl = (String) result.get("YARN_APPLICATION_TRACKING_URL");

            Matcher stagingDirectoryMatch = STAGING_DIRECTORY_PATTERN.matcher(stagingFile);
            Matcher yarnApplicationIdMatch = YARN_APPLICATION_ID_PATTERN.matcher(yarnApplicationUrl);

            if (stagingDirectoryMatch.matches() && yarnApplicationIdMatch.matches()) {
                String stagingDirectory = stagingDirectoryMatch.group(1);
                String sparkStagingDirectory = String.format("/user/%s/.sparkStaging/%s",
                        nameNodeProperties.getUser(), yarnApplicationIdMatch.group(1));

                removeHdfsDirectory(stagingDirectory);
                removeHdfsDirectory(sparkStagingDirectory);
            }
        }
    }

    private void removeHdfsDirectory(final String directoryPath) {
        LOGGER.info("Deleting directory in HDFS: [{}]", directoryPath);

        try {
            fileSystemTemplate.delete(directoryPath);
        } catch (IOException e) {
            LOGGER.warn("Failed to delete directory in HDFS: [{}]", directoryPath, e);
        }
    }
}
