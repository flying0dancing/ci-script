package com.lombardrisk.ignis.design.server.productconfig.schema;

import com.lombardrisk.ignis.client.design.schema.CopySchemaRequest;
import com.lombardrisk.ignis.client.design.schema.UpdateSchema;
import com.lombardrisk.ignis.common.MapperUtils;
import com.lombardrisk.ignis.common.assertions.VavrAssert;
import com.lombardrisk.ignis.common.reservedWord.ReservedWord;
import com.lombardrisk.ignis.data.common.Identifiable;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.design.field.DesignField;
import com.lombardrisk.ignis.design.field.FieldService;
import com.lombardrisk.ignis.design.field.model.DateField;
import com.lombardrisk.ignis.design.field.model.Field;
import com.lombardrisk.ignis.design.field.model.StringField;
import com.lombardrisk.ignis.design.server.productconfig.fixture.ProductConfigRepositoryFixture;
import com.lombardrisk.ignis.design.server.productconfig.fixture.ProductServiceFixtureFactory;
import com.lombardrisk.ignis.design.server.productconfig.fixture.SchemaServiceFixtureFactory;
import io.vavr.control.Validation;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static com.lombardrisk.ignis.client.design.fixtures.Populated.copySchemaRequest;
import static com.lombardrisk.ignis.design.field.DesignField.Populated.dateField;
import static com.lombardrisk.ignis.design.field.DesignField.Populated.stringField;
import static com.lombardrisk.ignis.design.server.fixtures.Design.Populated;
import static org.assertj.core.api.Assertions.assertThat;

public class SchemaServiceTest {

    @Rule
    public JUnitSoftAssertions soft = new JUnitSoftAssertions();

    private SchemaService schemaService;
    private FieldService fieldService;

