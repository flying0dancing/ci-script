package com.lombardrisk.ignis.functional.test;

import com.lombardrisk.ignis.client.design.productconfig.NewProductConfigRequest;
import com.lombardrisk.ignis.client.design.productconfig.ProductConfigDto;
import com.lombardrisk.ignis.client.design.schema.SchemaDto;
import com.lombardrisk.ignis.client.external.productconfig.view.FieldView;
import com.lombardrisk.ignis.client.external.productconfig.view.ProductConfigView;
import com.lombardrisk.ignis.functional.test.assertions.expected.ExpectedPhysicalTable;
import com.lombardrisk.ignis.functional.test.config.FunctionalTest;
import com.lombardrisk.ignis.functional.test.dsl.FcrEngine;
import com.lombardrisk.ignis.functional.test.dsl.FcrEngineTest;
import com.lombardrisk.ignis.functional.test.junit.PassingTestsCleanupRule;
import com.lombardrisk.ignis.functional.test.steps.DesignStudioSteps;
import com.lombardrisk.ignis.functional.test.steps.ProductConfigSteps;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

/**
 * Tests the FCR specific import functionality, are jobs rolled back, are physical tables dropped/created etc
 */
@SuppressWarnings({
        "ConstantConditions", "squid:S109", "squid:S00100", "findbugs:URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD", "squid:S00112"
})
@RunWith(SpringRunner.class)
@FunctionalTest
public class ProductConfigImportFT {

    private static final String ALL_FIELD_TYPES_SCHEMA_PATH = "design/all-field-types/all-field-types.schema.json";

    private ProductConfigDto designProduct;

    @Autowired
    private ProductConfigSteps productConfigSteps;
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
    public PassingTestsCleanupRule passingTestsCleanupRule = PassingTestsCleanupRule.withSteps(() -> {
        designStudioSteps.deleteProductConfig(designProduct.getId());
        test.cleanup();
    });

    @Test
    public void importProduct_PhysicalSchemaExists_AutoRollsBackProduct() throws IOException {
        String productConfigName = "ALL_FIELD_TYPES_IMPORT" + randomAlphanumeric(30);
        NewProductConfigRequest productConfigRequest = NewProductConfigRequest.builder()
                .name(productConfigName)
                .version("1")
                .build();

        designProduct = designStudioSteps.createProductConfig(productConfigRequest);

        SchemaDto table = designStudioSteps.createTable(
                designProduct.getId(),
                ALL_FIELD_TYPES_SCHEMA_PATH);

        File exportedProductConfig =
                designStudioSteps.exportProductConfig(designProduct.getId(), designProduct.getName());

        productConfigSteps.createPhysicalSchema(table.getPhysicalTableName());

        ProductConfigView importedProduct =
                test.importProductConfig(exportedProductConfig).waitForJobToFail();

        test.assertPhysicalTableDoesNotExist(importedProduct, "ALL_FLDS");
    }

    @Test
    public void deleteProduct_PhysicalSchemaIsNotEmpty_DoesNotDropPhysicalSchema() throws IOException {
        String productConfigName = "ALL_FIELD_TYPES_DELETE_" + randomAlphanumeric(30);
        NewProductConfigRequest productConfigRequest = NewProductConfigRequest.builder()
                .name(productConfigName)
                .version("1")
                .build();

        designProduct = designStudioSteps.createProductConfig(productConfigRequest);

        designStudioSteps.createTable(
                designProduct.getId(),
                ALL_FIELD_TYPES_SCHEMA_PATH);

        File exportedProductConfig =
                designStudioSteps.exportProductConfig(designProduct.getId(), designProduct.getName());

        ProductConfigView importedProduct =
                test.importProductConfig(exportedProductConfig).waitForJobToSucceed();

        productConfigSteps.populatePhysicalSchema(importedProduct.getSchemas().get(0).getPhysicalTableName());

        test.deleteProductConfig(importedProduct).waitForJobToFail();

        test.assertPhysicalTableExists(
                importedProduct,
                ExpectedPhysicalTable.expected()
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
                        .numberOfRows(1)
                        .build());
    }
}
