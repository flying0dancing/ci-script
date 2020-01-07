package com.lombardrisk.ignis.server.jpa;

import com.lombardrisk.ignis.common.fixtures.PopulatedDates;
import com.lombardrisk.ignis.server.config.IntegrationTestConfig;
import com.lombardrisk.ignis.server.product.fixture.ProductPopulated;
import com.lombardrisk.ignis.server.product.pipeline.PipelineJpaRepository;
import com.lombardrisk.ignis.server.product.pipeline.PipelineService;
import com.lombardrisk.ignis.server.product.pipeline.model.Pipeline;
import com.lombardrisk.ignis.server.product.productconfig.ProductConfigRepository;
import com.lombardrisk.ignis.server.product.productconfig.model.ImportStatus;
import com.lombardrisk.ignis.server.product.productconfig.model.ProductConfig;
import com.lombardrisk.ignis.server.product.productconfig.model.ProductSchemaDetailsOnly;
import com.lombardrisk.ignis.server.product.productconfig.model.ProductSchemaPipeline;
import com.lombardrisk.ignis.server.product.table.TableRepository;
import com.lombardrisk.ignis.server.product.table.model.Table;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManager;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

@RunWith(SpringRunner.class)
@IntegrationTestConfig
public class ProductConfigRepositoryIT {

    @Autowired
    private ProductConfigRepository productConfigRepository;

    @Autowired
    private TableRepository tableRepository;

    @Autowired
    private PipelineJpaRepository pipelineJpaRepository;

    @Autowired
    private EntityManager entityManager;

    private PipelineService pipelineService;

    @Rule
    public JUnitSoftAssertions soft = new JUnitSoftAssertions();

