package com.lombardrisk.ignis.server.job.staging.validate;

import com.google.common.collect.ImmutableSet;
import com.lombardrisk.ignis.client.external.fixture.ExternalClient;
import com.lombardrisk.ignis.client.external.job.staging.request.v2.StagingItemRequestV2;
import com.lombardrisk.ignis.client.external.job.staging.request.v2.StagingRequestV2;
import com.lombardrisk.ignis.common.assertions.VavrAssert;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.feature.FeatureNotActiveException;
import com.lombardrisk.ignis.feature.IgnisFeature;
import com.lombardrisk.ignis.server.dataset.DatasetService;
import com.lombardrisk.ignis.server.dataset.fixture.DatasetPopulated;
import com.lombardrisk.ignis.server.dataset.model.Dataset;
import com.lombardrisk.ignis.server.job.staging.model.DownstreamPipelineInstruction;
import com.lombardrisk.ignis.server.job.staging.model.StagingDatasetInstruction;
import com.lombardrisk.ignis.server.job.staging.model.StagingInstructions;
import com.lombardrisk.ignis.server.product.fixture.ProductPopulated;
import com.lombardrisk.ignis.server.product.pipeline.PipelineService;
import com.lombardrisk.ignis.server.product.pipeline.model.Pipeline;
import com.lombardrisk.ignis.server.product.table.TableService;
import com.lombardrisk.ignis.server.product.table.model.DecimalField;
import com.lombardrisk.ignis.server.product.table.model.Table;
import io.vavr.control.Validation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.togglz.core.manager.FeatureManager;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StagingRequestV2ValidatorTest {

    @Mock
    private TableService tableService;
    @Mock
    private DatasetService datasetService;
    @Mock
    private PipelineService pipelineService;
    @Mock
    private FeatureManager featureManager;

    private StagingRequestV2Validator stagingRequestValidator;

    @Before
    public void setUp() {
        when(featureManager.isActive(any()))
                .thenReturn(true);

        when(tableService.findTable(any(), any()))
                .thenReturn(Optional.of(ProductPopulated.table().build()));

        when(datasetService.findWithValidation(anyLong()))
                .thenReturn(Validation.valid(DatasetPopulated.dataset().build()));

        StagingRequestCommonValidator commonValidator = new StagingRequestCommonValidator(tableService);
        stagingRequestValidator = new StagingRequestV2Validator(
                commonValidator, featureManager, datasetService, pipelineService);
    }

    @Test
    public void validateV2_setsName() {
        StagingInstructions stagingJobRequest =
                VavrAssert.assertValid(stagingRequestValidator.validate(
                        ExternalClient.Populated.stagingRequestV2()
                                .name("test name")
                                .build()))
                        .getResult();

        assertThat(stagingJobRequest.getJobName())
                .isEqualTo("test name");
    }

    @Test
    public void validateV2_DuplicateSchemasInRequest_ReturnsInvalid() {
        Validation<List<CRUDFailure>, StagingInstructions> stagingJobRequest =
                stagingRequestValidator.validate(
                        ExternalClient.Populated.stagingRequestV2()
                                .items(newHashSet(
                                        ExternalClient.Populated.stagingItemRequestV2()
                                                .schema("table1")
                                                .autoValidate(true)
                                                .build(),
                                        ExternalClient.Populated.stagingItemRequestV2()
                                                .schema("table1")
                                                .autoValidate(false)
                                                .build(),
                                        ExternalClient.Populated.stagingItemRequestV2()
                                                .schema("table2")
                                                .build()))
                                .build());

        VavrAssert.assertCollectionFailure(stagingJobRequest)
                .withOnlyFailures(CRUDFailure.constraintFailure(
                        "Cannot stage duplicate schemas in the same request, [table1]"));
    }

    @Test
    public void validateV2_SchemaNotFound_ReturnsInvalid() {
        when(tableService.findTable(any(), any()))
                .thenReturn(Optional.empty());

        Validation<List<CRUDFailure>, StagingInstructions> stagingJobRequest = stagingRequestValidator.validate(
                ExternalClient.Populated.stagingRequestV2()
                        .items(newHashSet(
                                ExternalClient.Populated.stagingItemRequestV2()
                                        .schema("table1")
                                        .build(),
                                ExternalClient.Populated.stagingItemRequestV2()
                                        .schema("table2")
                                        .build()))
                        .build());

        assertThat(stagingJobRequest.getError())
                .extracting(CRUDFailure::getErrorMessage)
                .anyMatch(err -> err.contains("display name 'table1'"))
                .anyMatch(err -> err.contains("display name 'table2'"));
    }

    @Test
    public void validateV2_SchemaNotFoundWhenProductVersioningFeatured_ReturnsInvalid() {
        when(tableService.findTable("table1", LocalDate.of(1998, 3, 4)))
                .thenReturn(Optional.empty());
        when(tableService.findTable("table2", LocalDate.of(1998, 3, 4)))
                .thenReturn(Optional.empty());

        Validation<List<CRUDFailure>, StagingInstructions> stagingJobRequest = stagingRequestValidator.validate(
                ExternalClient.Populated.stagingRequestV2()
                        .metadata(ExternalClient.Populated.datasetMetadata()
                                .referenceDate("04/03/1998")
                                .build())
                        .items(newHashSet(
                                ExternalClient.Populated.stagingItemRequestV2()
                                        .schema("table1")
                                        .build(),
                                ExternalClient.Populated.stagingItemRequestV2()
                                        .schema("table2")
                                        .build()))
                        .build());

        assertThat(stagingJobRequest.getError())
                .extracting(CRUDFailure::getErrorMessage)
                .anyMatch(err -> err.contains("display name 'table1'"))
                .anyMatch(err -> err.contains("reference date '1998-03-04'"))
                .anyMatch(err -> err.contains("display name 'table2'"))
                .anyMatch(err -> err.contains("reference date '1998-03-04'"));
    }

    @Test
    public void validateV2_setsItemSchema() {
        DecimalField field1 = ProductPopulated.decimalField("field1").build();
        DecimalField field2 = ProductPopulated.decimalField("field2").build();

        Table schema = Table.builder()
                .id(22L)
                .physicalTableName("TABLE_NAME")
                .displayName("Table Name")
                .fields(ImmutableSet.of(field1, field2))
                .build();

        when(tableService.findTable(any(), any()))
                .thenReturn(Optional.of(schema));

        StagingInstructions stagingJobRequest = VavrAssert.assertValid(
                stagingRequestValidator.validate(ExternalClient.Populated.stagingRequestV2().build()))
                .getResult();

        Table schemaForDatasetInstruction = stagingJobRequest
                .getStagingDatasetInstructions().iterator().next().getSchema();

        assertThat(schemaForDatasetInstruction)
                .isEqualTo(schema);
    }

    @Test
    public void validateV2_setsReferenceDateOnInstructions() {
        StagingInstructions stagingInstructions = VavrAssert.assertValid(
                stagingRequestValidator.validate(
                        ExternalClient.Populated.stagingRequestV2()
                                .metadata(ExternalClient.Populated.datasetMetadata()
                                        .referenceDate("01/01/2018")
                                        .build())
                                .items(newHashSet(
                                        ExternalClient.Populated.stagingItemRequestV2()
                                                .schema("table1")
                                                .build(),
                                        ExternalClient.Populated.stagingItemRequestV2()
                                                .schema("table2")
                                                .build()))
                                .build()))
                .getResult();

        assertThat(stagingInstructions.getStagingDatasetInstructions())
                .extracting(StagingDatasetInstruction::getReferenceDate)
                .containsOnly(LocalDate.of(2018, 1, 1));
    }

    @Test
    public void validateV2_setsEntityCodeOnInstructions() {
        when(tableService.findTable(any(), any()))
                .thenReturn(Optional.of(ProductPopulated.table().build()));

        StagingInstructions stagingJobRequest = VavrAssert.assertValid(
                stagingRequestValidator.validate(
                        ExternalClient.Populated.stagingRequestV2()
                                .metadata(ExternalClient.Populated.datasetMetadata()
                                        .entityCode("my-entity-code")
                                        .build())
                                .items(newHashSet(
                                        ExternalClient.Populated.stagingItemRequestV2()
                                                .schema("table1")
                                                .build(),
                                        ExternalClient.Populated.stagingItemRequestV2()
                                                .schema("table2")
                                                .build()))
                                .build()))
                .getResult();

        assertThat(stagingJobRequest.getStagingDatasetInstructions())
                .extracting(StagingDatasetInstruction::getEntityCode)
                .containsOnly("my-entity-code");
    }

    @Test
    public void validateV2_setsFilePathAndHeaderOnInstructions() {
        when(tableService.findTable(any(), any()))
                .thenReturn(Optional.of(ProductPopulated.table().build()));

        StagingInstructions stagingJobRequest = VavrAssert.assertValid(
                stagingRequestValidator.validate(
                        ExternalClient.Populated.stagingRequestV2()
                                .items(newHashSet(
                                        ExternalClient.Populated.stagingItemRequestV2()
                                                .schema("table1")
                                                .source(ExternalClient.Populated.dataSource()
                                                        .filePath("/path/to/file/1")
                                                        .header(true)
                                                        .build())
                                                .build(),
                                        ExternalClient.Populated.stagingItemRequestV2()
                                                .schema("table2")
                                                .source(ExternalClient.Populated.dataSource()
                                                        .filePath("/path/to/file/2")
                                                        .header(false)
                                                        .build())
                                                .build()))
                                .build()))
                .getResult();

        assertThat(stagingJobRequest.getStagingDatasetInstructions())
                .extracting(StagingDatasetInstruction::getFilePath, StagingDatasetInstruction::isHeader)
                .containsExactlyInAnyOrder(
                        tuple("/path/to/file/1", true),
                        tuple("/path/to/file/2", false));
    }

    @Test
    public void validateV2_FindsSchemaByDisplayNameAndVersion() {
        stagingRequestValidator.validate(
                ExternalClient.Populated.stagingRequestV2()
                        .metadata(ExternalClient.Populated.datasetMetadata()
                                .referenceDate("23/10/2007")
                                .build())
                        .items(newHashSet(
                                ExternalClient.Populated.stagingItemRequestV2().schema("displayName").build()))
                        .build());

        verify(tableService).findTable("displayName", LocalDate.of(2007, 10, 23));
    }

    @Test
    public void validateV2_ReferenceDateInvalid_ReturnsValidationError() {
        Validation<List<CRUDFailure>, StagingInstructions> stagingJobRequest =
                stagingRequestValidator.validate(
                        ExternalClient.Populated.stagingRequestV2()
                                .metadata(ExternalClient.Populated.datasetMetadata()
                                        .referenceDate("notA Date")
                                        .build())
                                .build());

        assertThat(stagingJobRequest.getError())
                .extracting(CRUDFailure::getErrorMessage)
                .anyMatch(err -> err.contains("referenceDate"))
                .anyMatch(err -> err.contains("notA Date"));
    }

    @Test
    public void validateV2_setsAutoValidateOnInstructions() {
        Validation<List<CRUDFailure>, StagingInstructions> stagingJobRequest = stagingRequestValidator.validate(
                ExternalClient.Populated.stagingRequestV2()
                        .items(newHashSet(
                                ExternalClient.Populated.stagingItemRequestV2()
                                        .schema("MY_TABLE_1")
                                        .autoValidate(true)
                                        .build(),
                                ExternalClient.Populated.stagingItemRequestV2()
                                        .schema("MY_TABLE_2")
                                        .autoValidate(false)
                                        .build()))
                        .build());

        assertThat(stagingJobRequest.get().getStagingDatasetInstructions())
                .extracting(StagingDatasetInstruction::isAutoValidate)
                .containsExactlyInAnyOrder(true, false);
    }

    @Test
    public void validateV2_AppendDatasetDoesNotExist_ReturnsInvalid() {
        when(tableService.findTable(eq("THE_TABLE"), any()))
                .thenReturn(Optional.of(ProductPopulated.table().physicalTableName("THE_TABLE").build()));

        when(datasetService.findWithValidation(anyLong()))
                .thenReturn(Validation.invalid(CRUDFailure.notFoundIds("Dataset", 1234567L)));

        StagingRequestV2 stagingRequest = ExternalClient.Populated.stagingRequestV2()
                .items(newHashSet(
                        ExternalClient.Populated.stagingItemRequestV2()
                                .schema("THE_TABLE")
                                .appendToDatasetId(1234567L)
                                .build()))
                .build();

        VavrAssert.assertCollectionFailure(stagingRequestValidator.validate(stagingRequest))
                .withOnlyFailures(CRUDFailure.notFoundIds("Dataset", 1234567L));
    }

    @Test
    public void validateV2_AppendDatasetFlagInactive_ThrowsException() {
        when(featureManager.isActive(IgnisFeature.APPEND_DATASETS))
                .thenReturn(false);

        when(tableService.findTable(eq("THE_TABLE"), any()))
                .thenReturn(Optional.of(ProductPopulated.table().physicalTableName("THE_TABLE").build()));

        HashSet<StagingItemRequestV2> stagingItemRequests = newHashSet(
                ExternalClient.Populated.stagingItemRequestV2()
                        .schema("THE_TABLE")
                        .appendToDatasetId(1234567L)
                        .build());

        StagingRequestV2 stagingRequest = ExternalClient.Populated.stagingRequestV2()
                .items(stagingItemRequests).build();

        assertThatThrownBy(() -> stagingRequestValidator.validate(stagingRequest))
                .isInstanceOf(FeatureNotActiveException.class)
                .extracting(exc -> ((FeatureNotActiveException) exc).getFeature())
                .isEqualTo(IgnisFeature.APPEND_DATASETS);
    }

    @Test
    public void validateV2_AppendDatasetEntityCodeDoesNotMatch_ReturnsInvalid() {
        when(tableService.findTable(eq("THE_TABLE"), any()))
                .thenReturn(Optional.of(ProductPopulated.table().physicalTableName("THE_TABLE").build()));

        when(datasetService.findWithValidation(anyLong()))
                .thenReturn(Validation.valid(DatasetPopulated.dataset()
                        .id(1234567L)
                        .entityCode("some-entity-code")
                        .referenceDate(LocalDate.of(2018, 1, 1))
                        .build()));

        HashSet<StagingItemRequestV2> stagingItemRequests = newHashSet(
                ExternalClient.Populated.stagingItemRequestV2()
                        .schema("THE_TABLE")
                        .appendToDatasetId(1234567L)
                        .build());

        StagingRequestV2 request = ExternalClient.Populated.stagingRequestV2()
                .metadata(ExternalClient.Populated.datasetMetadata()
                        .entityCode("some-other-entity-code")
                        .referenceDate("01/01/2018")
                        .build())
                .items(stagingItemRequests).build();

        List<CRUDFailure> failures = VavrAssert.assertFailed(stagingRequestValidator.validate(request))
                .getValidation();

        assertThat(failures)
                .extracting(CRUDFailure::getErrorMessage)
                .allMatch(err -> err.contains("entityCode - some-other-entity-code"));
    }

    @Test
    public void validateV2_AppendDatasetReferenceDateDoesNotMatch_ReturnsInvalid() {
        when(tableService.findTable(eq("THE_TABLE"), any()))
                .thenReturn(Optional.of(ProductPopulated.table().physicalTableName("THE_TABLE").build()));

        when(datasetService.findWithValidation(anyLong()))
                .thenReturn(Validation.valid(DatasetPopulated.dataset()
                        .id(1234567L)
                        .entityCode("an-entity-code")
                        .referenceDate(LocalDate.of(2018, 1, 1))
                        .build()));

        HashSet<StagingItemRequestV2> stagingItemRequests = newHashSet(
                ExternalClient.Populated.stagingItemRequestV2()
                        .schema("THE_TABLE")
                        .appendToDatasetId(1234567L)
                        .build());

        StagingRequestV2 request = ExternalClient.Populated.stagingRequestV2()
                .metadata(ExternalClient.Populated.datasetMetadata()
                        .entityCode("an-entity-code")
                        .referenceDate("31/12/2018")
                        .build())
                .items(stagingItemRequests)
                .build();

        List<CRUDFailure> failures = VavrAssert.assertFailed(stagingRequestValidator.validate(request))
                .getValidation();

        assertThat(failures)
                .extracting(CRUDFailure::getErrorMessage)
                .allMatch(err -> err.contains("referenceDate - 31/12/2018"));
    }

    @Test
    public void validateV2_AppendDatasetSchemaDoesNotMatch_ReturnsInvalid() {
        when(tableService.findTable(eq("THE_TABLE"), any()))
                .thenReturn(Optional.of(ProductPopulated.table().id(1L).physicalTableName("THE_TABLE").build()));

        when(datasetService.findWithValidation(anyLong()))
                .thenReturn(Validation.valid(DatasetPopulated.dataset()
                        .id(1234567L)
                        .schema(ProductPopulated.table().id(2L).displayName("SOME_OTHER_TABLE").build())
                        .build()));

        HashSet<StagingItemRequestV2> stagingItemRequests = newHashSet(
                ExternalClient.Populated.stagingItemRequestV2()
                        .schema("THE_TABLE")
                        .appendToDatasetId(1234567L)
                        .build());

        StagingRequestV2 request = ExternalClient.Populated.stagingRequestV2().items(stagingItemRequests).build();

        List<CRUDFailure> failures = VavrAssert.assertFailed(stagingRequestValidator.validate(request))
                .getValidation();

        assertThat(failures)
                .extracting(CRUDFailure::getErrorMessage)
                .allMatch(err -> err.contains("schema - THE_TABLE"));
    }

    @Test
    public void validateV2_AppendDatasetsExists_ReturnsInstructions() {
        Table table1 = ProductPopulated.table().physicalTableName("TABLE1").build();
        Table table2 = ProductPopulated.table().physicalTableName("TABLE2").build();

        when(tableService.findTable(eq("TABLE1"), any()))
                .thenReturn(Optional.of(table1));
        when(tableService.findTable(eq("TABLE2"), any()))
                .thenReturn(Optional.of(table2));

        Dataset appendToDataset = DatasetPopulated.dataset()
                .id(1234L)
                .entityCode("anEntityCode")
                .referenceDate(LocalDate.of(2000, 1, 1))
                .schema(ProductPopulated.table().displayName("TABLE1").build())
                .build();

        when(datasetService.findWithValidation(anyLong()))
                .thenReturn(Validation.valid(appendToDataset));

        HashSet<StagingItemRequestV2> stagingItemRequests = newHashSet(
                ExternalClient.Populated.stagingItemRequestV2()
                        .schema("TABLE1")
                        .appendToDatasetId(1234L)
                        .build(),
                ExternalClient.Populated.stagingItemRequestV2()
                        .schema("TABLE2")
                        .build());

        Validation<List<CRUDFailure>, StagingInstructions> stagingJobRequest = stagingRequestValidator.validate(
                ExternalClient.Populated.stagingRequestV2()
                        .metadata(ExternalClient.Populated.datasetMetadata()
                                .entityCode("anEntityCode")
                                .referenceDate("01/01/2000")
                                .build())
                        .items(stagingItemRequests)
                        .build());

        StagingInstructions result = VavrAssert.assertValid(stagingJobRequest).getResult();

        assertThat(result.getStagingDatasetInstructions())
                .extracting(StagingDatasetInstruction::getSchema, StagingDatasetInstruction::getAppendToDataset)
                .containsExactlyInAnyOrder(tuple(table1, appendToDataset), tuple(table2, null));
    }

    @Test
    public void validateV2_AppendDatasetsExists_CallsDatasetService() {
        when(tableService.findTable(eq("TABLE1"), any()))
                .thenReturn(Optional.of(ProductPopulated.table().physicalTableName("TABLE1").build()));

        when(tableService.findTable(eq("TABLE2"), any()))
                .thenReturn(Optional.of(ProductPopulated.table().physicalTableName("TABLE2").build()));

        when(tableService.findTable(eq("TABLE3"), any()))
                .thenReturn(Optional.of(ProductPopulated.table().physicalTableName("TABLE3").build()));

        HashSet<StagingItemRequestV2> stagingItemRequests = newHashSet(
                ExternalClient.Populated.stagingItemRequestV2()
                        .schema("TABLE1")
                        .appendToDatasetId(1234L)
                        .build(),
                ExternalClient.Populated.stagingItemRequestV2()
                        .schema("TABLE2")
                        .build(),
                ExternalClient.Populated.stagingItemRequestV2()
                        .schema("TABLE3")
                        .appendToDatasetId(5678L)
                        .build());

        stagingRequestValidator.validate(
                ExternalClient.Populated.stagingRequestV2()
                        .items(stagingItemRequests)
                        .build());

        verify(datasetService).findWithValidation(1234L);
        verify(datasetService).findWithValidation(5678L);
        verifyNoMoreInteractions(datasetService);
    }

    @Test
    public void validateV2_DownstreamPipelinesExist_ReturnsDownstreamPipelineInstructions() {
        Pipeline pipeline = ProductPopulated.pipeline().id(1L).name("the pipeline name").build();

        when(pipelineService.findByName(any()))
                .thenReturn(Validation.valid(pipeline));

        StagingInstructions stagingInstructions = VavrAssert.assertValid(stagingRequestValidator.validate(
                ExternalClient.Populated.stagingRequestV2()
                        .metadata(ExternalClient.Populated.datasetMetadata()
                                .entityCode("ec1")
                                .referenceDate("01/01/1990")
                                .build())
                        .items(newHashSet())
                        .downstreamPipelines(newHashSet("the pipeline name"))
                        .build()))
                .getResult();

        assertThat(stagingInstructions.getDownstreamPipelineInstructions())
                .containsExactly(DownstreamPipelineInstruction.builder()
                        .pipeline(pipeline)
                        .entityCode("ec1")
                        .referenceDate(LocalDate.of(1990, 1, 1))
                        .build());
    }

    @Test
    public void validateV2_DownstreamPipelinesExist_CallsPipelineService() {
        Pipeline pipeline = ProductPopulated.pipeline().id(1L).build();

        when(pipelineService.findByName(any()))
                .thenReturn(Validation.valid(pipeline));

        VavrAssert.assertValid(stagingRequestValidator.validate(
                ExternalClient.Populated.stagingRequestV2()
                        .items(newHashSet())
                        .downstreamPipelines(newHashSet("pipeline number one", "pipeline number two"))
                        .build()));

        verify(pipelineService).findByName("pipeline number one");
        verify(pipelineService).findByName("pipeline number two");
        verifyNoMoreInteractions(pipelineService);
    }

    @Test
    public void validateV2_DownstreamPipelinesDoNotExist_ReturnsInvalid() {
        Pipeline pipeline = ProductPopulated.pipeline().id(1L).name("pipeline number one").build();

        CRUDFailure notFoundError = CRUDFailure
                .cannotFind("pipeline").with("name", "pipeline number two").asFailure();

        when(pipelineService.findByName(any()))
                .thenReturn(Validation.valid(pipeline))
                .thenReturn(Validation.invalid(notFoundError));

        VavrAssert.assertFailed(stagingRequestValidator.validate(
                ExternalClient.Populated.stagingRequestV2()
                        .items(newHashSet())
                        .downstreamPipelines(newHashSet("pipeline number one", "pipeline number two"))
                        .build()))
                .withFailure(Collections.singletonList(notFoundError));
    }
}
