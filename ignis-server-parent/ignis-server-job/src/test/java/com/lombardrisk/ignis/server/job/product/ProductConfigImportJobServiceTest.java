package com.lombardrisk.ignis.server.job.product;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.lombardrisk.ignis.client.external.fixture.ExternalClient;
import com.lombardrisk.ignis.client.external.job.JobStatus;
import com.lombardrisk.ignis.client.external.pipeline.export.PipelineExport;
import com.lombardrisk.ignis.client.external.productconfig.export.SchemaPeriod;
import com.lombardrisk.ignis.common.assertions.VavrAssert;
import com.lombardrisk.ignis.common.time.TimeSource;
import com.lombardrisk.ignis.data.common.Identifiable;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.pipeline.step.api.DrillbackColumnLink;
import com.lombardrisk.ignis.server.job.exception.JobStartException;
import com.lombardrisk.ignis.server.job.fixture.JobPopulated;
import com.lombardrisk.ignis.server.job.servicerequest.ServiceRequest;
import com.lombardrisk.ignis.server.job.servicerequest.ServiceRequestRepository;
import com.lombardrisk.ignis.server.job.staging.JobStarter;
import com.lombardrisk.ignis.server.product.fixture.ProductPopulated;
import com.lombardrisk.ignis.server.product.pipeline.PipelineImportService;
import com.lombardrisk.ignis.server.product.pipeline.PipelineService;
import com.lombardrisk.ignis.server.product.pipeline.PipelineStepService;
import com.lombardrisk.ignis.server.product.pipeline.model.Pipeline;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineJoinStep;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineStep;
import com.lombardrisk.ignis.server.product.productconfig.ProductConfigImportDiffer;
import com.lombardrisk.ignis.server.product.productconfig.ProductConfigImportEntityService;
import com.lombardrisk.ignis.server.product.productconfig.ProductConfigImportValidator;
import com.lombardrisk.ignis.server.product.productconfig.ProductConfigRepository;
import com.lombardrisk.ignis.server.product.productconfig.model.ImportStatus;
import com.lombardrisk.ignis.server.product.productconfig.model.ProductConfig;
import com.lombardrisk.ignis.server.product.productconfig.model.ProductConfigFileContents;
import com.lombardrisk.ignis.server.product.productconfig.model.ProductImportContext;
import com.lombardrisk.ignis.server.product.productconfig.model.ProductImportDiff;
import com.lombardrisk.ignis.server.product.productconfig.view.ProductConfigExportConverter;
import com.lombardrisk.ignis.server.product.rule.view.ValidationRuleExportConverter;
import com.lombardrisk.ignis.server.product.table.TableService;
import com.lombardrisk.ignis.server.product.table.model.Table;
import com.lombardrisk.ignis.server.product.table.view.FieldExportConverter;
import com.lombardrisk.ignis.server.product.table.view.SchemaExportConverter;
import com.lombardrisk.ignis.web.common.exception.GlobalExceptionHandler;
import io.vavr.control.Validation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static com.lombardrisk.ignis.common.fixtures.PopulatedDates.toDate;
import static com.lombardrisk.ignis.common.json.MapperWrapper.MAPPER;
import static com.lombardrisk.ignis.server.job.fixture.JobPopulated.usingJsonPath;
import static com.lombardrisk.ignis.server.product.fixture.ProductPopulated.decimalField;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Strict.class)
public class ProductConfigImportJobServiceTest {

    @Mock
    private ProductConfigRepository productRepository;
    @Mock
    private ProductConfigImportValidator productImportValidator;
    @Mock
    private ProductConfigImportDiffer productImportDiffer;
    @Mock
    private TableService tableService;
    @Mock
    private PipelineService pipelineService;
    @Mock
    private PipelineStepService pipelineStepService;
    @Mock
    private PipelineImportService pipelineImportService;
    @Mock
    private TimeSource timeSource;
    @Mock
    private ServiceRequestRepository serviceRequestRepository;
    @Mock
    private JobStarter jobStarter;

    private ProductConfigImportJobService productImporter;

    @Captor
    private ArgumentCaptor<ServiceRequest> serviceRequestCaptor;
    @Captor
    private ArgumentCaptor<ProductConfig> productConfigCaptor;
    @Captor
    private ArgumentCaptor<Iterable<Table>> schemasCaptor;

