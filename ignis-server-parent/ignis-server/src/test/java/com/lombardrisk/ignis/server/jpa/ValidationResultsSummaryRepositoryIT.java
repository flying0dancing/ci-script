package com.lombardrisk.ignis.server.jpa;

import com.lombardrisk.ignis.server.config.IntegrationTestConfig;
import com.lombardrisk.ignis.server.dataset.DatasetJpaRepository;
import com.lombardrisk.ignis.server.dataset.fixture.DatasetPopulated;
import com.lombardrisk.ignis.server.dataset.model.Dataset;
import com.lombardrisk.ignis.server.dataset.rule.ValidationResultsSummaryRepository;
import com.lombardrisk.ignis.server.dataset.rule.model.ValidationResultsSummary;
import com.lombardrisk.ignis.server.product.fixture.ProductPopulated;
import com.lombardrisk.ignis.server.product.productconfig.ProductConfigRepository;
import com.lombardrisk.ignis.server.product.productconfig.model.ProductConfig;
import com.lombardrisk.ignis.server.product.rule.ValidationRuleRepository;
import com.lombardrisk.ignis.server.product.rule.model.ValidationRule;
import com.lombardrisk.ignis.server.product.table.TableRepository;
import com.lombardrisk.ignis.server.product.table.model.Table;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("OptionalGetWithoutIsPresent")
@RunWith(SpringRunner.class)
@IntegrationTestConfig
public class ValidationResultsSummaryRepositoryIT {

    @Rule
    public JUnitSoftAssertions soft = new JUnitSoftAssertions();

    @Autowired
    private ValidationRuleRepository validationRuleRepository;

    @Autowired
    private DatasetJpaRepository datasetRepository;

    @Autowired
    private ValidationResultsSummaryRepository validationResultsSummaryRepository;

    @Autowired
    private TableRepository tableRepository;

    @Autowired
    private ProductConfigRepository productConfigRepository;

    @Test
    public void create_Retrieve() {
        //create
        ProductConfig productConfig = productConfigRepository.save(ProductPopulated.productConfig().build());

        Table savedTable = tableRepository.saveAndFlush(ProductPopulated.table()
                .productId(productConfig.getId())
                .validationRules(singleton(aRule()))
                .build());

        Dataset dataset = datasetRepository.saveAndFlush(
                DatasetPopulated.dataset()
                        .schema(savedTable)
                        .table("DATA_SET_SCHEMA")
                        .build());

        ValidationRule validationRule = savedTable.getValidationRules().iterator().next();

        ValidationResultsSummary validationRuleSummary = ValidationResultsSummary.builder()
                .dataset(dataset)
                .validationRule(validationRule)
                .numberOfFailures(1000L)
                .numberOfErrors(98627L)
                .build();

        ValidationResultsSummary savedSummary = validationResultsSummaryRepository
                .saveAndFlush(validationRuleSummary);

        //retrieve
        ValidationResultsSummary retrievedSummary = validationResultsSummaryRepository.findById(savedSummary.getId())
                .get();
        soft.assertThat(retrievedSummary).isNotNull();
        soft.assertThat(retrievedSummary.getDataset()).isEqualTo(dataset);
        soft.assertThat(retrievedSummary.getValidationRule()).isEqualTo(validationRule);
        soft.assertThat(retrievedSummary.getNumberOfFailures()).isEqualTo(1000L);
        soft.assertThat(retrievedSummary.getNumberOfErrors()).isEqualTo(98627L);
    }

