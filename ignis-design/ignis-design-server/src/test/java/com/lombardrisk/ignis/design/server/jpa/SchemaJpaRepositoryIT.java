package com.lombardrisk.ignis.design.server.jpa;

import com.google.common.collect.ImmutableSet;
import com.lombardrisk.ignis.design.field.model.BooleanField;
import com.lombardrisk.ignis.design.field.model.Field;
import com.lombardrisk.ignis.design.field.model.IntField;
import com.lombardrisk.ignis.design.field.model.StringField;
import com.lombardrisk.ignis.design.server.DesignStudioTestConfig;
import com.lombardrisk.ignis.design.server.productconfig.ProductConfig;
import com.lombardrisk.ignis.design.server.productconfig.rule.model.ValidationRule;
import com.lombardrisk.ignis.design.server.productconfig.schema.Schema;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import static com.lombardrisk.ignis.design.server.fixtures.Design.Populated;
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings({ "OptionalGetWithoutIsPresent" })
@RunWith(SpringRunner.class)
@DesignStudioTestConfig
public class SchemaJpaRepositoryIT {

    @Autowired
    private SchemaJpaRepository schemaRepository;
    @Autowired
    private FieldJpaRepository fieldJpaRepository;
    @Autowired
    private ValidationRuleJpaRepository ruleRepository;
    @Autowired
    private ProductConfigJpaRepository productConfigRepository;
    @Autowired
    private EntityManager entityManager;

    private ProductConfig product;

    @Before
    public void setUp() {
        product = productConfigRepository.saveAndFlush(Populated.productConfig().build());
    }

    @Rule
    public final JUnitSoftAssertions soft = new JUnitSoftAssertions();

    @Test
    public void saveAndRetrieve() {
        Date createdTime = new Date();
        Schema savedTable = schemaRepository.saveAndFlush(
                Schema.builder()
                        .productId(product.getId())
                        .physicalTableName("MYTABLE")
                        .displayName("DISPLAY NAME FOR MYTABLE")
                        .createdTime(createdTime)
                        .createdBy("me")
                        .fields(emptySet())
                        .majorVersion(1892)
                        .startDate(LocalDate.of(1912, 1, 1))
                        .endDate(LocalDate.of(1925, 3, 21))
                        .build());

        Schema table = schemaRepository.findById(savedTable.getId()).get();

        soft.assertThat(table.getProductId()).isEqualTo(product.getId());
        soft.assertThat(table.getPhysicalTableName()).isEqualTo("MYTABLE");
        soft.assertThat(table.getCreatedTime()).isEqualTo(createdTime);
        soft.assertThat(table.getCreatedBy()).isEqualTo("me");
        soft.assertThat(table.getMajorVersion()).isEqualTo(1892);
        soft.assertThat(table.getStartDate()).isEqualTo(LocalDate.of(1912, 1, 1));
        soft.assertThat(table.getEndDate()).isEqualTo(LocalDate.of(1925, 3, 21));
    }

    @Test
    public void oneToMany_cascade_ValidationRules() {
        Set<ValidationRule> validationRules = new LinkedHashSet<>(asList(
                Populated.validationRule()
                        .ruleId("R1")
                        .build(),
                Populated.validationRule()
                        .ruleId("R2")
                        .build()));

        Schema savedTable = schemaRepository.saveAndFlush(
                Populated.schema()
                        .productId(product.getId())
                        .validationRules(validationRules)
                        .fields(emptySet())
                        .build());
        entityManager.clear();
        savedTable = schemaRepository.findById(savedTable.getId()).get();

        Set<ValidationRule> savedValidationRules = savedTable.getValidationRules();

        soft.assertThat(savedValidationRules)
                .extracting(ValidationRule::getId)
                .hasSize(2)
                .isNotNull();
        soft.assertThat(savedValidationRules)
                .extracting(ValidationRule::getRuleId)
                .containsExactly("R1", "R2");
        soft.assertThat(ruleRepository.findAll())
                .hasSize(2);
    }

