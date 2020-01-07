package com.lombardrisk.ignis.server.staging;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.lombardrisk.ignis.client.external.fixture.ExternalClient;
import com.lombardrisk.ignis.client.external.job.staging.request.v1.StagingRequest;
import com.lombardrisk.ignis.common.assertions.VavrAssert;
import com.lombardrisk.ignis.common.fixtures.PopulatedDates;
import com.lombardrisk.ignis.common.time.TimeSource;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.feature.IgnisFeature;
import com.lombardrisk.ignis.hadoop.HdfsTemplate;
import com.lombardrisk.ignis.server.batch.staging.HdfsDatasetConf;
import com.lombardrisk.ignis.server.batch.staging.file.LocalFileService;
import com.lombardrisk.ignis.server.batch.staging.file.S3FileService;
import com.lombardrisk.ignis.server.batch.staging.file.error.HDFSErrorFileService;
import com.lombardrisk.ignis.server.batch.staging.file.error.S3ErrorFileService;
import com.lombardrisk.ignis.server.fixtures.Populated;
import com.lombardrisk.ignis.server.job.fixture.JobPopulated;
import com.lombardrisk.ignis.server.job.servicerequest.ServiceRequest;
import com.lombardrisk.ignis.server.job.servicerequest.ServiceRequestRepository;
import com.lombardrisk.ignis.server.job.staging.JobStarter;
import com.lombardrisk.ignis.server.job.staging.StagingDatasetRepository;
import com.lombardrisk.ignis.server.job.staging.StagingDatasetService;
import com.lombardrisk.ignis.server.job.staging.factory.StagingDatasetServiceFactory;
import com.lombardrisk.ignis.server.job.staging.file.ErrorFileLinkOrStream;
import com.lombardrisk.ignis.server.job.staging.model.StagingDataset;
import com.lombardrisk.ignis.server.product.fixture.ProductPopulated;
import com.lombardrisk.ignis.server.product.pipeline.PipelineService;
import com.lombardrisk.ignis.server.product.pipeline.model.Pipeline;
import com.lombardrisk.ignis.server.product.table.TableService;
import com.lombardrisk.ignis.server.product.table.model.DecimalField;
import com.lombardrisk.ignis.server.product.table.model.Table;
import com.lombardrisk.ignis.spark.api.staging.DatasetProperties;
import com.lombardrisk.ignis.spark.api.staging.DownstreamPipeline;
import com.lombardrisk.ignis.spark.api.staging.StagingAppConfig;
import com.lombardrisk.ignis.spark.api.staging.StagingDatasetConfig;
import com.lombardrisk.ignis.spark.api.staging.StagingErrorOutput;
import com.lombardrisk.ignis.spark.api.staging.StagingSchemaValidation;
import com.lombardrisk.ignis.spark.api.staging.datasource.DataSource;
import com.lombardrisk.ignis.spark.api.staging.datasource.HdfsCsvDataSource;
import com.lombardrisk.ignis.spark.api.staging.datasource.S3CsvDataSource;
import com.lombardrisk.ignis.spark.api.staging.field.BooleanFieldValidation;
import com.lombardrisk.ignis.spark.api.staging.field.DateFieldValidation;
import com.lombardrisk.ignis.spark.api.staging.field.DecimalFieldValidation;
import com.lombardrisk.ignis.spark.api.staging.field.DoubleFieldValidation;
import com.lombardrisk.ignis.spark.api.staging.field.FieldValidation;
import com.lombardrisk.ignis.spark.api.staging.field.FloatFieldValidation;
import com.lombardrisk.ignis.spark.api.staging.field.IntegerFieldValidation;
import com.lombardrisk.ignis.spark.api.staging.field.LongFieldValidation;
import com.lombardrisk.ignis.spark.api.staging.field.StringFieldValidation;
import com.lombardrisk.ignis.spark.api.staging.field.TimestampFieldValidation;
import io.vavr.control.Validation;
import org.apache.commons.lang3.time.DateUtils;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.togglz.core.manager.FeatureManager;
import org.togglz.junit.TogglzRule;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static com.lombardrisk.ignis.common.json.MapperWrapper.MAPPER;
import static com.lombardrisk.ignis.server.job.staging.file.ErrorFileLinkOrStream.FileNameAndStream;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class StagingDatasetServiceDeepTest {

    @RunWith(MockitoJUnitRunner.class)
    public static class StartJobLocalProductVersioningEnabled {

        @Rule
        public TogglzRule togglzRule = TogglzRule.allEnabled(IgnisFeature.class);

        @Rule
        public JUnitSoftAssertions soft = new JUnitSoftAssertions();

        @Mock
        private TableService tableService;

        @Mock
        private PipelineService pipelineService;

        @Mock
        private StagingDatasetRepository stagingDatasetRepository;

        @Mock
        private ServiceRequestRepository serviceRequestRepository;

        @Mock
        private JobStarter jobStarter;
        @Mock
        private FeatureManager featureManager;

        @Captor
        private ArgumentCaptor<ServiceRequest> serviceRequestArgumentCaptor;
        @Captor
        private ArgumentCaptor<StagingDataset> stagingDatasetArgumentCaptor;

        private StagingDatasetServiceFactory.StagingDatasetServiceFactoryBuilder datasetServiceFactoryBuilder;

        @Before
        public void setUp() {

            when(tableService.findTable(anyString(), any()))
                    .thenReturn(Optional.of(ProductPopulated.table().build()));

            when(pipelineService.findByName(any()))
                    .thenReturn(Validation.invalid(CRUDFailure.cannotFind("Pipeline").asFailure()));

            when(stagingDatasetRepository.save(any()))
                    .then(invocation -> {
                        StagingDataset stagingDataset = invocation.getArgument(0);
                        if (stagingDataset != null) {
                            stagingDataset.setId(1L);
                        }
                        return stagingDataset;
                    });

            when(serviceRequestRepository.save(any()))
                    .thenAnswer(invocation -> {
                        ServiceRequest serviceRequest = invocation.getArgument(0);
                        if (serviceRequest != null) {
                            serviceRequest.setId(101L);
                        }
                        return serviceRequest;
                    });

            HdfsDatasetConf datasetConf = new HdfsDatasetConf();
            datasetConf.setLocalPath("test/local/path");
            datasetConf.setRemotePath("hdfs://user/ubuntu/remote");

            datasetServiceFactoryBuilder = StagingDatasetServiceFactory.builder()
                    .dataSourceService(new LocalFileService(datasetConf))
                    .errorFileService(new HDFSErrorFileService(datasetConf, null))
                    .jobStarter(jobStarter)
                    .timeSource(new TimeSource(Clock.systemUTC()))
                    .tableService(tableService)
                    .pipelineService(pipelineService)
                    .stagingDatasetRepository(stagingDatasetRepository)
                    .serviceRequestRepository(serviceRequestRepository)
                    .featureManager(featureManager);
        }

        @Test
        public void startStagingJob_ReturnsId() throws Exception {
            when(jobStarter.startJob(any(), any(), any()))
                    .thenReturn(23L);

            StagingDatasetService datasetService = datasetServiceFactoryBuilder.build()
                    .getStagingDatasetService();

            VavrAssert.assertValid(
                    datasetService
                            .start(ExternalClient.Populated.stagingRequest().build(), "userName"))
                    .withResult(23L);
        }

        @Test
        public void startJob_OneStagingItem_PopulatesServiceRequestMessageWithCreatedStagingDatasetId() throws Exception {
            StagingRequest stagingRequest = ExternalClient.Populated.stagingRequest()
                    .items(newHashSet(ExternalClient.Populated.stagingItemRequest().build()))
                    .build();

            when(stagingDatasetRepository.save(any()))
                    .thenAnswer(invocation -> {
                        StagingDataset stagingDataset = invocation.getArgument(0);
                        stagingDataset.setId(991L);
                        return stagingDataset;
                    });

            StagingDatasetService datasetService = datasetServiceFactoryBuilder.build()
                    .getStagingDatasetService();

            VavrAssert.assertValid(
                    datasetService.start(stagingRequest, "me")
            );

            verify(jobStarter).startJob(any(), serviceRequestArgumentCaptor.capture(), any());

            ServiceRequest serviceRequest = serviceRequestArgumentCaptor.getValue();

            StagingAppConfig stagingAppConfigMessage = MAPPER.readValue(
                    serviceRequest.getRequestMessage(), StagingAppConfig.class);

            StagingDatasetConfig stagingDatasetConfig = stagingAppConfigMessage.getItems()
                    .iterator().next();

            assertThat(stagingDatasetConfig.getId())
                    .isEqualTo(991L);
        }

        @Test
        public void startJob_SchemaDoesNotExist_ReturnsCrudFailure() throws Exception {
            StagingRequest stagingRequest = ExternalClient.Populated.stagingRequest()
                    .items(newHashSet(
                            ExternalClient.Populated.stagingItemRequest().schema("FIND ME")
                                    .dataset(ExternalClient.Populated.datasetMetadata()
                                            .referenceDate("01/12/1991")
                                            .build())
                                    .build()
                    ))
                    .build();

            when(tableService.findTable(anyString(), any()))
                    .thenReturn(Optional.empty());

            StagingDatasetService datasetService = datasetServiceFactoryBuilder.build()
                    .getStagingDatasetService();

            Validation<List<CRUDFailure>, Long> result = datasetService.start(stagingRequest, "me");

            List<CRUDFailure> validation = VavrAssert.assertFailed(result)
                    .getValidation();

            assertThat(validation).hasSize(1);

            ErrorResponse errorResponse = validation.get(0).toErrorResponse();

            soft.assertThat(errorResponse.getErrorCode())
                    .isEqualTo("NOT_FOUND");

            soft.assertThat(errorResponse.getErrorMessage())
                    .contains("FIND ME", "1991-12-01");
        }

        @Test
        public void startJob_WithJobName_PopulatesServiceRequestMessageWithJobNameAndId() throws Exception {
            StagingRequest stagingRequest = ExternalClient.Populated.stagingRequest()
                    .name("JOB TO BE DONE")
                    .build();

            when(serviceRequestRepository.save(any()))
                    .thenAnswer(invocation -> {
                        ServiceRequest serviceRequest = invocation.getArgument(0);
                        serviceRequest.setId(8291L);
                        return serviceRequest;
                    });

            StagingDatasetService datasetService = datasetServiceFactoryBuilder.build()
                    .getStagingDatasetService();

            VavrAssert.assertValid(
                    datasetService.start(stagingRequest, "me")
            );

            verify(jobStarter).startJob(any(), serviceRequestArgumentCaptor.capture(), any());

            ServiceRequest serviceRequest = serviceRequestArgumentCaptor.getValue();

            StagingAppConfig stagingAppConfigMessage = MAPPER.readValue(
                    serviceRequest.getRequestMessage(), StagingAppConfig.class);

            assertThat(stagingAppConfigMessage.getName())
                    .isEqualTo("JOB TO BE DONE");
            assertThat(stagingAppConfigMessage.getServiceRequestId())
                    .isEqualTo(8291L);
        }

        @Test
        public void startJob_CreatesServiceRequestWithHdfsDatasource() throws Exception {

            StagingRequest stagingRequest = ExternalClient.Populated.stagingRequest()
                    .items(newHashSet(
                            ExternalClient.Populated.stagingItemRequest()
                                    .source(ExternalClient.Populated.dataSource()
                                            .filePath("file1.csv")
                                            .header(true)
                                            .build())
                                    .build()))
                    .build();

            when(tableService.findTable(anyString(), any()))
                    .thenReturn(Optional.of(ProductPopulated.table().physicalTableName("TABLE").build()));

            when(serviceRequestRepository.save(any()))
                    .thenAnswer(invocation -> {
                        ServiceRequest serviceRequest = invocation.getArgument(0);
                        serviceRequest.setId(101L);
                        return serviceRequest;
                    });

            HdfsDatasetConf datasetConf = new HdfsDatasetConf();
            datasetConf.setRemotePath("hdfs://user/ubuntu/remote");

            StagingDatasetService datasetService = datasetServiceFactoryBuilder
                    .dataSourceService(new LocalFileService(datasetConf))
                    .build()
                    .getStagingDatasetService();

            VavrAssert.assertValid(
                    datasetService.start(stagingRequest, "me")
            );

            verify(jobStarter).startJob(any(), serviceRequestArgumentCaptor.capture(), any());

            ServiceRequest serviceRequest = serviceRequestArgumentCaptor.getValue();

            StagingAppConfig stagingAppConfigMessage =
                    MAPPER.readValue(serviceRequest.getRequestMessage(), StagingAppConfig.class);

            DataSource source = stagingAppConfigMessage.getItems()
                    .iterator().next().getSource();

            assertThat(source)
                    .isEqualTo(HdfsCsvDataSource.builder()
                            .hdfsPath("hdfs://user/ubuntu/remote/101/TABLE.csv")
                            .localPath("file1.csv")
                            .header(true)
                            .build());
        }

        @Test
        public void startJob_CreatesServiceRequestWithHDFSErrorFileOutput() throws Exception {
            StagingRequest stagingRequest = ExternalClient.Populated.stagingRequest()
                    .items(newHashSet(ExternalClient.Populated.stagingItemRequest().build()))
                    .build();

            when(tableService.findTable(anyString(), any()))
                    .thenReturn(Optional.of(ProductPopulated.table().physicalTableName("TABLE").build()));

            when(serviceRequestRepository.save(any()))
                    .thenAnswer(invocation -> {
                        ServiceRequest serviceRequest = invocation.getArgument(0);
                        serviceRequest.setId(118L);
                        return serviceRequest;
                    });

            HdfsDatasetConf datasetConf = new HdfsDatasetConf();
            datasetConf.setRemotePath("hdfs://user/ubuntu/error/remote");

            StagingDatasetService datasetService = datasetServiceFactoryBuilder
                    .errorFileService(new HDFSErrorFileService(datasetConf, null))
                    .build()
                    .getStagingDatasetService();

            VavrAssert.assertValid(
                    datasetService.start(stagingRequest, "me")
            );

            verify(jobStarter).startJob(any(), serviceRequestArgumentCaptor.capture(), any());

            ServiceRequest serviceRequest = serviceRequestArgumentCaptor.getValue();

            StagingAppConfig stagingAppConfigMessage =
                    MAPPER.readValue(serviceRequest.getRequestMessage(), StagingAppConfig.class);

            StagingErrorOutput stagingErrorOutput = stagingAppConfigMessage.getItems()
                    .iterator().next().getStagingErrorOutput();

            assertThat(stagingErrorOutput)
                    .isEqualTo(StagingErrorOutput.builder()
                            .errorFileSystemUri("hdfs://user/ubuntu/error/remote")
                            .temporaryFilePath("hdfs://user/ubuntu/error/remote/tmp-error-dataset/1")
                            .errorFilePath("hdfs://user/ubuntu/error/remote/118/E_TABLE.csv")
                            .build());
        }

        @Test
        public void startJob_CreatesServiceRequestWithSchemaFields() throws Exception {
            StagingRequest stagingRequest = ExternalClient.Populated.stagingRequest()
                    .items(newHashSet(ExternalClient.Populated.stagingItemRequest().build()))
                    .build();

            Table table = ProductPopulated.table()
                    .fields(newLinkedHashSet(Arrays.asList(
                            ProductPopulated.booleanField("boolean").build(),
                            ProductPopulated.dateField("date").build(),
                            ProductPopulated.decimalField("decimal").build(),
                            ProductPopulated.doubleField("double").build(),
                            ProductPopulated.floatField("float").build(),
                            ProductPopulated.intField("int").build(),
                            ProductPopulated.longField("long").build(),
                            ProductPopulated.stringField("string").build(),
                            ProductPopulated.timestampField("timestamp").build()
                    )))
                    .build();
            when(tableService.findTable(anyString(), any()))
                    .thenReturn(Optional.of(table));

            StagingDatasetService datasetService = datasetServiceFactoryBuilder
                    .build()
                    .getStagingDatasetService();

            VavrAssert.assertValid(
                    datasetService.start(stagingRequest, "me")
            );

            verify(jobStarter).startJob(any(), serviceRequestArgumentCaptor.capture(), any());

            ServiceRequest serviceRequest = serviceRequestArgumentCaptor.getValue();

            StagingAppConfig stagingAppConfigMessage =
                    MAPPER.readValue(serviceRequest.getRequestMessage(), StagingAppConfig.class);

            StagingSchemaValidation stagingSchemaValidation = stagingAppConfigMessage.getItems().iterator().next()
                    .getStagingSchemaValidation();

            assertThat(stagingSchemaValidation.getFields())
                    .extracting(FieldValidation::getName, Object::getClass)
                    .containsExactly(
                            tuple("boolean", BooleanFieldValidation.class),
                            tuple("date", DateFieldValidation.class),
                            tuple("decimal", DecimalFieldValidation.class),
                            tuple("double", DoubleFieldValidation.class),
                            tuple("float", FloatFieldValidation.class),
                            tuple("int", IntegerFieldValidation.class),
                            tuple("long", LongFieldValidation.class),
                            tuple("string", StringFieldValidation.class),
                            tuple("timestamp", TimestampFieldValidation.class)
                    );
        }

        @Test
        public void startJob_SchemaWithDecimalField_CreatesServiceRequestWithDecimalField() throws Exception {
            StagingRequest stagingRequest = ExternalClient.Populated.stagingRequest()
                    .items(newHashSet(ExternalClient.Populated.stagingItemRequest().build()))
                    .build();

            Table table = ProductPopulated.table()
                    .fields(newLinkedHashSet(singletonList(
                            ProductPopulated.decimalField("decimal")
                                    .precision(10)
                                    .scale(1)
                                    .nullable(true)
                                    .build())))
                    .build();
            when(tableService.findTable(anyString(), any()))
                    .thenReturn(Optional.of(table));

            StagingDatasetService datasetService = datasetServiceFactoryBuilder.build()
                    .getStagingDatasetService();

            VavrAssert.assertValid(
                    datasetService.start(stagingRequest, "me")
            );

            verify(jobStarter).startJob(any(), serviceRequestArgumentCaptor.capture(), any());

            ServiceRequest serviceRequest = serviceRequestArgumentCaptor.getValue();

            StagingAppConfig stagingAppConfigMessage =
                    MAPPER.readValue(serviceRequest.getRequestMessage(), StagingAppConfig.class);

            StagingSchemaValidation stagingSchemaValidation = stagingAppConfigMessage.getItems().iterator().next()
                    .getStagingSchemaValidation();

            soft.assertThat(stagingSchemaValidation.getFields())
                    .hasSize(1);
            soft.assertThat(stagingSchemaValidation.getFields().iterator().next())
                    .isEqualTo(DecimalFieldValidation.builder()
                            .name("decimal")
                            .precision(10)
                            .scale(1)
                            .nullable(true)
                            .build());
        }

        @Test
        public void startJob_SchemaWithStringField_CreatesServiceRequestWithStringField() throws Exception {
            StagingRequest stagingRequest = ExternalClient.Populated.stagingRequest()
                    .items(newHashSet(ExternalClient.Populated.stagingItemRequest().build()))
                    .build();

            Table table = ProductPopulated.table()
                    .fields(newLinkedHashSet(singletonList(
                            ProductPopulated.stringField("string")
                                    .maxLength(10)
                                    .minLength(1)
                                    .regularExpression("min|max")
                                    .nullable(false)
                                    .build())))
                    .build();

            when(tableService.findTable(anyString(), any()))
                    .thenReturn(Optional.of(table));

            StagingDatasetService datasetService = datasetServiceFactoryBuilder.build()
                    .getStagingDatasetService();

            VavrAssert.assertValid(
                    datasetService.start(stagingRequest, "me")
            );

            verify(jobStarter).startJob(any(), serviceRequestArgumentCaptor.capture(), any());

            ServiceRequest serviceRequest = serviceRequestArgumentCaptor.getValue();

            StagingAppConfig stagingAppConfigMessage =
                    MAPPER.readValue(serviceRequest.getRequestMessage(), StagingAppConfig.class);

            StagingSchemaValidation stagingSchemaValidation = stagingAppConfigMessage.getItems().iterator().next()
                    .getStagingSchemaValidation();

            soft.assertThat(stagingSchemaValidation.getFields())
                    .hasSize(1);
            soft.assertThat(stagingSchemaValidation.getFields().iterator().next())
                    .isEqualTo(StringFieldValidation.builder()
                            .name("string")
                            .maxLength(10)
                            .minLength(1)
                            .regularExpression("min|max")
                            .nullable(false)
                            .build());
        }

        @Test
        public void startJob_SchemaWithDateFieldCreatesServiceRequestWithDateField() throws Exception {
            StagingRequest stagingRequest = ExternalClient.Populated.stagingRequest()
                    .items(newHashSet(ExternalClient.Populated.stagingItemRequest().build()))
                    .build();

            Table table = ProductPopulated.table()
                    .fields(newLinkedHashSet(singletonList(
                            ProductPopulated.dateField("date")
                                    .format("YYYY-MM-dd")
                                    .nullable(false)
                                    .build())))
                    .build();

            when(tableService.findTable(anyString(), any()))
                    .thenReturn(Optional.of(table));

            StagingDatasetService datasetService = datasetServiceFactoryBuilder.build()
                    .getStagingDatasetService();

            VavrAssert.assertValid(
                    datasetService.start(stagingRequest, "me")
            );

            verify(jobStarter).startJob(any(), serviceRequestArgumentCaptor.capture(), any());

            ServiceRequest serviceRequest = serviceRequestArgumentCaptor.getValue();

            StagingAppConfig stagingAppConfigMessage =
                    MAPPER.readValue(serviceRequest.getRequestMessage(), StagingAppConfig.class);

            StagingSchemaValidation stagingSchemaValidation = stagingAppConfigMessage.getItems().iterator().next()
                    .getStagingSchemaValidation();

            soft.assertThat(stagingSchemaValidation.getFields())
                    .hasSize(1);
            soft.assertThat(stagingSchemaValidation.getFields().iterator().next())
                    .isEqualTo(DateFieldValidation.builder()
                            .name("date")
                            .format("YYYY-MM-dd")
                            .nullable(false)
                            .build());
        }

        @Test
        public void startJob_OneStagingItemWithMetadata_PopulatesServiceRequestMessage() throws Exception {
            StagingRequest stagingRequest = ExternalClient.Populated.stagingRequest()
                    .items(newHashSet(ExternalClient.Populated.stagingItemRequest()
                            .dataset(Populated.datasetMetadata()
                                    .entityCode("FCR")
                                    .referenceDate("01/12/1991")
                                    .build())
                            .build()))
                    .build();

            StagingDatasetService datasetService = datasetServiceFactoryBuilder.build()
                    .getStagingDatasetService();

            VavrAssert.assertValid(
                    datasetService.start(stagingRequest, "me")
            );

            verify(jobStarter).startJob(any(), serviceRequestArgumentCaptor.capture(), any());

            ServiceRequest serviceRequest = serviceRequestArgumentCaptor.getValue();

            StagingAppConfig stagingAppConfigMessage = MAPPER.readValue(
                    serviceRequest.getRequestMessage(), StagingAppConfig.class);

            DatasetProperties datasetProperties = stagingAppConfigMessage.getItems()
                    .iterator().next().getDatasetProperties();

            assertThat(datasetProperties.getEntityCode())
                    .isEqualTo("FCR");
            assertThat(datasetProperties.getReferenceDate())
                    .isEqualTo(LocalDate.of(1991, 12, 1));
        }

        @Test
        public void startJob_OneStagingItem_PopulatesServiceRequestMessageWithSchemaValidation() throws Exception {
            StagingRequest stagingRequest = ExternalClient.Populated.stagingRequest()
                    .items(newHashSet(ExternalClient.Populated.stagingItemRequest()
                            .schema("TABLE")
                            .build()))
                    .build();

            DecimalField decimalField = ProductPopulated.decimalField("decimal1").build();

            when(tableService.findTable(anyString(), any()))
                    .thenReturn(Optional.of(ProductPopulated.table()
                            .displayName("TABLE")
                            .physicalTableName("TBL")
                            .id(10202L)
                            .build()));

            StagingDatasetService datasetService = datasetServiceFactoryBuilder.build()
                    .getStagingDatasetService();

            VavrAssert.assertValid(
                    datasetService.start(stagingRequest, "me")
            );

            verify(jobStarter).startJob(any(), serviceRequestArgumentCaptor.capture(), any());

            ServiceRequest serviceRequest = serviceRequestArgumentCaptor.getValue();

            StagingAppConfig stagingAppConfigMessage = MAPPER.readValue(
                    serviceRequest.getRequestMessage(), StagingAppConfig.class);

            StagingSchemaValidation schemaValidation = stagingAppConfigMessage.getItems()
                    .iterator().next().getStagingSchemaValidation();

            assertThat(schemaValidation.getPhysicalTableName())
                    .isEqualTo("TBL");
            assertThat(schemaValidation.getDisplayName())
                    .isEqualTo("TABLE");
            assertThat(schemaValidation.getSchemaId())
                    .isEqualTo(10202L);
        }

        @Test
        public void startJob_StagingRequestWithMultipleItems_SavesStagingDatasetsWithFileLocations() throws Exception {

            StagingRequest stagingRequest = ExternalClient.Populated.stagingRequest()
                    .items(newHashSet(
                            ExternalClient.Populated.stagingItemRequest()
                                    .schema("TRADES")
                                    .source(ExternalClient.Populated.dataSource()
                                            .filePath("trades_file.csv")
                                            .header(true)
                                            .build())
                                    .build(),
                            ExternalClient.Populated.stagingItemRequest()
                                    .schema("EXPOSURES")
                                    .source(ExternalClient.Populated.dataSource()
                                            .filePath("exposure_file.csv")
                                            .header(true)
                                            .build())
                                    .build()))
                    .build();

            when(tableService.findTable(eq("TRADES"), any()))
                    .thenReturn(Optional.of(ProductPopulated.table().physicalTableName("TRDS").build()));

            when(tableService.findTable(eq("EXPOSURES"), any()))
                    .thenReturn(Optional.of(ProductPopulated.table().physicalTableName("EXPS").build()));

            when(serviceRequestRepository.save(any()))
                    .thenAnswer(invocation -> {
                        ServiceRequest serviceRequest = invocation.getArgument(0);
                        serviceRequest.setId(211L);
                        return serviceRequest;
                    });
            HdfsDatasetConf datasetConf = new HdfsDatasetConf();
            datasetConf.setLocalPath("test/local/path");
            datasetConf.setRemotePath("hdfs://user/ubuntu/remote");

            StagingDatasetService datasetService = datasetServiceFactoryBuilder
                    .dataSourceService(new LocalFileService(datasetConf))
                    .build()
                    .getStagingDatasetService();

            VavrAssert.assertValid(
                    datasetService.start(stagingRequest, "me")
            );

            verify(stagingDatasetRepository, times(2)).save(stagingDatasetArgumentCaptor.capture());

            List<StagingDataset> stagingDatasets = stagingDatasetArgumentCaptor.getAllValues();

            soft.assertThat(stagingDatasets)
                    .extracting(StagingDataset::getStagingFile)
                    .contains("exposure_file.csv", "trades_file.csv");
            soft.assertThat(stagingDatasets)
                    .extracting(StagingDataset::getValidationErrorFile)
                    .contains("hdfs://user/ubuntu/remote/211/E_EXPS.csv", "hdfs://user/ubuntu/remote/211/E_TRDS.csv");
            soft.assertThat(stagingDatasets)
                    .extracting(StagingDataset::getStagingFileCopy)
                    .contains("hdfs://user/ubuntu/remote/211/EXPS.csv", "hdfs://user/ubuntu/remote/211/TRDS.csv");
        }

        @Test
        public void start_StagingRequestWithDownstreamPipelines_CallsJobStarterWithPipelines() throws Exception {
            Pipeline pipeline = ProductPopulated.pipeline().id(123L).name("the pipeline name").build();

            when(pipelineService.findByName(any()))
                    .thenReturn(Validation.valid(pipeline));

            StagingDatasetService stagingDatasetService =
                    datasetServiceFactoryBuilder.build().getStagingDatasetService();

            VavrAssert.assertValid(stagingDatasetService.start(
                    ExternalClient.Populated.stagingRequestV2()
                            .name("job name")
                            .metadata(ExternalClient.Populated.datasetMetadata()
                                    .entityCode("e1")
                                    .referenceDate("01/01/2019")
                                    .build())
                            .items(newHashSet())
                            .downstreamPipelines(newHashSet("the pipeline name"))
                            .build(), "me"));

            verify(jobStarter).startJob(anyString(), serviceRequestArgumentCaptor.capture(), any());

            StagingAppConfig appConfig = MAPPER.readValue(
                    serviceRequestArgumentCaptor.getValue().getRequestMessage(), StagingAppConfig.class);

            assertThat(appConfig.getDownstreamPipelines())
                    .containsExactly(DownstreamPipeline.builder()
                            .pipelineId(123L)
                            .pipelineName("the pipeline name")
                            .entityCode("e1")
                            .referenceDate(LocalDate.of(2019, 1, 1))
                            .build());
        }

        @Test
        public void start_DownstreamPipelineDoesNotExist_ReturnsError() throws Exception {
            CRUDFailure notFoundError = CRUDFailure.cannotFind("Pipeline").asFailure();

            when(pipelineService.findByName(any()))
                    .thenReturn(Validation.invalid(notFoundError));

            StagingDatasetService stagingDatasetService =
                    datasetServiceFactoryBuilder.build().getStagingDatasetService();

            VavrAssert.assertFailed(stagingDatasetService.start(
                    ExternalClient.Populated.stagingRequestV2()
                            .name("job name")
                            .metadata(ExternalClient.Populated.datasetMetadata()
                                    .entityCode("e1")
                                    .referenceDate("01/01/2019")
                                    .build())
                            .items(newHashSet())
                            .downstreamPipelines(newHashSet("the pipeline name"))
                            .build(), "me"))
                    .withFailure(singletonList(notFoundError));

            verifyZeroInteractions(jobStarter);
        }
    }

    @RunWith(MockitoJUnitRunner.class)
    public static class StartJobS3ProductVersioningEnabled {

        @Rule
        public TogglzRule togglzRule = TogglzRule.allEnabled(IgnisFeature.class);

        @Rule
        public JUnitSoftAssertions soft = new JUnitSoftAssertions();

        @Mock
        private TableService tableService;

        @Mock
        private StagingDatasetRepository stagingDatasetRepository;

        @Mock
        private ServiceRequestRepository serviceRequestRepository;

        @Mock
        private JobStarter jobStarter;

        @Captor
        private ArgumentCaptor<ServiceRequest> serviceRequestArgumentCaptor;
        @Captor
        private ArgumentCaptor<StagingDataset> stagingDatasetArgumentCaptor;

        private StagingDatasetServiceFactory.StagingDatasetServiceFactoryBuilder datasetServiceFactoryBuilder;

        @Before
        public void setUp() {
            when(tableService.findTable(anyString(), any()))
                    .thenReturn(Optional.of(ProductPopulated.table().build()));

            when(stagingDatasetRepository.save(any()))
                    .then(invocation -> {
                        StagingDataset stagingDataset = invocation.getArgument(0);
                        stagingDataset.setId(1L);
                        return stagingDataset;
                    });

            TimeSource timeSource = new TimeSource(Clock.systemUTC());
            datasetServiceFactoryBuilder = StagingDatasetServiceFactory.builder()
                    .dataSourceService(new S3FileService(null, "s3a", "bucket_1", null))
                    .errorFileService(new S3ErrorFileService(null, "s3a", "bucket_error", "errorPrefix", timeSource))
                    .jobStarter(jobStarter)
                    .timeSource(timeSource)
                    .tableService(tableService)
                    .stagingDatasetRepository(stagingDatasetRepository)
                    .serviceRequestRepository(serviceRequestRepository);
        }

        @Test
        public void startJob_CreatesServiceRequestWithS3Datasource() throws Exception {
            StagingRequest stagingRequest = ExternalClient.Populated.stagingRequest()
                    .items(newHashSet(
                            ExternalClient.Populated.stagingItemRequest()
                                    .source(ExternalClient.Populated.dataSource()
                                            .filePath("path/to/key/file1.csv")
                                            .header(false)
                                            .build())
                                    .build()
                    ))
                    .build();

            when(tableService.findTable(anyString(), any()))
                    .thenReturn(Optional.of(ProductPopulated.table().physicalTableName("TABLE").build()));

            when(serviceRequestRepository.save(any()))
                    .thenAnswer(invocation -> {
                        ServiceRequest serviceRequest = invocation.getArgument(0);
                        serviceRequest.setId(101L);
                        return serviceRequest;
                    });

            StagingDatasetService datasetService = datasetServiceFactoryBuilder.build()
                    .getStagingDatasetService();

            VavrAssert.assertValid(
                    datasetService.start(stagingRequest, "me")
            );

            verify(jobStarter).startJob(any(), serviceRequestArgumentCaptor.capture(), any());

            ServiceRequest serviceRequest = serviceRequestArgumentCaptor.getValue();

            StagingAppConfig stagingAppConfigMessage =
                    MAPPER.readValue(serviceRequest.getRequestMessage(), StagingAppConfig.class);

            assertThat(stagingAppConfigMessage.getItems().iterator().next().getSource())
                    .isEqualTo(S3CsvDataSource.builder()
                            .s3Protocol("s3a")
                            .s3Bucket("bucket_1")
                            .s3Key("path/to/key/file1.csv")
                            .header(false)
                            .build());
        }

        @Test
        public void startJob_CreatesServiceRequestWithS3ErrorFileOutput() throws Exception {
            StagingRequest stagingRequest = ExternalClient.Populated.stagingRequest()
                    .items(newHashSet(
                            ExternalClient.Populated.stagingItemRequest()
                                    .build()
                    ))
                    .build();

            when(tableService.findTable(anyString(), any()))
                    .thenReturn(Optional.of(ProductPopulated.table().physicalTableName("TABLE").build()));

            when(serviceRequestRepository.save(any()))
                    .thenAnswer(invocation -> {
                        ServiceRequest serviceRequest = invocation.getArgument(0);
                        serviceRequest.setId(101L);
                        return serviceRequest;
                    });

            StagingDatasetService stagingDatasetService =
                    datasetServiceFactoryBuilder.build().getStagingDatasetService();

            VavrAssert.assertValid(
                    stagingDatasetService.start(stagingRequest, "me")
            );

            verify(jobStarter).startJob(any(), serviceRequestArgumentCaptor.capture(), any());

            ServiceRequest serviceRequest = serviceRequestArgumentCaptor.getValue();

            StagingAppConfig stagingAppConfigMessage =
                    MAPPER.readValue(serviceRequest.getRequestMessage(), StagingAppConfig.class);

            StagingErrorOutput stagingErrorOutput = stagingAppConfigMessage.getItems()
                    .iterator().next().getStagingErrorOutput();

            assertThat(stagingErrorOutput)
                    .isEqualTo(StagingErrorOutput.builder()
                            .errorFileSystemUri("s3a://bucket_error")
                            .errorFilePath("s3a://bucket_error/errorPrefix/stagingJob/101/E_TABLE.csv")
                            .temporaryFilePath("s3a://bucket_error/errorPrefix/temp/1/")
                            .build());
        }

        @Test
        public void startJob_NoS3Prefix_CreatesServiceRequestWithS3ErrorFileOutput() throws Exception {
            StagingRequest stagingRequest = ExternalClient.Populated.stagingRequest()
                    .items(newHashSet(ExternalClient.Populated.stagingItemRequest().build()))
                    .build();

            when(tableService.findTable(anyString(), any()))
                    .thenReturn(Optional.of(ProductPopulated.table().physicalTableName("TABLE").build()));

            when(serviceRequestRepository.save(any()))
                    .thenAnswer(invocation -> {
                        ServiceRequest serviceRequest = invocation.getArgument(0);
                        serviceRequest.setId(101L);
                        return serviceRequest;
                    });

            StagingDatasetService stagingDatasetService = datasetServiceFactoryBuilder
                    .errorFileService(new S3ErrorFileService(null, "s3a", "bucket_error", null, null))
                    .build().getStagingDatasetService();

            VavrAssert.assertValid(
                    stagingDatasetService.start(stagingRequest, "me")
            );

            verify(jobStarter).startJob(any(), serviceRequestArgumentCaptor.capture(), any());

            ServiceRequest serviceRequest = serviceRequestArgumentCaptor.getValue();

            StagingAppConfig stagingAppConfigMessage =
                    MAPPER.readValue(serviceRequest.getRequestMessage(), StagingAppConfig.class);

            StagingErrorOutput stagingErrorOutput = stagingAppConfigMessage.getItems()
                    .iterator().next().getStagingErrorOutput();

            assertThat(stagingErrorOutput)
                    .isEqualTo(StagingErrorOutput.builder()
                            .errorFileSystemUri("s3a://bucket_error")
                            .errorFilePath("s3a://bucket_error/stagingJob/101/E_TABLE.csv")
                            .temporaryFilePath("s3a://bucket_error/temp/1/")
                            .build());
        }

        @Test
        public void startJob_StagingRequestWithMultipleItems_SavesStagingDatasetsWithFileLocations() throws Exception {
            StagingRequest stagingRequest = ExternalClient.Populated.stagingRequest()
                    .items(newHashSet(
                            ExternalClient.Populated.stagingItemRequest()
                                    .schema("TRADES")
                                    .source(ExternalClient.Populated.dataSource()
                                            .filePath("csvs/trades_file.csv")
                                            .header(true)
                                            .build())
                                    .build(),
                            ExternalClient.Populated.stagingItemRequest()
                                    .schema("EXPOSURES")
                                    .source(ExternalClient.Populated.dataSource()
                                            .filePath("csvs/exposure_file.csv")
                                            .header(true)
                                            .build())
                                    .build()))
                    .build();

            when(tableService.findTable(eq("TRADES"), any()))
                    .thenReturn(Optional.of(ProductPopulated.table().physicalTableName("TRDS").build()));

            when(tableService.findTable(eq("EXPOSURES"), any()))
                    .thenReturn(Optional.of(ProductPopulated.table().physicalTableName("EXPS").build()));

            when(serviceRequestRepository.save(any()))
                    .thenAnswer(invocation -> {
                        ServiceRequest serviceRequest = invocation.getArgument(0);
                        serviceRequest.setId(211L);
                        return serviceRequest;
                    });

            StagingDatasetService stagingDatasetService = datasetServiceFactoryBuilder
                    .dataSourceService(new S3FileService(null, "s3a", "source_bucket", null))
                    .errorFileService(new S3ErrorFileService(null, "s3a", "error_bucket", "error_files", null))
                    .build()
                    .getStagingDatasetService();

            VavrAssert.assertValid(
                    stagingDatasetService.start(stagingRequest, "me")
            );

            verify(stagingDatasetRepository, times(2)).save(stagingDatasetArgumentCaptor.capture());

            List<StagingDataset> stagingDatasets = stagingDatasetArgumentCaptor.getAllValues();

            soft.assertThat(stagingDatasets)
                    .extracting(StagingDataset::getStagingFile)
                    .contains("csvs/exposure_file.csv", "csvs/trades_file.csv");
            soft.assertThat(stagingDatasets)
                    .extracting(StagingDataset::getValidationErrorFile)
                    .contains(
                            "error_files/stagingJob/211/E_EXPS.csv",
                            "error_files/stagingJob/211/E_TRDS.csv");
            soft.assertThat(stagingDatasets)
                    .extracting(StagingDataset::getStagingFileCopy)
                    .containsExactly(null, null);
        }
    }

    @RunWith(MockitoJUnitRunner.class)
    public static class DownloadValidationErrorFileHDFS {

        @Rule
        public TogglzRule togglzRule = TogglzRule.allEnabled(IgnisFeature.class);

        @Rule
        public JUnitSoftAssertions soft = new JUnitSoftAssertions();

        @Mock
        private StagingDatasetRepository stagingDatasetRepository;

        @Mock
        private JobStarter jobStarter;
        @Mock
        private HdfsTemplate fileSystemTemplate;

        private StagingDatasetServiceFactory.StagingDatasetServiceFactoryBuilder datasetServiceFactoryBuilder;

        @Before
        public void setUp() {
            datasetServiceFactoryBuilder = StagingDatasetServiceFactory.builder()
                    .dataSourceService(new LocalFileService(null))
                    .errorFileService(new HDFSErrorFileService(null, fileSystemTemplate))
                    .jobStarter(jobStarter)
                    .timeSource(new TimeSource(Clock.systemUTC()))
                    .stagingDatasetRepository(stagingDatasetRepository);
        }

        @Test
        public void downloadValidationErrorFile_StagingDatasetExists_ReturnsValidationErrorFile() throws Exception {
            when(stagingDatasetRepository.findById(any()))
                    .thenReturn(Optional.of(JobPopulated.stagingDataset()
                            .validationErrorFile("errors/csvs/test/E_TABLE.csv")
                            .build()));

            ByteArrayInputStream inputStream = mock(ByteArrayInputStream.class);
            when(fileSystemTemplate.open(any()))
                    .thenReturn(inputStream);

            StagingDatasetService stagingDatasetService =
                    datasetServiceFactoryBuilder.build().getStagingDatasetService();

            ErrorFileLinkOrStream result = VavrAssert.assertValid(
                    stagingDatasetService.downloadStagingErrorFile(10L))
                    .getResult();

            FileNameAndStream fileNameAndStream = result.getFileNameAndStream();
            assertThat(fileNameAndStream)
                    .isEqualTo(new FileNameAndStream("E_TABLE.csv", inputStream));
        }

        @Test
        public void downloadValidationErrorFile_StagingDatasetDoesNotExist_ReturnsCRUDFailure() throws Exception {
            when(stagingDatasetRepository.findById(any()))
                    .thenReturn(Optional.empty());

            StagingDatasetService stagingDatasetService =
                    datasetServiceFactoryBuilder.build().getStagingDatasetService();

            VavrAssert.assertFailed(
                    stagingDatasetService.downloadStagingErrorFile(10L))
                    .withFailure(CRUDFailure.notFoundIds("StagingDataset", 10L));
        }
    }

    @RunWith(MockitoJUnitRunner.class)
    public static class DownloadValidationErrorFileS3 {

        @Rule
        public TogglzRule togglzRule = TogglzRule.allEnabled(IgnisFeature.class);

        @Rule
        public JUnitSoftAssertions soft = new JUnitSoftAssertions();

        @Mock
        private AmazonS3Client amazonS3Client;

        @Mock
        private StagingDatasetRepository stagingDatasetRepository;

        @Mock
        private JobStarter jobStarter;

        @Captor
        private ArgumentCaptor<GeneratePresignedUrlRequest> generatePresignedUrlCaptor;

        private StagingDatasetServiceFactory.StagingDatasetServiceFactoryBuilder datasetServiceFactoryBuilder;

        @Before
        public void setUp() {
            TimeSource timeSource = new TimeSource(Clock.systemUTC());
            datasetServiceFactoryBuilder = StagingDatasetServiceFactory.builder()
                    .dataSourceService(new S3FileService(null, "s3a", "bucket_1", null))
                    .errorFileService(new S3ErrorFileService(
                            amazonS3Client,
                            "s3a",
                            "bucket_error",
                            "errorPrefix",
                            timeSource))
                    .jobStarter(jobStarter)
                    .timeSource(timeSource)
                    .stagingDatasetRepository(stagingDatasetRepository);
        }

        @Test
        public void downloadValidationErrorFile_StagingDatasetExists_ReturnsValidationErrorFile() throws Exception {
            when(stagingDatasetRepository.findById(any()))
                    .thenReturn(Optional.of(JobPopulated.stagingDataset().build()));

            when(amazonS3Client.generatePresignedUrl(any(GeneratePresignedUrlRequest.class)))
                    .thenReturn(new URL("https://s3.com/1671671726?token=12312312"));

            StagingDatasetService stagingDatasetService =
                    datasetServiceFactoryBuilder.build().getStagingDatasetService();

            ErrorFileLinkOrStream result = VavrAssert.assertValid(
                    stagingDatasetService.downloadStagingErrorFile(10L))
                    .getResult();

            URL downloadLink = result.getLink();
            assertThat(downloadLink.toString())
                    .isEqualTo("https://s3.com/1671671726?token=12312312");
        }

        @Test
        public void downloadValidationErrorFile_StagingDatasetExists_AddExpiryToPresignedUrl() throws Exception {
            when(stagingDatasetRepository.findById(any()))
                    .thenReturn(Optional.of(JobPopulated.stagingDataset()
                            .validationErrorFile("prefix/some_file.csv")
                            .build()));

            LocalDateTime currentTime = LocalDateTime.of(2000, 1, 4, 1, 0, 0);

            TimeSource timeSource = new TimeSource(
                    Clock.fixed(currentTime.toInstant(ZoneOffset.UTC), ZoneId.of("UTC")));

            StagingDatasetService stagingDatasetService = datasetServiceFactoryBuilder
                    .errorFileService(new S3ErrorFileService(
                            amazonS3Client,
                            "s3a",
                            "error_bucket",
                            "prefix",
                            timeSource))
                    .build().getStagingDatasetService();

            VavrAssert.assertValid(
                    stagingDatasetService.downloadStagingErrorFile(10L));

            verify(amazonS3Client).generatePresignedUrl(generatePresignedUrlCaptor.capture());

            GeneratePresignedUrlRequest captorValue = generatePresignedUrlCaptor.getValue();

            soft.assertThat(captorValue.getBucketName())
                    .isEqualTo("error_bucket");
            soft.assertThat(captorValue.getKey())
                    .isEqualTo("prefix/some_file.csv");
            soft.assertThat(captorValue.getExpiration())
                    .isEqualTo(
                            DateUtils.addSeconds(PopulatedDates.toDateTime("2000-1-4T01:00:00"), 1000));
        }

        @Test
        public void downloadValidationErrorFile_StagingDatasetDoesNotExist_ReturnsCRUDFailure() throws Exception {
            when(stagingDatasetRepository.findById(any()))
                    .thenReturn(Optional.empty());

            StagingDatasetService stagingDatasetService =
                    datasetServiceFactoryBuilder.build().getStagingDatasetService();

            VavrAssert.assertFailed(
                    stagingDatasetService.downloadStagingErrorFile(10L))
                    .withFailure(CRUDFailure.notFoundIds("StagingDataset", 10L));
        }
    }
}
