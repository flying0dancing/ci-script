package com.lombardrisk.ignis.server.job.staging.validate;

import com.google.common.collect.ImmutableSet;
import com.lombardrisk.ignis.client.external.fixture.ExternalClient;
import com.lombardrisk.ignis.client.external.job.staging.request.DatasetMetadata;
import com.lombardrisk.ignis.client.external.job.staging.request.v1.StagingItemRequest;
import com.lombardrisk.ignis.client.external.job.staging.request.v1.StagingRequest;
import com.lombardrisk.ignis.common.assertions.VavrAssert;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.server.job.staging.model.StagingDatasetInstruction;
import com.lombardrisk.ignis.server.job.staging.model.StagingInstructions;
import com.lombardrisk.ignis.server.product.fixture.ProductPopulated;
import com.lombardrisk.ignis.server.product.table.TableService;
import com.lombardrisk.ignis.server.product.table.model.DecimalField;
import com.lombardrisk.ignis.server.product.table.model.Table;
import io.vavr.control.Validation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("deprecation")
@RunWith(MockitoJUnitRunner.class)
public class StagingRequestV1ValidatorTest {

    @Mock
    private TableService tableService;

    private StagingRequestV1Validator stagingRequestV1Validator;

    @Before
    public void setUp() {
        when(tableService.findTable(any(), any()))
                .thenReturn(Optional.of(ProductPopulated.table().build()));

        StagingRequestCommonValidator commonValidator = new StagingRequestCommonValidator(tableService);
        stagingRequestV1Validator = new StagingRequestV1Validator(commonValidator);
    }

    @Test
    public void validate_setsName() {
        Validation<List<CRUDFailure>, StagingInstructions> stagingJobRequest = stagingRequestV1Validator.validate(
                ExternalClient.Populated.stagingRequest()
                        .name("test name")
                        .build());

        assertThat(stagingJobRequest.get().getJobName())
                .isEqualTo("test name");
    }

    @Test
    public void validate_DuplicateSchemasInRequest_ReturnsInvalid() {

        Validation<List<CRUDFailure>, StagingInstructions> stagingJobRequest = stagingRequestV1Validator.validate(
                StagingRequest.builder()
                        .items(newHashSet(
                                ExternalClient.Populated.stagingItemRequest()
                                        .schema("table1")
                                        .autoValidate(true)
                                        .build(),
                                ExternalClient.Populated.stagingItemRequest()
                                        .schema("table1")
                                        .autoValidate(false)
                                        .build(),
                                ExternalClient.Populated.stagingItemRequest()
                                        .schema("table2")
                                        .build()))
                        .build());

        VavrAssert.assertCollectionFailure(stagingJobRequest)
                .withOnlyFailures(CRUDFailure.constraintFailure(
                        "Cannot stage duplicate schemas in the same request, [table1]"));
    }

    @Test
    public void validate_SchemaNotFound_ReturnsInvalid() {
        when(tableService.findTable(any(), any()))
                .thenReturn(Optional.empty());

        Validation<List<CRUDFailure>, StagingInstructions> stagingJobRequest = stagingRequestV1Validator.validate(
                StagingRequest.builder()
                        .items(newHashSet(
                                ExternalClient.Populated.stagingItemRequest()
                                        .schema("table1")
                                        .build(),
                                ExternalClient.Populated.stagingItemRequest()
                                        .schema("table2")
                                        .build()))
                        .build());

        assertThat(stagingJobRequest.getError())
                .extracting(CRUDFailure::getErrorMessage)
                .anyMatch(err -> err.contains("display name 'table1'"))
                .anyMatch(err -> err.contains("display name 'table2'"));
    }