    @Before
    public void setup() {
        ProductConfigExportConverter productConfigExportConverter = new ProductConfigExportConverter(
                new SchemaExportConverter(
                        new FieldExportConverter(), new ValidationRuleExportConverter(), timeSource));

        ProductConfigImportEntityService productConfigImportEntityService =
                new ProductConfigImportEntityService(
                        productRepository,
                        timeSource,
                        tableService,
                        pipelineImportService);

        productImporter = new ProductConfigImportJobService(
                productRepository,
                productImportValidator,
                productImportDiffer,
                productConfigExportConverter,
                tableService,
                pipelineService,
                pipelineStepService,
                productConfigImportEntityService,
                serviceRequestRepository,
                jobStarter);

        when(productImportValidator.validateCanImport(any()))
                .thenReturn(emptyList());

        when(productImportDiffer.calculateDiff(any()))
                .thenReturn(ProductPopulated.productImportDiff().build());

        when(productRepository.save(any()))
                .thenReturn(ProductPopulated.productConfig().id(0L).build());

        when(serviceRequestRepository.save(any()))
                .thenReturn(JobPopulated.importProductJobServiceRequest().build());

        when(productImportValidator.validateCanRollback(any()))
                .thenReturn(Validation.valid(ProductPopulated.productConfig().build()));

        when(serviceRequestRepository.findById(any()))
                .thenReturn(Optional.of(JobPopulated.importProductJobServiceRequest().build()));

        when(pipelineImportService.importProductPipelines(any(), any(), any()))
                .thenReturn(Validation.valid(singletonList(ProductPopulated.pipeline().build())));
    }

    @Test
    public void importProductConfig_ReturnsProductConfigIdentifiable() {
        when(productRepository.save(any()))
                .thenReturn(ProductPopulated.productConfig().id(221L).build());

        Identifiable productConfig =
                productImporter.importProductConfig(ProductPopulated.productConfigFileContents().build(), null).get();

        assertThat(productConfig.getId())
                .isEqualTo(221L);
    }

    @Test
    public void importProductConfig_TransformationPipelineFlagEnabled_CallsPipelineImportWithExistingTables() {
        Table existingSchema = ProductPopulated.table().build();
        ProductConfig config = ProductPopulated.productConfig()
                .id(221L)
                .build();

        PipelineExport pipelineExport = ExternalClient.Populated.pipelineExport().build();

        when(productRepository.save(any()))
                .thenReturn(config);

        when(productImportDiffer.calculateDiff(any()))
                .thenReturn(ProductPopulated.productImportDiff()
                        .existingSchemas(singleton(existingSchema))
                        .build());

        VavrAssert.assertValid(
                productImporter.importProductConfig(
                        ProductPopulated.productConfigFileContents()
                                .pipelineExports(singletonList(pipelineExport))
                                .build(),
                        null));

        verify(pipelineImportService).importProductPipelines(
                singletonList(pipelineExport),
                221L,
                singleton(existingSchema));
    }

    @Test
    public void importProductConfig_SavesProductConfig() {
        when(productImportDiffer.calculateDiff(any()))
                .thenReturn(ProductPopulated.productImportDiff()
                        .productName("Prod")
                        .productVersion("2.1")
                        .build());
        when(timeSource.nowAsDate())
                .thenReturn(toDate("1999-09-09"));

        productImporter.importProductConfig(
                ProductPopulated.productConfigFileContents()
                        .productMetadata(ProductPopulated.productManifest()
                                .name("Prod")
                                .version("2.1")
                                .build())
                        .build(),
                null);

        verify(productRepository).save(productConfigCaptor.capture());

        assertThat(productConfigCaptor.getValue())
                .extracting(
                        ProductConfig::getName,
                        ProductConfig::getVersion,
                        ProductConfig::getImportStatus,
                        ProductConfig::getCreatedTime)
                .containsSequence("Prod", "2.1", ImportStatus.IN_PROGRESS, toDate("1999-09-09"));
    }

