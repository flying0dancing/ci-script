package com.lombardrisk.ignis.design.server.productconfig.fixture;

import com.lombardrisk.ignis.common.time.TimeSource;
import com.lombardrisk.ignis.design.server.configuration.ImportCsvConfiguration;
import com.lombardrisk.ignis.design.server.pipeline.PipelineService;
import com.lombardrisk.ignis.design.server.pipeline.PipelineStepSelectsValidator;
import com.lombardrisk.ignis.design.server.pipeline.PipelineStepService;
import com.lombardrisk.ignis.design.server.pipeline.fixture.PipelineServiceFixtureFactory;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineStep;
import com.lombardrisk.ignis.design.server.pipeline.validator.PipelineConstraintsValidator;
import com.lombardrisk.ignis.design.server.productconfig.ProductConfigImportService;
import com.lombardrisk.ignis.design.server.productconfig.ProductConfigService;
import com.lombardrisk.ignis.design.server.productconfig.api.ProductPipelineRepository;
import com.lombardrisk.ignis.design.server.productconfig.converter.ProductConfigConverter;
import com.lombardrisk.ignis.design.server.productconfig.converter.request.SchemaRequestConverter;
import com.lombardrisk.ignis.design.server.productconfig.export.ProductConfigExportFileService;
import com.lombardrisk.ignis.design.server.productconfig.export.ProductConfigImportFileService;
import com.lombardrisk.ignis.design.server.productconfig.export.ProductConfigZipObjectMapper;
import com.lombardrisk.ignis.design.server.productconfig.export.converter.ProductConfigExportConverter;
import com.lombardrisk.ignis.design.server.productconfig.export.converter.SchemaExportConverter;
import com.lombardrisk.ignis.design.server.productconfig.export.validator.ProductImportValidator;
import com.lombardrisk.ignis.design.server.productconfig.schema.RuleService;
import com.lombardrisk.ignis.design.server.productconfig.schema.SchemaService;
import com.lombardrisk.ignis.design.server.productconfig.validation.PipelineValidator;
import com.lombardrisk.ignis.design.server.productconfig.validation.ProductConfigValidator;
import com.lombardrisk.ignis.pipeline.PipelinePlanGenerator;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Clock;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductServiceFixtureFactory {

    private final ProductConfigService productService;
    private final ProductConfigImportService productImportService;
    private final ProductConfigExportFileService productExportService;
    private final ProductConfigRepositoryFixture productRepository;
    private final ProductPipelineRepository productPipelineRepository;

    //Dependencies of the product module on the pipeline module
    private final PipelineDependencies pipelineDependencies;

    public static ProductServiceFixtureFactory create() {
        SchemaServiceFixtureFactory schemaServiceFactory = SchemaServiceFixtureFactory.create();
        ImportCsvConfiguration importCsvConfiguration = new ImportCsvConfiguration();

        return ProductServiceFixtureFactory.create(schemaServiceFactory, importCsvConfiguration.getImportMaxLines());
    }

    public static ProductServiceFixtureFactory create(
            final SchemaServiceFixtureFactory schemaServiceFactory,
            final Integer csvMaxLines) {
        TimeSource timeSource = new TimeSource(Clock.systemUTC());

        ProductConfigRepositoryFixture productConfigRepository =
                new ProductConfigRepositoryFixture(schemaServiceFactory.getSchemaRepository());

        SchemaService schemaService = schemaServiceFactory.getSchemaService();

        RuleService ruleService = schemaServiceFactory.getRuleService();

        SchemaExportConverter schemaExportConverter = new SchemaExportConverter(timeSource);
        ProductConfigExportConverter productConfigExportConverter =
                new ProductConfigExportConverter(schemaExportConverter);

        ProductConfigZipObjectMapper productConfigZipObjectMapper = new ProductConfigZipObjectMapper();
        ProductConfigImportFileService productConfigImportFileService =
                new ProductConfigImportFileService(productConfigZipObjectMapper);

        PipelineDependencies pipelineDependencies = PipelineDependencies.create(schemaServiceFactory, csvMaxLines);

        ProductImportValidator productImportValidator = new ProductImportValidator(
                productConfigRepository,
                schemaServiceFactory.getSchemaConstraintsValidator(),
                schemaExportConverter,
                pipelineDependencies.getPipelineConstraintsValidator()
        );

        ProductPipelineRepositoryFixture productPipelineRepository =
                new ProductPipelineRepositoryFixture(pipelineDependencies.getPipelineService());

        SchemaRequestConverter schemaRequestConverter = new SchemaRequestConverter();

        PipelineValidator pipelineValidator = new PipelineValidator(
                pipelineDependencies.getPipelineStepSelectsValidator(),
                pipelineDependencies.getPipelinePlanGenerator());

        ProductConfigService productConfigService = new ProductConfigService(
                schemaService,
                productConfigRepository,
                new ProductConfigConverter(),
                new SchemaRequestConverter(),
                productPipelineRepository);

        ProductConfigValidator productConfigValidator =
                new ProductConfigValidator(productPipelineRepository, pipelineValidator, productConfigService);

        ProductConfigImportService productConfigImportService = new ProductConfigImportService(
                productConfigImportFileService,
                productImportValidator,
                schemaExportConverter,
                productConfigService,
                schemaService,
                schemaServiceFactory.getFieldService(),
                ruleService,
                pipelineDependencies.pipelineService,
                pipelineDependencies.pipelineStepService,
                productConfigRepository);

        ProductConfigExportFileService productConfigExportFileService = new ProductConfigExportFileService(
                productPipelineRepository,
                productConfigExportConverter,
                productConfigZipObjectMapper,
                productConfigService);

        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken("user", ""));

        return new ProductServiceFixtureFactory(
                productConfigService,
                productConfigImportService,
                productConfigExportFileService,
                productConfigRepository,
                productPipelineRepository,
                pipelineDependencies);
    }

    @AllArgsConstructor
    @Getter
    public static class PipelineDependencies {

        private final PipelineService pipelineService;
        private final PipelineStepService pipelineStepService;
        private final PipelineStepSelectsValidator pipelineStepSelectsValidator;
        private final PipelinePlanGenerator<Long, PipelineStep> pipelinePlanGenerator;
        private final PipelineConstraintsValidator pipelineConstraintsValidator;

        public static PipelineDependencies create() {
            PipelineServiceFixtureFactory.Exports pipelineService = PipelineServiceFixtureFactory.create().getExports();

            return new PipelineDependencies(
                    pipelineService.getPipelineService(),
                    pipelineService.getPipelineStepService(),
                    pipelineService.getPipelineStepSelectsValidator(),
                    pipelineService.getPipelinePlanGenerator(),
                    pipelineService.getPipelineConstraintsValidator());
        }

        public static PipelineDependencies create(
                final SchemaServiceFixtureFactory schemaFactory,
                final Integer csvMaxLines) {
            PipelineServiceFixtureFactory.Exports pipelineService =
                    PipelineServiceFixtureFactory.create(schemaFactory, csvMaxLines)
                            .getExports();

            return new PipelineDependencies(
                    pipelineService.getPipelineService(),
                    pipelineService.getPipelineStepService(),
                    pipelineService.getPipelineStepSelectsValidator(),
                    pipelineService.getPipelinePlanGenerator(),
                    pipelineService.getPipelineConstraintsValidator());
        }
    }
}
