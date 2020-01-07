package com.lombardrisk.ignis.server.jpa;

import com.google.common.collect.ImmutableSet;
import com.lombardrisk.ignis.server.config.IntegrationTestConfig;
import com.lombardrisk.ignis.server.product.fixture.ProductPopulated;
import com.lombardrisk.ignis.server.product.productconfig.ProductConfigRepository;
import com.lombardrisk.ignis.server.product.productconfig.model.ProductConfig;
import com.lombardrisk.ignis.server.product.rule.ValidationRuleRepository;
import com.lombardrisk.ignis.server.product.rule.model.ValidationRule;
import com.lombardrisk.ignis.server.product.table.TableRepository;
import com.lombardrisk.ignis.server.product.table.model.BooleanField;
import com.lombardrisk.ignis.server.product.table.model.DecimalField;
import com.lombardrisk.ignis.server.product.table.model.Field;
import com.lombardrisk.ignis.server.product.table.model.IntField;
import com.lombardrisk.ignis.server.product.table.model.StringField;
import com.lombardrisk.ignis.server.product.table.model.Table;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManager;
import javax.validation.ConstraintViolationException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.lombardrisk.ignis.server.product.fixture.ProductPopulated.decimalField;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SuppressWarnings("OptionalGetWithoutIsPresent")
@RunWith(SpringRunner.class)
@IntegrationTestConfig
public class TableRepositoryIT {

    @Autowired
    private ProductConfigRepository productConfigRepository;
    @Autowired
    private TableRepository tableRepository;
    @Autowired
    private ValidationRuleRepository ruleRepository;
    @Autowired
    private EntityManager entityManager;

    @Rule
    public final JUnitSoftAssertions soft = new JUnitSoftAssertions();

    @Test
    public void saveAndRetrieve() {
        Date createdTime = new Date();
        ProductConfig productConfig = productConfigRepository.save(ProductPopulated.productConfig().build());
        Table savedTable = tableRepository.saveAndFlush(
                Table.builder()
                        .productId(productConfig.getId())
                        .physicalTableName("MYTABLE")
                        .displayName("MYTABLE display name")
                        .createdTime(createdTime)
                        .createdBy("me")
                        .version(1)
                        .startDate(LocalDate.of(1997, 6, 5))
                        .endDate(LocalDate.of(1994, 3, 2))
                        .hasDatasets(true)
                        .build());

        Table table = tableRepository.findById(savedTable.getId()).get();

        soft.assertThat(table.getPhysicalTableName()).isEqualTo("MYTABLE");
        soft.assertThat(table.getCreatedTime()).isEqualTo(createdTime);
        soft.assertThat(table.getCreatedBy()).isEqualTo("me");
        soft.assertThat(table.getVersion()).isEqualTo(1);
        soft.assertThat(table.getHasDatasets()).isTrue();
        soft.assertThat(table.getStartDate()).isEqualTo(LocalDate.of(1997, 6, 5));
        soft.assertThat(table.getEndDate()).isEqualTo(LocalDate.of(1994, 3, 2));
    }

    @Test
    public void oneToMany_cascade_ValidationRules() {
        Set<ValidationRule> validationRules = new LinkedHashSet<>(asList(
                ProductPopulated.validationRule()
                        .ruleId("R1")
                        .build(),
                ProductPopulated.validationRule()
                        .ruleId("R2")
                        .build()));
        ProductConfig productConfig = productConfigRepository.save(ProductPopulated.productConfig().build());

        Table savedTable = tableRepository.saveAndFlush(
                ProductPopulated.table()
                        .productId(productConfig.getId())
                        .validationRules(validationRules)
                        .build());
        entityManager.clear();
        savedTable = tableRepository.findById(savedTable.getId()).get();

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
                ProductPopulated.validationRule().ruleId("rule id 1").build(),
                ProductPopulated.validationRule().ruleId("rule id 2").build());

        ProductConfig productConfig = productConfigRepository.save(ProductPopulated.productConfig().build());
        Table savedTable = tableRepository.saveAndFlush(
                ProductPopulated.table()
                        .productId(productConfig.getId())
                        .validationRules(validationRules)
                        .build());

        tableRepository.delete(savedTable);

