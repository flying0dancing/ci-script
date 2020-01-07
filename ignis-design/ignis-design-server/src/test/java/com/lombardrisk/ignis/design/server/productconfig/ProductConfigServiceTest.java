package com.lombardrisk.ignis.design.server.productconfig;

import com.google.common.collect.ImmutableSet;
import com.lombardrisk.ignis.client.design.pipeline.CreatePipelineRequest;
import com.lombardrisk.ignis.client.design.productconfig.NewProductConfigRequest;
import com.lombardrisk.ignis.client.design.productconfig.ProductConfigDto;
import com.lombardrisk.ignis.client.design.productconfig.UpdateProductConfig;
import com.lombardrisk.ignis.client.design.rule.RuleDto;
import com.lombardrisk.ignis.client.design.schema.NewSchemaVersionRequest;
import com.lombardrisk.ignis.client.design.schema.SchemaDto;
import com.lombardrisk.ignis.client.design.schema.field.FieldDto;
import com.lombardrisk.ignis.common.assertions.VavrAssert;
import com.lombardrisk.ignis.data.common.Identifiable;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.design.field.DesignField;
import com.lombardrisk.ignis.design.field.FieldService;
import com.lombardrisk.ignis.design.field.model.Field;
import com.lombardrisk.ignis.design.field.request.FieldRequest;
import com.lombardrisk.ignis.design.server.pipeline.PipelineService;
import com.lombardrisk.ignis.design.server.productconfig.fixture.ProductServiceFixtureFactory;
import com.lombardrisk.ignis.design.server.productconfig.fixture.RuleServiceFactory;
import com.lombardrisk.ignis.design.server.productconfig.fixture.SchemaServiceFixtureFactory;
import com.lombardrisk.ignis.design.server.productconfig.rule.model.TestResult;
import com.lombardrisk.ignis.design.server.productconfig.rule.model.ValidationRule;
import com.lombardrisk.ignis.design.server.productconfig.rule.model.ValidationRuleExample;
import com.lombardrisk.ignis.design.server.productconfig.rule.model.ValidationRuleExampleField;
import com.lombardrisk.ignis.design.server.productconfig.rule.model.ValidationRuleSeverity;
import com.lombardrisk.ignis.design.server.productconfig.rule.model.ValidationRuleType;
import com.lombardrisk.ignis.design.server.productconfig.schema.RuleService;
import com.lombardrisk.ignis.design.server.productconfig.schema.Schema;
import com.lombardrisk.ignis.design.server.productconfig.schema.SchemaService;
import com.lombardrisk.ignis.design.server.productconfig.schema.request.CreateSchemaRequest;
import io.vavr.control.Validation;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.collect.Sets.newHashSet;
import static com.lombardrisk.ignis.client.design.rule.RuleDto.Severity.CRITICAL;
import static com.lombardrisk.ignis.client.design.rule.RuleDto.Type.QUALITY;
import static com.lombardrisk.ignis.data.common.failure.CRUDFailure.Type.INVALID_REQUEST_PARAMETERS;
import static com.lombardrisk.ignis.design.field.DesignField.Populated.decimalFieldRequest;
import static com.lombardrisk.ignis.design.field.DesignField.Populated.stringFieldRequest;
import static com.lombardrisk.ignis.design.server.fixtures.Design.Populated;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Enclosed.class)
public class ProductConfigServiceTest {

    public static class JustProducts {

        @Rule
        public JUnitSoftAssertions soft = new JUnitSoftAssertions();

        private ProductConfigService productConfigService;
        private PipelineService pipelineService;
        private FieldService fieldService;
        private RuleService ruleService;

        @Before
        public void setUp() {

            SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("user", ""));
            SchemaServiceFixtureFactory schemaServiceFactory = SchemaServiceFixtureFactory.create();
            ProductServiceFixtureFactory productServiceFactory =
                    ProductServiceFixtureFactory.create(schemaServiceFactory, 100);

            productConfigService = productServiceFactory.getProductService();
            fieldService = schemaServiceFactory.getFieldService();
            pipelineService = productServiceFactory.getPipelineDependencies().getPipelineService();
            ruleService = schemaServiceFactory.getRuleService();
        }

        @Test
        public void entityName() {
            assertThat(productConfigService.entityName()).isEqualTo(ProductConfig.class.getSimpleName());
        }

        @Test
        public void createProductConfig_ValidProductRequest_ReturnsSavedProduct() {
            NewProductConfigRequest productConfigRequest = Populated.newProductRequest()
                    .name("name")
                    .version("version")
                    .build();

            ProductConfig failureOrProduct = productConfigService.createProductConfig(productConfigRequest).get();

            soft.assertThat(failureOrProduct.getName())
                    .isEqualTo("name");
            soft.assertThat(failureOrProduct.getVersion())
                    .isEqualTo("version");
            soft.assertThat(failureOrProduct.getTables())
                    .isNull();
        }

        @Test
        public void createProductConfig_ValidProductRequest_SavesProduct() {
            NewProductConfigRequest productConfigRequest = Populated.newProductRequest()
                    .name("new product config name")
                    .version("new product config version")
                    .build();

            productConfigService.createProductConfig(productConfigRequest);

            ProductConfig productConfig = productConfigService.findAll().get(0);

            assertThat(productConfig.getName())
                    .isEqualTo("new product config name");
            assertThat(productConfig.getVersion())
                    .isEqualTo("new product config version");
        }

        @Test
        public void createProductConfig_ProductNameExists_ReturnsFailure() {
            productConfigService.createProductConfig(
                    Populated.newProductRequest("existingProduct").build());

            Validation<ErrorResponse, ProductConfig> productValidation = productConfigService.createProductConfig(
                    Populated.newProductRequest("existingProduct").build());

            ErrorResponse failure = productValidation.getError();
            assertThat(failure.getErrorCode())
                    .isEqualTo("name");
            assertThat(failure.getErrorMessage())
                    .contains("existingProduct", "already exists");
        }