    @Test
    public void importProductConfig_SavesNewSchemasAndNewVersionedSchemas() {
        when(tableService.saveTables(any()))
                .then(answer -> answer.<Set<Table>>getArgument(0).stream()
                        .peek(table -> table.setId(0L))
                        .collect(toList()));

        when(productImportDiffer.calculateDiff(any()))
                .thenReturn(ProductPopulated.productImportDiff()
                        .existingSchemas(newHashSet(
                                ProductPopulated.table().physicalTableName("A").version(1).build()))
                        .newSchemas(newHashSet(
                                ProductPopulated.table().physicalTableName("C").version(1).build()))
                        .newVersionedSchemas(newHashSet(
                                ProductPopulated.table().physicalTableName("C").version(2).build()))
                        .build());

        productImporter.importProductConfig(ProductPopulated.productConfigFileContents().build(), null);

        verify(tableService, times(2)).saveTables(schemasCaptor.capture());

        Iterable<Table> newSchemas = schemasCaptor.getAllValues().get(0);
        Iterable<Table> newVersionedSchemas = schemasCaptor.getAllValues().get(1);

        assertThat(newSchemas)
                .extracting(Table::getVersionedName)
                .containsExactly("C v.1");

        assertThat(newVersionedSchemas)
                .extracting(Table::getVersionedName)
                .containsExactly("C v.2");
    }

    @Test
    public void importProductConfig_WithExistingSchemas_SavesProductConfigWithNewSchemasOnly() {
        when(tableService.saveTables(any()))
                .then(answer -> answer.<Set<Table>>getArgument(0).stream()
                        .peek(table -> table.setId(0L))
                        .collect(toList()));

        when(productRepository.save(any()))
                .then(invocation -> {
                    ProductConfig productConfig = (ProductConfig) invocation.getArguments()[0];
                    productConfig.setId(100L);
                    return productConfig;
                });

        when(productImportDiffer.calculateDiff(any()))
                .thenReturn(ProductPopulated.productImportDiff()
                        .existingSchemas(newHashSet(
                                ProductPopulated.table().physicalTableName("A").version(1).build(),
                                ProductPopulated.table().physicalTableName("B").version(1).build()))
                        .newSchemas(newHashSet(
                                ProductPopulated.table().physicalTableName("C").version(1).build()))
                        .newVersionedSchemas(newHashSet(
                                ProductPopulated.table().physicalTableName("C").version(2).build(),
                                ProductPopulated.table().physicalTableName("B").version(2).build()))
                        .build());

        productImporter.importProductConfig(ProductPopulated.productConfigFileContents().build(), null);

        verify(tableService, times(2)).saveTables(schemasCaptor.capture());

        List<Table> newProductSchemas = schemasCaptor.getAllValues()
                .stream()
                .flatMap(iterable -> ImmutableList.copyOf(iterable).stream())
                .filter(schema -> schema.getProductId().equals(100L))
                .collect(toList());

        assertThat(newProductSchemas)
                .extracting(Table::getVersionedName)
                .containsExactlyInAnyOrder("B v.2", "C v.1", "C v.2");
    }

    @Test
    public void importProductConfig_SavesExistingSchemasEndDates() {
        Table existingSchema1 = ProductPopulated.table().id(11L).build();
        Table existingSchema2 = ProductPopulated.table().id(22L).build();

        when(productImportDiffer.calculateDiff(any()))
                .thenReturn(ProductPopulated.productImportDiff()
                        .existingSchemaToNewPeriod(ImmutableMap.of(
                                existingSchema1,
                                SchemaPeriod.between(LocalDate.of(1970, 1, 1), LocalDate.of(1998, 9, 8)),
                                existingSchema2,
                                SchemaPeriod.between(LocalDate.of(2000, 1, 1), LocalDate.of(2001, 9, 8))))
                        .existingSchemas(newHashSet(existingSchema1, existingSchema2))
                        .build());

        productImporter.importProductConfig(ProductPopulated.productConfigFileContents().build(), null);

        verify(tableService).updatePeriod(
                11L,
                SchemaPeriod.between(LocalDate.of(1970, 1, 1), LocalDate.of(1998, 9, 8)));
        verify(tableService).updatePeriod(
                22L,
                SchemaPeriod.between(LocalDate.of(2000, 1, 1), LocalDate.of(2001, 9, 8)));
    }

    @Test
    public void importProductConfig_InvalidProductConfig_ReturnsValidationError() {
        when(productImportValidator.validateCanImport(any()))
                .thenReturn(singletonList(ErrorResponse.valueOf(null, "invalid")));

        Validation<List<ErrorResponse>, Identifiable> productConfigValidation =
                productImporter.importProductConfig(ProductPopulated.productConfigFileContents().build(), null);

        assertThat(productConfigValidation.getError())
                .extracting(ErrorResponse::getErrorCode)
                .containsExactly("invalid");
    }