    @Test
    public void oneToOne_delete_ValidationRules() {
        Set<ValidationRule> validationRules = ImmutableSet.of(
                Populated.validationRule().ruleId("rule id 1").build(),
                Populated.validationRule().ruleId("rule id 2").build());

        Schema savedTable = schemaRepository.saveAndFlush(
                Populated.schema()
                        .productId(product.getId())
                        .validationRules(validationRules)
                        .fields(emptySet())
                        .build());

        schemaRepository.delete(savedTable);

        soft.assertThat(schemaRepository.findAll()).isEmpty();
        soft.assertThat(ruleRepository.findAll()).isEmpty();
    }

    @Test
    public void save_PreservesInsertionOrderOfFields() {
        Schema createdTable = schemaRepository.save(Populated.schema()
                .productId(product.getId())
                .physicalTableName("table_with_ordered_fields")
                .fields(emptySet())
                .build());

        fieldJpaRepository.save((Field) IntField.builder().name("int_field").schemaId(createdTable.getId()).build());
        fieldJpaRepository.save((Field) StringField.builder()
                .name("string_field")
                .schemaId(createdTable.getId())
                .build());
        fieldJpaRepository.save((Field) BooleanField.builder()
                .name("boolean_field")
                .schemaId(createdTable.getId())
                .build());

        entityManager.flush();
        entityManager.clear();

        Schema retrieved = schemaRepository.getOne(createdTable.getId());
        soft.assertThat(retrieved.getPhysicalTableName()).isEqualTo("table_with_ordered_fields");
        soft.assertThat(retrieved.getFields())
                .extracting(Field::getName)
                .containsExactly("int_field", "string_field", "boolean_field");
    }

    @Test
    public void findByProductIdAndSchemaId_ReturnsSchema() {
        Schema schemaWithProduct = Populated.schema()
                .productId(product.getId())
                .fields(emptySet())
                .physicalTableName("PRODUCT")
                .build();

        Schema saveSchema = schemaRepository.save(schemaWithProduct);
        product.getTables().add(saveSchema);

        ProductConfig createdProduct = productConfigRepository.save(product);

        Optional<Schema> byIdAndProductId =
                schemaRepository.findByIdAndProductId(createdProduct.getId(), saveSchema.getId());

        soft.assertThat(byIdAndProductId).hasValue(saveSchema);

        Optional<Schema> schemaForNonExistentProduct = schemaRepository
                .findByIdAndProductId(-900, saveSchema.getId());

        soft.assertThat(schemaForNonExistentProduct).isEmpty();
    }

    @Test
    public void findSecondLatestVersion_ThreeVersions_ReturnsSecondVersion() {
        schemaRepository.save(Populated.schema()
                .productId(product.getId())
                .displayName("DISPLAY_NAME")
                .fields(emptySet())
                .majorVersion(1)
                .latest(false)
                .build());
        Schema two = schemaRepository.save(Populated.schema()
                .productId(product.getId())
                .displayName("DISPLAY_NAME")
                .fields(emptySet())
                .majorVersion(2)
                .latest(false)
                .build());
        schemaRepository.save(Populated.schema()
                .productId(product.getId())
                .displayName("DISPLAY_NAME")
                .fields(emptySet())
                .majorVersion(3)
                .latest(true)
                .build());

        Optional<Schema> secondToLatestVersion = schemaRepository.findSecondLatestVersion("DISPLAY_NAME");
        assertThat(secondToLatestVersion)
                .hasValue(two);
    }

    @Test
    public void findSecondLatestVersion_NoPreviousVersion_ReturnsEmpty() {
        Schema schemaWithProduct = Populated.schema()
                .productId(product.getId())
                .displayName("latest")
                .fields(emptySet())
                .majorVersion(1)
                .build();

        schemaRepository.save(schemaWithProduct);

        Optional<Schema> secondToLatestVersion = schemaRepository.findSecondLatestVersion("latest");
        assertThat(secondToLatestVersion).isEmpty();
    }

