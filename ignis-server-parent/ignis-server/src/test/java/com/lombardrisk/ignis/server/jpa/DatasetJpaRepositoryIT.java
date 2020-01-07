package com.lombardrisk.ignis.server.jpa;

import com.google.common.collect.ImmutableList;
import com.lombardrisk.ignis.api.dataset.DatasetType;
import com.lombardrisk.ignis.api.dataset.ValidationStatus;
import com.lombardrisk.ignis.server.config.IntegrationTestConfig;
import com.lombardrisk.ignis.server.dataset.DatasetJpaRepository;
import com.lombardrisk.ignis.server.dataset.fixture.DatasetPopulated;
import com.lombardrisk.ignis.server.dataset.model.Dataset;
import com.lombardrisk.ignis.server.dataset.model.DatasetOnly;
import com.lombardrisk.ignis.server.dataset.model.DatasetServiceRequest;
import com.lombardrisk.ignis.server.dataset.pipeline.invocation.PipelineInvocationJpaRepository;
import com.lombardrisk.ignis.server.dataset.pipeline.invocation.model.PipelineInvocation;
import com.lombardrisk.ignis.server.job.fixture.JobPopulated;
import com.lombardrisk.ignis.server.job.servicerequest.ServiceRequest;
import com.lombardrisk.ignis.server.job.servicerequest.ServiceRequestRepository;
import com.lombardrisk.ignis.server.job.staging.StagingDatasetRepository;
import com.lombardrisk.ignis.server.job.staging.model.StagingDataset;
import com.lombardrisk.ignis.server.product.fixture.ProductPopulated;
import com.lombardrisk.ignis.server.product.pipeline.PipelineJpaRepository;
import com.lombardrisk.ignis.server.product.pipeline.PipelineService;
import com.lombardrisk.ignis.server.product.pipeline.model.Pipeline;
import com.lombardrisk.ignis.server.product.productconfig.ProductConfigRepository;
import com.lombardrisk.ignis.server.product.productconfig.model.ProductConfig;
import com.lombardrisk.ignis.server.product.table.TableRepository;
import com.lombardrisk.ignis.server.product.table.model.Table;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManager;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.time.DateUtils.parseDate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.data.domain.Sort.Direction.DESC;

@RunWith(SpringRunner.class)
@IntegrationTestConfig
public class DatasetJpaRepositoryIT {

    @Autowired
    private DatasetJpaRepository datasetRepository;

    @Autowired
    private StagingDatasetRepository stagingDatasetRepository;

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @Autowired
    private TableRepository tableRepository;

    @Autowired
    private ProductConfigRepository productConfigRepository;

    @Autowired
    private PipelineInvocationJpaRepository pipelineInvocationJpaRepository;

    @Autowired
    private PipelineJpaRepository pipelineJpaRepository;

    @Autowired
    private EntityManager entityManager;

    private PipelineService pipelineService;

    @Rule
    public JUnitSoftAssertions soft = new JUnitSoftAssertions();

    @Before
    public void setUp() {
        pipelineService = new PipelineService(pipelineJpaRepository);
    }

    @Test
    public void create_updateRetrieve() throws ParseException {
        Date createdTime = parseDate("2018-01-01", "yyyy-MM-dd");
        LocalDate referenceDate = LocalDate.of(2018, 1, 1);

        ProductConfig productConfig = productConfigRepository.save(ProductPopulated.productConfig().build());
        Table savedTable =
                tableRepository.saveAndFlush(ProductPopulated.table().productId(productConfig.getId()).build());
        Pipeline savedPipeline = pipelineService.savePipeline(ProductPopulated.pipeline()
                .steps(singleton(ProductPopulated.mapPipelineStep()
                        .schemaInId(savedTable.getId())
                        .schemaOutId(savedTable.getId())
                        .selects(emptySet())
                        .build()))
                .build());
        PipelineInvocation savedPipelineInvocation = pipelineInvocationJpaRepository.saveAndFlush(
                DatasetPopulated.pipelineInvocation()
                        .pipelineId(savedPipeline.getId())
                        .steps(singleton(DatasetPopulated.pipelineStepInvocation()
                                .pipelineStepId(savedPipeline.getSteps().iterator().next().getId())
                                .pipelineStep(savedPipeline.getSteps().iterator().next())
                                .inputDatasets(emptySet())
                                .build()))
                        .build());

        Dataset savedDataset =
                datasetRepository.saveAndFlush(
                        DatasetPopulated.dataset()
                                .stagingJobs(emptySet())
                                .name("DATA_SET")
                                .table("DATA_SET_SCHEMA")
                                .datasetType(DatasetType.STAGING_DATASET)
                                .recordsCount(21L)
                                .createdTime(createdTime)
                                .lastUpdated(createdTime)
                                .latestValidationJobId(10292L)
                                .pipelineJobId(8282L)
                                .pipelineInvocationId(savedPipelineInvocation.getId())
                                .pipelineStepInvocationId(savedPipelineInvocation.getSteps().iterator().next().getId())
                                .schema(savedTable)
                                .predicate("1 = 1")
                                .rowKeySeed(616L)
                                .entityCode("ENTITY_123")
                                .referenceDate(referenceDate)
                                .runKey(1L)
                                .build());

        soft.assertThat(savedDataset.getId())
                .isNotNull();

        soft.assertThat(savedDataset.getStagingJobs())
                .isEmpty();

        soft.assertThat(savedDataset.getName())
                .isEqualTo("DATA_SET");
        soft.assertThat(savedDataset.getTable())
                .isEqualTo("DATA_SET_SCHEMA");
        soft.assertThat(savedDataset.getDatasetType())
                .isEqualTo(DatasetType.STAGING_DATASET);

        soft.assertThat(savedDataset.getRecordsCount())
                .isEqualTo(21);
        soft.assertThat(savedDataset.getCreatedTime())
                .isEqualTo(createdTime);

        soft.assertThat(savedDataset.getEntityCode())
                .isEqualTo("ENTITY_123");
        soft.assertThat(savedDataset.getReferenceDate())
                .isEqualTo(referenceDate);

        soft.assertThat(savedDataset.getPredicate())
                .isEqualTo("1 = 1");
        soft.assertThat(savedDataset.getRowKeySeed())
                .isEqualTo(616L);

        soft.assertThat(savedDataset.getSchema())
                .isEqualTo(savedTable);

        soft.assertThat(savedDataset.getLatestValidationJobId())
                .isEqualTo(10292L);
        soft.assertThat(savedDataset.getPipelineJobId())
                .isEqualTo(8282L);
        soft.assertThat(savedDataset.getPipelineInvocationId())
                .isEqualTo(savedPipelineInvocation.getId());
        soft.assertThat(savedDataset.getPipelineStepInvocationId())
                .isEqualTo(savedPipelineInvocation.getSteps().iterator().next().getId());

        soft.assertThat(savedDataset.getRunKey())
                .isEqualTo(1L);

        soft.assertThat(savedDataset.getLastUpdated())
                .isEqualTo(createdTime);
    }