    @Test
    public void importProductConfig_CannotStartImportJob_ReturnsValidationError() throws JobStartException {
        when(jobStarter.startJob(any(), any(), any()))
                .thenThrow(new JobStartException("cannot start"));

        Validation<List<ErrorResponse>, Identifiable> productConfigValidation =
                productImporter.importProductConfig(ProductPopulated.productConfigFileContents().build(), null);

        assertThat(productConfigValidation.getError())
                .extracting(ErrorResponse::getErrorCode)
                .containsExactly("IMPORT_PRODUCT_ERROR");
    }

    @Test
    public void importProductConfig_ThrowsExceptionBeforeStartingJob_SetsSerivceRequestStatusToABANDONED() throws JobStartException {
        when(jobStarter.startJob(any(), any(), any()))
                .thenThrow(NullPointerException.class);

        ServiceRequest serviceRequest = ServiceRequest.builder()
                .id(125L)
                .jobExecutionId(null)
                .build();

        when(serviceRequestRepository.save(any())).thenReturn(serviceRequest);
        when(serviceRequestRepository.findFirstById(any())).thenReturn(serviceRequest);

        productImporter.importProductConfig(ProductPopulated.productConfigFileContents().build(), null);

        verify(serviceRequestRepository, times(2)).save(serviceRequestCaptor.capture());
        List<ServiceRequest> serviceRequests = serviceRequestCaptor.getAllValues();
        ServiceRequest lastUpdate = serviceRequests.get(serviceRequests.size() - 1);

        assertThat(serviceRequest).isSameAs(lastUpdate);
        assertThat(lastUpdate.getStatus()).isEqualTo(JobStatus.ABANDONED);
    }

    @Test
    public void importProductConfig_ThrowsExceptionAfterStartingJob_KeepsServiceRequestStatusUnchanged() throws JobStartException {
        when(jobStarter.startJob(any(), any(), any()))
                .thenThrow(NullPointerException.class);

        ServiceRequest serviceRequest = ServiceRequest.builder()
                .id(125L)
                .status(JobStatus.STARTED)
                .jobExecutionId(4050L)
                .build();

        when(serviceRequestRepository.save(any())).thenReturn(serviceRequest);
        when(serviceRequestRepository.findFirstById(any())).thenReturn(serviceRequest);

        productImporter.importProductConfig(ProductPopulated.productConfigFileContents().build(), null);
        assertThat(serviceRequest.getStatus()).isEqualTo(JobStatus.STARTED);
    }

    @Test
    public void importProductConfig_ThrowsException_ReturnsUNEXPECTED_ERROR_RESPONSE() throws JobStartException {
        when(jobStarter.startJob(any(), any(), any()))
                .thenThrow(NullPointerException.class);

        Validation<List<ErrorResponse>, Identifiable> productConfigValidation =
                productImporter.importProductConfig(ProductPopulated.productConfigFileContents().build(), null);

        assertThat(productConfigValidation.getError())
                .extracting(ErrorResponse::getErrorCode)
                .contains(GlobalExceptionHandler.UNEXPECTED_ERROR_RESPONSE.getErrorCode());
    }

    @Test
    public void importProductConfig_WithNewSchemasOnly_SavesServiceRequestMessageWithNewSchemaIds() {
        when(productImportDiffer.calculateDiff(any()))
                .thenReturn(ProductPopulated.productImportDiff()
                        .newSchemas(newHashSet(
                                ProductPopulated.table().physicalTableName("T").id(1L).fields(newHashSet()).build(),
                                ProductPopulated.table().physicalTableName("A").id(2L).fields(newHashSet()).build(),
                                ProductPopulated.table().physicalTableName("B").id(3L).fields(newHashSet()).build()))
                        .existingSchemas(newHashSet())
                        .newVersionedSchemas(newHashSet())
                        .build());

        productImporter.importProductConfig(ProductPopulated.productConfigFileContents().build(), null);

        verify(serviceRequestRepository).save(serviceRequestCaptor.capture());

        String serviceRequestMsg = serviceRequestCaptor.getValue().getRequestMessage();

        usingJsonPath("$.newSchemaNameToId.T")
                .assertValue(serviceRequestMsg, equalTo(1));
        usingJsonPath("$.newSchemaNameToId.A")
                .assertValue(serviceRequestMsg, equalTo(2));
        usingJsonPath("$.newSchemaNameToId.B")
                .assertValue(serviceRequestMsg, equalTo(3));

        usingJsonPath("$.newSchemaNameToId.*")
                .assertValue(serviceRequestMsg, containsInAnyOrder(1, 2, 3));
    }