    @Test
    public void findSecondLatestVersion_NoSchemasAtAll_ReturnsEmpty() {
        Optional<Schema> secondToLatestVersion = schemaRepository.findSecondLatestVersion("NOTHING");
        assertThat(secondToLatestVersion).isEmpty();
    }

    @Test
    public void findPreviousVersion_NameAndVersionPresent_ReturnsSchema() {
        schemaRepository.save(Populated.schema("chipPan")
                .productId(product.getId())
                .fields(emptySet())
                .majorVersion(23)
                .build());
        Schema chipPan4 = schemaRepository.save(Populated.schema()
                .productId(product.getId())
                .displayName("chipPan")
                .fields(emptySet())
                .majorVersion(4)
                .build());
        schemaRepository.save(Populated.schema("oven")
                .productId(product.getId())
                .fields(emptySet())
                .majorVersion(23)
                .build());

        Optional<Schema> result = schemaRepository.findPreviousVersion("chipPan", 23);
        soft.assertThat(result)
                .map(Schema::getId)
                .hasValue(chipPan4.getId());
        soft.assertThat(result)
                .map(Schema::getDisplayName)
                .hasValue("chipPan");
        soft.assertThat(result)
                .map(Schema::getMajorVersion)
                .hasValue(4);
    }

    @Test
    public void findPreviousVersion_NoPreviousVersion_ReturnsEmpty() {
        schemaRepository.save(Populated.schema("chipPan").productId(product.getId()).fields(emptySet())
                .majorVersion(23).build());

        Optional<Schema> result = schemaRepository.findPreviousVersion("chipPan", 23);
        assertThat(result).isEmpty();
    }

    @Test
    public void findPreviousVersion_VersionButNoNamePresent_ReturnsEmpty() {
        schemaRepository.save(Populated.schema("chipPan").productId(product.getId())
                .fields(emptySet())
                .majorVersion(4).build());

        Optional<Schema> result = schemaRepository.findPreviousVersion("oven", 4);
        assertThat(result).isEmpty();
    }

    @Test
    public void findNextVersion_NameAndVersionPresent_ReturnsSchema() {
        Schema chipPan23 = schemaRepository.save(Populated.schema("chipPan")
                .productId(product.getId())
                .fields(emptySet())
                .majorVersion(23)
                .build());
        schemaRepository.save(Populated.schema("chipPan").productId(product.getId()).fields(emptySet())
                .majorVersion(4).build());
        schemaRepository.save(Populated.schema("oven").productId(product.getId()).fields(emptySet())
                .majorVersion(23).build());

        Optional<Schema> result = schemaRepository.findNextVersion("chipPan", 4);
        soft.assertThat(result)
                .map(Schema::getId)
                .hasValue(chipPan23.getId());
        soft.assertThat(result)
                .map(Schema::getDisplayName)
                .hasValue("chipPan");
        soft.assertThat(result)
                .map(Schema::getMajorVersion)
                .hasValue(23);
    }

    @Test
    public void findNextVersion_NoNextVersion_ReturnsEmpty() {
        schemaRepository.save(Populated.schema("chipPan").productId(product.getId())
                .fields(emptySet())
                .majorVersion(22)
                .build());
        schemaRepository.save(Populated.schema("chipPan").productId(product.getId())
                .fields(emptySet())
                .majorVersion(23)
                .build());

        Optional<Schema> result = schemaRepository.findNextVersion("chipPan", 23);
        assertThat(result).isEmpty();
    }

    @Test
    public void findNextVersion_VersionButNoNamePresent_ReturnsEmpty() {
        schemaRepository.save(Populated.schema("chipPan").productId(product.getId())
                .fields(emptySet())
                .majorVersion(4).build());
        schemaRepository.save(Populated.schema("oven").productId(product.getId())
                .fields(emptySet())
                .majorVersion(5)
                .build());

        Optional<Schema> result = schemaRepository.findNextVersion("chipPan", 4);
        assertThat(result).isEmpty();
    }