    @Test
    public void findByIdPrefetchRules() throws ParseException {
        Date createdTime = parseDate("2018-01-01", "yyyy-MM-dd");
        LocalDate referenceDate = LocalDate.of(2018, 1, 1);

        ProductConfig productConfig = productConfigRepository.save(ProductPopulated.productConfig().build());
        Table savedTable =
                tableRepository.saveAndFlush(ProductPopulated.table().productId(productConfig.getId())
                        .validationRules(newHashSet(ProductPopulated.validationRule().build()))
                        .build());

        Dataset dataset = datasetRepository.saveAndFlush(
                DatasetPopulated.dataset()
                        .stagingJobs(emptySet())
                        .schema(savedTable)
                        .build());

        Dataset retrieved = datasetRepository.findByIdPrefetchSchemaRules(dataset.getId())
                .get();

        assertThat(retrieved.getSchema().getValidationRules())
                .hasSize(1);
    }

    @Test
    public void create_WithStagingDatasets() {
        ProductConfig productConfig = productConfigRepository.save(ProductPopulated.productConfig().build());
        Table savedTable =
                tableRepository.saveAndFlush(ProductPopulated.table().productId(productConfig.getId()).build());

        ServiceRequest request1 = serviceRequestRepository.saveAndFlush(
                JobPopulated.stagingJobServiceRequest().build());

        ServiceRequest request2 = serviceRequestRepository.saveAndFlush(
                JobPopulated.stagingJobServiceRequest().build());

        StagingDataset stagingDataset1 = stagingDatasetRepository.saveAndFlush(
                JobPopulated.stagingDataset().serviceRequestId(request1.getId()).build());

        StagingDataset stagingDataset2 = stagingDatasetRepository.saveAndFlush(
                JobPopulated.stagingDataset().serviceRequestId(request2.getId()).build());

        Dataset savedDataset =
                datasetRepository.saveAndFlush(
                        DatasetPopulated.dataset()
                                .stagingJobs(newHashSet(
                                        DatasetServiceRequest.builder()
                                                .id(stagingDataset1.getId()).serviceRequestId(request1.getId())
                                                .build(),
                                        DatasetServiceRequest.builder()
                                                .id(stagingDataset2.getId()).serviceRequestId(request2.getId())
                                                .build()))
                                .schema(savedTable)
                                .build());

        soft.assertThat(savedDataset.getStagingJobs())
                .containsOnly(
                        DatasetServiceRequest.builder()
                                .id(stagingDataset1.getId())
                                .serviceRequestId(request1.getId())
                                .build(),
                        DatasetServiceRequest.builder()
                                .id(stagingDataset2.getId())
                                .serviceRequestId(request2.getId())
                                .build());
    }