    @Test
    public void importProductConfig_NewSchemasAndNewVersionedSchemas_SavesServiceRequestMessageWithNewSchemaIdsAndNewFieldIds() {
        when(productImportDiffer.calculateDiff(any()))
                .thenReturn(ProductPopulated.productImportDiff()
                        .existingSchemas(newHashSet(
                                ProductPopulated.table().id(1L)
                                        .physicalTableName("A").version(1)
                                        .fields(newHashSet(
                                                decimalField("F1").id(11L).build()))
                                        .build()))
                        .newSchemas(newHashSet(
                                ProductPopulated.table().id(2L)
                                        .physicalTableName("B").version(1)
                                        .fields(newHashSet(
                                                decimalField("G1").id(21L).build()))
                                        .build()))
                        .newVersionedSchemas(newHashSet(
                                ProductPopulated.table().id(3L)
                                        .physicalTableName("A").version(2)
                                        .fields(newHashSet(
                                                decimalField("F1").id(31L).build(),
                                                decimalField("F2").id(32L).build()))
                                        .build(),
                                ProductPopulated.table().id(4L)
                                        .physicalTableName("B").version(2)
                                        .fields(newHashSet(
                                                decimalField("G1").id(41L).build(),
                                                decimalField("G2").id(42L).build(),
                                                decimalField("G3").id(43L).build()))
                                        .build()))
                        .build());

        productImporter.importProductConfig(ProductPopulated.productConfigFileContents().build(), null);

        verify(serviceRequestRepository).save(serviceRequestCaptor.capture());

        String serviceRequestMsg = serviceRequestCaptor.getValue().getRequestMessage();

        usingJsonPath("$.newSchemaNameToId.B")
                .assertValue(serviceRequestMsg, equalTo(2));

        usingJsonPath("$.schemaNameToNewFields.A.F2")
                .assertValue(serviceRequestMsg, equalTo(32));
        usingJsonPath("$.schemaNameToNewFields.A.*")
                .assertValue(serviceRequestMsg, contains(32));

        usingJsonPath("$.schemaNameToNewFields.B.G2")
                .assertValue(serviceRequestMsg, equalTo(42));
        usingJsonPath("$.schemaNameToNewFields.B.G3")
                .assertValue(serviceRequestMsg, equalTo(43));
        usingJsonPath("$.schemaNameToNewFields.B.*")
                .assertValue(serviceRequestMsg, containsInAnyOrder(42, 43));
    }

    @Test
    public void importProductConfig_NewSchemaPeriods_SavesServiceRequestMessageWithNewSchemaPeriods() {
        when(productImportDiffer.calculateDiff(any()))
                .thenReturn(ProductPopulated.productImportDiff()
                        .existingSchemaToNewPeriod(ImmutableMap.of(
                                ProductPopulated.table().id(1L).build(),
                                SchemaPeriod.between(LocalDate.of(1970, 1, 1), LocalDate.of(1999, 2, 20)),

                                ProductPopulated.table().id(2L).build(),
                                SchemaPeriod.between(LocalDate.of(2001, 1, 1), LocalDate.of(2005, 3, 18))))
                        .build());

        productImporter.importProductConfig(ProductPopulated.productConfigFileContents().build(), null);

        verify(serviceRequestRepository).save(serviceRequestCaptor.capture());

        String serviceRequestMsg = serviceRequestCaptor.getValue().getRequestMessage();

        usingJsonPath("$.existingSchemaIdToNewPeriod.*")
                .assertValue(serviceRequestMsg, hasSize(2));

        usingJsonPath("$.existingSchemaIdToNewPeriod.1.startDate")
                .assertValue(serviceRequestMsg, equalTo("1970-01-01"));
        usingJsonPath("$.existingSchemaIdToNewPeriod.1.endDate")
                .assertValue(serviceRequestMsg, equalTo("1999-02-20"));
        usingJsonPath("$.existingSchemaIdToNewPeriod.2.startDate")
                .assertValue(serviceRequestMsg, equalTo("2001-01-01"));
        usingJsonPath("$.existingSchemaIdToNewPeriod.2.endDate")
                .assertValue(serviceRequestMsg, equalTo("2005-03-18"));
    }