    @Before
    public void setUp() {
        ProductServiceFixtureFactory productServiceFactory = ProductServiceFixtureFactory.create();
        ProductConfigRepositoryFixture productRepository = productServiceFactory.getProductRepository();
        SchemaServiceFixtureFactory schemaServiceFactory = SchemaServiceFixtureFactory.create();
        schemaService = schemaServiceFactory.getSchemaService();
        fieldService = schemaServiceFactory.getFieldService();

        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("user", ""));
    }

    @Test
    public void updateWithoutFields_ShouldReturnUpdatedSchema() {
        Schema existingSchema = schemaService.createNew(Populated.schema().build());
        LocalDate startDate = LocalDate.of(2018, 1, 1);
        LocalDate endDate = LocalDate.of(2018, 9, 1);
        UpdateSchema updateSchemaRequest = Populated.schemaUpdateRequest()
                .displayName("updated display name")
                .physicalTableName("updated physical aTable name")
                .startDate(startDate)
                .endDate(endDate)
                .build();

        Schema updatedSchema = VavrAssert.assertValid(
                schemaService
                        .updateWithoutFields(-1L, existingSchema.getId(), updateSchemaRequest))
                .getResult();

        soft.assertThat(updatedSchema)
                .hasFieldOrPropertyWithValue("displayName", "updated display name");
        soft.assertThat(updatedSchema)
                .hasFieldOrPropertyWithValue("physicalTableName", "updated physical aTable name");
        soft.assertThat(updatedSchema)
                .hasFieldOrPropertyWithValue("startDate", startDate);
        soft.assertThat(updatedSchema)
                .hasFieldOrPropertyWithValue("endDate", endDate);
    }

    @Test
    public void updateWithoutFields_StartDateBeforeEndDate_ShouldReturnUpdatedSchema() {
        Schema existingSchema = schemaService.createNew(Populated.schema().build());
        UpdateSchema updateSchemaRequest = Populated.schemaUpdateRequest()
                .displayName("updated display name")
                .physicalTableName("updated physical aTable name")
                .startDate(LocalDate.of(2019, 1, 1))
                .endDate(LocalDate.of(2018, 9, 1))
                .build();

        VavrAssert.assertFailed(
                schemaService
                        .updateWithoutFields(-1L, existingSchema.getId(), updateSchemaRequest))
                .withFailure(CRUDFailure.invalidRequestParameter("startDate", "Start date must be before end date"));
    }

    @Test
    public void updateWithoutFields_SchemaNotFound_ShouldReturnFailure() {
        Validation<CRUDFailure, Schema> updatedSchemaValidation =
                schemaService.updateWithoutFields(123L, 654L, Populated.schemaUpdateRequest().build());

        assertThat(updatedSchemaValidation.getError())
                .isEqualTo(CRUDFailure.notFoundIds("Schema", 654L));
    }

    @Test
    public void updateWithoutFields_EndDateNullAndLatestVersion_ShouldSaveEndDateOnSchema() {
        LocalDate existingStartDate = LocalDate.of(2018, 1, 1);
        LocalDate existingEndDate = LocalDate.of(2018, 9, 1);
        Set<Field> existingFields = newHashSet(DesignField.Populated.booleanField().build());
        Schema existingTable = schemaService.createNew(Populated.schema()
                .startDate(existingStartDate)
                .endDate(existingEndDate)
                .fields(existingFields)
                .latest(true)
                .build());

        UpdateSchema updateSchemaRequest = Populated.schemaUpdateRequest()
                .endDate(null)
                .build();

        Schema updatedSchema = VavrAssert.assertValid(
                schemaService
                        .updateWithoutFields(-1, existingTable.getId(), updateSchemaRequest))
                .getResult();

        assertThat(updatedSchema.getEndDate())
                .isNull();
    }

    @Test
    public void updateWithoutFields_EndDateNotAfterExistingStartDate_ShouldReturnError() {
        LocalDate existingStartDate = LocalDate.of(2018, 1, 1);
        LocalDate existingEndDate = LocalDate.of(2018, 9, 1);

        Schema existingTable = schemaService.createNew(Populated.schema()
                .startDate(existingStartDate)
                .endDate(existingEndDate)
                .fields(newHashSet(DesignField.Populated.booleanField().build()))
                .latest(true)
                .build());

        UpdateSchema updateSchemaRequest = Populated.schemaUpdateRequest()
                .endDate(existingStartDate)
                .build();

        VavrAssert.assertFailed(
                schemaService
                        .updateWithoutFields(-1, existingTable.getId(), updateSchemaRequest))
                .withFailure(CRUDFailure.invalidRequestParameter(
                        "startDate", "Start date must be before end date"));
    }

    @Test
    public void updateWithoutFields_EndDateNullAndNotLatestVersion_ShouldReturnError() {
        LocalDate existingStartDate = LocalDate.of(2018, 1, 1);
        LocalDate existingEndDate = LocalDate.of(2018, 9, 1);
        Set<Field> existingFields = newHashSet(DesignField.Populated.booleanField().build());
        Schema existingTable = schemaService.createNew(Populated.schema()
                .startDate(existingStartDate)
                .endDate(existingEndDate)
                .fields(existingFields)
                .latest(false)
                .build());

        UpdateSchema updateSchemaRequest = Populated.schemaUpdateRequest()
                .endDate(null)
                .build();

        VavrAssert.assertFailed(
                schemaService
                        .updateWithoutFields(-1, existingTable.getId(), updateSchemaRequest))
                .withFailure(CRUDFailure.invalidRequestParameter(
                        "endDate",
                        "End date cannot be blank if schema is not the latest version"));
    }

    @Test
    public void updateWithoutFields_NewVesionStartDateIsBeforeEndDateOfPreviousVersion_ReturnsError() {
        Schema oldVersion = schemaService.createNew(Populated.schema()
                .displayName("Test Schema")
                .majorVersion(1)
                .startDate(LocalDate.of(2000, 1, 1))
                .endDate(LocalDate.of(2001, 1, 1))
                .build());

        Schema newVersion = schemaService.createNew(Populated.schema()
                .displayName("Test Schema")
                .majorVersion(2)
                .startDate(LocalDate.of(2002, 1, 1))
                .endDate(null)
                .build());

        UpdateSchema updateRequest = Populated.schemaUpdateRequest()
                .startDate(LocalDate.of(2000, 12, 1))
                .endDate(null)
                .build();

        VavrAssert.assertFailed(schemaService.updateWithoutFields(-1, newVersion.getId(), updateRequest))
                .withFailure(CRUDFailure.constraintFailure(
                        "Start date 2000-12-01 is before end date 2001-01-01 of previous version Test Schema v1"));
    }

    @Test
    public void updateWithoutFields_OldVersionUpdatedToHaveEndDateAfterStartDateOfNextVersions_ShouldReturnError() {
        Schema oldVersion = schemaService.createNew(Populated.schema()
                .majorVersion(1)
                .displayName("MACCY")
                .startDate(LocalDate.of(2000, 1, 1))
                .endDate(LocalDate.of(2001, 1, 1))
                .build());

        Schema nextVersion = schemaService.createNew(Populated.schema()
                .majorVersion(2)
                .displayName("MACCY")
                .startDate(LocalDate.of(2002, 1, 1))
                .endDate(null)
                .build());

        UpdateSchema updateRequest = Populated.schemaUpdateRequest()
                .startDate(LocalDate.of(2001, 2, 1))
                .endDate(LocalDate.of(2002, 2, 1))
                .build();

        VavrAssert.assertFailed(schemaService.updateWithoutFields(-2, oldVersion.getId(), updateRequest))
                .withFailure(CRUDFailure.constraintFailure(
                        "End date 2002-02-01 is after start date of next schema version MACCY v2 start date 2002-01-01"));
    }

    @Test
    public void updateWithoutFields_NextSchemaHasStartDateButNoEndDate_ShouldBeSuccessful() {
        Schema oldVersion = schemaService.createNew(Populated.schema()
                .majorVersion(1)
                .displayName("MACCY")
                .startDate(LocalDate.of(2000, 1, 1))
                .endDate(LocalDate.of(2001, 1, 1))
                .build());

        Schema nextVersion = schemaService.createNew(Populated.schema()
                .majorVersion(2)
                .displayName("MACCY")
                .startDate(LocalDate.of(2002, 1, 1))
                .endDate(null)
                .build());

        UpdateSchema updateRequest = Populated.schemaUpdateRequest()
                .startDate(LocalDate.of(2000, 1, 1))
                .endDate(LocalDate.of(2001, 12, 1))
                .build();

        VavrAssert.assertValid(
                schemaService.updateWithoutFields(-2, oldVersion.getId(), updateRequest));
    }

    @Test
    public void updateWithoutFields_NewNameIsReservedKeyWord_ShouldReturnError() {
        LocalDate existingStartDate = LocalDate.of(2018, 1, 1);
        LocalDate existingEndDate = LocalDate.of(2018, 9, 1);
        Set<Field> existingFields = newHashSet(DesignField.Populated.booleanField().build());
        Schema existingTable = schemaService.createNew(Populated.schema()
                .startDate(existingStartDate)
                .endDate(existingEndDate)
                .fields(existingFields)
                .latest(true)
                .build());

        UpdateSchema updateSchemaRequest = Populated.schemaUpdateRequest()
                .physicalTableName(ReservedWord.TABLE.getValue())
                .build();

        VavrAssert.assertFailed(
                schemaService
                        .updateWithoutFields(-1, existingTable.getId(), updateSchemaRequest))
                .withFailure(CRUDFailure.constraintFailure(
                        "TABLE is a reserved word not allowed in a physical table name"));
    }

    @Test
    public void updateWithoutFields_SchemaNameVALIDATION_RULE_RESULTS_ShouldReturnError() {
        LocalDate existingStartDate = LocalDate.of(2018, 1, 1);
        LocalDate existingEndDate = LocalDate.of(2018, 9, 1);
        Set<Field> existingFields = newHashSet(DesignField.Populated.booleanField().build());
        Schema existingTable = schemaService.createNew(Populated.schema()
                .startDate(existingStartDate)
                .endDate(existingEndDate)
                .fields(existingFields)
                .latest(true)
                .build());

        UpdateSchema updateSchemaRequest = Populated.schemaUpdateRequest()
                .physicalTableName("VALIDATION_RULE_RESULTS")
                .build();

        VavrAssert.assertFailed(
                schemaService
                        .updateWithoutFields(-1, existingTable.getId(), updateSchemaRequest))
                .withFailure(CRUDFailure.constraintFailure(
                        "VALIDATION_RULE_RESULTS is a reserved word not allowed in a physical table name"));
    }

    @Test
    public void findByProductIdAndId_NoSchemasForProduct_ReturnsFailure() {
        Validation<CRUDFailure, Schema> byProductIdAndId = schemaService.findByProductIdAndId(109, 88);

        assertThat(byProductIdAndId.getError())
                .isEqualTo(CRUDFailure.notFoundIds("Schema", 88L));
    }

    @Test
    public void findByProductIdAndId_SchemaForProduct_ReturnsFailure() {
        Schema schema = schemaService.createNew(Populated.schema().build());

        VavrAssert.assertValid(schemaService.findByProductIdAndId(-1, schema.getId()))
                .withResult(schema);
    }

    @Test
    public void deleteByProductIdAndId_SchemaForProduct_ReturnsSuccessfullyDeletedId() {

        Schema savedSchema = schemaService.createNew(Populated.schema()
                .productId(120L)
                .build());

        VavrAssert.assertValid(
                schemaService
                        .deleteByProductIdAndId(-1, savedSchema.getId()))
                .extracting(Identifiable::getId)
                .withResult(savedSchema.getId());
    }

    @Test
    public void deleteByProductIdAndId_NoSchemaFound_ReturnsFailure() {
        Validation<CRUDFailure, Identifiable> byProductIdAndId = schemaService.deleteByProductIdAndId(-1, 200L);

        assertThat(byProductIdAndId.getError())
                .isEqualTo(CRUDFailure.notFoundIds("Schema", 200L));
    }

    @Test
    public void copySchema_SchemaExists_CreatesNewSchemaWithProperties() {
        Schema existingSchema = schemaService.createNew(Populated.schema()
                .productId(101L)
                .displayName("Schema A")
                .physicalTableName("A")
                .majorVersion(10)
                .latest(false)
                .startDate(LocalDate.of(2018, 1, 1))
                .endDate(LocalDate.of(2018, 9, 1))
                .build());

        Long newSchemaId = VavrAssert.assertValid(
                schemaService.copySchema(101L, existingSchema.getId(), CopySchemaRequest.builder()
                        .displayName("Copied A")
                        .physicalTableName("COP_A")
                        .startDate(LocalDate.of(2019, 1, 1))
                        .endDate(LocalDate.of(2019, 9, 1))
                        .build()))
                .getResult().getId();

        Schema copied = VavrAssert.assertValid(schemaService.findWithValidation(newSchemaId))
                .getResult();

        soft.assertThat(copied.getProductId())
                .isEqualTo(101L);
        soft.assertThat(copied.getDisplayName())
                .isEqualTo("Copied A");
        soft.assertThat(copied.getPhysicalTableName())
                .isEqualTo("COP_A");
        soft.assertThat(copied.getStartDate())
                .isEqualTo(LocalDate.of(2019, 1, 1));
        soft.assertThat(copied.getEndDate())
                .isEqualTo(LocalDate.of(2019, 9, 1));
        soft.assertThat(copied.getMajorVersion())
                .isEqualTo(1);
        soft.assertThat(copied.getLatest())
                .isTrue();
    }

    @Test
    public void copySchema_SchemaWithFieldsExists_CreatesFieldsOnNewSchema() {
        Schema existingSchema = schemaService.createNew(Populated.schema()
                .productId(101L)
                .displayName("Schema A")
                .physicalTableName("A")
                .majorVersion(10)
                .latest(false)
                .startDate(LocalDate.of(2018, 1, 1))
                .endDate(LocalDate.of(2018, 9, 1))
                .build());

        Field fieldA = fieldService.createNewField(stringField("A").build(), existingSchema.getId());
        Field fieldB = fieldService.createNewField(dateField("B").build(), existingSchema.getId());

        Long newSchemaId = VavrAssert.assertValid(
                schemaService.copySchema(101L, existingSchema.getId(), CopySchemaRequest.builder()
                        .displayName("Copied A")
                        .physicalTableName("COP_A")
                        .startDate(LocalDate.of(2019, 1, 1))
                        .endDate(LocalDate.of(2019, 9, 1))
                        .build()))
                .getResult().getId();

        Schema copied = VavrAssert.assertValid(schemaService.findWithValidation(newSchemaId))
                .getResult();

        soft.assertThat(MapperUtils.map(copied.getFields(), Field::getId))
                .doesNotContain(fieldA.getId(), fieldB.getId());
        soft.assertThat(copied.getFields())
                .extracting(Field::getName)
                .containsExactly("A", "B");
        soft.assertThat(copied.getFields())
                .extracting(field -> field.getClass().getName())
                .containsExactly(StringField.class.getName(), DateField.class.getName());
    }

    @Test
    public void copySchema_SchemaDoesNorExist_ReturnsError() {
        VavrAssert.assertCollectionFailure(
                schemaService.copySchema(101L, -1L, copySchemaRequest().build()))
                .withFailure(CRUDFailure.notFoundIds("Schema", -1L));
    }

    @Test
    public void copySchema_SchemaExistsForNewSchemaName_ReturnsError() {
        Schema schemaToBeCopied = schemaService.createNew(Populated.schema("TO_COPY").productId(101L).build());
        Schema existingSchema = schemaService.createNew(Populated.schema()
                .productId(101L)
                .displayName("Schema A")
                .build());

        VavrAssert.assertCollectionFailure(
                schemaService.copySchema(101L, schemaToBeCopied.getId(), copySchemaRequest()
                        .displayName("Schema A")
                        .build()))
                .withFailure(CRUDFailure.constraintFailure(
                        "Schema(s) exist for display name, [Schema A] and version [1]"));
    }

    @Test
    public void copySchema_SchemaExistsForNewSchemaPhysicalName_ReturnsError() {
        Schema schemaToBeCopied = schemaService.createNew(Populated.schema("TO_COPY").productId(101L).build());
        Schema existingSchema = schemaService.createNew(Populated.schema()
                .productId(101L)
                .physicalTableName("A")
                .build());

        VavrAssert.assertCollectionFailure(
                schemaService.copySchema(101L, schemaToBeCopied.getId(), copySchemaRequest()
                        .physicalTableName("A")
                        .build()))
                .withFailure(CRUDFailure.constraintFailure(
                        "Schema(s) exist for physical table name, [A] and version [1]"));
    }

    @Test
    public void createExampleCsv_SchemaValid_AddsHeaderToOutputStream() {
        Schema schema = schemaService.createNew(Populated.schema().build());

        fieldService.createNewField(DesignField.Populated.longField("Id").build(), schema.getId());
        fieldService.createNewField(DesignField.Populated.stringField("Name").build(), schema.getId());
        fieldService.createNewField(DesignField.Populated.stringField("Description").build(), schema.getId());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        VavrAssert.assertValid(schemaService.createExampleCsv(schema.getId(), outputStream));

        assertThat(new String(outputStream.toByteArray()))
                .isEqualTo("Id,Name,Description");
    }

    @Test
    public void createExampleCsv_SchemaNotFound_ReturnsError() {
        VavrAssert.assertFailed(schemaService.createExampleCsv(-1, null))
                .withFailure(ErrorResponse.valueOf("Could not find Schema for ids [-1]", "NOT_FOUND"));
    }

    @Test
    public void validateAndCreateNew_NameIsReservedKeyWord_ShouldReturnError() {
        Schema newSchemaRequest = Populated.schema()
                .physicalTableName(ReservedWord.TABLE.getValue())
                .build();

        VavrAssert.assertCollectionFailure(
                schemaService
                        .validateAndCreateNew(newSchemaRequest))
                .withFailure(CRUDFailure.constraintFailure(
                        "TABLE is a reserved word not allowed in a physical table name"));
    }

    @Test
    public void validateAndCreateNew_NameIsVALIDATION_RULE_RESULTS_ShouldReturnError() {
        Schema newSchemaRequest = Populated.schema()
                .physicalTableName("VALIDATION_RULE_RESULTS")
                .build();

        VavrAssert.assertCollectionFailure(
                schemaService
                        .validateAndCreateNew(newSchemaRequest))
                .withFailure(CRUDFailure.constraintFailure(
                        "VALIDATION_RULE_RESULTS is a reserved word not allowed in a physical table name"));
    }
}
