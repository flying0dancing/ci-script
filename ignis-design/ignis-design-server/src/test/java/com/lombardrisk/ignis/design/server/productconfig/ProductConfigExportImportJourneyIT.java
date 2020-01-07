package com.lombardrisk.ignis.design.server.productconfig;

import com.google.common.collect.ImmutableList;
import com.lombardrisk.ignis.common.assertions.VavrAssert;
import com.lombardrisk.ignis.design.field.DesignField;
import com.lombardrisk.ignis.design.field.FieldService;
import com.lombardrisk.ignis.design.field.model.DecimalField;
import com.lombardrisk.ignis.design.field.model.Field;
import com.lombardrisk.ignis.design.field.model.StringField;
import com.lombardrisk.ignis.design.server.DesignStudioTestConfig;
import com.lombardrisk.ignis.design.server.productconfig.export.ProductConfigExportFileService;
import com.lombardrisk.ignis.design.server.productconfig.rule.model.ValidationRule;
import com.lombardrisk.ignis.design.server.productconfig.schema.RuleService;
import com.lombardrisk.ignis.design.server.productconfig.schema.Schema;
import com.lombardrisk.ignis.design.server.productconfig.schema.request.CreateSchemaRequest;
import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import static com.lombardrisk.ignis.common.assertions.VavrAssert.assertValid;
import static com.lombardrisk.ignis.design.server.fixtures.Design.Populated;

@RunWith(SpringRunner.class)
@DesignStudioTestConfig
public class ProductConfigExportImportJourneyIT {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private final SoftAssertions soft = new SoftAssertions();

    @Autowired
    private ProductConfigService productConfigService;

    @Autowired
    private FieldService fieldService;

    @Autowired
    private ProductConfigExportFileService productConfigExportFileService;

    @Autowired
    private ProductConfigImportService productConfigImportService;

    @Autowired
    private RuleService ruleService;

    @Autowired
    private EntityManager entityManager;

    @Before
    public void setUp() {
        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken("test", ""));
    }

    @Test
    public void createExportImport() throws IOException {
        ProductConfig productConfig = productConfigService.saveProductConfig(
                Populated.newProductRequest("PIZZA BOARD").build());

        CreateSchemaRequest schemaToImport = Populated.createSchemaRequest()
                .displayName("PIZZA")
                .build();

        Schema createdSchema = assertValid(productConfigService.createNewSchemaOnProduct(
                productConfig.getId(), schemaToImport)).getResult();

        fieldService.save(
                createdSchema.getId(),
                DesignField.Populated.stringFieldRequest("TYPE").build());
        fieldService.save(
                createdSchema.getId(),
                DesignField.Populated.decimalFieldRequest("SAUCE_WATER_CONTENT").build());
        fieldService.save(createdSchema.getId(),
                DesignField.Populated.stringFieldRequest("CRUST_TYPE").build());

        flushTransaction();

        ruleService.saveValidationRule(createdSchema.getId(), Populated.validationRule()
                .name("Naples style pizza rules")
                .expression("TYPE == 'NEAPOLITAN' && CRUST_TYPE == 'LEOPARD' && SAUCE_WATER_CONTENT > 50.0")
                .build())
                .get();

        flushTransaction();

        ProductConfig productToExport = productConfigService.findById(productConfig.getId())
                .get();

        File file = temporaryFolder.newFile("PIZZA BOARD.zip");
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            VavrAssert.assertValid(
                    productConfigExportFileService.exportProduct(productToExport.getId(), fileOutputStream));
        }

        assertValid(productConfigService.deleteById(productConfig.getId()));
        Long importedProductId = assertValid(productConfigImportService.importProductConfig(
                "PIZZA BOARD.zip", new FileInputStream(file)))
                .getResult()
                .getId();

        ProductConfig importedProduct = productConfigService.findById(importedProductId).get();

        soft.assertThat(importedProduct.getName())
                .isEqualTo(productToExport.getName());
        soft.assertThat(importedProduct.getVersion())
                .isEqualTo(productToExport.getVersion());
        soft.assertThat(importedProduct.getTables())
                .hasSize(1);
        soft.assertAll();

        Schema importedSchema = importedProduct.getTables().iterator().next();

        soft.assertThat(importedSchema.getDisplayName())
                .isEqualTo("PIZZA");
        soft.assertThat(importedSchema.getFields())
                .hasSize(3);
        soft.assertThat(importedSchema.getValidationRules())
                .hasSize(1);
        soft.assertAll();

        List<Field> importedSchemaFields = ImmutableList.copyOf(importedSchema.getFields().iterator());
        Field pizzaType = importedSchemaFields.get(0);
        soft.assertThat(pizzaType.getName())
                .isEqualTo("TYPE");
        soft.assertThat(pizzaType)
                .isInstanceOf(StringField.class);
        Field waterContent = importedSchemaFields.get(1);
        soft.assertThat(waterContent.getName())
                .isEqualTo("SAUCE_WATER_CONTENT");
        soft.assertThat(waterContent)
                .isInstanceOf(DecimalField.class);
        Field crustType = importedSchemaFields.get(2);
        soft.assertThat(crustType.getName())
                .isEqualTo("CRUST_TYPE");
        soft.assertThat(crustType)
                .isInstanceOf(StringField.class);

        ValidationRule importedValidationRule = importedSchema.getValidationRules().iterator().next();

        soft.assertThat(importedValidationRule.getName())
                .isEqualTo("Naples style pizza rules");
        soft.assertThat(importedValidationRule.getExpression())
                .isEqualTo("TYPE == 'NEAPOLITAN' && CRUST_TYPE == 'LEOPARD' && SAUCE_WATER_CONTENT > 50.0");
        soft.assertAll();
    }

    private void flushTransaction() {
        entityManager.flush();
        entityManager.clear();
    }
}