    @Test
    public void importProductConfig_NewSchemaPeriods_SavesServiceRequestMessageWithOldSchemaPeriods() {
        when(productImportDiffer.calculateDiff(any()))
                .thenReturn(ProductPopulated.productImportDiff()
                        .existingSchemaToNewPeriod(ImmutableMap.of(
                                ProductPopulated.table().id(3L)
                                        .startDate(LocalDate.of(2005, 5, 5))
                                        .endDate(LocalDate.of(2006, 6, 6))
                                        .build(),
                                SchemaPeriod.max(),

                                ProductPopulated.table().id(4L)
                                        .startDate(LocalDate.of(2015, 12, 15))
                                        .endDate(LocalDate.of(2016, 12, 16))
                                        .build(),
                                SchemaPeriod.max()))
                        .build());

        productImporter.importProductConfig(ProductPopulated.productConfigFileContents().build(), null);

        verify(serviceRequestRepository).save(serviceRequestCaptor.capture());

        String serviceRequestMsg = serviceRequestCaptor.getValue().getRequestMessage();

        usingJsonPath("$.existingSchemaIdToOldPeriod.*")
                .assertValue(serviceRequestMsg, hasSize(2));

        usingJsonPath("$.existingSchemaIdToOldPeriod.3.startDate")
                .assertValue(serviceRequestMsg, equalTo("2005-05-05"));
        usingJsonPath("$.existingSchemaIdToOldPeriod.3.endDate")
                .assertValue(serviceRequestMsg, equalTo("2006-06-06"));
        usingJsonPath("$.existingSchemaIdToOldPeriod.4.startDate")
                .assertValue(serviceRequestMsg, equalTo("2015-12-15"));
        usingJsonPath("$.existingSchemaIdToOldPeriod.4.endDate")
                .assertValue(serviceRequestMsg, equalTo("2016-12-16"));
    }

    @Test
    public void importProductConfig_PipelineStepIncluded_AddsDrillbackColumnsToContext() throws Exception {
        ProductConfigFileContents productConfigFileContents = ProductPopulated.productConfigFileContents().build();

        Pipeline.PipelineBuilder joinPipeline = ProductPopulated.pipeline()
                .steps(newHashSet(ProductPopulated.joinPipelineStep()
                        .schemaOut(ProductPopulated.schemaDetails()
                                .physicalTableName("SCHEMA_OUT")
                                .build())
                        .joins(newLinkedHashSet(asList(
                                ProductPopulated.join()
                                        .leftSchema(ProductPopulated.schemaDetails().physicalTableName("A").build())
                                        .rightSchema(ProductPopulated.schemaDetails().physicalTableName("B").build())
                                        .build(),
                                ProductPopulated.join()
                                        .leftSchema(ProductPopulated.schemaDetails().physicalTableName("B").build())
                                        .rightSchema(ProductPopulated.schemaDetails().physicalTableName("C").build())
                                        .build(),
                                ProductPopulated.join()
                                        .leftSchema(ProductPopulated.schemaDetails().physicalTableName("C").build())
                                        .rightSchema(ProductPopulated.schemaDetails().physicalTableName("D").build())
                                        .build())))
                        .build()));

        when(pipelineService.findByProductId(anyLong()))
                .thenReturn(asList(
                        joinPipeline.name("ONE").build(),
                        joinPipeline.name("TWO").build()));

        when(pipelineStepService.findAdditionalPhoenixDrillbackColumns(any(), any()))
                .thenReturn(newHashSet(
                        DrillbackColumnLink.builder().outputSchema("SCHEMA_OUT")
                                .inputSchema("A").inputColumn("ROW_KEY").inputColumnSqlType("BIGINT")
                                .build(),
                        DrillbackColumnLink.builder().outputSchema("SCHEMA_OUT")
                                .inputSchema("B").inputColumn("ROW_KEY").inputColumnSqlType("BIGINT")
                                .build(),
                        DrillbackColumnLink.builder().outputSchema("SCHEMA_OUT")
                                .inputSchema("C").inputColumn("ROW_KEY").inputColumnSqlType("BIGINT")
                                .build(),
                        DrillbackColumnLink.builder().outputSchema("SCHEMA_OUT")
                                .inputSchema("D").inputColumn("ROW_KEY").inputColumnSqlType("BIGINT")
                                .build()));

        productImporter.importProductConfig(productConfigFileContents, null);

        verify(serviceRequestRepository).save(serviceRequestCaptor.capture());
        String serviceRequestMsg = serviceRequestCaptor.getValue().getRequestMessage();

        ProductImportContext productImportContext = MAPPER.readValue(serviceRequestMsg, ProductImportContext.class);

        assertThat(productImportContext.getSchemaNameToDrillBackColumns())
                .hasSize(1);
        assertThat(productImportContext.getSchemaNameToDrillBackColumns().get("SCHEMA_OUT"))
                .extracting(DrillbackColumnLink::toDrillbackColumn)
                .containsOnlyOnce(
                        "FCR_SYS__A__ROW_KEY",
                        "FCR_SYS__B__ROW_KEY",
                        "FCR_SYS__C__ROW_KEY",
                        "FCR_SYS__D__ROW_KEY");
        assertThat(productImportContext.getSchemaNameToDrillBackColumns().get("SCHEMA_OUT"))
                .extracting(DrillbackColumnLink::getInputColumnSqlType)
                .containsOnly("BIGINT");
    }