    @Test
    public void delete_WithStagingDatasets() {
        ProductConfig productConfig =
                productConfigRepository.save(ProductPopulated.productConfig().build());

        Table savedTable =
                tableRepository.saveAndFlush(ProductPopulated.table().productId(productConfig.getId()).build());

        ServiceRequest request1 = serviceRequestRepository.saveAndFlush(
                JobPopulated.stagingJobServiceRequest().build());

        ServiceRequest request2 = serviceRequestRepository.saveAndFlush(
                JobPopulated.stagingJobServiceRequest().build());

        StagingDataset stagingDataset1 = stagingDatasetRepository.saveAndFlush(
                JobPopulated.stagingDataset().serviceRequestId(request1.getId()).build());

        StagingDataset stagingDataset2 = stagingDatasetRepository.saveAndFlush(
                JobPopulated.stagingDataset().serviceRequestId(request2.getId()).build());

        Dataset savedDataset =
                datasetRepository.saveAndFlush(
                        DatasetPopulated.dataset()
                                .stagingJobs(newHashSet(
                                        DatasetServiceRequest.builder()
                                                .id(stagingDataset1.getId()).serviceRequestId(request1.getId())
                                                .build(),
                                        DatasetServiceRequest.builder()
                                                .id(stagingDataset2.getId()).serviceRequestId(request2.getId())
                                                .build()))
                                .schema(savedTable)
                                .build());

        datasetRepository.delete(savedDataset);

        soft.assertThat(datasetRepository.findAll())
                .isEmpty();

        soft.assertThat(serviceRequestRepository.findAll())
                .containsExactlyInAnyOrder(request1, request2);

        soft.assertThat(stagingDatasetRepository.findAll())
                .containsExactlyInAnyOrder(stagingDataset1, stagingDataset2);

        soft.assertThat(stagingDatasetRepository.findAll())
                .extracting(StagingDataset::getDatasetId)
                .containsOnlyNulls();
    }

    @Test
    public void update_Dataset_updatesDataset() throws ParseException {
        Date updatedTime = parseDate("2018-12-31", "yyyy-MM-dd");

        ProductConfig productConfig = productConfigRepository.save(ProductPopulated.productConfig().build());
        Table savedTable =
                tableRepository.saveAndFlush(ProductPopulated.table().productId(productConfig.getId()).build());

        Dataset savedDataset =
                datasetRepository.saveAndFlush(
                        DatasetPopulated.dataset()
                                .stagingJobs(emptySet())
                                .name("unnamed")
                                .schema(savedTable)
                                .lastUpdated(updatedTime)
                                .build());

        savedDataset.setName("Jack's Data");
        datasetRepository.saveAndFlush(savedDataset);
        entityManager.clear();
        Dataset updatedDataset = datasetRepository.getOne(savedDataset.getId());

        soft.assertThat(updatedDataset.getName())
                .isEqualTo("Jack's Data");
        soft.assertThat(updatedDataset.getId())
                .isEqualTo(savedDataset.getId());
        soft.assertThat(updatedDataset.getLastUpdated().getTime())
                .isEqualTo(updatedTime.getTime());
    }