    @Test
    public void findByDataset_Id() {
        //create
        ProductConfig productConfig = productConfigRepository.save(ProductPopulated.productConfig().build());

        Table savedTable = tableRepository.saveAndFlush(ProductPopulated.table()
                .productId(productConfig.getId())
                .validationRules(singleton(aRule()))
                .build());

        Dataset dataset = datasetRepository.saveAndFlush(
                DatasetPopulated.dataset()
                        .schema(savedTable)
                        .table("DATA_SET_SCHEMA")
                        .build());

        ValidationRule validationRule = savedTable.getValidationRules().iterator().next();

        ValidationResultsSummary validationRuleSummary = ValidationResultsSummary.builder()
                .dataset(dataset)
                .validationRule(validationRule)
                .numberOfFailures(1000L)
                .build();

        ValidationResultsSummary savedSummary = validationResultsSummaryRepository
                .saveAndFlush(validationRuleSummary);

        //retrieve
        List<ValidationResultsSummary> retrievedSummaries = validationResultsSummaryRepository
                .findByDatasetId(dataset.getId());
        assertThat(retrievedSummaries).containsExactly(savedSummary);

        List<ValidationResultsSummary> summariesForIncorrectDatasetId = validationResultsSummaryRepository
                .findByDatasetId(0L);
        assertThat(summariesForIncorrectDatasetId).isEmpty();
    }

    @Test
    public void deleteAllByDataset() {
        //create
        ProductConfig productConfig = productConfigRepository.save(ProductPopulated.productConfig().build());

        Table savedTable = tableRepository.saveAndFlush(ProductPopulated.table()
                .productId(productConfig.getId())
                .validationRules(singleton(aRule()))
                .build());

        Dataset dataset1 = datasetRepository.saveAndFlush(
                DatasetPopulated.dataset()
                        .name("Dataset1")
                        .schema(savedTable)
                        .table("DATA_SET_1")
                        .runKey(1L)
                        .build());
        Dataset dataset2 = datasetRepository.saveAndFlush(
                DatasetPopulated.dataset()
                        .name("Dataset1")
                        .schema(savedTable)
                        .table("DATA_SET_1")
                        .runKey(2L)
                        .build());

        ValidationRule validationRule = savedTable.getValidationRules().iterator().next();

        ValidationResultsSummary summaryForDataset1 = validationResultsSummaryRepository
                .saveAndFlush(ValidationResultsSummary.builder()
                        .dataset(dataset1)
                        .validationRule(validationRule)
                        .numberOfFailures(1000L)
                        .build());

        ValidationResultsSummary summaryForDataset2 = validationResultsSummaryRepository
                .saveAndFlush(ValidationResultsSummary.builder()
                        .dataset(dataset2)
                        .validationRule(validationRule)
                        .numberOfFailures(1000L)
                        .build());

        //create
        assertThat(validationResultsSummaryRepository.findById(summaryForDataset1.getId()))
                .isNotNull();
        assertThat(validationResultsSummaryRepository.findById(summaryForDataset2.getId()))
                .isNotNull();

        //delete
        validationResultsSummaryRepository.deleteAllByDataset(dataset1);

        assertThat(validationResultsSummaryRepository.findById(summaryForDataset1.getId()))
                .isEmpty();

        assertThat(validationResultsSummaryRepository.findById(summaryForDataset2.getId()))
                .isNotEmpty();

        assertThat(validationRuleRepository.findById(validationRule.getId()))
                .isNotEmpty();
    }

    @Test
    public void deleteAllByValidationRule() {
        ProductConfig productConfig = productConfigRepository.save(ProductPopulated.productConfig().build());

        Table savedTable = tableRepository.saveAndFlush(ProductPopulated.table()
                .productId(productConfig.getId())
                .validationRules(newHashSet(
                        ValidationRule.builder()
                                .name("validationRule1")
                                .ruleId("1")
                                .description("description")
                                .expression("EXPRESSION")
                                .build(),
                        ValidationRule.builder()
                                .name("validationRule2")
                                .ruleId("2")
                                .description("description")
                                .expression("EXPRESSION")
                                .build()))
                .build());

        Dataset dataset = datasetRepository.saveAndFlush(
                DatasetPopulated.dataset()
                        .name("Dataset1")
                        .schema(savedTable)
                        .table("DATA_SET_1")
                        .build());

        List<ValidationRule> validationRules = newArrayList(savedTable.getValidationRules());

        ValidationRule validationRule1 = validationRules.get(0);
        ValidationRule validationRule2 = validationRules.get(1);

        ValidationResultsSummary validationRule1Summary = validationResultsSummaryRepository.saveAndFlush(
                ValidationResultsSummary.builder()
                        .dataset(dataset)
                        .validationRule(validationRule1)
                        .build());

        ValidationResultsSummary validationRule2Summary = validationResultsSummaryRepository.saveAndFlush(
                ValidationResultsSummary.builder()
                        .dataset(dataset)
                        .validationRule(validationRule2)
                        .build());

        validationResultsSummaryRepository.saveAll(newArrayList(validationRule1Summary, validationRule2Summary));

        validationResultsSummaryRepository.deleteAllByValidationRule(validationRule1);

        assertThat(validationResultsSummaryRepository.findById(validationRule1.getId()))
                .isEmpty();

        assertThat(validationResultsSummaryRepository.findById(validationRule2Summary.getId()))
                .isPresent();
    }