        @Test
        public void updateProduct_ProductNotFound_ReturnsFailure() {
            Validation<CRUDFailure, ProductConfigDto> result = productConfigService.updateProduct(199L, null);

            assertThat(result.getError())
                    .isEqualTo(CRUDFailure.notFoundIds("ProductConfig", 199L));
        }

        @Test
        public void updateProduct_NameUpdate_UpdatesName() {
            Long productConfigId = productConfigService.saveProductConfig(
                    Populated.newProductRequest("oldName").build())
                    .getId();

            productConfigService.updateProduct(productConfigId, UpdateProductConfig.builder()
                    .name("newName")
                    .build());

            ProductConfig productConfig = productConfigService.findById(productConfigId).get();
            assertThat(productConfig.getName())
                    .isEqualTo("newName");
        }

        @Test
        public void updateProduct_VersionUpdate_UpdatesVersion() {
            Long productConfigId =
                    productConfigService.saveProductConfig(Populated.newProductRequest().version("old").build())
                            .getId();

            productConfigService.updateProduct(productConfigId, UpdateProductConfig.builder()
                    .version("1.0.0-RELEASE")
                    .build());

            ProductConfig productConfig = productConfigService.findById(productConfigId).get();
            assertThat(productConfig.getVersion())
                    .isEqualTo("1.0.0-RELEASE");
        }

        @Test
        public void findOne_ProductExists_ReturnsDtoWithNameAndVersion() {
            Long productId = productConfigService.saveProductConfig(Populated.newProductRequest()
                    .name("product1")
                    .version("1.0.0-RELEASE")
                    .build())
                    .getId();

            ProductConfigDto productConfigDto = VavrAssert.assertValid(productConfigService.findOne(productId))
                    .getResult();
            soft.assertThat(productConfigDto.getName())
                    .isEqualTo("product1");
            soft.assertThat(productConfigDto.getVersion())
                    .isEqualTo("1.0.0-RELEASE");
        }

        @Test
        public void findOne_ProductDoesNotExist_ReturnsCRUDFailure() {
            VavrAssert.assertFailed(productConfigService.findOne(-1))
                    .withFailure(CRUDFailure.notFoundIds("ProductConfig", -1L));
        }

        @Test
        public void findAllDtos_ProductsPresent_ReturnsDtosWithNameAndVersion() {
            productConfigService.saveProductConfig(Populated.newProductRequest()
                    .name("product1")
                    .version("1.0.0-RELEASE")
                    .build());

            List<ProductConfigDto> allDtos = productConfigService.findAllProductConfigs();

            assertThat(allDtos).hasSize(1);

            ProductConfigDto productConfigDto = allDtos.get(0);
            soft.assertThat(productConfigDto.getName())
                    .isEqualTo("product1");
            soft.assertThat(productConfigDto.getVersion())
                    .isEqualTo("1.0.0-RELEASE");
        }

        @Test
        public void findAllDtos_NoProductsPresent_ReturnsEmptyList() {
            assertThat(
                    productConfigService.findAllProductConfigs()
            ).isEmpty();
        }

        @Test
        public void findAllDtos_ProductsAndSchema_ReturnsSchemaWithProperties() {
            SecurityContextHolder.getContext().setAuthentication(
                    new TestingAuthenticationToken("ADMIN", ""));

            NewProductConfigRequest productConfigRequest = Populated.newProductRequest().build();

            Long id = productConfigService.saveProductConfig(productConfigRequest).getId();

            CreateSchemaRequest schema = CreateSchemaRequest.builder()
                    .displayName("LIQUIDITY")
                    .physicalTableName("DSREG06")
                    .majorVersion(1)
                    .startDate(LocalDate.of(2018, 1, 1))
                    .endDate(LocalDate.of(2019, 1, 1))
                    .build();

            Long schemaId = productConfigService.createNewSchemaOnProduct(id, schema).get().getId();

            List<ProductConfigDto> allDtos = productConfigService.findAllProductConfigs();

            List<SchemaDto> schemas = allDtos.get(0).getSchemas();

            assertThat(schemas).hasSize(1);
            SchemaDto schemaDto = schemas.get(0);
            soft.assertThat(schemaDto.getDisplayName())
                    .isEqualTo("LIQUIDITY");
            soft.assertThat(schemaDto.getPhysicalTableName())
                    .isEqualTo("DSREG06");
            soft.assertThat(schemaDto.getCreatedBy())
                    .isEqualTo("ADMIN");
            soft.assertThat(schemaDto.getMajorVersion())
                    .isEqualTo(1);
            soft.assertThat(schemaDto.getLatest())
                    .isTrue();

            soft.assertThat(schemaDto.getStartDate())
                    .isEqualTo(LocalDate.of(2018, 1, 1));
            soft.assertThat(schemaDto.getEndDate())
                    .isEqualTo(LocalDate.of(2019, 1, 1));
            soft.assertThat(schemaDto.getId())
                    .isEqualTo(schemaId);
        }

        @Test
        public void findAllDtos_ProductsAndSchema_ReturnsSchemaWithFields() {
            Long id = productConfigService.saveProductConfig(Populated.newProductRequest().build())
                    .getId();

            Long schemaId = VavrAssert.assertValid(
                    productConfigService.createNewSchemaOnProduct(id, Populated.createSchemaRequest().build()))
                    .getResult().getId();

            fieldService.save(schemaId, decimalFieldRequest().name("decimal")
                    .nullable(false)
                    .precision(10)
                    .scale(1)
                    .build()).get();

            List<ProductConfigDto> allDtos = productConfigService.findAllProductConfigs();

            List<SchemaDto> schemas = allDtos.get(0).getSchemas();

            FieldDto.DecimalFieldDto field = (FieldDto.DecimalFieldDto) schemas.get(0).getFields().get(0);
            soft.assertThat(field.getName())
                    .isEqualTo("decimal");
            soft.assertThat(field.isNullable())
                    .isFalse();
            soft.assertThat(field.getScale())
                    .isEqualTo(1);
            soft.assertThat(field.getPrecision())
                    .isEqualTo(10);
        }

