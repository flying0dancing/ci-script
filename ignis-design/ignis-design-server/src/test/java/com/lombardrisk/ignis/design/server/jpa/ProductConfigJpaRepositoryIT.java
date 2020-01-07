package com.lombardrisk.ignis.design.server.jpa;

import com.lombardrisk.ignis.design.field.DesignField;
import com.lombardrisk.ignis.design.field.model.DecimalField;
import com.lombardrisk.ignis.design.field.model.StringField;
import com.lombardrisk.ignis.design.server.DesignStudioTestConfig;
import com.lombardrisk.ignis.design.server.fixtures.Design.Populated;
import com.lombardrisk.ignis.design.server.productconfig.ProductConfig;
import com.lombardrisk.ignis.design.server.productconfig.schema.Schema;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Sets.newHashSet;
import static com.lombardrisk.ignis.design.field.DesignField.Populated.decimalField;
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@RunWith(SpringRunner.class)
@DesignStudioTestConfig
public class ProductConfigJpaRepositoryIT {

    @Autowired
    private ProductConfigJpaRepository productConfigRepository;

    @Autowired
    private SchemaJpaRepository schemaRepository;

    @Autowired
    private FieldJpaRepository fieldJpaRepository;

    @Autowired
    private EntityManager entityManager;

    @Rule
    public JUnitSoftAssertions soft = new JUnitSoftAssertions();

    @Test
    public void crudOperations() {
        ProductConfig productConfig = ProductConfig.builder()
                .name("my first product config")
                .version("v1.0")
                .build();

        ProductConfig savedProductConfig = productConfigRepository.saveAndFlush(productConfig);

        List<Schema> savedSchemas = schemaRepository.saveAll(asList(
                Populated.schema()
                        .productId(savedProductConfig.getId())
                        .physicalTableName("TABLE_1")
                        .displayName("TABLE_1 display name")
                        .fields(emptySet())
                        .build(),
                Populated.schema()
                        .productId(savedProductConfig.getId())
                        .physicalTableName("TABLE_2")
                        .displayName("TABLE_2 display name")
                        .fields(emptySet())
                        .build()));

        soft.assertThat(savedProductConfig.getId())
                .isNotNull();
        soft.assertThat(savedProductConfig.getName())
                .isEqualTo("my first product config");
        soft.assertThat(savedProductConfig.getVersion())
                .isEqualTo("v1.0");
        soft.assertThat(savedProductConfig.getTables())
                .isNull();

        savedProductConfig.setTables(newHashSet(savedSchemas));

        ProductConfig savedProductWithSchemas = productConfigRepository.saveAndFlush(savedProductConfig);

        List<Long> savedSchemaIds = savedProductWithSchemas.getTables()
                .stream()
                .map(Schema::getId)
                .collect(toList());

        soft.assertThat(schemaRepository.findAll())
                .extracting(Schema::getId)
                .containsOnlyElementsOf(savedSchemaIds);

        savedProductWithSchemas.setName("my first product config updated");

        ProductConfig updatedProductConfig = productConfigRepository.saveAndFlush(savedProductWithSchemas);

        soft.assertThat(updatedProductConfig.getName())
                .isEqualTo("my first product config updated");

        productConfigRepository.delete(updatedProductConfig);

        Optional<ProductConfig> deletedProduct = productConfigRepository.findById(updatedProductConfig.getId());

        soft.assertThat(deletedProduct).isEmpty();
    }

    @Test
    public void addNewSchemaToProduct() {
        ProductConfig productConfig = productConfigRepository.save(Populated.productConfig().build());

        Schema version1 = schemaRepository.save(Populated.schema().majorVersion(1).build());
        productConfig.getTables().add(version1);
        ProductConfig productWithOneSchema = productConfigRepository.save(productConfig);

        Schema version2 = schemaRepository.save(Populated.schema().majorVersion(2).build());
        productWithOneSchema.getTables().add(version2);

        ProductConfig productWithTwoSchemas = productConfigRepository.save(productWithOneSchema);
        assertThat(productWithTwoSchemas.getTables())
                .isEqualTo(newHashSet(version1, version2));
    }

    @Test
    public void deleteProduct_DeletesAllSchemas() {
        ProductConfig productConfig = productConfigRepository.save(Populated.productConfig().build());

        Schema decimalSchema = schemaRepository.saveAndFlush(Populated.schema()
                .productId(productConfig.getId())
                .physicalTableName("DTABLE")
                .displayName("Decimal Table")
                .fields(newHashSet())
                .build());
        DecimalField decimalField =
                fieldJpaRepository.save(decimalField("NUMBER").schemaId(decimalSchema.getId()).build());

        Schema stringSchema = schemaRepository.saveAndFlush(Populated.schema()
                .productId(productConfig.getId())
                .physicalTableName("TABLE_2")
                .displayName("TABLE_2 display name")
                .fields(newHashSet())
                .build());

        StringField stringField = fieldJpaRepository.save(DesignField.Populated.stringField("STRING")
                .schemaId(stringSchema.getId())
                .build());

        decimalSchema.getValidationRules().add(Populated.validationRule()
                .ruleId("decimal field rule")
                .expression("NUMBER > 0.01")
                .contextFields(newHashSet(decimalField))
                .build());
        decimalSchema = schemaRepository.saveAndFlush(decimalSchema);

        stringSchema.getValidationRules().add(Populated.validationRule()
                .ruleId("string field rule")
                .expression("STRING == 'HELLO WORLD'")
                .contextFields(newHashSet(stringField))
                .build());
        schemaRepository.saveAndFlush(stringSchema);

        productConfig.setTables(newHashSet(decimalSchema, stringSchema));

        ProductConfig savedProductConfig = productConfigRepository.saveAndFlush(productConfig);
        entityManager.flush();
        entityManager.clear();

        productConfigRepository.deleteById(savedProductConfig.getId());

        soft.assertThat(schemaRepository.findAllById(newHashSet(stringSchema.getId(), decimalSchema.getId())))
                .isEmpty();
        soft.assertThat(productConfigRepository.findById(productConfig.getId()))
                .isEmpty();
    }

    @Test
    public void findByName() {
        soft.assertThat(productConfigRepository.findByName("some product config name"))
                .isEmpty();

        ProductConfig newProductConfig = Populated.productConfig().name("some product config name").build();

        productConfigRepository.saveAndFlush(newProductConfig);

        soft.assertThat(productConfigRepository.findByName("some product config name"))
                .isPresent();
    }
}
