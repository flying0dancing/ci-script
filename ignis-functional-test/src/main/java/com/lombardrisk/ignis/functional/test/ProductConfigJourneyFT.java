package com.lombardrisk.ignis.functional.test;

import com.google.common.collect.ImmutableList;
import com.lombardrisk.ignis.client.design.productconfig.NewProductConfigRequest;
import com.lombardrisk.ignis.client.design.productconfig.ProductConfigDto;
import com.lombardrisk.ignis.client.design.schema.SchemaDto;
import com.lombardrisk.ignis.client.design.schema.field.FieldDto;
import com.lombardrisk.ignis.client.external.productconfig.view.FieldView;
import com.lombardrisk.ignis.client.external.productconfig.view.ProductConfigView;
import com.lombardrisk.ignis.functional.test.assertions.expected.ExpectedPhysicalTable;
import com.lombardrisk.ignis.functional.test.config.FunctionalTest;
import com.lombardrisk.ignis.functional.test.dsl.FcrEngine;
import com.lombardrisk.ignis.functional.test.dsl.FcrEngineTest;
import com.lombardrisk.ignis.functional.test.junit.PassingTestsCleanupRule;
import com.lombardrisk.ignis.functional.test.steps.DesignStudioSteps;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

@SuppressWarnings({ "findbugs:URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD", "squid:S109" })
@RunWith(SpringRunner.class)
@FunctionalTest
public class ProductConfigJourneyFT {

    private static final String ALL_FIELD_TYPES_SCHEMA_PATH = "design/all-field-types/all-field-types.schema.json";

    private ProductConfigDto designedProduct;

    @Autowired
    private DesignStudioSteps designStudioSteps;

    @Autowired
    private FcrEngine fcrEngine;

    private FcrEngineTest test;

    @Before
    public void setUp() {
        test = fcrEngine.newTest();
    }

    @Rule
    public PassingTestsCleanupRule passingTestsCleanupRule = PassingTestsCleanupRule.withSteps(
            () -> {
                designStudioSteps.deleteProductConfig(designedProduct.getId());
                test.cleanup();
            });

    private static final ExpectedPhysicalTable ALL_FIELDS_V1_TABLE = ExpectedPhysicalTable.expected()
            .name("ALL_FLDS")
            .columns(asList(
                    FieldView.of("STRING_FIELD", FieldView.Type.STRING),
                    FieldView.of("DOUBLE_FIELD", FieldView.Type.DOUBLE),
                    FieldView.of("DECIMAL_FIELD", FieldView.Type.DECIMAL),
                    FieldView.of("INTEGER_FIELD", FieldView.Type.INTEGER),
                    FieldView.of("LONG_FIELD", FieldView.Type.LONG),
                    FieldView.of("DATE_FIELD", FieldView.Type.DATE),
                    FieldView.of("FLOAT_FIELD", FieldView.Type.FLOAT),
                    FieldView.of("BOOLEAN_FIELD", FieldView.Type.BOOLEAN),
                    FieldView.of("TIMESTAMP_FIELD", FieldView.Type.TIMESTAMP)))
            .numberOfRows(0)
            .build();

    private static final ExpectedPhysicalTable ALL_FIELDS_V2_TABLE = ALL_FIELDS_V1_TABLE.copy()
            .column(FieldView.of("RATE", FieldView.Type.DECIMAL))
            .build();

    private static final ExpectedPhysicalTable NEW_SCHEMA_V2_TABLE = ExpectedPhysicalTable.expected()
            .name("NEW_")
            .columns(asList(
                    FieldView.of("SECOND_BOOLEAN", FieldView.Type.BOOLEAN),
                    FieldView.of("S_ID", FieldView.Type.LONG)))
            .numberOfRows(0)
            .build();

    @Test
    public void exportImportUpgradeDelete() throws IOException {
        designedProduct = createAllFieldTypesDesignedProduct();
        File productConfigZipV1 =
                designStudioSteps.exportProductConfig(designedProduct.getId(), designedProduct.getName());
        ProductConfigView productConfigV1 = test.importProductConfig(productConfigZipV1).waitForJobToSucceed();

        test.assertProductConfigMatchesDesign(productConfigV1, designedProduct, designedProduct.getSchemas());
        test.assertPhysicalTableExists(productConfigV1, ALL_FIELDS_V1_TABLE);

        designedProduct = upgradeToNextVersionedDesignedProduct(designedProduct);
        SchemaDto allFieldSchema = upgradeAllFieldTypesSchema(designedProduct);
        List<SchemaDto> newSchemas = addNewSchemas(designedProduct);

        designedProduct = designStudioSteps.findProduct(designedProduct.getId());
        File productConfigZipV2 =
                designStudioSteps.exportProductConfig(designedProduct.getId(), designedProduct.getName());

        ProductConfigView productConfigV2 = test.importProductConfig(productConfigZipV2).waitForJobToSucceed();

        ImmutableList<SchemaDto> schemasV2 = ImmutableList.<SchemaDto>builder()
                .addAll(newSchemas)
                .add(allFieldSchema)
                .build();
        test.assertProductConfigMatchesDesign(productConfigV2, designedProduct, schemasV2);
        test.assertPhysicalTableExists(productConfigV2, ALL_FIELDS_V2_TABLE);
        test.assertPhysicalTableExists(productConfigV2, NEW_SCHEMA_V2_TABLE);

        test.deleteProductConfig(productConfigV2).waitForJobToSucceed();

        test.assertPhysicalTableExists(productConfigV1, ALL_FIELDS_V1_TABLE);
        test.assertPhysicalTableDoesNotExist(productConfigV2, "NEW_");

        test.deleteProductConfig(productConfigV1).waitForJobToSucceed();

        test.assertPhysicalTableDoesNotExist(productConfigV1, "ALL_FLDS");
        test.assertProductConfigDoesNotExist(productConfigV2);
        test.assertProductConfigDoesNotExist(productConfigV1);
    }