        @Test
        public void findAllDtos_ProductsAndSchema_ReturnsSchemaWithRules() {
            Long id = productConfigService.saveProductConfig(Populated.newProductRequest().build())
                    .getId();

            CreateSchemaRequest schema = Populated.createSchemaRequest().build();
            Long schemaId = productConfigService.createNewSchemaOnProduct(id, schema).get().getId();

            fieldService.save(schemaId, stringFieldRequest("F1").build()).get();

            ruleService.saveValidationRule(schemaId, Populated.validationRule()
                    .name("rule123")
                    .ruleId("123")
                    .startDate(LocalDate.of(1991, 1, 1))
                    .endDate(LocalDate.of(1992, 1, 1))
                    .expression("F1=='1'")
                    .description("Simple rule for simple pleasures")
                    .version(1)
                    .validationRuleType(ValidationRuleType.QUALITY)
                    .validationRuleSeverity(ValidationRuleSeverity.CRITICAL)
                    .build())
                    .get();

            List<ProductConfigDto> allDtos = productConfigService.findAllProductConfigs();

            List<SchemaDto> schemas = allDtos.get(0).getSchemas();

            List<RuleDto> validationRules = schemas.get(0).getValidationRules();

            assertThat(validationRules).hasSize(1);

            RuleDto rule = validationRules.get(0);
            soft.assertThat(rule.getName())
                    .isEqualTo("rule123");
            soft.assertThat(rule.getRuleId())
                    .isEqualTo("123");
            soft.assertThat(rule.getDescription())
                    .isEqualTo("Simple rule for simple pleasures");
            soft.assertThat(rule.getStartDate())
                    .isEqualTo(LocalDate.of(1991, 1, 1));
            soft.assertThat(rule.getEndDate())
                    .isEqualTo(LocalDate.of(1992, 1, 1));
            soft.assertThat(rule.getExpression())
                    .isEqualTo("F1=='1'");
            soft.assertThat(rule.getVersion())
                    .isEqualTo(1);
            soft.assertThat(rule.getValidationRuleSeverity())
                    .isEqualTo(CRITICAL);
            soft.assertThat(rule.getValidationRuleType())
                    .isEqualTo(QUALITY);
        }

        @Test
        public void deleteById_ProductExists_DeletesProduct() {
            ProductConfig productConfig = productConfigService.saveProductConfig(Populated.newProductRequest().build());

            assertThat(productConfigService.findById(productConfig.getId()).isDefined())
                    .isTrue();

            CreateSchemaRequest.CreateSchemaRequestBuilder request = Populated.createSchemaRequest();

            productConfigService.createNewSchemaOnProduct(productConfig.getId(), request.majorVersion(1).build()).get();
            productConfigService.createNewSchemaOnProduct(productConfig.getId(), request.majorVersion(2).build()).get();
            productConfigService.createNewSchemaOnProduct(productConfig.getId(), request.majorVersion(3).build()).get();

            productConfigService.deleteById(productConfig.getId());

            assertThat(productConfigService.findById(productConfig.getId()).isDefined())
                    .isFalse();
        }

        @Test
        public void deleteById_ProductWithPipeline_DeletesProductPipelines() {
            ProductConfig productConfig = productConfigService.saveProductConfig(Populated.newProductRequest().build());

            Long pipelineId = VavrAssert.assertValid(
                    pipelineService.saveNewPipeline(CreatePipelineRequest.builder()
                            .productId(productConfig.getId())
                            .name("the pipeline")
                            .build()))
                    .getResult()
                    .getId();

            assertThat(productConfigService.findById(productConfig.getId()).isDefined())
                    .isTrue();

            assertThat(pipelineService.findById(pipelineId).isDefined())
                    .isTrue();

            productConfigService.deleteById(productConfig.getId());

            assertThat(productConfigService.findById(productConfig.getId()).isDefined())
                    .isFalse();

            assertThat(pipelineService.findById(productConfig.getId()).isDefined())
                    .isFalse();
        }