    @Test
    public void importProductConfig_PipelineJoinStepIncluded_CallsPipelineStepService() {
        ProductConfigFileContents productConfigFileContents = ProductPopulated.productConfigFileContents().build();

        PipelineJoinStep step = ProductPopulated.joinPipelineStep()
                .schemaOut(ProductPopulated.schemaDetails()
                        .physicalTableName("SCHEMA_OUT")
                        .build())
                .joins(newLinkedHashSet(asList(
                        ProductPopulated.join()
                                .leftSchema(ProductPopulated.schemaDetails().physicalTableName("A").build())
                                .rightSchema(ProductPopulated.schemaDetails().physicalTableName("B").build())
                                .build(),
                        ProductPopulated.join()
                                .leftSchema(ProductPopulated.schemaDetails().physicalTableName("B").build())
                                .rightSchema(ProductPopulated.schemaDetails().physicalTableName("C").build())
                                .build(),
                        ProductPopulated.join()
                                .leftSchema(ProductPopulated.schemaDetails().physicalTableName("C").build())
                                .rightSchema(ProductPopulated.schemaDetails().physicalTableName("D").build())
                                .build())))
                .build();

        Pipeline.PipelineBuilder joinPipeline = ProductPopulated.pipeline().steps(newHashSet(step));

        when(pipelineService.findByProductId(anyLong()))
                .thenReturn(singletonList(joinPipeline.name("pipeline").build()));

        ProductImportDiff diff = ProductPopulated.productImportDiff().build();

        when(productImportDiffer.calculateDiff(any()))
                .thenReturn(diff);

        productImporter.importProductConfig(productConfigFileContents, null);

        ArgumentCaptor<PipelineStep> stepArgumentCaptor = ArgumentCaptor.forClass(PipelineStep.class);
        ArgumentCaptor<ProductImportDiff> diffArgumentCaptor = ArgumentCaptor.forClass(ProductImportDiff.class);

        verify(pipelineStepService).findAdditionalPhoenixDrillbackColumns(
                stepArgumentCaptor.capture(), diffArgumentCaptor.capture());

        assertThat(stepArgumentCaptor.getAllValues())
                .containsOnly(step);

        assertThat(diffArgumentCaptor.getValue())
                .isSameAs(diff);
    }

    @Test
    public void rollbackProductConfig_ReturnsProductConfigIdentifiable() {
        when(productImportValidator.validateCanRollback(any()))
                .thenReturn(Validation.valid(ProductPopulated.productConfig().id(123L).build()));

        Identifiable productConfig =
                productImporter.rollbackProductConfig(123L, null).get();

        assertThat(productConfig.getId())
                .isEqualTo(123L);
    }

    @Test
    public void rollbackProductConfig_InvalidProductConfigId_ReturnsValidationError() {
        when(productImportValidator.validateCanRollback(any()))
                .thenReturn(Validation.invalid(ErrorResponse.valueOf(null, "invalid")));

        Validation<ErrorResponse, Identifiable> productConfigValidation =
                productImporter.rollbackProductConfig(123L, null);

        assertThat(productConfigValidation.getError())
                .extracting(ErrorResponse::getErrorCode)
                .isEqualTo("invalid");
    }