    @Test
    public void validate_SchemaNotFoundWhenProductVersioningFeatured_ReturnsInvalid() {
        when(tableService.findTable("table1", LocalDate.of(1998, 3, 4)))
                .thenReturn(Optional.empty());
        when(tableService.findTable("table2", LocalDate.of(1996, 12, 4)))
                .thenReturn(Optional.empty());

        Validation<List<CRUDFailure>, StagingInstructions> stagingJobRequest = stagingRequestV1Validator.validate(
                StagingRequest.builder()
                        .items(newHashSet(
                                ExternalClient.Populated.stagingItemRequest()
                                        .schema("table1")
                                        .dataset(ExternalClient.Populated.datasetMetadata()
                                                .referenceDate("04/03/1998")
                                                .build())
                                        .build(),
                                ExternalClient.Populated.stagingItemRequest()
                                        .schema("table2")
                                        .dataset(ExternalClient.Populated.datasetMetadata()
                                                .referenceDate("04/12/1996")
                                                .build())
                                        .build()))
                        .build());

        assertThat(stagingJobRequest.getError())
                .extracting(CRUDFailure::getErrorMessage)
                .anyMatch(err -> err.contains("display name 'table1'"))
                .anyMatch(err -> err.contains("reference date '1998-03-04'"))
                .anyMatch(err -> err.contains("display name 'table2'"))
                .anyMatch(err -> err.contains("reference date '1996-12-04'"));
    }

    @Test
    public void validate_setsItemSchema() {
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

        Validation<List<CRUDFailure>, StagingInstructions> stagingJobRequest = stagingRequestV1Validator.validate(
                StagingRequest.builder()
                        .items(newHashSet(
                                ExternalClient.Populated.stagingItemRequest().build()))
                        .build());

        Table schemaForDatasetInstruction = stagingJobRequest.get()
                .getStagingDatasetInstructions().iterator().next().getSchema();

        assertThat(schemaForDatasetInstruction)
                .isEqualTo(schema);
    }

    @Test
    public void validate_setsReferenceDateOnInstructions() {
        Validation<List<CRUDFailure>, StagingInstructions> stagingJobRequest = stagingRequestV1Validator.validate(
                StagingRequest.builder()
                        .items(newHashSet(
                                ExternalClient.Populated.stagingItemRequest()
                                        .dataset(ExternalClient.Populated.datasetMetadata()
                                                .referenceDate("01/01/2018")
                                                .build())
                                        .build()))
                        .build());

        VavrAssert.assertValid(stagingJobRequest);

        StagingDatasetInstruction stagingDatasetInstruction = stagingJobRequest.get()
                .getStagingDatasetInstructions()
                .iterator()
                .next();

        assertThat(stagingDatasetInstruction.getReferenceDate())
                .isEqualTo(LocalDate.of(2018, 1, 1));
    }

    @Test
    public void validate_setsEntityCodeOnInstructions() {
        when(tableService.findTable(any(), any()))
                .thenReturn(Optional.of(ProductPopulated.table().build()));

        HashSet<StagingItemRequest> stagingItemRequests = newHashSet(
                ExternalClient.Populated.stagingItemRequest()
                        .schema("table1")
                        .dataset(ExternalClient.Populated.datasetMetadata().entityCode("e1").build())
                        .build(),
                ExternalClient.Populated.stagingItemRequest()
                        .schema("table2")
                        .dataset(ExternalClient.Populated.datasetMetadata().entityCode("e2").build())
                        .build());

        Validation<List<CRUDFailure>, StagingInstructions> stagingJobRequest = stagingRequestV1Validator.validate(
                StagingRequest.builder()
                        .items(stagingItemRequests)
                        .build());

        assertThat(stagingJobRequest.get().getStagingDatasetInstructions())
                .extracting(StagingDatasetInstruction::getEntityCode)
                .containsExactlyInAnyOrder("e1", "e2");
    }

    @Test
    public void validate_setsFilePathAndHeaderOnInstructions() {
        when(tableService.findTable(any(), any()))
                .thenReturn(Optional.of(ProductPopulated.table().build()));

        HashSet<StagingItemRequest> stagingItemRequests = newHashSet(
                ExternalClient.Populated.stagingItemRequest()
                        .schema("table1")
                        .source(ExternalClient.Populated.dataSource()
                                .filePath("/path/to/file/1")
                                .header(true)
                                .build())
                        .build(),
                ExternalClient.Populated.stagingItemRequest()
                        .schema("table2")
                        .source(ExternalClient.Populated.dataSource()
                                .filePath("/path/to/file/2")
                                .header(false)
                                .build())
                        .build());

        Validation<List<CRUDFailure>, StagingInstructions> stagingJobRequest = stagingRequestV1Validator.validate(
                StagingRequest.builder()
                        .items(stagingItemRequests)
                        .build());

        assertThat(stagingJobRequest.get().getStagingDatasetInstructions())
                .extracting(StagingDatasetInstruction::getFilePath, StagingDatasetInstruction::isHeader)
                .containsExactlyInAnyOrder(tuple("/path/to/file/1", true), tuple("/path/to/file/2", false));
    }