    @Test
    public void findByPhysicalTableNameAndMajorVersion_Exists_ReturnsValue() {
        Schema toSearchFor = schemaRepository.save(Populated.schema("phys")
                .productId(product.getId())
                .majorVersion(1)
                .fields(emptySet())
                .build());
        schemaRepository.save(Populated.schema()
                .productId(product.getId())
                .physicalTableName("ical")
                .majorVersion(1)
                .fields(emptySet())
                .build());

        Optional<Schema> byPhysicalTableNameAndMajorVersion = schemaRepository
                .findByPhysicalTableNameAndMajorVersion("phys", 1);

        assertThat(byPhysicalTableNameAndMajorVersion)
                .hasValue(toSearchFor);
    }

    @Test
    public void findByPhysicalTableNameAndMajorVersion_DoesNotExist_ReturnsEmpty() {
        schemaRepository.save(Populated.schema("phys").productId(product.getId())
                .fields(emptySet())
                .majorVersion(1)
                .build());
        schemaRepository.save(Populated.schema("ical").productId(product.getId())
                .fields(emptySet())
                .majorVersion(1)
                .build());

        Optional<Schema> byPhysicalTableNameAndMajorVersion =
                schemaRepository.findByPhysicalTableNameAndMajorVersion("phys", 2);

        assertThat(byPhysicalTableNameAndMajorVersion)
                .isEmpty();
    }

    @Test
    public void findByDisplayNameAndMajorVersion_Exists_ReturnsValue() {
        Schema toSearchFor = schemaRepository.save(Populated.schema("dis")
                .productId(product.getId())
                .majorVersion(1)
                .fields(emptySet())
                .build());
        schemaRepository.save(Populated.schema("play").productId(product.getId())
                .fields(emptySet())
                .majorVersion(1)
                .build());

        Optional<Schema> byDisplayNameAndMajorVersion =
                schemaRepository.findByDisplayNameAndMajorVersion("dis", 1);

        assertThat(byDisplayNameAndMajorVersion)
                .hasValue(toSearchFor);
    }

    @Test
    public void findByDisplayNameAndMajorVersion_DoesNotExist_ReturnsEmpty() {
        schemaRepository.save(Populated.schema("dis").productId(product.getId())
                .majorVersion(1)
                .fields(emptySet())
                .build());
        schemaRepository.save(Populated.schema("play").productId(product.getId())
                .majorVersion(1)
                .fields(emptySet())
                .build());

        Optional<Schema> byDisplayNameAndMajorVersion =
                schemaRepository.findByDisplayNameAndMajorVersion("dis", 10);

        assertThat(byDisplayNameAndMajorVersion)
                .isEmpty();
    }

    @Test
    public void findMaxVersionByPhysicalTableName_DoesNotExist_ReturnsEmpty() {
        schemaRepository.save(Populated.schema("dis").productId(product.getId())
                .fields(emptySet())
                .majorVersion(1)
                .build());
        schemaRepository.save(Populated.schema("play").productId(product.getId())
                .fields(emptySet())
                .majorVersion(1)
                .build());

        Optional<Schema> byDisplayNameAndMajorVersion =
                schemaRepository.findMaxVersionByPhysicalTableName("not_exist");

        assertThat(byDisplayNameAndMajorVersion)
                .isEmpty();
    }

    @Test
    public void findMaxVersionByPhysicalTableName_MultipleVersions_ReturnsLatest() {
        schemaRepository.save(Populated.schema("table1").productId(product.getId())
                .fields(emptySet())
                .majorVersion(1)
                .build());
        schemaRepository.save(Populated.schema("table1").productId(product.getId())
                .fields(emptySet())
                .majorVersion(2)
                .build());
        schemaRepository.save(Populated.schema("table1").productId(product.getId())
                .fields(emptySet())
                .majorVersion(3)
                .build());
        Schema version4 =
                schemaRepository.save(Populated.schema("table1").productId(product.getId())
                        .fields(emptySet())
                        .majorVersion(4)
                        .build());

        Optional<Schema> byDisplayNameAndMajorVersion =
                schemaRepository.findMaxVersionByPhysicalTableName("table1");

        assertThat(byDisplayNameAndMajorVersion)
                .hasValue(version4);
    }
}