    @Test
    public void rollbackProductConfig_CannotStartRollbackJob_ReturnsValidationError() throws JobStartException {
        when(jobStarter.startJob(any(), any(), any()))
                .thenThrow(new JobStartException("cannot start"));

        Validation<ErrorResponse, Identifiable> productConfigValidation =
                productImporter.rollbackProductConfig(123L, null);

        assertThat(productConfigValidation.getError())
                .extracting(ErrorResponse::getErrorCode)
                .isEqualTo("ROLLBACK_PRODUCT_ERROR");
    }

    @Test
    public void rollbackProductConfig_DeletesProductConfig() {
        productImporter.rollbackProductConfig(0L, null);

        verify(productRepository).deleteById(any());
    }

    @Test
    public void rollbackProductConfig_DeletesProductConfigAndPipelines() {
        productImporter.rollbackProductConfig(0L, null);

        verify(productRepository).deleteById(any());
        verify(pipelineService).deletePipelinesForProducts(any());
    }

    @Test
    public void rollbackProductConfig_AllSchemasAssociatedToAnotherProduct_DoesNotDeleteAnySchema() {
        when(productRepository.countByTablesId(any()))
                .thenReturn(2);

        when(productImportValidator.validateCanRollback(any()))
                .thenReturn(Validation.valid(ProductPopulated.productConfig()
                        .tables(newHashSet(
                                ProductPopulated.table().id(1L).build(),
                                ProductPopulated.table().id(1L).build(),
                                ProductPopulated.table().id(1L).build()))
                        .build()));

        productImporter.rollbackProductConfig(0L, null);

        verifyZeroInteractions(tableService);
    }

    @Test
    public void rollbackProductConfig_ProductConfigWithSchemasNotAssociatedToAnotherProduct_DeletesSchemas() {
        when(productImportValidator.validateCanRollback(any()))
                .thenReturn(Validation.valid(ProductPopulated.productConfig()
                        .tables(newHashSet(
                                ProductPopulated.table().id(1L).build(),
                                ProductPopulated.table().id(2L).build(),
                                ProductPopulated.table().id(3L).build()))
                        .build()));

        when(productRepository.countByTablesId(1L))
                .thenReturn(2);
        when(productRepository.countByTablesId(2L))
                .thenReturn(2);
        when(productRepository.countByTablesId(3L))
                .thenReturn(1);

        productImporter.rollbackProductConfig(0L, null);

        verify(tableService).delete(any());
    }

    @Test
    public void rollbackProductConfig_ProductConfigWithNewSchemaPeriods_UpdatesSchemasToOldPeriods() {
        when(productImportValidator.validateCanRollback(any()))
                .thenReturn(Validation.valid(
                        ProductPopulated.productConfig()
                                .id(123L)
                                .importRequestId(456L)
                                .build()));
        when(serviceRequestRepository.findById(456L))
                .thenReturn(Optional.of(
                        JobPopulated.stagingJobServiceRequest()
                                .requestMessage(("{ 'existingSchemaIdToOldPeriod': { "
                                        + "'3': { "
                                        + "'startDate': '1800-01-01',"
                                        + "'endDate': null"
                                        + "} } }").replace("'", "\""))
                                .build()));

        productImporter.rollbackProductConfig(123L, null);

        verify(tableService).updatePeriod(3L, SchemaPeriod.between(LocalDate.of(1800, 1, 1), null));
    }

    @Test
    public void rollbackProductConfig_MissingImportRequest_ReturnsValidationError() {
        when(productImportValidator.validateCanRollback(any()))
                .thenReturn(Validation.valid(
                        ProductPopulated.productConfig()
                                .id(123L)
                                .importRequestId(456L)
                                .build()));
        when(serviceRequestRepository.findById(456L))
                .thenReturn(Optional.empty());

        Validation<ErrorResponse, Identifiable> rollbackValidation =
                productImporter.rollbackProductConfig(123L, null);

        assertThat(rollbackValidation.getError())
                .extracting(ErrorResponse::getErrorCode)
                .isEqualTo("MISSING_IMPORT_REQUEST");
    }
}
