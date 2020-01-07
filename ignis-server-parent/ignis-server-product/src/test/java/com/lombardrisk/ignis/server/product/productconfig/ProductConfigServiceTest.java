package com.lombardrisk.ignis.server.product.productconfig;

import com.google.common.collect.ImmutableSet;
import com.lombardrisk.ignis.api.rule.ValidationRuleSeverity;
import com.lombardrisk.ignis.api.rule.ValidationRuleType;
import com.lombardrisk.ignis.client.external.pipeline.view.PipelineStepView;
import com.lombardrisk.ignis.client.external.pipeline.view.PipelineView;
import com.lombardrisk.ignis.client.external.productconfig.view.ProductConfigView;
import com.lombardrisk.ignis.client.external.productconfig.view.SchemaView;
import com.lombardrisk.ignis.common.assertions.VavrAssert;
import com.lombardrisk.ignis.common.fixtures.PopulatedDates;
import com.lombardrisk.ignis.common.time.TimeSource;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.server.product.fixture.ProductPopulated;
import com.lombardrisk.ignis.server.product.pipeline.model.Pipeline;
import com.lombardrisk.ignis.server.product.productconfig.model.ImportStatus;
import com.lombardrisk.ignis.server.product.productconfig.model.ProductConfig;
import com.lombardrisk.ignis.server.product.rule.model.ValidationRule;
import com.lombardrisk.ignis.server.product.table.TableRepository;
import com.lombardrisk.ignis.server.product.table.TableService;
import com.lombardrisk.ignis.server.product.table.model.Table;
import io.vavr.control.Either;
import io.vavr.control.Option;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static com.lombardrisk.ignis.common.fixtures.PopulatedDates.toDate;
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProductConfigServiceTest {

    @Rule
    public JUnitSoftAssertions soft = new JUnitSoftAssertions();
    @Mock
    private TableService tableService;
    @Mock
    private ProductConfigRepository productConfigRepository;
    @Mock
    private TableRepository tableRepository;
    @Mock
    private Validator validator;
    @Mock
    private TimeSource timeSource;

    @InjectMocks
    private ProductConfigService productConfigService;

    @Before
    public void setUp() {
        when(productConfigRepository.save(any()))
                .then(invocationOnMock -> invocationOnMock.getArguments()[0]);

        when(validator.validate(any()))
                .thenReturn(emptySet());
    }

    @Test
    public void entityName() {
        assertThat(productConfigService.entityName()).isEqualTo(ProductConfig.class.getSimpleName());
    }

    @Test
    public void findById_DelegatesToRepository() {
        productConfigService.findById(121L);
        verify(productConfigRepository).findById(121L);
    }

    @Test
    public void findById_ProductFound_ReturnsProduct() {
        ProductConfig productConfig = ProductPopulated.productConfig().build();
        when(productConfigRepository.findById(any()))
                .thenReturn(Optional.of(productConfig));

        Option<ProductConfig> foundProduct = productConfigService.findById(121L);

        assertThat(foundProduct.get()).isEqualTo(productConfig);
    }

    @Test
    public void findById_ProductNotFound_ReturnsOptionNone() {
        when(productConfigRepository.findById(any()))
                .thenReturn(Optional.empty());

        Option<ProductConfig> foundProduct = productConfigService.findById(121L);

        assertThat(foundProduct.isEmpty()).isTrue();
    }

    @Test
    public void findByName_ProductFound_ReturnsProduct() {
        when(productConfigRepository.findByName(anyString()))
                .thenReturn(Optional.of(ProductPopulated.productConfig().build()));

        VavrAssert.assertValid(productConfigService.findByName("test"));
    }

    @Test
    public void findByName_ProductNotFound_ReturnsError() {
        when(productConfigRepository.findByName(anyString()))
                .thenReturn(Optional.empty());

        VavrAssert.assertFailed(productConfigService.findByName("test"))
                .withFailure(CRUDFailure.cannotFind("Product").with("Name", "test").asFailure());
    }

    @Test
    public void delete_DelegatesToRepository() {
        ProductConfig productConfig = ProductPopulated.productConfig().build();

        productConfigService.delete(productConfig);

        verify(productConfigRepository).delete(productConfig);
    }

    @Test
    public void delete_ReturnsProductConfig() {
        ProductConfig productConfig = ProductPopulated.productConfig().build();

        ProductConfig deleted = productConfigService.delete(productConfig);

        assertThat(deleted).isSameAs(productConfig);
    }

    @Test
    public void findByIds_DelegatesToRepository() {
        productConfigService.findAllByIds(asList(1L, 2L, 3L));

        verify(productConfigRepository).findAllById(asList(1L, 2L, 3L));
    }

    @Test
    public void findByIds_ReturnsResultFromRepository() {
        ProductConfig productConfig = ProductPopulated.productConfig().build();

        when(productConfigRepository.findAllById(asList(1L, 2L, 3L)))
                .thenReturn(singletonList(productConfig));

        List<ProductConfig> allByIds = productConfigService.findAllByIds(asList(1L, 2L, 3L));

        assertThat(allByIds)
                .containsExactly(productConfig);
    }

    @Test
    public void repository_returnsProductConfigRepository() {
        assertThat(productConfigService.repository())
                .isEqualTo(productConfigRepository);
    }

    @Test
    public void saveProductConfig_WithTables_CallsTableService() {
        ImmutableSet<Table> tables = ImmutableSet.of(
                ProductPopulated.table().build(),
                ProductPopulated.table().build());

        ProductConfig productConfig = ProductPopulated.productConfig()
                .tables(tables)
                .build();

        productConfigService.saveProductConfig(productConfig);

        verify(tableService).saveTables(tables);
    }

    @Test
    public void saveProductConfig_WithProductConfig_SavesProductConfig() {
        when(timeSource.nowAsDate())
                .thenReturn(toDate("2100-10-10"));

        ProductConfig productConfig = ProductPopulated.productConfig()
                .name("name")
                .version("1.1.1.1")
                .importStatus(ImportStatus.IN_PROGRESS)
                .createdTime(toDate("2000-01-01"))
                .importRequestId(222L)
                .tables(singleton(ProductPopulated.table().build()))
                .build();

        productConfigService.saveProductConfig(productConfig);

        ArgumentCaptor<ProductConfig> productCaptor = ArgumentCaptor.forClass(ProductConfig.class);

        verify(productConfigRepository).save(productCaptor.capture());
        assertThat(productCaptor.getValue())
                .extracting(
                        ProductConfig::getName,
                        ProductConfig::getVersion,
                        ProductConfig::getImportStatus,
                        ProductConfig::getImportRequestId,
                        ProductConfig::getCreatedTime)
                .contains("name", "1.1.1.1", null, null, toDate("2100-10-10"));
    }

    @Test
    public void updateImportStatus_CallsProductConfigRepository() {
        productConfigService.updateImportStatus(123L, ImportStatus.ERROR);

        verify(productConfigRepository).updateImportStatus(123L, ImportStatus.ERROR);
    }

    @Test
    public void saveProductConfig_WithProductConfig_SavesProductConfigWithTables() {
        ImmutableSet<Table> tables = ImmutableSet.of(
                ProductPopulated.table().physicalTableName("table1").build(),
                ProductPopulated.table().physicalTableName("table2").build());

        ProductConfig productConfig = ProductPopulated.productConfig()
                .tables(tables)
                .build();

        Table savedTable1 = ProductPopulated.table().id(1L).build();
        Table savedTable2 = ProductPopulated.table().id(2L).build();

        when(tableService.saveTables(tables))
                .thenReturn(asList(savedTable1, savedTable2));

        productConfigService.saveProductConfig(productConfig);

        ArgumentCaptor<ProductConfig> captor = ArgumentCaptor.forClass(ProductConfig.class);

        verify(productConfigRepository).save(captor.capture());
        assertThat(captor.getValue().getTables())
                .contains(savedTable1, savedTable2);
    }

    @Test
    public void saveProductConfig_ReturnsSavedProductConfigFromRepository() {
        ProductConfig productConfig = ProductPopulated.productConfig().build();
        ProductConfig savedProductConfig = ProductPopulated.productConfig().id(1L).build();

        when(productConfigRepository.save(any()))
                .thenReturn(savedProductConfig);

        Either<List<ErrorResponse>, ProductConfig> importedProductConfig =
                productConfigService.saveProductConfig(productConfig);

        assertThat(importedProductConfig.get())
                .isSameAs(savedProductConfig);
    }

    @Test
    public void saveProductConfig_TableViolations_ReturnsError() {
        ProductConfig invalidProductConfig = ProductPopulated.productConfig().build();

        Set<ConstraintViolation<ProductConfig>> violations = newHashSet(
                new DummyConstraintViolation(invalidProductConfig, "Product config is not valid"));

        when(validator.validate(any(ProductConfig.class)))
                .thenReturn(violations);

        Either<List<ErrorResponse>, ProductConfig> productConfig =
                productConfigService.saveProductConfig(invalidProductConfig);

        assertThat(productConfig.isLeft()).isTrue();
        assertThat(productConfig.getLeft().get(0).getErrorMessage()).contains("Product config is not valid");
    }

    @Test
    public void saveProductConfig_CallsValidator() {
        ProductConfig productConfig = ProductPopulated.productConfig().build();

        productConfigService.saveProductConfig(productConfig);

        verify(validator).validate(productConfig);
        verifyNoMoreInteractions(validator);
    }

    @Test
    public void saveProductConfig_ProductWithName_FindsProductConfigByName() {
        ProductConfig newProductConfig = ProductPopulated.productConfig()
                .name("PRODUCT_NAME")
                .build();

        when(productConfigRepository.findByName(any()))
                .thenReturn(Optional.empty());

        productConfigService.saveProductConfig(newProductConfig);

        verify(productConfigRepository).findByName("PRODUCT_NAME");
    }

    @Test
    public void saveProductConfig_ProductWithSameNameExists_ReturnsError() {
        ProductConfig newProductConfig = ProductPopulated.productConfig()
                .name("PRODUCT_NAME")
                .build();

        ProductConfig existingProductConfig = ProductPopulated.productConfig()
                .name("PRODUCT_NAME")
                .build();

        when(productConfigRepository.findByName(any()))
                .thenReturn(Optional.of(existingProductConfig));

        Either<List<ErrorResponse>, ProductConfig> productConfig =
                productConfigService.saveProductConfig(newProductConfig);

        assertThat(productConfig.isLeft()).isTrue();
        assertThat(productConfig.getLeft().get(0).getErrorMessage()).isEqualTo(
                "Product with name [PRODUCT_NAME] already exists");
    }

    @Test
    public void saveProductConfig_ProductWithSameNameExists_DoesNotSaveProductConfig() {
        ProductConfig newProductConfig = ProductPopulated.productConfig()
                .name("PRODUCT_NAME")
                .build();

        ProductConfig existingProductConfig = ProductPopulated.productConfig()
                .name("PRODUCT_NAME")
                .build();

        when(productConfigRepository.findByName(any()))
                .thenReturn(Optional.of(existingProductConfig));

        productConfigService.saveProductConfig(newProductConfig);

        verify(productConfigRepository, never()).save(any());
    }

    @Test
    public void saveProductConfig_GetsAllTableNames() {
        ProductConfig newProductConfig = ProductPopulated.productConfig()
                .tables(ImmutableSet.of(
                        ProductPopulated.table().physicalTableName("TABLE_1").build(),
                        ProductPopulated.table().physicalTableName("TABLE_2").build(),
                        ProductPopulated.table().physicalTableName("TABLE_3").build()))
                .build();

        when(tableService.getAllTableNames())
                .thenReturn(asList("TABLE_2", "TABLE_3"));

        productConfigService.saveProductConfig(newProductConfig);

        verify(tableService).getAllTableNames();
    }

    @Test
    public void saveProductConfig_TableWithSameNameExists_ReturnsError() {
        ProductConfig newProductConfig = ProductPopulated.productConfig()
                .tables(ImmutableSet.of(
                        ProductPopulated.table().physicalTableName("TABLE_1").build(),
                        ProductPopulated.table().physicalTableName("TABLE_2").build(),
                        ProductPopulated.table().physicalTableName("TABLE_3").build()))
                .build();

        when(tableService.getAllTableNames())
                .thenReturn(asList("TABLE_2", "TABLE_3"));

        Either<List<ErrorResponse>, ProductConfig> productConfig =
                productConfigService.saveProductConfig(newProductConfig);

        assertThat(productConfig.isLeft())
                .isTrue();
        assertThat(productConfig.getLeft())
                .hasSize(2);
        assertThat(productConfig.getLeft())
                .extracting(ErrorResponse::getErrorMessage)
                .contains(
                        "Table with name [TABLE_2] already exists",
                        "Table with name [TABLE_3] already exists"
                );
    }

    @Test
    public void saveProductConfig_TableWithSameNameExists_DoesNotSaveTables() {
        ProductConfig newProductConfig = ProductPopulated.productConfig()
                .tables(ImmutableSet.of(
                        ProductPopulated.table().physicalTableName("TABLE_1").build(),
                        ProductPopulated.table().physicalTableName("TABLE_2").build(),
                        ProductPopulated.table().physicalTableName("TABLE_3").build()))
                .build();

        when(tableService.getAllTableNames())
                .thenReturn(asList("TABLE_2", "TABLE_3"));

        productConfigService.saveProductConfig(newProductConfig);

        verifyZeroInteractions(tableRepository);
    }

    @Test
    public void saveProductConfig_TableWithSameNameExists_DoesNotSaveProductConfig() {
        ProductConfig newProductConfig = ProductPopulated.productConfig()
                .tables(ImmutableSet.of(
                        ProductPopulated.table().physicalTableName("TABLE_1").build(),
                        ProductPopulated.table().physicalTableName("TABLE_2").build(),
                        ProductPopulated.table().physicalTableName("TABLE_3").build()))
                .build();

        when(tableService.getAllTableNames())
                .thenReturn(asList("TABLE_2", "TABLE_3"));

        productConfigService.saveProductConfig(newProductConfig);

        verify(productConfigRepository, never()).save(any());
    }

    @Test
    public void findView_ViewExists_ConvertsProductProperties() {
        when(productConfigRepository.findById(any()))
                .thenReturn(Optional.of(ProductPopulated.productConfig()
                        .id(88L)
                        .name("Proddy")
                        .version("G")
                        .importStatus(ImportStatus.IN_PROGRESS)
                        .createdTime(new Date())
                        .build()));

        ProductConfigView productConfigView =
                VavrAssert.assertValid(productConfigService.findView(102L))
                        .getResult();

        soft.assertThat(productConfigView.getId())
                .isEqualTo(88L);
        soft.assertThat(productConfigView.getName())
                .isEqualTo("Proddy");
        soft.assertThat(productConfigView.getVersion())
                .isEqualTo("G");
        soft.assertThat(productConfigView.getImportStatus())
                .isEqualTo("IN_PROGRESS");
    }

    @Test
    public void findView_ViewExists_ConvertsSchemas() {
        when(productConfigRepository.findById(any()))
                .thenReturn(Optional.of(ProductPopulated.productConfig()
                        .tables(newHashSet(
                                ProductPopulated.table()
                                        .id(929L)
                                        .displayName("displayName")
                                        .physicalTableName("PHYZ")
                                        .version(2)
                                        .startDate(LocalDate.of(1999, 1, 1))
                                        .endDate(LocalDate.of(2001, 1, 1))
                                        .hasDatasets(true)
                                        .createdTime(PopulatedDates.toDateTime("2001-01-01T01:22:10"))
                                        .createdBy("admin")
                                        .build()))
                        .build()));

        ProductConfigView productConfigView =
                VavrAssert.assertValid(productConfigService.findView(102L))
                        .getResult();

        assertThat(productConfigView.getSchemas())
                .hasSize(1);

        SchemaView schemaView = productConfigView.getSchemas().get(0);
        soft.assertThat(schemaView.getId())
                .isEqualTo(929L);
        soft.assertThat(schemaView.getDisplayName())
                .isEqualTo("displayName");
        soft.assertThat(schemaView.getPhysicalTableName())
                .isEqualTo("PHYZ");
        soft.assertThat(schemaView.getVersion())
                .isEqualTo(2);
        soft.assertThat(schemaView.getStartDate())
                .isEqualTo(LocalDate.of(1999, 1, 1));
        soft.assertThat(schemaView.getEndDate())
                .isEqualTo(LocalDate.of(2001, 1, 1));
        soft.assertThat(schemaView.getHasDatasets())
                .isEqualTo(true);
        soft.assertThat(schemaView.getCreatedTime())
                .isEqualTo(PopulatedDates.toDateTime("2001-01-01T01:22:10"));
        soft.assertThat(schemaView.getCreatedBy())
                .isEqualTo("admin");
    }

    @Test
    public void findView_ViewExists_ConvertsRules() {
        ValidationRule validationRule = ProductPopulated.validationRule()
                .id(5623L)
                .ruleId("cil 123")
                .name("name")
                .description("description")
                .version(1)
                .startDate(LocalDate.of(2001, 1, 1))
                .endDate(LocalDate.of(2002, 1, 1))
                .validationRuleType(ValidationRuleType.QUALITY)
                .validationRuleSeverity(ValidationRuleSeverity.CRITICAL)
                .expression("expression")
                .build();

        when(productConfigRepository.findById(any()))
                .thenReturn(Optional.of(ProductPopulated.productConfig()
                        .tables(newHashSet(
                                ProductPopulated.table().validationRules(newHashSet(validationRule)).build()))
                        .build()));

        ProductConfigView productConfigView =
                VavrAssert.assertValid(productConfigService.findView(102L))
                        .getResult();

        assertThat(productConfigView.getSchemas())
                .hasSize(1);
    }

    @Test
    public void findView_ViewExists_ConvertsPipelines() {
        Pipeline pipeline = ProductPopulated.pipeline()
                .id(929L)
                .productId(100L)
                .name("pipeline1")
                .steps(newHashSet(ProductPopulated.mapPipelineStep()
                        .id(26L)
                        .name("step1")
                        .description("description")
                        .selects(newLinkedHashSet(asList(
                                ProductPopulated.select().select("A").outputFieldId(2431L).build(),
                                ProductPopulated.select().select("B").outputFieldId(2432L).build(),
                                ProductPopulated.select().select("C").outputFieldId(2433L).build())))
                        .schemaIn(ProductPopulated.schemaDetails()
                                .id(101L)
                                .displayName("Input")
                                .physicalTableName("IN")
                                .version(7)
                                .build())
                        .schemaOut(ProductPopulated.schemaDetails()
                                .id(102L)
                                .displayName("Output")
                                .physicalTableName("OUT")
                                .version(8)
                                .build())
                        .build()))
                .build();

        when(productConfigRepository.findById(any()))
                .thenReturn(Optional.of(ProductPopulated.productConfig()
                        .pipelines(newHashSet(pipeline))
                        .build()));

        ProductConfigView productConfigView =
                VavrAssert.assertValid(productConfigService.findView(1021231L))
                        .getResult();

        assertThat(productConfigView.getPipelines())
                .hasSize(1);

        PipelineView pipelineView = productConfigView.getPipelines().get(0);
        soft.assertThat(pipelineView.getId())
                .isEqualTo(929L);
        soft.assertThat(pipelineView.getName())
                .isEqualTo("pipeline1");

        assertThat(pipelineView.getSteps())
                .hasSize(1);
        PipelineStepView pipelineStepView = pipelineView.getSteps().get(0);
        soft.assertThat(pipelineStepView.getId())
                .isEqualTo(26L);
        soft.assertThat(pipelineStepView.getName())
                .isEqualTo("step1");
        soft.assertThat(pipelineStepView.getDescription())
                .isEqualTo("description");
        soft.assertThat(pipelineStepView.getType())
                .isEqualTo(com.lombardrisk.ignis.client.external.pipeline.export.TransformationType.MAP);
        soft.assertThat(pipelineStepView.getSchemaIn().getId())
                .isEqualTo(101);
        soft.assertThat(pipelineStepView.getSchemaIn().getDisplayName())
                .isEqualTo("Input");
        soft.assertThat(pipelineStepView.getSchemaIn().getPhysicalTableName())
                .isEqualTo("IN");
        soft.assertThat(pipelineStepView.getSchemaIn().getVersion())
                .isEqualTo(7);
        soft.assertThat(pipelineStepView.getSchemaOut().getId())
                .isEqualTo(102);
        soft.assertThat(pipelineStepView.getSchemaOut().getDisplayName())
                .isEqualTo("Output");
        soft.assertThat(pipelineStepView.getSchemaOut().getPhysicalTableName())
                .isEqualTo("OUT");
        soft.assertThat(pipelineStepView.getSchemaOut().getVersion())
                .isEqualTo(8);
    }

    @Test
    public void findView_ProductNotFound_ReturnsOptionNone() {
        when(productConfigRepository.findById(any()))
                .thenReturn(Optional.empty());

        VavrAssert.assertFailed(productConfigService.findView(121L))
                .withFailure(CRUDFailure.notFoundIds("ProductConfig", 121L));
    }
}