        @Test
        public void deleteById_ProductDoesNotExist_ReturnsFailure() {
            Validation<CRUDFailure, Identifiable> result = productConfigService.deleteById(8432923);

            assertThat(result.getError())
                    .isEqualTo(CRUDFailure.notFoundIds("ProductConfig", 8432923L));
        }
    }

    public static class Schemas {

        @Rule
        public JUnitSoftAssertions soft = new JUnitSoftAssertions();

        private ProductConfigService productConfigService;
        private RuleService ruleService;
        private FieldService fieldService;
        private SchemaService schemaService;

        @Before
        public void setUp() {
            Integer csvImportMaxLines = 100;

            SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("user", ""));

            SchemaServiceFixtureFactory schemaServiceFactory = SchemaServiceFixtureFactory.create();
            ProductServiceFixtureFactory productServiceFactory =
                    ProductServiceFixtureFactory.create(schemaServiceFactory, csvImportMaxLines);
            productConfigService = productServiceFactory.getProductService();
            schemaService = schemaServiceFactory.getSchemaService();
            fieldService = schemaServiceFactory.getFieldService();
            ruleService = RuleServiceFactory.create(schemaServiceFactory.getSchemaRepository());
        }

        @Test
        public void createSchema_ProductNotFound_ReturnsError() {
            List<CRUDFailure> result = productConfigService.createNewSchemaOnProduct(100L, null).getError();

            assertThat(result)
                    .contains(CRUDFailure.notFoundIds("ProductConfig", 100L));
        }

        @Test
        public void createSchema_AddsSchemaToProduct() {
            ProductConfig productConfig = productConfigService.saveProductConfig(Populated.newProductRequest().build());

            CreateSchemaRequest newSchema = Populated.createSchemaRequest()
                    .displayName("NewSchemaDisplayName")
                    .build();

            productConfigService.createNewSchemaOnProduct(productConfig.getId(), newSchema).get();

            ProductConfig retrieved = productConfigService.findById(productConfig.getId()).get();

            assertThat(retrieved.getTables())
                    .extracting(Schema::getDisplayName)
                    .contains("NewSchemaDisplayName");
        }

        @Test
        public void createSchema_DisplayNameTaken_ReturnsError() {
            ProductConfig productConfig = productConfigService.saveProductConfig(Populated.newProductRequest().build());

            CreateSchemaRequest attemptOne = Populated.createSchemaRequest()
                    .displayName("ThisOneIsTaken")
                    .physicalTableName("TOIT")
                    .majorVersion(3)
                    .build();
            CreateSchemaRequest attemptTwo = Populated.createSchemaRequest()
                    .displayName("ThisOneIsTaken")
                    .physicalTableName("TOIT_")
                    .majorVersion(3)
                    .build();

            productConfigService.createNewSchemaOnProduct(productConfig.getId(), attemptOne).get();
            Validation<List<CRUDFailure>, Schema> secondAttempt =
                    productConfigService.createNewSchemaOnProduct(productConfig.getId(), attemptTwo);

            VavrAssert.assertCollectionFailure(secondAttempt)
                    .withFailure(CRUDFailure.constraintFailure(
                            "Schema(s) exist for display name, [ThisOneIsTaken] and version [3]"));
        }

        @Test
        public void createSchema_PhysicalNameTaken_ReturnsError() {
            ProductConfig productConfig = productConfigService.saveProductConfig(Populated.newProductRequest().build());

            CreateSchemaRequest attemptOne = Populated.createSchemaRequest()
                    .displayName("XYZ")
                    .physicalTableName("X")
                    .build();
            CreateSchemaRequest attemptTwo = Populated.createSchemaRequest()
                    .displayName("ABC")
                    .physicalTableName("X")
                    .build();

            productConfigService.createNewSchemaOnProduct(productConfig.getId(), attemptOne).get();
            Validation<List<CRUDFailure>, Schema> secondAttempt =
                    productConfigService.createNewSchemaOnProduct(productConfig.getId(), attemptTwo);

            VavrAssert.assertCollectionFailure(secondAttempt)
                    .withFailure(CRUDFailure.constraintFailure(
                            "Schema(s) exist for physical table name, [X] and version [1]"));
        }

        @Test
        public void createSchema_SchemaStartDateAfterEndDateSame_ReturnsError() {
            ProductConfig productConfig = productConfigService.saveProductConfig(Populated.newProductRequest().build());

            CreateSchemaRequest attemptOne = Populated.createSchemaRequest()
                    .displayName("XYZ")
                    .physicalTableName("X")
                    .startDate(LocalDate.of(2, 1, 1))
                    .endDate(LocalDate.of(1, 1, 1))
                    .build();

            VavrAssert.assertCollectionFailure(
                    productConfigService.createNewSchemaOnProduct(productConfig.getId(), attemptOne))
                    .withFailure(CRUDFailure.constraintFailure(
                            "Schema end date [0001-01-01] must be at least one day after start date, [0002-01-01]"));
        }

        @Test
        public void createSchema_BothDisplayAndPhysicalNameTaken_ReturnsError() {
            ProductConfig productConfig = productConfigService.saveProductConfig(Populated.newProductRequest().build());

            CreateSchemaRequest attemptOne = Populated.createSchemaRequest()
                    .displayName("XYZ")
                    .physicalTableName("X")
                    .majorVersion(1)
                    .build();
            CreateSchemaRequest attemptTwo = Populated.createSchemaRequest()
                    .displayName("XYZ")
                    .physicalTableName("X")
                    .majorVersion(1)
                    .build();

            productConfigService.createNewSchemaOnProduct(productConfig.getId(), attemptOne).get();
            Validation<List<CRUDFailure>, Schema> secondAttempt =
                    productConfigService.createNewSchemaOnProduct(productConfig.getId(), attemptTwo);

            VavrAssert.assertCollectionFailure(secondAttempt).withFailure(
                    CRUDFailure.constraintFailure("Schema(s) exist for display name, [XYZ] and version [1]"),
                    CRUDFailure.constraintFailure("Schema(s) exist for physical table name, [X] and version [1]"));
        }

        @Test
        public void removeSchemaFromProduct_RemovesSchema() {
            ProductConfig productConfig = productConfigService.saveProductConfig(Populated.newProductRequest().build());

            CreateSchemaRequest newSchema = Populated.createSchemaRequest().displayName("SHOULD NOT BE ADDED").build();

            Schema createdSchema =
                    productConfigService.createNewSchemaOnProduct(productConfig.getId(), newSchema).get();

            productConfigService.removeSchemaFromProduct(productConfig.getId(), createdSchema.getId());

            ProductConfig retrieved = productConfigService.findById(productConfig.getId()).get();

            assertThat(retrieved.getTables())
                    .extracting(Schema::getDisplayName)
                    .doesNotContain("SHOULD NOT BE ADDED");
        }

        @Test
        public void removeSchemaFromProduct_MultipleVersionsRemovingLatest_SetsSecondLatestAsLatest() {
            ProductConfig productConfig = productConfigService.saveProductConfig(Populated.newProductRequest().build());

            Schema oldVersion = productConfigService.createNewSchemaOnProduct(
                    productConfig.getId(), Populated.createSchemaRequest()
                            .startDate(LocalDate.of(2018, 1, 1))
                            .majorVersion(1)
                            .build())
                    .get();

            Schema newVersion = productConfigService.createNewSchemaVersion(
                    productConfig.getId(), oldVersion.getId(), Populated.newSchemaVersionRequest()
                            .startDate(LocalDate.of(2018, 2, 1))
                            .build())
                    .get();

            soft.assertThat(newVersion.getLatest())
                    .isTrue();
            soft.assertThat(newVersion.getMajorVersion())
                    .isEqualTo(2);

            productConfigService.removeSchemaFromProduct(productConfig.getId(), newVersion.getId());

            ProductConfig retrieved = productConfigService.findById(productConfig.getId()).get();

            soft.assertThat(retrieved.getTables())
                    .containsOnly(oldVersion);

            Schema nowTheLatest = retrieved.getTables().iterator().next();
            soft.assertThat(nowTheLatest.getLatest())
                    .isTrue();
            soft.assertThat(nowTheLatest.getMajorVersion())
                    .isEqualTo(1);
        }

        @SuppressWarnings("OptionalGetWithoutIsPresent")
        @Test
        public void removeSchemaFromProduct_MultipleVersionsNotRemovingLatest_DoesNotAlterLatest() {
            ProductConfig productConfig = productConfigService.saveProductConfig(Populated.newProductRequest().build());

            Schema versionOne = productConfigService.createNewSchemaOnProduct(
                    productConfig.getId(),
                    Populated.createSchemaRequest()
                            .majorVersion(1)
                            .startDate(LocalDate.of(2018, 1, 1))
                            .build())
                    .get();

            Schema versionTwo = productConfigService.createNewSchemaVersion(
                    productConfig.getId(), versionOne.getId(),
                    Populated.newSchemaVersionRequest()
                            .startDate(LocalDate.of(2018, 2, 1))
                            .build())
                    .get();

            Schema versionThree = productConfigService.createNewSchemaVersion(
                    productConfig.getId(), versionTwo.getId(),
                    Populated.newSchemaVersionRequest()
                            .startDate(LocalDate.of(2018, 3, 1))
                            .build())
                    .get();

            productConfigService.removeSchemaFromProduct(productConfig.getId(), versionTwo.getId());

            ProductConfig retrieved = productConfigService.findById(productConfig.getId()).get();

            soft.assertThat(retrieved.getTables())
                    .containsOnly(versionOne, versionThree);

            Schema findVersionOne = retrieved.getTables().stream()
                    .filter(schema -> schema.getId().equals(versionOne.getId()))
                    .findFirst().get();
            Schema findVersionThree = retrieved.getTables().stream()
                    .filter(schema -> schema.getId().equals(versionThree.getId()))
                    .findFirst().get();

            soft.assertThat(findVersionThree.getLatest())
                    .isTrue();
            soft.assertThat(findVersionOne.getLatest())
                    .isFalse();
        }

        @Test
        public void createNewSchemaVersion_SchemaExists_CreatesNewSchemaWithIncrementedVersionAndStartDate() {
            Long productId = productConfigService.saveProductConfig(Populated.newProductRequest().build())
                    .getId();

            CreateSchemaRequest schema = Populated.createSchemaRequest()
                    .majorVersion(1)
                    .startDate(LocalDate.of(2015, 1, 1))
                    .build();

            Schema createdSchema = productConfigService.createNewSchemaOnProduct(productId, schema).get();

            NewSchemaVersionRequest request = Populated.newSchemaVersionRequest()
                    .startDate(LocalDate.of(2016, 1, 1))
                    .build();

            Schema newVersion = productConfigService.createNewSchemaVersion(productId, createdSchema.getId(), request)
                    .get();

            soft.assertThat(newVersion.getMajorVersion())
                    .isEqualTo(2);
            soft.assertThat(newVersion.getLatest())
                    .isTrue();
            soft.assertThat(newVersion.getStartDate())
                    .isEqualTo(LocalDate.of(2016, 1, 1));
            soft.assertThat(newVersion.getEndDate())
                    .isNull();
        }

        @Test
        public void createNewSchemaVersion_SchemaIsNotLatest_CreatesNewSchemaWithIncrementedVersionAndStartDate() {
            Long productId = productConfigService.saveProductConfig(Populated.newProductRequest().build())
                    .getId();

            Schema schemaVersion1 =
                    productConfigService.createNewSchemaOnProduct(productId, Populated.createSchemaRequest()
                            .majorVersion(1)
                            .startDate(LocalDate.of(2015, 1, 1))
                            .build()).get();

            Schema version2 = productConfigService.createNewSchemaVersion(
                    productId,
                    schemaVersion1.getId(),
                    Populated.newSchemaVersionRequest()
                            .startDate(LocalDate.of(2016, 1, 1))
                            .build())
                    .get();

            Schema version3 = productConfigService.createNewSchemaVersion(
                    productId,
                    version2.getId(),
                    Populated.newSchemaVersionRequest()
                            .startDate(LocalDate.of(2017, 1, 1))
                            .build())
                    .get();

            Schema version4From2 = productConfigService.createNewSchemaVersion(
                    productId,
                    version2.getId(),
                    Populated.newSchemaVersionRequest()
                            .startDate(LocalDate.of(2018, 1, 1))
                            .build())
                    .get();

            soft.assertThat(version4From2.getMajorVersion())
                    .isEqualTo(4);
            soft.assertThat(version4From2.getLatest())
                    .isTrue();
            soft.assertThat(version4From2.getStartDate())
                    .isEqualTo(LocalDate.of(2018, 1, 1));
            soft.assertThat(version4From2.getEndDate())
                    .isNull();
        }

        @Test
        public void createNewSchemaVersion_SchemaIsNotLatestExists_SetsPreviousLatestEndDate() {
            Long productId = productConfigService.saveProductConfig(Populated.newProductRequest().build())
                    .getId();

            Schema schemaVersion1 =
                    productConfigService.createNewSchemaOnProduct(productId, Populated.createSchemaRequest()
                            .majorVersion(1)
                            .startDate(LocalDate.of(2015, 1, 1))
                            .build()).get();

            Schema version2 = productConfigService.createNewSchemaVersion(
                    productId,
                    schemaVersion1.getId(),
                    Populated.newSchemaVersionRequest()
                            .startDate(LocalDate.of(2016, 1, 1))
                            .build())
                    .get();

            Schema version3 = productConfigService.createNewSchemaVersion(
                    productId,
                    version2.getId(),
                    Populated.newSchemaVersionRequest()
                            .startDate(LocalDate.of(2017, 1, 1))
                            .build())
                    .get();

            Schema version4From2 = productConfigService.createNewSchemaVersion(
                    productId,
                    version2.getId(),
                    Populated.newSchemaVersionRequest()
                            .startDate(LocalDate.of(2018, 1, 1))
                            .build())
                    .get();

            Schema retrievedVersion3 = schemaService.findById(version3.getId()).get();

            soft.assertThat(retrievedVersion3.getEndDate())
                    .isEqualTo(LocalDate.of(2017, 12, 31));
        }

        @Test
        public void createNewSchemaVersion_SchemaIsNotLatestExistsNewStartDateBeforeLatestVersionsEnd_ReturnsError() {
            Long productId = productConfigService.saveProductConfig(Populated.newProductRequest().build())
                    .getId();

            Schema schemaVersion1 =
                    productConfigService.createNewSchemaOnProduct(productId, Populated.createSchemaRequest()
                            .majorVersion(1)
                            .startDate(LocalDate.of(2015, 1, 1))
                            .build()).get();

            Schema version2 = productConfigService.createNewSchemaVersion(
                    productId,
                    schemaVersion1.getId(),
                    Populated.newSchemaVersionRequest()
                            .startDate(LocalDate.of(2016, 1, 1))
                            .build())
                    .get();

            Schema version3 = productConfigService.createNewSchemaVersion(
                    productId,
                    version2.getId(),
                    Populated.newSchemaVersionRequest()
                            .startDate(LocalDate.of(2017, 1, 1))
                            .build())
                    .get();

            VavrAssert.assertFailed(
                    productConfigService.createNewSchemaVersion(
                            productId,
                            version2.getId(),
                            Populated.newSchemaVersionRequest()
                                    .startDate(LocalDate.of(2016, 2, 1))
                                    .build()))
                    .extracting(CRUDFailure::getErrorMessage)
                    .withFailure(
                            "Invalid request: start date - New start date '2016-02-01' must be after latest versions's start date '2017-01-01'");
        }

        @Test
        public void createNewSchemaVersion_SchemaDatesOverlap_ReturnsInvalidParameterError() {
            Long productId = productConfigService.saveProductConfig(Populated.newProductRequest().build())
                    .getId();

            CreateSchemaRequest schema = Populated.createSchemaRequest()
                    .majorVersion(1)
                    .startDate(LocalDate.of(2015, 1, 1))
                    .build();

            Schema createdSchema = productConfigService.createNewSchemaOnProduct(productId, schema).get();

            NewSchemaVersionRequest request = Populated.newSchemaVersionRequest()
                    .startDate(LocalDate.of(2014, 1, 1))
                    .build();

            CRUDFailure newVersionResult = productConfigService
                    .createNewSchemaVersion(productId, createdSchema.getId(), request)
                    .getError();

            assertThat(newVersionResult.getErrorCode())
                    .isEqualTo(INVALID_REQUEST_PARAMETERS.name());

            assertThat(newVersionResult.getErrorMessage())
                    .contains("start date", "2014-01-01", "must be after", "2015-01-01");
        }

        @Test
        public void createNewSchemaVersion_SchemaDateIsDayAfterPreviousVersionStartDate_ReturnsInvalidParameterError() {
            Long productId = productConfigService.saveProductConfig(Populated.newProductRequest().build())
                    .getId();

            CreateSchemaRequest schema = Populated.createSchemaRequest()
                    .majorVersion(1)
                    .startDate(LocalDate.of(2015, 1, 1))
                    .build();

            Schema createdSchema = productConfigService.createNewSchemaOnProduct(productId, schema).get();

            NewSchemaVersionRequest request = Populated.newSchemaVersionRequest()
                    .startDate(LocalDate.of(2015, 1, 2))
                    .build();

            CRUDFailure newVersionResult = productConfigService
                    .createNewSchemaVersion(productId, createdSchema.getId(), request)
                    .getError();

            assertThat(newVersionResult.getErrorCode())
                    .isEqualTo(INVALID_REQUEST_PARAMETERS.name());

            assertThat(newVersionResult.getErrorMessage())
                    .isEqualTo(
                            "Supplied parameters are invalid [(startDate, Start date results in previous schema version have same start and end date)]");
        }

        @SuppressWarnings("OptionalGetWithoutIsPresent")
        @Test
        public void createNewSchemaVersion_SchemaExists_UpdatesOldSchema() {
            Long productId = productConfigService.saveProductConfig(Populated.newProductRequest().build())
                    .getId();

            CreateSchemaRequest newSchema = Populated.createSchemaRequest()
                    .majorVersion(1)
                    .startDate(LocalDate.of(2015, 1, 1))
                    .build();

            Schema createdSchema = productConfigService.createNewSchemaOnProduct(productId, newSchema).get();

            NewSchemaVersionRequest request = Populated.newSchemaVersionRequest()
                    .startDate(LocalDate.of(2017, 1, 1))
                    .build();

            productConfigService.createNewSchemaVersion(productId, createdSchema.getId(), request);

            Schema updatedOldSchema = productConfigService.findById(productId).get().getTables().stream()
                    .filter(schema -> schema.getId().equals(createdSchema.getId()))
                    .findFirst()
                    .get();

            soft.assertThat(updatedOldSchema.getLatest())
                    .isFalse();
            soft.assertThat(updatedOldSchema.getEndDate())
                    .isEqualTo(LocalDate.of(2016, 12, 31));
        }

        @Test
        public void createNewSchemaVersion_MultipleSchemaVersionsExist_UpdatesLatestSchemaSchema() {
            Long productId = productConfigService.saveProductConfig(Populated.newProductRequest().build())
                    .getId();

            Schema version1 = productConfigService.createNewSchemaOnProduct(
                    productId,
                    Populated.createSchemaRequest()
                            .majorVersion(1)
                            .startDate(LocalDate.of(2019, 1, 1))
                            .endDate(LocalDate.of(2019, 2, 1))
                            .build())
                    .get();

            Schema schemaV2 = VavrAssert.assertValid(
                    productConfigService.createNewSchemaVersion(
                            productId,
                            version1.getId(),
                            Populated.newSchemaVersionRequest()
                                    .startDate(LocalDate.of(2019, 1, 3))
                                    .build()))
                    .getResult();

            Schema schemaV3 = VavrAssert.assertValid(
                    productConfigService.createNewSchemaVersion(
                            productId,
                            version1.getId(),
                            Populated.newSchemaVersionRequest()
                                    .startDate(LocalDate.of(2019, 1, 5))
                                    .build()))
                    .getResult();

            soft.assertThat(version1.getLatest())
                    .isFalse();
            soft.assertThat(version1.getStartDate())
                    .isEqualTo(LocalDate.of(2019, 1, 1));
            soft.assertThat(version1.getEndDate())
                    .isEqualTo(LocalDate.of(2019, 1, 2));

            soft.assertThat(schemaV2.getLatest())
                    .isFalse();
            soft.assertThat(schemaV2.getStartDate())
                    .isEqualTo(LocalDate.of(2019, 1, 3));
            soft.assertThat(schemaV2.getEndDate())
                    .isEqualTo(LocalDate.of(2019, 1, 4));

            soft.assertThat(schemaV3.getLatest())
                    .isTrue();
            soft.assertThat(schemaV3.getStartDate())
                    .isEqualTo(LocalDate.of(2019, 1, 5));
        }

        @Test
        public void createNewSchemaVersion_MultipleSchemaVersionsExistIncorrectStartDateVersion3_UpdatesLatestSchemaSchema() {
            Long productId = productConfigService.saveProductConfig(Populated.newProductRequest().build())
                    .getId();

            Schema version1 = VavrAssert.assertValid(productConfigService.createNewSchemaOnProduct(
                    productId,
                    Populated.createSchemaRequest()
                            .majorVersion(1)
                            .startDate(LocalDate.of(2019, 1, 1))
                            .endDate(LocalDate.of(2019, 2, 1))
                            .build()))
                    .getResult();

            Schema schemaV2 = VavrAssert.assertValid(
                    productConfigService.createNewSchemaVersion(
                            productId,
                            version1.getId(),
                            Populated.newSchemaVersionRequest()
                                    .startDate(LocalDate.of(2019, 1, 3))
                                    .build()))
                    .getResult();

            VavrAssert.assertFailed(
                    productConfigService.createNewSchemaVersion(
                            productId,
                            version1.getId(),
                            Populated.newSchemaVersionRequest()
                                    .startDate(LocalDate.of(2019, 1, 3))
                                    .build()));

            soft.assertThat(version1.getLatest())
                    .isFalse();
            soft.assertThat(version1.getStartDate())
                    .isEqualTo(LocalDate.of(2019, 1, 1));
            soft.assertThat(version1.getEndDate())
                    .isEqualTo(LocalDate.of(2019, 1, 2));

            soft.assertThat(schemaV2.getLatest())
                    .isTrue();
            soft.assertThat(schemaV2.getStartDate())
                    .isEqualTo(LocalDate.of(2019, 1, 3));
        }

        @Test
        public void createNewSchemaVersion_SchemaExists_CopiesSchemaProperties() {
            Long productId = productConfigService.saveProductConfig(Populated.newProductRequest().build())
                    .getId();

            CreateSchemaRequest schema = Populated.createSchemaRequest()
                    .displayName("display")
                    .physicalTableName("physical")
                    .startDate(LocalDate.of(2018, 1, 1))
                    .build();

            Schema createdSchema = productConfigService.createNewSchemaOnProduct(productId, schema).get();

            Schema newVersion = productConfigService.createNewSchemaVersion(productId, createdSchema.getId(),
                    Populated.newSchemaVersionRequest()
                            .startDate(LocalDate.of(2018, 2, 1))
                            .build())
                    .get();

            soft.assertThat(newVersion.getDisplayName())
                    .isEqualTo("display");
            soft.assertThat(newVersion.getPhysicalTableName())
                    .isEqualTo("physical");
        }

        @Test
        public void createNewSchemaVersion_SchemaWithAllFieldTypes_CopiesSchemaWithAllFieldTypes() {
            Long productId = productConfigService.saveProductConfig(Populated.newProductRequest().build())
                    .getId();

            List<FieldRequest> fieldRequests = Arrays.asList(
                    DesignField.Populated.booleanFieldRequest("boolean").build(),
                    DesignField.Populated.dateFieldRequest("date").build(),
                    decimalFieldRequest("decimal").build(),
                    DesignField.Populated.doubleFieldRequest("double").build(),
                    DesignField.Populated.floatFieldRequest("float").build(),
                    DesignField.Populated.intFieldRequest("int").build(),
                    DesignField.Populated.longFieldRequest("long").build(),
                    stringFieldRequest("string").build(),
                    DesignField.Populated.timestampFieldRequest("timestamp").build());

            CreateSchemaRequest schema = Populated.createSchemaRequest()
                    .startDate(LocalDate.of(2018, 1, 1))
                    .build();

            Schema createdSchema = productConfigService.createNewSchemaOnProduct(productId, schema).get();

            for (FieldRequest fieldRequest : fieldRequests) {
                fieldService.save(createdSchema.getId(), fieldRequest).get();
            }

            Schema newVersion = productConfigService.createNewSchemaVersion(productId, createdSchema.getId(),
                    Populated.newSchemaVersionRequest()
                            .startDate(LocalDate.of(2018, 2, 1))
                            .build())
                    .get();

            assertThat(newVersion.getFields()
                    .stream()
                    .sorted(Comparator.comparing(Field::getId))
                    .collect(Collectors.toList()))
                    .extracting(Field::getName)
                    .containsExactly(
                            "boolean",
                            "date",
                            "decimal",
                            "double",
                            "float",
                            "int",
                            "long",
                            "string",
                            "timestamp");
        }

        @Test
        public void createNewSchemaVersion_SchemaWithValidationRules_CopiesSchemaWithRules() {
            Long productId = productConfigService.saveProductConfig(Populated.newProductRequest().build())
                    .getId();

            ImmutableSet<FieldRequest> fieldRequests = ImmutableSet.of(
                    decimalFieldRequest().name("D").build(),
                    stringFieldRequest().name("S").build());

            CreateSchemaRequest schema = Populated.createSchemaRequest()
                    .startDate(LocalDate.of(2018, 1, 1))
                    .build();

            Schema createdSchema = productConfigService.createNewSchemaOnProduct(productId, schema).get();
            fieldRequests.forEach(field -> fieldService.save(createdSchema.getId(), field).get());

            ValidationRule originalRule =
                    ruleService.saveValidationRule(createdSchema.getId(), Populated.validationRule()
                            .name("Rule1")
                            .ruleId("R1")
                            .validationRuleSeverity(ValidationRuleSeverity.CRITICAL)
                            .validationRuleType(ValidationRuleType.QUALITY)
                            .startDate(LocalDate.of(2017, 1, 1))
                            .endDate(LocalDate.of(2018, 3, 3))
                            .expression("S == 'HELLO' && D < 0.01")
                            .validationRuleExamples(newHashSet(
                                    Populated.validationRuleExample()
                                            .validationRuleExampleFields(newHashSet(
                                                    Populated.validationRuleExampleField()
                                                            .name("D")
                                                            .value("0.001")
                                                            .build(),
                                                    Populated.validationRuleExampleField()
                                                            .name("S")
                                                            .value("HELLO")
                                                            .build()
                                            ))
                                            .expectedResult(TestResult.PASS)
                                            .build()
                            ))
                            .build()).get();

            Schema newVersion = productConfigService.createNewSchemaVersion(productId, createdSchema.getId(),
                    Populated.newSchemaVersionRequest()
                            .startDate(LocalDate.of(2018, 2, 1))
                            .build())
                    .get();

            ValidationRule newVersionRule = newVersion.getValidationRules().iterator().next();
            ValidationRuleExample validationRuleExample = newVersionRule.getValidationRuleExamples().iterator().next();

            soft.assertThat(newVersionRule.getId())
                    .isNotEqualTo(originalRule.getId());
            soft.assertThat(newVersionRule.getRuleId())
                    .isEqualTo("R1");
            soft.assertThat(newVersionRule.getName())
                    .isEqualTo("Rule1");
            soft.assertThat(newVersionRule.getExpression())
                    .isEqualTo("S == 'HELLO' && D < 0.01");
            soft.assertThat(newVersionRule.getStartDate())
                    .isEqualTo(LocalDate.of(2017, 1, 1));
            soft.assertThat(newVersionRule.getEndDate())
                    .isEqualTo(LocalDate.of(2018, 3, 3));
            soft.assertThat(newVersionRule.getContextFields())
                    .extracting(Field::getName)
                    .contains("S", "D");

            soft.assertThat(validationRuleExample.getExpectedResult())
                    .isEqualTo(TestResult.PASS);
            soft.assertThat(validationRuleExample.getValidationRuleExampleFields())
                    .extracting(ValidationRuleExampleField::getName)
                    .contains("S", "D");
            soft.assertThat(validationRuleExample.getValidationRuleExampleFields())
                    .extracting(ValidationRuleExampleField::getValue)
                    .contains("HELLO", "0.001");
        }

        @Test
        public void createNewSchemaVersion_RuleEndsBeforeNewSchemaStartDate_DoesNotCopyRule() {
            Long productId = productConfigService.saveProductConfig(Populated.newProductRequest().build())
                    .getId();

            ImmutableSet<FieldRequest> fieldRequests = ImmutableSet.of(
                    decimalFieldRequest().name("D").build(),
                    stringFieldRequest().name("S").build());

            CreateSchemaRequest schema = Populated.createSchemaRequest()
                    .startDate(LocalDate.of(2018, 1, 1))
                    .build();

            Schema createdSchema = productConfigService.createNewSchemaOnProduct(productId, schema).get();
            fieldRequests.forEach(field -> fieldService.save(createdSchema.getId(), field).get());

            ruleService.saveValidationRule(createdSchema.getId(), Populated.validationRule()
                    .endDate(LocalDate.of(2017, 12, 1))
                    .expression("S == 'HELLO' && D < 0.01")
                    .build()).get();

            Schema newVersion = productConfigService.createNewSchemaVersion(productId, createdSchema.getId(),
                    Populated.newSchemaVersionRequest()
                            .startDate(LocalDate.of(2018, 1, 5))
                            .build())
                    .get();

            assertThat(newVersion.getValidationRules())
                    .isEmpty();
        }

        @Test
        public void createNewSchemaVersion_ProductDoesNotExist_ReturnsError() {
            Validation<CRUDFailure, Schema> result = productConfigService
                    .createNewSchemaVersion(-900L, null, Populated.newSchemaVersionRequest().build());

            assertThat(result.getError())
                    .isEqualTo(CRUDFailure.notFoundIds("ProductConfig", -900L));
        }

        @Test
        public void createNewSchemaVersion_SchemaIdNotPresentOnProduct_ReturnsError() {
            ProductConfig product1 = productConfigService.saveProductConfig(
                    Populated.newProductRequest("one").build());
            ProductConfig product2 = productConfigService.saveProductConfig(
                    Populated.newProductRequest("two").build());

            CreateSchemaRequest schema = Populated.createSchemaRequest()
                    .majorVersion(1)
                    .build();

            Schema createdSchema = productConfigService.createNewSchemaOnProduct(product1.getId(), schema).get();

            Validation<CRUDFailure, Schema> result = productConfigService
                    .createNewSchemaVersion(
                            product2.getId(),
                            createdSchema.getId(),
                            Populated.newSchemaVersionRequest().build());

            assertThat(result.getError())
                    .isEqualTo(CRUDFailure.notFoundIds("Schema", createdSchema.getId()));
        }
    }
}