    @Test
    public void update_FieldUpdated() {
        ProductConfig productConfig = productConfigRepository.save(ProductPopulated.productConfig().build());

        Table savedTable = tableRepository.saveAndFlush(ProductPopulated.table()
                .productId(productConfig.getId())
                .validationRules(singleton(aRule()))
                .build());

        Dataset dataset = datasetRepository.saveAndFlush(
                DatasetPopulated.dataset()
                        .schema(savedTable)
                        .table("DATA_SET_SCHEMA")
                        .build());

        ValidationRule savedRule = savedTable.getValidationRules().iterator().next();

        ValidationResultsSummary validationRuleSummary = ValidationResultsSummary.builder()
                .dataset(dataset)
                .validationRule(savedRule)
                .numberOfFailures(1000L)
                .build();

        validationResultsSummaryRepository.saveAndFlush(validationRuleSummary);
        ValidationResultsSummary retrievedSummary = validationResultsSummaryRepository
                .findById(validationRuleSummary.getId())
                .get();

        //update
        retrievedSummary.setNumberOfFailures(3000L);
        validationResultsSummaryRepository.saveAndFlush(retrievedSummary);

        ValidationResultsSummary updatedSummary = validationResultsSummaryRepository
                .findById(retrievedSummary.getId())
                .get();
        assertThat(updatedSummary.getNumberOfFailures()).isEqualTo(3000L);
    }

    @Test
    public void delete_doesNotDeleteAssociatedEntities() {
        ProductConfig productConfig = productConfigRepository.save(ProductPopulated.productConfig().build());

        Table savedTable = tableRepository.saveAndFlush(ProductPopulated.table()
                .productId(productConfig.getId())
                .validationRules(singleton(aRule()))
                .build());

        Dataset dataset = datasetRepository.saveAndFlush(
                DatasetPopulated.dataset()
                        .schema(savedTable)
                        .table("DATA_SET_SCHEMA")
                        .build());

        ValidationRule savedRule = savedTable.getValidationRules().iterator().next();

        ValidationResultsSummary validationRuleSummary = ValidationResultsSummary.builder()
                .dataset(dataset)
                .validationRule(savedRule)
                .numberOfFailures(1000L)
                .build();

        validationResultsSummaryRepository.saveAndFlush(validationRuleSummary);
        ValidationResultsSummary retrievedSummary = validationResultsSummaryRepository
                .findById(validationRuleSummary.getId())
                .get();

        //delete
        validationResultsSummaryRepository.delete(retrievedSummary);
        assertThat(validationResultsSummaryRepository.findById(retrievedSummary.getId()))
                .isEmpty();

        assertThat(validationRuleRepository.findById(savedRule.getId()))
                .isNotNull();

        assertThat(datasetRepository.findById(dataset.getId()))
                .isNotNull();
    }

    private ValidationRule aRule() {
        return ValidationRule.builder()
                .name("validationRule1")
                .ruleId("1")
                .description("description")
                .expression("EXPRESSION")
                .build();
    }
}