        soft.assertThat(tableRepository.findAll()).isEmpty();
        soft.assertThat(ruleRepository.findAll()).isEmpty();
    }

    @Test
    public void findByDisplayName_tableWithFields_ReturnsTablesWithFieldsOrderedById() {
        ProductConfig productConfig = productConfigRepository.save(ProductPopulated.productConfig().build());

        Table table = ProductPopulated.table()
                .productId(productConfig.getId())
                .displayName("T_70")
                .build();

        DecimalField firstField = decimalField("A").build();
        table.setFields(new LinkedHashSet<>(asList(
                decimalField("B").build(), decimalField("C").build(), firstField)));

        tableRepository.saveAndFlush(table);

        entityManager
                .createNativeQuery("update DATASET_SCHEMA_FIELD set ID=-3 where ID = " + firstField.getId())
                .executeUpdate();
        clearEntityManager();

        table = tableRepository.findByDisplayName("T_70");

        soft.assertThat(table.getDisplayName()).isEqualTo("T_70");
        soft.assertThat(table.getFields())
                .extracting(Field::getName)
                .containsExactly("A", "B", "C");
    }

    @Test
    public void findByDisplayNameAndReferenceDate() {
        ProductConfig productConfig = productConfigRepository.save(ProductPopulated.productConfig().build());

        soft.assertThat(
                tableRepository.findByDisplayNameAndReferenceDate("missing", LocalDate.of(2000, 1, 1))
        ).isEmpty();

        tableRepository.saveAndFlush(
                ProductPopulated.table()
                        .productId(productConfig.getId())
                        .physicalTableName("CLOSED_PERIOD")
                        .displayName("closed period")
                        .version(31)
                        .startDate(LocalDate.of(2000, 1, 1))
                        .endDate(LocalDate.of(2000, 2, 1))
                        .build());

        clearEntityManager();

        soft.assertThat(
                tableRepository.findByDisplayNameAndReferenceDate("closed period", LocalDate.of(1999, 12, 31))
        ).isEmpty();
        soft.assertThat(
                tableRepository.findByDisplayNameAndReferenceDate("closed period", LocalDate.of(2000, 1, 1))
        ).isPresent();
        soft.assertThat(
                tableRepository.findByDisplayNameAndReferenceDate("closed period", LocalDate.of(2000, 1, 13))
        ).isPresent();
        soft.assertThat(
                tableRepository.findByDisplayNameAndReferenceDate("closed period", LocalDate.of(2000, 2, 1))
        ).isPresent();
        soft.assertThat(
                tableRepository.findByDisplayNameAndReferenceDate("closed period", LocalDate.of(2000, 2, 2))
        ).isEmpty();

        tableRepository.saveAndFlush(
                ProductPopulated.table()
                        .productId(productConfig.getId())
                        .physicalTableName("OPEN_PERIOD")
                        .displayName("open period")
                        .version(31)
                        .startDate(LocalDate.of(2000, 1, 1))
                        .endDate(null)
                        .build());

        clearEntityManager();

        soft.assertThat(
                tableRepository.findByDisplayNameAndReferenceDate("open period", LocalDate.of(1990, 12, 31))
        ).isEmpty();
        soft.assertThat(
                tableRepository.findByDisplayNameAndReferenceDate("open period", LocalDate.of(2000, 1, 1))
        ).isPresent();
        soft.assertThat(
                tableRepository.findByDisplayNameAndReferenceDate("open period", LocalDate.of(2200, 2, 1))
        ).isPresent();
    }

    @Test
    public void save_PreservesInsertionOrderOfFields() {
        ProductConfig productConfig = productConfigRepository.save(ProductPopulated.productConfig().build());

        Field strField = StringField.builder().name("string_field").build();
        Field intField = IntField.builder().name("int_field").build();
        Field boolField = BooleanField.builder().name("boolean_field").build();

        Table table = ProductPopulated.table()
                .productId(productConfig.getId())
                .physicalTableName("TABLE_WITH_ORDERED_FIELDS")
                .fields(new LinkedHashSet<>(asList(intField, strField, boolField)))
                .build();

        Table savedTable = tableRepository.saveAndFlush(table);

        soft.assertThat(savedTable.getPhysicalTableName()).isEqualTo("TABLE_WITH_ORDERED_FIELDS");
        soft.assertThat(savedTable.getFields())
                .extracting(Field::getName)
                .containsExactly("int_field", "string_field", "boolean_field");
    }

    @Test
    public void save_PhysicalTableNameContainsLowerCase_ThrowsConstraintViolation() {
        ProductConfig productConfig = productConfigRepository.save(ProductPopulated.productConfig().build());

        Field strField = StringField.builder().name("string_field").build();

        Table table = ProductPopulated.table()
                .productId(productConfig.getId())
                .physicalTableName("lower_case")
                .fields(new LinkedHashSet<>(Collections.singletonList(strField)))
                .build();

        assertThatThrownBy(() -> tableRepository.saveAndFlush(table))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    public void updatePeriodDates_SchemaId_UpdatesStartAndEndDates() {
        ProductConfig productConfig = productConfigRepository.save(ProductPopulated.productConfig().build());

        Table schema = tableRepository.saveAndFlush(
                ProductPopulated.table()
                        .productId(productConfig.getId())
                        .startDate(LocalDate.of(1970, 1, 1))
                        .endDate(LocalDate.of(1970, 1, 1))
                        .build());

        int updateCount = tableRepository.updatePeriodDates(2342L, LocalDate.of(2000, 12, 12), null);
        clearEntityManager();
        soft.assertThat(updateCount).isEqualTo(0);

        updateCount = tableRepository.updatePeriodDates(schema.getId(), LocalDate.of(2013, 12, 12), null);
        clearEntityManager();
        soft.assertThat(updateCount).isEqualTo(1);

        Table savedSchema = tableRepository.getOne(schema.getId());
        soft.assertThat(savedSchema.getStartDate())
                .isEqualTo(LocalDate.of(2013, 12, 12));
        soft.assertThat(savedSchema.getEndDate())
                .isNull();
    }

    private void clearEntityManager() {
        entityManager.flush();
        entityManager.clear();
    }
}