    private ProductConfigDto createAllFieldTypesDesignedProduct() throws IOException {
        String productConfigName = "ALL_FIELD_TYPES_EXP_IMP_DEL_" + randomAlphanumeric(30);
        NewProductConfigRequest productConfigRequest = NewProductConfigRequest.builder()
                .name(productConfigName)
                .version("1")
                .build();

        ProductConfigDto product = designStudioSteps.createProductConfig(productConfigRequest);

        SchemaDto schema = designStudioSteps.createTable(
                product.getId(), ALL_FIELD_TYPES_SCHEMA_PATH);

        designStudioSteps.addRules(
                "design/all-field-types/all-field-types.ruleset.json",
                product.getId(),
                schema);

        return designStudioSteps.findProduct(product.getId());
    }

    private static SchemaDto findAllFieldTypesSchema(final List<SchemaDto> schemas) {
        return schemas.stream()
                .filter(schemaView -> schemaView.getDisplayName().startsWith("ALL_FIELD_TYPES"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Cannot find 'ALL_FIELD_TYPES' schema"));
    }

    private SchemaDto upgradeSchema(
            final Long designedProductId,
            final SchemaDto previousVersionedSchema,
            final FieldDto... newFields) {
        LocalDate nextVersionedSchemaStartDate = toNextQuarter(previousVersionedSchema.getStartDate());

        SchemaDto nextVersionedSchema = designStudioSteps.createNextVersionedSchema(
                designedProductId,
                previousVersionedSchema.getId(),
                nextVersionedSchemaStartDate);

        designStudioSteps.addFields(designedProductId, nextVersionedSchema.getId(), asList(newFields));

        return designStudioSteps.getSchema(designedProductId, nextVersionedSchema.getId());
    }

    private LocalDate toLocalDate(final Date nextVersionedSchemaStartDate) {
        return LocalDateTime.ofInstant(nextVersionedSchemaStartDate.toInstant(), ZoneOffset.of(ZoneOffset.UTC.getId()))
                .toLocalDate();
    }

    private SchemaDto addNewSchema(final Long designedProductId) {
        String uniqueSuffix = randomAlphabetic(20).toUpperCase();
        SchemaDto newSchema = SchemaDto.builder()
                .displayName("NEW SCHEMA " + uniqueSuffix)
                .physicalTableName("NEW_" + uniqueSuffix)
                .majorVersion(1)
                .startDate(LocalDate.of(2000, 1, 1))
                .createdBy("FT")
                .build();

        SchemaDto createdSchema = designStudioSteps.addSchema(designedProductId, newSchema);
        designStudioSteps.addFields(
                designedProductId,
                createdSchema.getId(),
                singletonList(FieldDto.BooleanFieldDto.builder()
                        .name("SECOND_BOOLEAN")
                        .nullable(true)
                        .build()));

        return designStudioSteps.getSchema(designedProductId, createdSchema.getId());
    }

    private ProductConfigDto upgradeToNextVersionedDesignedProduct(final ProductConfigDto designedProduct) {
        ProductConfigDto productConfigDto = designStudioSteps.updateProduct(
                ProductConfigDto.builder()
                        .id(designedProduct.getId())
                        .version("2")
                        .build());

        return designStudioSteps.findProduct(productConfigDto.getId());
    }

    private SchemaDto upgradeAllFieldTypesSchema(final ProductConfigDto designedProduct) {
        SchemaDto allFieldTypesSchema = findAllFieldTypesSchema(designedProduct.getSchemas());
        FieldDto.DecimalFieldDto newField = FieldDto.DecimalFieldDto.builder()
                .name("RATE")
                .nullable(true)
                .precision(7)
                .scale(6)
                .build();

        return upgradeSchema(designedProduct.getId(), allFieldTypesSchema, newField);
    }

    private List<SchemaDto> addNewSchemas(final ProductConfigDto designedProduct) {
        SchemaDto newSchemaView = addNewSchema(designedProduct.getId());

        SchemaDto upgradedNewSchema = upgradeSchema(
                designedProduct.getId(),
                newSchemaView,
                FieldDto.LongFieldDto.builder()
                        .name("S_ID")
                        .nullable(true)
                        .build());

        return ImmutableList.of(newSchemaView, upgradedNewSchema);
    }

    private static LocalDate toNextQuarter(final LocalDate startDate) {
        return startDate.plusMonths(3);
    }
}