    @Test
    public void delete_Dataset_updatesDataset() {
        ProductConfig productConfig = productConfigRepository.save(ProductPopulated.productConfig().build());
        Table savedTable =
                tableRepository.saveAndFlush(ProductPopulated.table().productId(productConfig.getId()).build());

        Dataset dataset = datasetRepository.saveAndFlush(
                DatasetPopulated.dataset()
                        .stagingJobs(emptySet())
                        .schema(savedTable)
                        .build());

        soft.assertThat(dataset).isNotNull();

        datasetRepository.delete(dataset);
        datasetRepository.flush();

        soft.assertThat(datasetRepository.findAll()).isEmpty();
        soft.assertThat(tableRepository.findById(savedTable.getId())).isNotNull();
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    public void delete_Schema_removesSchema() {
        ProductConfig productConfig = productConfigRepository.save(ProductPopulated.productConfig().build());
        Table savedTable =
                tableRepository.saveAndFlush(ProductPopulated.table().productId(productConfig.getId()).build());

        Dataset dataset = datasetRepository.saveAndFlush(
                DatasetPopulated.dataset().stagingJobs(emptySet()).schema(savedTable).build());

        Dataset updatedDataset = datasetRepository.findById(dataset.getId()).get();
        updatedDataset.setSchema(null);

        datasetRepository.saveAndFlush(updatedDataset);
        updatedDataset = datasetRepository.findById(updatedDataset.getId()).get();

        soft.assertThat(updatedDataset.getSchema()).isNull();

        tableRepository.delete(savedTable);
        tableRepository.flush();

        soft.assertThat(tableRepository.findById(savedTable.getId()))
                .isEmpty();
    }

    @Test
    public void findBySchemaId_ReturnsDatasets() {
        ProductConfig productConfig = productConfigRepository.save(ProductPopulated.productConfig().build());
        Table savedTable = tableRepository.saveAndFlush(
                ProductPopulated.table()
                        .productId(productConfig.getId())
                        .physicalTableName("SHARED")
                        .build());

        datasetRepository.saveAndFlush(
                DatasetPopulated.dataset()
                        .stagingJobs(emptySet())
                        .schema(savedTable)
                        .runKey(1L)
                        .build());

        datasetRepository.saveAndFlush(
                DatasetPopulated.dataset()
                        .stagingJobs(emptySet())
                        .schema(savedTable)
                        .runKey(2L)
                        .build());

        List<Dataset> datasetsWithSharedSchema = datasetRepository.findBySchemaId(savedTable.getId());

        soft.assertThat(datasetsWithSharedSchema)
                .extracting(Dataset::getSchema)
                .extracting(Table::getPhysicalTableName)
                .containsExactly("SHARED", "SHARED");
    }

    @Test
    public void findBySchemas_ReturnsDatasets() {
        ProductConfig productConfig = productConfigRepository.save(ProductPopulated.productConfig().build());
        Table savedTable = tableRepository.saveAndFlush(
                ProductPopulated.table()
                        .productId(productConfig.getId())
                        .physicalTableName("SHARED")
                        .displayName("SHARED")
                        .build());

        Table savedTable2 = tableRepository.saveAndFlush(
                ProductPopulated.table()
                        .productId(productConfig.getId())
                        .physicalTableName("SHARED_2")
                        .displayName("SHARED 2")
                        .build());

        datasetRepository.saveAndFlush(
                DatasetPopulated.dataset().stagingJobs(emptySet()).schema(savedTable).runKey(1L).build());
        datasetRepository.saveAndFlush(
                DatasetPopulated.dataset().stagingJobs(emptySet()).schema(savedTable).runKey(2L).build());
        datasetRepository.saveAndFlush(
                DatasetPopulated.dataset().stagingJobs(emptySet()).schema(savedTable2).runKey(3L).build());

        List<Dataset> datasetsWithSharedSchema = datasetRepository.findBySchemas(asList(savedTable, savedTable2));

        soft.assertThat(datasetsWithSharedSchema)
                .extracting(Dataset::getSchema)
                .extracting(Table::getPhysicalTableName)
                .containsExactly("SHARED", "SHARED", "SHARED_2");
    }

    @Test
    public void findByStagingJobsServiceRequestIdAndValidationStatus_ReturnsDatasets() {
        ProductConfig productConfig = productConfigRepository.save(ProductPopulated.productConfig().build());
        Table savedTable = tableRepository.saveAndFlush(
                ProductPopulated.table()
                        .productId(productConfig.getId())
                        .physicalTableName("SHARED")
                        .build());

        ServiceRequest request1 =
                serviceRequestRepository.saveAndFlush(JobPopulated.stagingJobServiceRequest().build());

        ServiceRequest request2 =
                serviceRequestRepository.saveAndFlush(JobPopulated.stagingJobServiceRequest().build());

        ServiceRequest request3 =
                serviceRequestRepository.saveAndFlush(JobPopulated.stagingJobServiceRequest().build());

        StagingDataset stagingDataset1 =
                stagingDatasetRepository.saveAndFlush(JobPopulated.stagingDataset()
                        .serviceRequestId(request1.getId()).build());

        StagingDataset stagingDataset2 =
                stagingDatasetRepository.saveAndFlush(JobPopulated.stagingDataset()
                        .serviceRequestId(request2.getId()).build());

        StagingDataset stagingDataset3 =
                stagingDatasetRepository.saveAndFlush(JobPopulated.stagingDataset()
                        .serviceRequestId(request3.getId()).build());

        Dataset dataset1 = datasetRepository.saveAndFlush(
                DatasetPopulated.dataset()
                        .name("one")
                        .schema(savedTable)
                        .validationStatus(ValidationStatus.QUEUED)
                        .build());

        dataset1.getStagingJobs().add(
                DatasetServiceRequest.builder()
                        .id(stagingDataset1.getId()).serviceRequestId(request1.getId())
                        .build());

        datasetRepository.saveAndFlush(dataset1);

        Dataset dataset2 = datasetRepository.saveAndFlush(
                DatasetPopulated.dataset()
                        .name("two")
                        .schema(savedTable)
                        .validationStatus(ValidationStatus.QUEUED)
                        .build());

        dataset2.getStagingJobs().add(
                DatasetServiceRequest.builder()
                        .id(stagingDataset2.getId()).serviceRequestId(request2.getId())
                        .build());

        datasetRepository.saveAndFlush(dataset2);

        Dataset dataset3 = datasetRepository.saveAndFlush(
                DatasetPopulated.dataset()
                        .name("three")
                        .schema(savedTable)
                        .validationStatus(ValidationStatus.NOT_VALIDATED)
                        .build());

        dataset3.getStagingJobs().add(DatasetServiceRequest.builder()
                .id(stagingDataset3.getId()).serviceRequestId(request3.getId())
                .build());

        datasetRepository.saveAndFlush(dataset3);

        List<Dataset> datasets1 = datasetRepository.findByStagingJobsServiceRequestIdAndValidationStatus(
                request1.getId(), ValidationStatus.QUEUED);

        soft.assertThat(datasets1).hasSize(1);
        soft.assertThat(datasets1.get(0).getName()).isEqualTo("one");

        List<Dataset> datasets2 =
                datasetRepository.findByStagingJobsServiceRequestIdAndValidationStatus(
                        request1.getId(), ValidationStatus.NOT_VALIDATED);

        assertThat(datasets2).isEmpty();
    }

    @Test
    public void findAllDatasetsOnlyByName_NameAndMetadata_ReturnsDatasets() {
        ProductConfig productConfig = productConfigRepository.save(ProductPopulated.productConfig().build());

        Table savedTable = tableRepository.saveAndFlush(
                ProductPopulated.table()
                        .productId(productConfig.getId())
                        .physicalTableName("SHARED")
                        .displayName("the display name")
                        .build());

        LocalDate referenceDate = LocalDate.of(2018, 1, 1);

        Dataset.DatasetBuilder metadataDataset = DatasetPopulated.dataset()
                .name("DatasetName")
                .entityCode("entity234")
                .referenceDate(referenceDate)
                .schema(savedTable)
                .stagingJobs(emptySet());

        datasetRepository.saveAll(ImmutableList.of(
                metadataDataset.recordsCount(1L).runKey(1L).build(),
                metadataDataset.recordsCount(2L).runKey(2L).build(),
                metadataDataset.recordsCount(3L).runKey(3L).build(),
                metadataDataset.recordsCount(4L).runKey(4L).build(),
                metadataDataset.recordsCount(5L).runKey(5L).build(),
                metadataDataset.recordsCount(6L).runKey(6L).build(),
                metadataDataset.recordsCount(7L).runKey(7L).build(),
                metadataDataset.recordsCount(8L).runKey(8L).build()
        ));

        PageRequest pageRequest = PageRequest.of(0, 4, Sort.by("recordsCount"));

        Page<DatasetOnly> datasets =
                datasetRepository.findAllDatasetsOnlyByName("DatasetName", "entity234", referenceDate, pageRequest);

        soft.assertThat(datasets.getTotalPages())
                .isEqualTo(2);
        soft.assertThat(datasets.getTotalElements())
                .isEqualTo(8);
        soft.assertThat(datasets.getContent())
                .extracting(DatasetOnly::getRecordsCount)
                .containsExactly(1L, 2L, 3L, 4L);
    }

    @Test
    public void findAllDatasetsOnlyByDisplayName_DisplayNameAndMetadata_ReturnsDatasets() {
        ProductConfig productConfig = productConfigRepository.save(ProductPopulated.productConfig().build());
        Table savedTable = tableRepository.saveAndFlush(
                ProductPopulated.table()
                        .productId(productConfig.getId())
                        .physicalTableName("SHARED")
                        .displayName("the display name")
                        .build());

        LocalDate referenceDate = LocalDate.of(2018, 1, 1);

        Dataset.DatasetBuilder metadataDataset = DatasetPopulated.dataset()
                .name("DatasetName")
                .entityCode("entity234")
                .referenceDate(referenceDate)
                .schema(savedTable)
                .stagingJobs(emptySet());

        datasetRepository.saveAll(ImmutableList.of(
                metadataDataset.recordsCount(1L).runKey(1L).build(),
                metadataDataset.recordsCount(2L).runKey(2L).build(),
                metadataDataset.recordsCount(3L).runKey(3L).build(),
                metadataDataset.recordsCount(4L).runKey(4L).build(),
                metadataDataset.recordsCount(5L).runKey(5L).build(),
                metadataDataset.recordsCount(6L).runKey(6L).build(),
                metadataDataset.recordsCount(7L).runKey(7L).build(),
                metadataDataset.recordsCount(8L).runKey(8L).build()
        ));

        PageRequest pageRequest = PageRequest.of(0, 4, Sort.by("recordsCount"));

        Page<DatasetOnly> datasets = datasetRepository.findAllDatasetsOnlyByDisplayName(
                "the display name", "entity234", referenceDate, pageRequest);

        soft.assertThat(datasets.getTotalPages())
                .isEqualTo(2);
        soft.assertThat(datasets.getTotalElements())
                .isEqualTo(8);
        soft.assertThat(datasets.getContent())
                .extracting(DatasetOnly::getRecordsCount)
                .containsExactly(1L, 2L, 3L, 4L);
    }

    @Test
    public void findAllDatasetsOnly_ReturnsDatasets() {
        ProductConfig productConfig = productConfigRepository.save(ProductPopulated.productConfig().build());
        Table tableWithoutRules = tableRepository.saveAndFlush(
                ProductPopulated.table()
                        .productId(productConfig.getId())
                        .physicalTableName("S1")
                        .displayName("display s1")
                        .fields(newHashSet())
                        .validationRules(newHashSet())
                        .build());
        Table tableWithRules = tableRepository.saveAndFlush(
                ProductPopulated.table()
                        .productId(productConfig.getId())
                        .physicalTableName("S2")
                        .displayName("display s2")
                        .fields(newHashSet())
                        .validationRules(newHashSet())
                        .build());
        tableWithRules.getValidationRules().addAll(newHashSet(
                ProductPopulated.validationRule().ruleId("r1").build(),
                ProductPopulated.validationRule().ruleId("r2").build()));

        tableWithRules = tableRepository.saveAndFlush(tableWithRules);

        ServiceRequest request1 =
                serviceRequestRepository.saveAndFlush(JobPopulated.stagingJobServiceRequest().build());

        ServiceRequest request2 =
                serviceRequestRepository.saveAndFlush(JobPopulated.stagingJobServiceRequest().build());

        StagingDataset stagingDataset1 = stagingDatasetRepository.saveAndFlush(
                JobPopulated.stagingDataset().serviceRequestId(request1.getId()).build());

        StagingDataset stagingDataset2 = stagingDatasetRepository.saveAndFlush(
                JobPopulated.stagingDataset().serviceRequestId(request2.getId()).build());

        LocalDate referenceDate = LocalDate.of(2018, 1, 1);
        Dataset dataset1 = datasetRepository.saveAndFlush(DatasetPopulated.dataset()
                .stagingJobs(emptySet())
                .schema(tableWithoutRules)
                .entityCode("ENTITY_456")
                .referenceDate(referenceDate)
                .runKey(1L)
                .build());

        Dataset dataset2 = datasetRepository.saveAndFlush(DatasetPopulated.dataset()
                .schema(tableWithRules)
                .entityCode("ENTITY_456")
                .referenceDate(referenceDate)
                .runKey(2L)
                .build());

        dataset2.setStagingJobs(newHashSet(
                DatasetServiceRequest.builder()
                        .id(stagingDataset1.getId()).serviceRequestId(request1.getId())
                        .build(),
                DatasetServiceRequest.builder()
                        .id(stagingDataset2.getId()).serviceRequestId(request2.getId())
                        .build()));

        Dataset lastSavedDataset = datasetRepository.saveAndFlush(dataset2);

        entityManager.clear();

        Page<DatasetOnly> datasets = datasetRepository.findAllDatasetsOnly(PageRequest.of(0, 10, DESC, "id"));

        soft.assertThat(datasets).hasSize(2);

        DatasetOnly datasetOnlyWithRules = datasets.getContent().get(0);
        soft.assertThat(datasetOnlyWithRules.hasRules()).isTrue();
        soft.assertThat(datasetOnlyWithRules.getId())
                .isEqualTo(lastSavedDataset.getId());
        soft.assertThat(datasetOnlyWithRules.getName())
                .isEqualTo(lastSavedDataset.getName());
        soft.assertThat(datasetOnlyWithRules.getSchemaDisplayName())
                .isEqualTo(lastSavedDataset.getSchema().getDisplayName());
        soft.assertThat(datasetOnlyWithRules.getSchemaId())
                .isEqualTo(lastSavedDataset.getSchema().getId());

        soft.assertThat(datasetOnlyWithRules.getRowKeySeed())
                .isEqualTo(lastSavedDataset.getRowKeySeed());
        soft.assertThat(datasetOnlyWithRules.getPredicate())
                .isEqualTo(lastSavedDataset.getPredicate());
        soft.assertThat(datasetOnlyWithRules.getCreatedTime())
                .isInSameMinuteWindowAs(lastSavedDataset.getCreatedTime());
        soft.assertThat(datasetOnlyWithRules.getRecordsCount())
                .isEqualTo(lastSavedDataset.getRecordsCount());

        soft.assertThat(datasetOnlyWithRules.getValidationJobId())
                .isEqualTo(lastSavedDataset.getLatestValidationJobId());
        soft.assertThat(datasetOnlyWithRules.getValidationStatus())
                .isEqualTo(lastSavedDataset.getValidationStatus().name());

        soft.assertThat(datasetOnlyWithRules.getEntityCode())
                .isEqualTo(lastSavedDataset.getEntityCode());
        soft.assertThat(datasetOnlyWithRules.getReferenceDate())
                .isEqualTo(lastSavedDataset.getReferenceDate());
        soft.assertThat(datasetOnlyWithRules.hasRules())
                .isEqualTo(isNotEmpty(lastSavedDataset.getSchema().getValidationRules()));

        soft.assertThat(datasetOnlyWithRules.getRunKey())
                .isEqualTo(lastSavedDataset.getRunKey());

        soft.assertThat(datasetOnlyWithRules.getLastUpdated())
                .isEqualTo(lastSavedDataset.getLastUpdated());

        DatasetOnly datasetOnlyWithoutRules = datasets.getContent().get(1);
        soft.assertThat(datasetOnlyWithoutRules.hasRules()).isFalse();
    }

    @Test
    public void findAllDatasetsOnly_DatasetWithPipelineInvocation_ReturnsDatasets() {
        ProductConfig productConfig = productConfigRepository.save(ProductPopulated.productConfig().build());

        Table savedSchema = tableRepository.saveAndFlush(tableRepository.saveAndFlush(
                ProductPopulated.table()
                        .productId(productConfig.getId())
                        .physicalTableName("S1")
                        .displayName("display s1")
                        .fields(newHashSet())
                        .validationRules(newHashSet())
                        .build()));

        Pipeline savedPipeline = pipelineService.savePipeline(ProductPopulated.pipeline()
                .steps(singleton(ProductPopulated.mapPipelineStep()
                        .schemaInId(savedSchema.getId())
                        .schemaOutId(savedSchema.getId())
                        .selects(emptySet())
                        .build()))
                .build());

        PipelineInvocation savedPipelineInvocation = pipelineInvocationJpaRepository.saveAndFlush(
                DatasetPopulated.pipelineInvocation()
                        .pipelineId(savedPipeline.getId())
                        .steps(singleton(DatasetPopulated.pipelineStepInvocation()
                                .pipelineStepId(savedPipeline.getSteps().iterator().next().getId())
                                .pipelineStep(savedPipeline.getSteps().iterator().next())
                                .inputDatasets(emptySet())
                                .build()))
                        .build());

        LocalDate referenceDate = LocalDate.of(2018, 1, 1);
        datasetRepository.saveAndFlush(DatasetPopulated.dataset()
                .schema(savedSchema)
                .entityCode("ENTITY_456")
                .referenceDate(referenceDate)
                .pipelineInvocationId(savedPipelineInvocation.getId())
                .pipelineStepInvocationId(savedPipelineInvocation.getSteps().iterator().next().getId())
                .stagingJobs(emptySet())
                .build());
        entityManager.clear();

        Page<DatasetOnly> datasets = datasetRepository.findAllDatasetsOnly(PageRequest.of(0, 10, DESC, "id"));

        soft.assertThat(datasets).hasSize(1);

        DatasetOnly datasetWithPipelineInvocation = datasets.getContent().get(0);

        soft.assertThat(datasetWithPipelineInvocation.getPipelineInvocationId())
                .isEqualTo(savedPipelineInvocation.getId());

        soft.assertThat(datasetWithPipelineInvocation.getPipelineStepInvocationId())
                .isEqualTo(savedPipelineInvocation.getSteps().iterator().next().getId());
    }

    @Test
    public void findLatestDatasetForSchemaId_ReturnsDatasets() {
        ProductConfig productConfig = productConfigRepository.save(ProductPopulated.productConfig().build());

        Table savedTable = tableRepository.saveAndFlush(
                ProductPopulated.table()
                        .productId(productConfig.getId())
                        .physicalTableName("FANGORN")
                        .build());

        Dataset notLatest = datasetRepository.saveAndFlush(
                DatasetPopulated.dataset()
                        .schema(savedTable)
                        .entityCode("TREE_BEARD")
                        .referenceDate(LocalDate.of(100, 1, 1))
                        .stagingJobs(emptySet())
                        .runKey(1L)
                        .build());

        Dataset latest = datasetRepository.saveAndFlush(
                DatasetPopulated.dataset()
                        .schema(savedTable)
                        .entityCode("TREE_BEARD")
                        .referenceDate(LocalDate.of(100, 1, 1))
                        .stagingJobs(emptySet())
                        .runKey(2L)
                        .build());

        Optional<Dataset> dataset1 = datasetRepository.findLatestDatasetForSchemaId(
                savedTable.getId(),
                "TREE_BEARD",
                LocalDate.of(100, 1, 1));

        assertThat(dataset1).hasValue(latest);

        Optional<Dataset> datasets2 = datasetRepository.findLatestDatasetForSchemaId(
                savedTable.getId(),
                "TREE_BEARD",
                LocalDate.of(101, 1, 1));

        assertThat(datasets2).isEmpty();
    }

    @Test
    public void findByPipelineInvocationIdAndSchemaId_ReturnsDatasets() {
        ProductConfig productConfig = productConfigRepository.save(ProductPopulated.productConfig().build());
        Table savedTable = tableRepository.saveAndFlush(
                ProductPopulated.table().productId(productConfig.getId()).physicalTableName("FANGORN").build());
        Pipeline savedPipeline = pipelineService.savePipeline(ProductPopulated.pipeline()
                .steps(singleton(ProductPopulated.mapPipelineStep()
                        .schemaInId(savedTable.getId())
                        .schemaOutId(savedTable.getId())
                        .selects(emptySet())
                        .build()))
                .build());
        PipelineInvocation savedPipelineInvocation = pipelineInvocationJpaRepository.saveAndFlush(
                DatasetPopulated.pipelineInvocation()
                        .pipelineId(savedPipeline.getId())
                        .steps(singleton(DatasetPopulated.pipelineStepInvocation()
                                .pipelineStepId(savedPipeline.getSteps().iterator().next().getId())
                                .pipelineStep(savedPipeline.getSteps().iterator().next())
                                .inputDatasets(emptySet())
                                .build()))
                        .build());

        Dataset created = datasetRepository.saveAndFlush(
                DatasetPopulated.dataset()
                        .schema(savedTable)
                        .pipelineInvocationId(savedPipelineInvocation.getId())
                        .stagingJobs(emptySet())
                        .build());

        Optional<Dataset> retrieved = datasetRepository.findByPipelineInvocationIdAndSchemaId(
                savedPipelineInvocation.getId(), savedTable.getId());

        assertThat(retrieved)
                .hasValue(created);
    }

    @SuppressWarnings({ "unchecked", "OptionalGetWithoutIsPresent" })
    @Test
    public void findLatestDataset() {
        ProductConfig productConfig = productConfigRepository.save(ProductPopulated.productConfig().build());

        Table savedTable =
                tableRepository.saveAndFlush(ProductPopulated.table().productId(productConfig.getId()).build());

        datasetRepository.saveAndFlush(DatasetPopulated.dataset()
                .schema(savedTable)
                .name("MY_NAME").runKey(1L).entityCode("EC").referenceDate(LocalDate.of(2019, 4, 1)).build());
        datasetRepository.saveAndFlush(DatasetPopulated.dataset()
                .schema(savedTable)
                .name("MY_NAME").runKey(2L).entityCode("EC").referenceDate(LocalDate.of(2019, 4, 1)).build());
        datasetRepository.saveAndFlush(DatasetPopulated.dataset()
                .schema(savedTable)
                .name("MY_NAME").runKey(3L).entityCode("EC").referenceDate(LocalDate.of(2019, 4, 1)).build());
        Dataset dataset4 = datasetRepository.saveAndFlush(DatasetPopulated.dataset()
                .schema(savedTable)
                .name("MY_NAME").runKey(4L).entityCode("EC").referenceDate(LocalDate.of(2019, 4, 1)).build());
        Dataset dataset5 = datasetRepository.saveAndFlush(DatasetPopulated.dataset()
                .schema(savedTable)
                .name("MY_NAME").runKey(1L).entityCode("OTHEREC").referenceDate(LocalDate.of(2019, 4, 1)).build());
        Dataset dataset6 = datasetRepository.saveAndFlush(DatasetPopulated.dataset()
                .schema(savedTable)
                .name("MY_NAME").runKey(1L).entityCode("EC").referenceDate(LocalDate.of(2020, 4, 1)).build());
        Dataset dataset7 = datasetRepository.saveAndFlush(DatasetPopulated.dataset()
                .schema(savedTable)
                .name("OTHER_NAME").runKey(1L).entityCode("EC").referenceDate(LocalDate.of(2019, 4, 1)).build());

        soft.assertThat(datasetRepository
                .findLatestDataset("MY_NAME", "EC", LocalDate.of(2019, 4, 1)).get())
                .extracting(
                        Dataset::getId,
                        Dataset::getName,
                        Dataset::getEntityCode,
                        Dataset::getReferenceDate,
                        Dataset::getRunKey)
                .containsExactly(
                        dataset4.getId(), "MY_NAME", "EC", LocalDate.of(2019, 4, 1), 4L);

        soft.assertThat(datasetRepository
                .findLatestDataset("MY_NAME", "OTHEREC", LocalDate.of(2019, 4, 1)).get())
                .extracting(
                        Dataset::getId,
                        Dataset::getName,
                        Dataset::getEntityCode,
                        Dataset::getReferenceDate,
                        Dataset::getRunKey)
                .containsExactly(
                        dataset5.getId(), "MY_NAME", "OTHEREC", LocalDate.of(2019, 4, 1), 1L);

        soft.assertThat(datasetRepository
                .findLatestDataset("MY_NAME", "EC", LocalDate.of(2020, 4, 1)).get())
                .extracting(
                        Dataset::getId,
                        Dataset::getName,
                        Dataset::getEntityCode,
                        Dataset::getReferenceDate,
                        Dataset::getRunKey)
                .containsExactly(
                        dataset6.getId(), "MY_NAME", "EC", LocalDate.of(2020, 4, 1), 1L);

        soft.assertThat(datasetRepository
                .findLatestDataset("OTHER_NAME", "EC", LocalDate.of(2019, 4, 1)).get())
                .extracting(
                        Dataset::getId,
                        Dataset::getName,
                        Dataset::getEntityCode,
                        Dataset::getReferenceDate,
                        Dataset::getRunKey)
                .containsExactly(
                        dataset7.getId(), "OTHER_NAME", "EC", LocalDate.of(2019, 4, 1), 1L);

        soft.assertThat(datasetRepository
                .findLatestDataset("doesn't exist", "EC", LocalDate.of(2019, 4, 1)))
                .isEmpty();
    }

    @SuppressWarnings({ "unchecked", "OptionalGetWithoutIsPresent" })
    @Test
    public void findByNameAndAndRunKeyAndAndReferenceDateAndEntityCode() {
        ProductConfig productConfig = productConfigRepository.save(ProductPopulated.productConfig().build());
        Table savedTable =
                tableRepository.saveAndFlush(ProductPopulated.table().productId(productConfig.getId()).build());

        LocalDate aprilFools = LocalDate.of(2019, 4, 1);

        Dataset dataset1 = datasetRepository.saveAndFlush(DatasetPopulated.dataset()
                .schema(savedTable)
                .name("MY_NAME").runKey(2L).entityCode("entity").referenceDate(aprilFools).build());
        Dataset dataset2 = datasetRepository.saveAndFlush(DatasetPopulated.dataset()
                .schema(savedTable)
                .name("MY_NAME").runKey(3L).entityCode("entity").referenceDate(aprilFools).build());
        Dataset dataset3 = datasetRepository.saveAndFlush(DatasetPopulated.dataset()
                .schema(savedTable)
                .name("MY_NAMEX").runKey(2L).entityCode("entity").referenceDate(aprilFools).build());
        Dataset dataset4 = datasetRepository.saveAndFlush(DatasetPopulated.dataset()
                .schema(savedTable)
                .name("MY_NAME").runKey(2L).entityCode("entity1").referenceDate(aprilFools).build());
        Dataset dataset5 = datasetRepository.saveAndFlush(DatasetPopulated.dataset()
                .schema(savedTable)
                .name("MY_NAME").runKey(2L).entityCode("entity").referenceDate(LocalDate.of(2019, 4, 2)).build());

        assertThat(datasetRepository.findByNameAndAndRunKeyAndAndReferenceDateAndEntityCode(
                "MY_NAME", 2L, aprilFools, "entity").get())
                .extracting(Dataset::getId)
                .isEqualTo(dataset1.getId());
    }
}
