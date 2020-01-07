package com.lombardrisk.ignis.functional.test.dsl;

import com.google.common.collect.ImmutableSet;
import com.lombardrisk.ignis.client.core.page.request.PageRequest;
import com.lombardrisk.ignis.client.core.view.IdView;
import com.lombardrisk.ignis.client.design.productconfig.ProductConfigDto;
import com.lombardrisk.ignis.client.design.schema.SchemaDto;
import com.lombardrisk.ignis.client.external.dataset.model.Dataset;
import com.lombardrisk.ignis.client.external.drillback.DatasetRowDataView;
import com.lombardrisk.ignis.client.external.job.pipeline.PipelineRequest;
import com.lombardrisk.ignis.client.external.job.staging.DatasetState;
import com.lombardrisk.ignis.client.external.job.staging.StagingItemView;
import com.lombardrisk.ignis.client.external.job.staging.request.DataSource;
import com.lombardrisk.ignis.client.external.job.staging.request.v1.StagingItemRequest;
import com.lombardrisk.ignis.client.external.job.staging.request.v1.StagingRequest;
import com.lombardrisk.ignis.client.external.job.staging.request.v2.StagingItemRequestV2;
import com.lombardrisk.ignis.client.external.job.staging.request.v2.StagingRequestV2;
import com.lombardrisk.ignis.client.external.productconfig.view.ProductConfigView;
import com.lombardrisk.ignis.client.external.productconfig.view.SchemaView;
import com.lombardrisk.ignis.client.external.rule.ValidationResultsDetailView;
import com.lombardrisk.ignis.client.external.rule.ValidationRuleSummaryView;
import com.lombardrisk.ignis.functional.test.assertions.HdfsAssertions;
import com.lombardrisk.ignis.functional.test.assertions.PhysicalSchemaAssertions;
import com.lombardrisk.ignis.functional.test.assertions.ProductConfigViewAssertions;
import com.lombardrisk.ignis.functional.test.assertions.SchemaViewAssertions;
import com.lombardrisk.ignis.functional.test.assertions.ValidationResultsDetailAssertions;
import com.lombardrisk.ignis.functional.test.assertions.ValidationRuleSummaryViewsAssertions;
import com.lombardrisk.ignis.functional.test.assertions.expected.ExpectedDataset;
import com.lombardrisk.ignis.functional.test.assertions.expected.ExpectedPhysicalTable;
import com.lombardrisk.ignis.functional.test.assertions.expected.ExpectedResultsDetails;
import com.lombardrisk.ignis.functional.test.assertions.expected.ExpectedRuleSummaries;
import com.lombardrisk.ignis.functional.test.datasource.CsvFinder;
import com.lombardrisk.ignis.functional.test.steps.CleanupSteps;
import com.lombardrisk.ignis.functional.test.steps.JobSteps;
import com.lombardrisk.ignis.functional.test.steps.PipelineSteps;
import com.lombardrisk.ignis.functional.test.steps.ProductConfigSteps;
import com.lombardrisk.ignis.functional.test.steps.StagingSteps;
import com.lombardrisk.ignis.functional.test.steps.ValidationSteps;
import io.vavr.control.Try;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class FcrEngineTest {

    private final List<ProductConfigView> productConfigs = new ArrayList<>();
    private final Map<Long, StagingJobContext> stagingJobs = new HashMap<>();
    private final Map<Long, PipelineJobContext> pipelineJobs = new HashMap<>();
    private final Map<Long, ValidationJobContext> validationJobs = new HashMap<>();
    private final List<ValidationRuleSummaryView> ruleSummaries = new ArrayList<>();

    private final JobSteps jobSteps;
    private final ProductConfigSteps productConfigSteps;
    private final StagingSteps stagingSteps;
    private final PipelineSteps pipelineSteps;
    private final ValidationSteps validationSteps;
    private final CleanupSteps cleanupSteps;
    private final CsvFinder csvFinder;

    private final HdfsAssertions hdfsAssertions;
    private final PhysicalSchemaAssertions physicalSchemaAssertions;

    public void cleanup() {
        cleanupSteps.deleteSummaryViews(ruleSummaries);

        cleanupSteps.deleteDatasets(getDatasets());
        cleanupSteps.deletePipelineInvocationsForDatasets(getDatasets());

        cleanupSteps.deleteServiceRequests(
                ImmutableSet.<Long>builder()
                        .addAll(stagingJobs.keySet())
                        .addAll(pipelineJobs.keySet())
                        .addAll(validationJobs.keySet())
                        .build());

        productConfigs.forEach(cleanupSteps::deleteProductConfig);
    }

    public ImportProductJobContext importProductConfig(final String productConfigPath) {
        try {
            IdView importingProductId = productConfigSteps.importProductConfigFromFolder(productConfigPath);

            ProductConfigView importingProduct = productConfigSteps.findProduct(importingProductId.getId());

            productConfigs.add(importingProduct);

            return new ImportProductJobContext(importingProduct, productConfigSteps, jobSteps);
        } catch (Exception e) {
            throw new FcrStepFailedException("Failed to import product config", e);
        }
    }

    public ImportProductJobContext importProductConfig(final File productConfig) {
        try {
            IdView importingProductId = productConfigSteps.importProductConfig(productConfig);

            ProductConfigView importingProduct = productConfigSteps.findProduct(importingProductId.getId());

            productConfigs.add(importingProduct);

            return new ImportProductJobContext(importingProduct, productConfigSteps, jobSteps);
        } catch (Exception e) {
            throw new FcrStepFailedException("Failed to import product config", e);
        }
    }

    public DeleteProductJobContext deleteProductConfig(final ProductConfigView productConfigView) {
        try {
            productConfigSteps.deleteProduct(productConfigView.getId());

            return new DeleteProductJobContext(productConfigView, jobSteps);
        } catch (Exception e) {
            throw new FcrStepFailedException("Failed to delete product config", e);
        }
    }

    public StagingJobContext stageDatasets(
            final ProductConfigView importedProduct,
            final StagingRequest stagingRequest) {

        try {
            StagingRequest updatedRequest = updateStagingRequest(importedProduct, stagingRequest);

            StagingJobContext stagingJob = stagingSteps.runStagingJobV1(updatedRequest);

            this.stagingJobs.put(stagingJob.getJobId(), stagingJob);

            return stagingJob;
        } catch (Exception e) {
            throw new FcrStepFailedException("Failed to start staging job", e);
        }
    }

    public StagingJobContext stageDatasets(
            final ProductConfigView importedProduct,
            final StagingRequestV2 stagingRequest) {

        try {
            StagingRequestV2 updatedRequest = updateStagingRequest(importedProduct, stagingRequest);

            StagingJobContext stagingJob = stagingSteps.runStagingJobV2(updatedRequest);

            this.stagingJobs.put(stagingJob.getJobId(), stagingJob);

            return stagingJob;
        } catch (Exception e) {
            throw new FcrStepFailedException("Failed to start staging job", e);
        }
    }

    private StagingRequest updateStagingRequest(
            final ProductConfigView importedProduct,
            final StagingRequest stagingRequest) {

        StagingRequest.StagingRequestBuilder stagingRequestCopy =
                StagingRequest.builder().name(stagingRequest.getName());

        Set<StagingItemRequest> items = newHashSet();
        for (StagingItemRequest stagingItemRequest : stagingRequest.getItems()) {
            SchemaView importedSchema =
                    findLatestSchemaVersion(byDisplayName(stagingItemRequest.getSchema()), importedProduct);

            DataSource source = stagingItemRequest.getSource();

            items.add(stagingItemRequest.copy()
                    .schema(importedSchema.getDisplayName())
                    .source(DataSource.builder()
                            .header(source.isHeader())
                            .filePath(csvFinder.findPath(source.getFilePath()))
                            .build())
                    .build());
        }
        stagingRequestCopy.items(items);

        return stagingRequestCopy.build();
    }

    private StagingRequestV2 updateStagingRequest(
            final ProductConfigView importedProduct,
            final StagingRequestV2 stagingRequest) {

        StagingRequestV2.StagingRequestV2Builder stagingRequestCopy =
                StagingRequestV2.builder()
                        .name(stagingRequest.getName())
                        .metadata(stagingRequest.getMetadata());

        Set<StagingItemRequestV2> items = newHashSet();
        for (StagingItemRequestV2 stagingItemRequest : stagingRequest.getItems()) {
            SchemaView importedSchema =
                    findLatestSchemaVersion(byDisplayName(stagingItemRequest.getSchema()), importedProduct);

            DataSource source = stagingItemRequest.getSource();

            items.add(stagingItemRequest.copy()
                    .schema(importedSchema.getDisplayName())
                    .source(DataSource.builder()
                            .header(source.isHeader())
                            .filePath(csvFinder.findPath(source.getFilePath()))
                            .build())
                    .build());
        }
        stagingRequestCopy.items(items);

        return stagingRequestCopy.build();
    }

    public PipelineJobContext runPipelineJob(final PipelineRequest pipelineRequest) {

        try {
            PipelineJobContext pipelineJob = pipelineSteps.runPipelineJob(pipelineRequest);
            this.pipelineJobs.put(pipelineJob.getJobId(), pipelineJob);

            return pipelineJob;
        } catch (Exception e) {
            throw new FcrStepFailedException("Failed to start pipeline job", e);
        }
    }

    public ValidationJobContext validateDataset(final StagingJobContext stagingJob, final String schemaName) {
        try {
            Dataset stagedDataset = stagingJob.findStagedDatasetByName(schemaName);

            ValidationJobContext validationJob = validationSteps.validateDataset(stagedDataset.getId());

            this.validationJobs.put(validationJob.getJobId(), validationJob);

            return validationJob;
        } catch (Exception e) {
            throw new FcrStepFailedException("Failed to validate dataset", e);
        }
    }

    public void assertProductConfigDoesNotExist(final ProductConfigView productConfigView) {
        assertThatThrownBy(() -> productConfigSteps.findProduct(productConfigView.getId()))
                .hasMessageContaining("NOT_FOUND");
    }

    public void assertProductConfigMatchesDesign(
            final ProductConfigView importedProduct,
            final ProductConfigDto designedProduct,
            final List<SchemaDto> expectedSchemas) {

        ProductConfigViewAssertions assertions = new ProductConfigViewAssertions();
        assertions.assertProductConfigIdenticalWithDesign(importedProduct, designedProduct, expectedSchemas);
    }

    public void assertStagingFilesDoNotExist(final List<StagingItemView> stagingFiles) throws IOException {
        for (StagingItemView stagingFile : stagingFiles) {
            if (stagingFile.getStagingFileCopyLocation().isPresent()) {
                hdfsAssertions.doesNotHaveFile(Paths.get(stagingFile.getStagingFile()));
            }
        }
        hdfsAssertions.assertAll();
    }

    public void assertStagingDatasetsExist(
            final ProductConfigView importedProduct,
            final StagingJobContext stagingJob,
            final ExpectedDataset... expectedDatasets) {

        List<ExpectedDataset> expected = Arrays.stream(expectedDatasets)
                .map(expectedDataset -> expectedDatasetWithSchema(importedProduct, expectedDataset))
                .collect(toList());

        physicalSchemaAssertions
                .hasExpectedDatasetsInPhoenix(expected, stagingJob.getStagedDatasets())
                .assertAll();
    }

    public void assertPipelineDatasetsExist(
            final ProductConfigView importedProduct,
            final PipelineJobContext pipelineJob,
            final ExpectedDataset... expectedDatasets) {

        List<ExpectedDataset> expected = Arrays.stream(expectedDatasets)
                .map(expectedDataset -> expectedDatasetWithSchema(importedProduct, expectedDataset))
                .collect(toList());

        physicalSchemaAssertions
                .hasExpectedDatasetsInPhoenix(expected, pipelineJob.getPipelineDatasets())
                .assertAll();
    }

    public void assertDrillBackRowData(
            final PipelineJobContext pipelineJob,
            final String schemaPhysicalName,
            final Long pipelineId,
            final Long pipelineStepId,
            final List<Map<String, Object>> data) {

        Dataset pipelineDatasets = pipelineJob.getPipelineDatasets()
                .stream()
                .filter(dataset -> dataset.getName().startsWith(schemaPhysicalName))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "Could not find dataset with name " + schemaPhysicalName
                                + " in " + pipelineJob.getPipelineDatasets()));

        DatasetRowDataView datasetRowDataView = pipelineSteps.drillBackDataset(
                pipelineDatasets.getId(), pipelineId, pipelineStepId);

        assertThat(datasetRowDataView.getData())
                .extracting(this::removeSystemColumns)
                .containsExactlyInAnyOrderElementsOf(data);
    }

    private Map<String, Object> removeSystemColumns(final Map<String, Object> row) {
        row.remove("ROW_KEY");

        Set<String> columns = row.keySet()
                .stream()
                .filter(column -> column.startsWith("FCR_SYS__"))
                .collect(Collectors.toSet());

        columns.forEach(row::remove);
        return row;
    }

    public void assertDatasetDoesNotExist(
            final ProductConfigView importedProduct,
            final StagingJobContext stagingJob,
            final String schemaName) {

        SchemaView importedSchema = findLatestSchemaVersion(byPhysicalTableName(schemaName), importedProduct);

        physicalSchemaAssertions
                .doesNotHaveDatasetWithName(importedSchema.getPhysicalTableName(), stagingJob.getStagedDatasets())
                .assertAll();
    }

    public void assertStagingDatasetState(
            final ProductConfigView importedProduct,
            final StagingJobContext stagingJob,
            final String schemaName,
            final DatasetState datasetState) {

        SchemaView importedSchema = findLatestSchemaVersion(byPhysicalTableName(schemaName), importedProduct);

        physicalSchemaAssertions
                .hasStagingDatasetWithStatus(importedSchema, datasetState, stagingJob.getStagingDatasets())
                .assertAll();
    }

    public void assertPhysicalTableExists(
            final ProductConfigView productConfig,
            final ExpectedPhysicalTable expected) {

        SchemaView importedSchema = findLatestSchemaVersion(byPhysicalTableName(expected.getName()), productConfig);

        SchemaViewAssertions schemaViewAssertions = new SchemaViewAssertions();
        schemaViewAssertions.assertSchema(importedSchema, expected);

        physicalSchemaAssertions
                .hasExpectedTableSchema(expected, importedSchema.getPhysicalTableName())
                .assertAll();
    }

    public void assertPhysicalTableDoesNotExist(
            final ProductConfigView productConfig,
            final String physicalTableName) {

        SchemaView importedSchema = findLatestSchemaVersion(byPhysicalTableName(physicalTableName), productConfig);

        physicalSchemaAssertions
                .doesNotHaveTableWithName(importedSchema.getPhysicalTableName())
                .assertAll();
    }

    public void assertStagingValidationErrors(
            final StagingJobContext stagingJob,
            final String schemaName,
            final String... expectedErrors) {

        StagingItemView stagingDataset = stagingJob.findStagingItemBySchemaName(schemaName);

        List<String> validationErrors = Try.of(() -> stagingSteps.getValidationErrors(stagingDataset.getId()))
                .getOrElseThrow(throwable -> new FcrStepFailedException("Failed to get validation errors", throwable));

        assertThat(validationErrors).containsOnly(expectedErrors);
    }

    public void assertValidationRuleSummaries(
            final ValidationJobContext validationJob,
            final ExpectedRuleSummaries expectedSummaries) {

        List<ValidationRuleSummaryView> actualRuleSummaries =
                validationSteps.findResultSummaries(validationJob.getDatasetId());

        ValidationRuleSummaryViewsAssertions.assertRuleSummaries(actualRuleSummaries)
                .hasDatasetId(validationJob.getDatasetId())
                .hasTotalRecords(expectedSummaries.getTotalNumberOfRecords())
                .containsSummaries(expectedSummaries.getRuleSummaries())
                .assertAll();

        this.ruleSummaries.addAll(actualRuleSummaries);
    }

    public void assertValidationResultsDetails(
            final ValidationJobContext validationJob,
            final PageRequest pageRequest,
            final ExpectedResultsDetails expectedResultsDetail) {

        List<ValidationRuleSummaryView> actualRuleSummaries =
                validationSteps.findResultSummaries(validationJob.getDatasetId());

        ValidationResultsDetailView actualResultsDetail =
                validationSteps.findResultDetails(validationJob.getDatasetId(), pageRequest);

        ValidationResultsDetailAssertions
                .resultsDetail(actualResultsDetail)
                .hasResultsDetails(expectedResultsDetail);

        this.ruleSummaries.addAll(actualRuleSummaries);
    }

    private ExpectedDataset expectedDatasetWithSchema(
            final ProductConfigView importedProduct,
            final ExpectedDataset expectedDataset) {

        SchemaView importedSchema =
                findLatestSchemaVersion(byPhysicalTableName(expectedDataset.getSchema()), importedProduct);

        return expectedDataset.copy().schema(importedSchema.getPhysicalTableName()).build();
    }

    private List<Dataset> getDatasets() {
        Stream<Dataset> stagingDatasets = stagingJobs.values().stream()
                .map(StagingJobContext::getStagedDatasets)
                .flatMap(List::stream);

        Stream<Dataset> pipelineDatasets = pipelineJobs.values().stream()
                .map(PipelineJobContext::getPipelineDatasets)
                .flatMap(List::stream);

        return Stream.concat(stagingDatasets, pipelineDatasets)
                .sorted(comparing(Dataset::getCreatedTime))
                .collect(toList());
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private SchemaView findLatestSchemaVersion(
            final Function<SchemaView, Boolean> predicate, final ProductConfigView productConfig) {

        Optional<SchemaView> importedSchema = productConfig.getSchemas().stream()
                .filter(predicate::apply)
                .max(comparing(SchemaView::getVersion));

        assertThat(importedSchema)
                .describedAs("Schema not imported")
                .isPresent();

        return importedSchema.get();
    }

    private static Function<SchemaView, Boolean> byDisplayName(final String displayName) {
        return schemaView -> schemaView.getDisplayName().startsWith(displayName);
    }

    private static Function<SchemaView, Boolean> byPhysicalTableName(final String physicalTableName) {
        return schemaView -> schemaView.getPhysicalTableName().startsWith(physicalTableName);
    }
}