    @Test
    public void validate_FindsSchemaByDisplayNameAndVersion() {
        stagingRequestV1Validator.validate(
                StagingRequest.builder()
                        .items(newHashSet(
                                ExternalClient.Populated.stagingItemRequest()
                                        .schema("displayName")
                                        .dataset(ExternalClient.Populated.datasetMetadata()
                                                .referenceDate("23/10/2007")
                                                .build())
                                        .build()))
                        .build());

        verify(tableService).findTable("displayName", LocalDate.of(2007, 10, 23));
    }

    @Test
    public void validate_ReferenceDateInvalid_ReturnsValidationError() {
        Validation<List<CRUDFailure>, StagingInstructions> stagingJobRequest = stagingRequestV1Validator.validate(
                StagingRequest.builder()
                        .items(newHashSet(
                                ExternalClient.Populated.stagingItemRequest()
                                        .dataset(DatasetMetadata.builder()
                                                .entityCode("entity123")
                                                .referenceDate("notA Date")
                                                .build())
                                        .build()))
                        .build());

        assertThat(stagingJobRequest.getError())
                .extracting(CRUDFailure::getErrorMessage)
                .anyMatch(err -> err.contains("referenceDate"))
                .anyMatch(err -> err.contains("notA Date"));
    }

    @Test
    public void validate_setsAutoValidateOnInstructions() {
        Validation<List<CRUDFailure>, StagingInstructions> stagingJobRequest = stagingRequestV1Validator.validate(
                StagingRequest.builder()
                        .items(newHashSet(
                                ExternalClient.Populated.stagingItemRequest()
                                        .schema("MY_TABLE_1")
                                        .autoValidate(true)
                                        .build(),
                                ExternalClient.Populated.stagingItemRequest()
                                        .schema("MY_TABLE_2")
                                        .autoValidate(false)
                                        .build()))
                        .build());

        assertThat(stagingJobRequest.get().getStagingDatasetInstructions())
                .extracting(StagingDatasetInstruction::isAutoValidate)
                .containsExactlyInAnyOrder(true, false);
    }

    @Test
    public void validate_setsAppendToDatasetAsNull() {
        StagingInstructions stagingInstructions = VavrAssert.assertValid(
                stagingRequestV1Validator.validate(
                        StagingRequest.builder()
                                .items(newHashSet(
                                        ExternalClient.Populated.stagingItemRequest()
                                                .schema("MY_TABLE_1")
                                                .autoValidate(true)
                                                .build(),
                                        ExternalClient.Populated.stagingItemRequest()
                                                .schema("MY_TABLE_2")
                                                .autoValidate(false)
                                                .build()))
                                .build()))
                .getResult();

        assertThat(stagingInstructions.getStagingDatasetInstructions())
                .extracting(StagingDatasetInstruction::getAppendToDataset)
                .containsOnlyNulls();
    }

    @Test
    public void validate_SetsDownstreamPipelinesToEmpty() {
        StagingInstructions stagingInstructions = VavrAssert.assertValid(
                stagingRequestV1Validator.validate(
                        StagingRequest.builder()
                                .items(newHashSet(
                                        ExternalClient.Populated.stagingItemRequest()
                                                .schema("MY_TABLE_1")
                                                .autoValidate(true)
                                                .build(),
                                        ExternalClient.Populated.stagingItemRequest()
                                                .schema("MY_TABLE_2")
                                                .autoValidate(false)
                                                .build()))
                                .build()))
                .getResult();

        assertThat(stagingInstructions.getDownstreamPipelineInstructions())
                .isEmpty();
    }
}