    @Before
    public void setUp() {
        pipelineService = new PipelineService(pipelineJpaRepository);
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    public void removeTable_DoesNotDeleteProduct() {
        ProductConfig savedProductConfig = productConfigRepository.saveAndFlush(ProductPopulated.productConfig()
                .name("my first product config")
                .version("v1.0")
                .importStatus(ImportStatus.SUCCESS)
                .tables(new HashSet<>())
                .pipelines(new HashSet<>())
                .build());

        List<Table> savedTables = tableRepository.saveAll(asList(
                ProductPopulated.table()
                        .productId(savedProductConfig.getId())
                        .physicalTableName("TABLE_1")
                        .displayName("TABLE_1")
                        .build(),
                ProductPopulated.table()
                        .productId(savedProductConfig.getId())
                        .physicalTableName("TABLE_2")
                        .displayName("TABLE_2")
                        .build()));

        flushAndClear();

        tableRepository.deleteAll(savedTables);
        flushAndClear();

        assertThat(productConfigRepository.findById(savedProductConfig.getId()).get())
                .isEqualToIgnoringGivenFields(savedProductConfig, "createdTime");
    }

    @Test
    public void crudOperations() {
        ProductConfig savedProductConfig = productConfigRepository.saveAndFlush(ProductPopulated.productConfig()
                .name("my first product config")
                .version("v1.0")
                .importStatus(ImportStatus.SUCCESS)
                .tables(new HashSet<>())
                .build());

        List<Table> savedTables = tableRepository.saveAll(asList(
                ProductPopulated.table()
                        .productId(savedProductConfig.getId())
                        .physicalTableName("TABLE_1")
                        .displayName("TABLE_1")
                        .build(),
                ProductPopulated.table()
                        .productId(savedProductConfig.getId())
                        .physicalTableName("TABLE_2")
                        .displayName("TABLE_2")
                        .build()));

        List<Long> savedTableIds = savedTables.stream().map(Table::getId).collect(toList());

        soft.assertThat(savedProductConfig.getId())
                .isNotNull();
        soft.assertThat(savedProductConfig.getName())
                .isEqualTo("my first product config");
        soft.assertThat(savedProductConfig.getVersion())
                .isEqualTo("v1.0");
        soft.assertThat(savedProductConfig.getImportStatus())
                .isEqualTo(ImportStatus.SUCCESS);

        entityManager.flush();
        entityManager.clear();

        soft.assertThat(
                productConfigRepository.findById(savedProductConfig.getId()).get()
                        .getTables())
                .extracting(Table::getId)
                .containsOnlyElementsOf(savedTableIds);

        savedProductConfig.setName("my first product config updated");

        ProductConfig updatedProductConfig = productConfigRepository.saveAndFlush(savedProductConfig);

        soft.assertThat(updatedProductConfig.getName())
                .isEqualTo("my first product config updated");

        productConfigRepository.delete(updatedProductConfig);

        Optional<ProductConfig> deletedProduct = productConfigRepository.findById(updatedProductConfig.getId());

        soft.assertThat(deletedProduct).isEmpty();
    }

    @Test
    public void createRetrieve_PipelinesRetrievedByJoin() {
        ProductConfig savedProductConfig = productConfigRepository.saveAndFlush(ProductPopulated.productConfig()
                .tables(new HashSet<>())
                .build());

        Table createdTable = tableRepository.save(ProductPopulated.table()
                .productId(savedProductConfig.getId())
                .displayName("THE ONE")
                .physicalTableName("ONE")
                .build());

        Pipeline createdPipeline = pipelineService.savePipeline(ProductPopulated.pipeline()
                .productId(savedProductConfig.getId())
                .steps(singleton(ProductPopulated.mapPipelineStep()
                        .schemaInId(createdTable.getId())
                        .schemaOutId(createdTable.getId())
                        .selects(emptySet())
                        .build()))
                .build());

        entityManager.flush();
        entityManager.clear();

        ProductConfig retrievedProduct = productConfigRepository.findById(savedProductConfig.getId())
                .get();

        assertThat(retrievedProduct.getPipelines())
                .hasSize(1);
        Pipeline productPipeline = retrievedProduct.getPipelines().iterator().next();
        assertThat(productPipeline.getId())
                .isEqualTo(createdPipeline.getId());
        assertThat(productPipeline.getName())
                .isEqualTo(createdPipeline.getName());
    }

    @Test
    public void updateImportStatus_UpdatesImportStatus() {
        ProductConfig savedProductConfig = productConfigRepository.saveAndFlush(ProductPopulated.productConfig()
                .importStatus(ImportStatus.SUCCESS)
                .build());

        productConfigRepository.updateImportStatus(savedProductConfig.getId(), ImportStatus.IN_PROGRESS);

        flushAndClear();

        ProductConfig productConfig = productConfigRepository.getOne(savedProductConfig.getId());

        assertThat(productConfig.getImportStatus()).isEqualTo(ImportStatus.IN_PROGRESS);
    }

    @Test
    public void deleteTable_DoesNotDeleteProduct() {
        ProductConfig savedProductConfig = productConfigRepository.saveAndFlush(ProductPopulated.productConfig()
                .name("my first product config")
                .version("v1.0")
                .tables(new HashSet<>())
                .build());
        List<Table> savedTables = tableRepository.saveAll(asList(
                ProductPopulated.table()
                        .productId(savedProductConfig.getId())
                        .physicalTableName("TABLE_1")
                        .displayName("TABLE_1")
                        .build(),
                ProductPopulated.table()
                        .productId(savedProductConfig.getId())
                        .physicalTableName("TABLE_2")
                        .displayName("TABLE_2")
                        .build()));

        entityManager.flush();
        entityManager.clear();

        ProductConfig retrievedProduct = productConfigRepository.findById(savedProductConfig.getId()).get();

        tableRepository.delete(retrievedProduct.getTables().iterator().next());

        soft.assertThat(
                productConfigRepository.findById(savedProductConfig.getId()))
                .isPresent();
    }

    @Test
    public void findFirstByNameOrderByCreatedTimeDesc() {
        soft.assertThat(productConfigRepository.findFirstByNameOrderByCreatedTimeDesc("my product"))
                .isEmpty();

        ProductConfig olderProduct = ProductPopulated.productConfig()
                .name("my product")
                .version("1.0.0")
                .createdTime(PopulatedDates.toDate("1999-12-20"))
                .build();

        ProductConfig latestProduct = ProductPopulated.productConfig()
                .name("my product")
                .version("2.0.0")
                .createdTime(PopulatedDates.toDate("2000-12-20"))
                .build();

        ProductConfig unrelatedLaterProduct = ProductPopulated.productConfig()
                .name("different product")
                .version("1.0.0")
                .createdTime(PopulatedDates.toDate("2010-12-20"))
                .build();

        productConfigRepository.saveAndFlush(olderProduct);
        productConfigRepository.saveAndFlush(latestProduct);
        productConfigRepository.saveAndFlush(unrelatedLaterProduct);

        soft.assertThat(productConfigRepository.findFirstByNameOrderByCreatedTimeDesc("my product"))
                .get()
                .extracting(ProductConfig::getCreatedTime)
                .isEqualTo(PopulatedDates.toDate("2000-12-20"));
    }

    @Test
    public void existsByNameAndVersion() {
        soft.assertThat(productConfigRepository.existsByNameAndVersion("my product", "v1.0.0"))
                .isFalse();

        ProductConfig newProductConfig = ProductPopulated.productConfig()
                .name("my product")
                .version("v1.0.0")
                .build();

        productConfigRepository.saveAndFlush(newProductConfig);

        soft.assertThat(productConfigRepository.existsByNameAndVersion("my product", "v1.0.0"))
                .isTrue();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void findSchemasByPhysicalNameNotInProduct() {
        soft.assertThat(
                productConfigRepository.findSchemasByPhysicalNameNotInProduct(
                        "unknown", 2, null))
                .isEmpty();

        ProductConfig productV1 = productConfigRepository.saveAndFlush(
                ProductPopulated.productConfig().name("P I").version("1.0").build());

        tableRepository.saveAndFlush(ProductPopulated.table()
                .physicalTableName("A").version(1)
                .displayName("A")
                .productId(productV1.getId())
                .build());
        tableRepository.saveAndFlush(ProductPopulated.table()
                .physicalTableName("A").version(2)
                .displayName("A")
                .productId(productV1.getId())
                .build());

        ProductConfig productV2 = productConfigRepository.saveAndFlush(
                ProductPopulated.productConfig().name("P I").version("2.0").build());

        tableRepository.save(ProductPopulated.table()
                .physicalTableName("B").version(2)
                .displayName("B")
                .productId(productV2.getId())
                .build());

        soft.assertThat(
                productConfigRepository.findSchemasByPhysicalNameNotInProduct(
                        "B", 2, "P I"))
                .isEmpty();

        soft.assertThat(
                productConfigRepository.findSchemasByPhysicalNameNotInProduct(
                        "A", 2, "P II"))
                .extracting(
                        ProductSchemaDetailsOnly::getSchemaPhysicalName,
                        ProductSchemaDetailsOnly::getSchemaVersion,
                        ProductSchemaDetailsOnly::getProductName,
                        ProductSchemaDetailsOnly::getProductVersion)
                .containsSequence(tuple("A", 2, "P I", "1.0"));
    }

    @Test
    public void updateImportRequestId() {
        ProductConfig productConfig =
                productConfigRepository.saveAndFlush(
                        ProductPopulated.productConfig()
                                .tables(newHashSet())
                                .importRequestId(-1231L)
                                .build());

        productConfigRepository.updateImportRequestId(productConfig.getId(), 1111L);

        flushAndClear();

        soft.assertThat(
                productConfigRepository.getOne(productConfig.getId())
                        .getImportRequestId())
                .isEqualTo(1111L);
    }

    @Test
    public void findByNameAndTablesPhysicalTableNames() {
        soft.assertThat(
                productConfigRepository.findAllPreviousSchemas(
                        "nonExistentProduct", newHashSet("A")))
                .isEmpty();

        ProductConfig productConfig = productConfigRepository.saveAndFlush(
                ProductPopulated.productConfig("P1").tables(newHashSet()).build());

        Table schema = tableRepository.saveAndFlush(ProductPopulated.table()
                .productId(productConfig.getId())
                .build());

        soft.assertThat(
                productConfigRepository.findAllPreviousSchemas(
                        "P1", newHashSet("nonExistentSchema")))
                .isEmpty();

        ProductConfig productConfig2 = productConfigRepository.saveAndFlush(
                ProductPopulated.productConfig("P2").tables(newHashSet()).build());

        List<Table> schemas = tableRepository.saveAll(newHashSet(
                ProductPopulated.table("FITLERED").version(1).productId(productConfig2.getId()).build(),
                ProductPopulated.table("A").version(1).productId(productConfig2.getId()).build(),
                ProductPopulated.table("A").version(2).productId(productConfig2.getId()).build(),
                ProductPopulated.table("B").version(3).productId(productConfig2.getId()).build()));

        flushAndClear();

        soft.assertThat(
                productConfigRepository.findAllPreviousSchemas(
                        "P2", newHashSet("A", "B")))
                .extracting(Table::getVersionedName)
                .containsExactlyInAnyOrder("A v.1", "A v.2", "B v.3");
    }

    @Test
    public void findAllWithoutFieldsRulesAndSteps_returnsProductWithoutFieldsRulesAndSteps() {
        ProductConfig savedProductConfig = productConfigRepository.saveAndFlush(ProductPopulated.productConfig()
                .tables(new HashSet<>())
                .build());

        Table createdTable = tableRepository.save(ProductPopulated.table()
                .productId(savedProductConfig.getId())
                .displayName("THE ONE")
                .physicalTableName("ONE")
                .build());

        Pipeline createdPipeline = pipelineService.savePipeline(ProductPopulated.pipeline()
                .productId(savedProductConfig.getId())
                .steps(singleton(ProductPopulated.mapPipelineStep()
                        .schemaInId(createdTable.getId())
                        .schemaOutId(createdTable.getId())
                        .selects(emptySet())
                        .build()))
                .build());

        entityManager.flush();
        entityManager.clear();

        List<ProductSchemaPipeline> retrievedProductList = productConfigRepository.findAllWithoutFieldsRulesAndSteps();

        soft.assertThat(retrievedProductList)
                .hasSize(1);

        ProductSchemaPipeline retrievedProduct = retrievedProductList.get(0);

        soft.assertThat(retrievedProduct.getId()).isEqualTo(savedProductConfig.getId());
        soft.assertThat(retrievedProduct.getName()).isEqualTo(savedProductConfig.getName());
        soft.assertThat(retrievedProduct.getVersion()).isEqualTo(savedProductConfig.getVersion());
        soft.assertThat(retrievedProduct.getCreatedTime()).isInSameDayAs(savedProductConfig.getCreatedTime());
        soft.assertThat(retrievedProduct.getImportStatus()).isEqualTo(savedProductConfig.getImportStatus().toString());

        soft.assertThat(retrievedProduct.getSchemaId()).isEqualTo(createdTable.getId());
        soft.assertThat(retrievedProduct.getSchemaDisplayName()).isEqualTo(createdTable.getDisplayName());
        soft.assertThat(retrievedProduct.getSchemaPhysicalTableName()).isEqualTo(createdTable.getPhysicalTableName());
        soft.assertThat(retrievedProduct.getSchemaVersion()).isEqualTo(createdTable.getVersion());
        soft.assertThat(retrievedProduct.getSchemaCreatedTime()).isInSameDayAs(createdTable.getCreatedTime());
        soft.assertThat(retrievedProduct.getSchemaStartDate()).isEqualTo(createdTable.getStartDate());
        soft.assertThat(retrievedProduct.getSchemaEndDate()).isEqualTo(createdTable.getEndDate());
        soft.assertThat(retrievedProduct.getSchemaCreatedBy()).isEqualTo(createdTable.getCreatedBy());
        soft.assertThat(retrievedProduct.getSchemaHasDatasets()).isEqualTo(createdTable.getHasDatasets());

        soft.assertThat(retrievedProduct.getPipelineId()).isEqualTo(createdPipeline.getId());
        soft.assertThat(retrievedProduct.getPipelineName()).isEqualTo(createdPipeline.getName());
    }
}
