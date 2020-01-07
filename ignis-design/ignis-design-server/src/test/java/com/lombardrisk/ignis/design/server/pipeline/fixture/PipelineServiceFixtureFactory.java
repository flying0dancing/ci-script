package com.lombardrisk.ignis.design.server.pipeline.fixture;

import com.lombardrisk.ignis.design.field.FieldService;
import com.lombardrisk.ignis.design.server.configuration.ImportCsvConfiguration;
import com.lombardrisk.ignis.design.server.pipeline.PipelineService;
import com.lombardrisk.ignis.design.server.pipeline.PipelineStepExecutor;
import com.lombardrisk.ignis.design.server.pipeline.PipelineStepSelectsValidator;
import com.lombardrisk.ignis.design.server.pipeline.PipelineStepService;
import com.lombardrisk.ignis.design.server.pipeline.SparkUDFService;
import com.lombardrisk.ignis.design.server.pipeline.api.PipelineStepTestRepository;
import com.lombardrisk.ignis.design.server.pipeline.converter.PipelineConverter;
import com.lombardrisk.ignis.design.server.pipeline.converter.PipelineStepConverter;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineStep;
import com.lombardrisk.ignis.design.server.pipeline.test.PipelineStepTestExecuteService;
import com.lombardrisk.ignis.design.server.pipeline.test.PipelineStepTestRowService;
import com.lombardrisk.ignis.design.server.pipeline.test.PipelineStepTestService;
import com.lombardrisk.ignis.design.server.pipeline.validator.PipelineConstraintsValidator;
import com.lombardrisk.ignis.design.server.productconfig.fixture.SchemaServiceFixtureFactory;
import com.lombardrisk.ignis.design.server.productconfig.schema.SchemaService;
import com.lombardrisk.ignis.pipeline.PipelinePlanGenerator;
import io.vavr.control.Validation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PipelineServiceFixtureFactory {

    private final Exports exports;
    private final Dependencies dependencies;
    private final TestHelpers testHelpers;

    public static PipelineServiceFixtureFactory create() {
        ImportCsvConfiguration importCsvConfiguration = new ImportCsvConfiguration();
        return create(SchemaServiceFixtureFactory.create(), importCsvConfiguration.getImportMaxLines());
    }

    public static PipelineServiceFixtureFactory create(
            final SchemaServiceFixtureFactory schemaServiceFixtureFactory,
            final Integer csvMaxLines) {
        Dependencies dependencies = buildDependencies(schemaServiceFixtureFactory);

        PipelineStepExecutor pipelineStepExecutor = mock(PipelineStepExecutor.class);
        when(pipelineStepExecutor.executeFullTransformation(any(), any()))
                .thenReturn(Validation.valid(null));
        SparkUDFService sparkUDFService = mock(SparkUDFService.class);

        PipelineStepConverter stepConverter = new PipelineStepConverter();
        PipelinePlanGenerator<Long, PipelineStep> pipelinePlanGenerator = new PipelinePlanGenerator<>();

        PipelineService pipelineService = new PipelineService(
                dependencies.pipelineRepository,
                new PipelineConverter(stepConverter, pipelinePlanGenerator));

        PipelineStepSelectsValidator stepSelectsValidator =
                new PipelineStepSelectsValidator(dependencies.schemaService, pipelineStepExecutor, sparkUDFService);

        PipelineStepService pipelineStepService = new PipelineStepService(
                dependencies.getPipelineStepRepository(),
                stepConverter,
                dependencies.pipelineRepository,
                dependencies.pipelineStepTestRepository,
                stepSelectsValidator);

        PipelineStepTestExecuteService pipelineStepTestExecuteService =
                new PipelineStepTestExecuteService(
                        pipelineStepExecutor,
                        sparkUDFService,
                        dependencies.schemaService);

        Exports exports = buildExports(
                dependencies,
                pipelineService,
                stepSelectsValidator,
                pipelineStepService,
                pipelineStepTestExecuteService,
                pipelinePlanGenerator,
                csvMaxLines);

        TestHelpers testHelpers = TestHelpers.builder()
                .examplePipelineHelper(new ExamplePipelineHelper(
                        dependencies.schemaService,
                        dependencies.fieldService,
                        pipelineService,
                        pipelineStepService))
                .pipelineStepExecutor(pipelineStepExecutor)
                .build();

        return new PipelineServiceFixtureFactory(exports, dependencies, testHelpers);
    }

    private static Exports buildExports(
            final Dependencies dependencies,
            final PipelineService pipelineService,
            final PipelineStepSelectsValidator stepSelectsValidator,
            final PipelineStepService pipelineStepService,
            final PipelineStepTestExecuteService pipelineStepTestExecuteService,
            final PipelinePlanGenerator<Long, PipelineStep> pipelinePlanGenerator,
            final Integer csvMaxImportLines) {

        PipelineStepTestService pipelineStepTestService =
                new PipelineStepTestService(
                        dependencies.pipelineStepRepository,
                        dependencies.pipelineStepTestRepository,
                        dependencies.pipelineStepTestRowRepository,
                        pipelineStepTestExecuteService,
                        stepSelectsValidator,
                        dependencies.schemaService);

        PipelineStepTestRowService pipelineStepTestRowService =
                new PipelineStepTestRowService(
                        dependencies.pipelineStepRepository,
                        dependencies.pipelineStepTestRepository,
                        dependencies.pipelineStepTestRowRepository,
                        dependencies.schemaService,
                        dependencies.fieldService,
                        csvMaxImportLines);

        return Exports.builder()
                .pipelineService(pipelineService)
                .pipelineStepService(pipelineStepService)
                .pipelineStepTestService(pipelineStepTestService)
                .pipelineStepTestRowService(
                        new PipelineStepTestRowService(
                                dependencies.pipelineStepRepository,
                                dependencies.pipelineStepTestRepository,
                                dependencies.pipelineStepTestRowRepository,
                                dependencies.schemaService,
                                dependencies.fieldService,
                                csvMaxImportLines))
                .pipelineStepSelectsValidator(stepSelectsValidator)
                .pipelinePlanGenerator(pipelinePlanGenerator)
                .pipelineConstraintsValidator(
                        new PipelineConstraintsValidator(dependencies.pipelineRepository))
                .build();
    }

    private static Dependencies buildDependencies() {
        SchemaServiceFixtureFactory schemaServiceFactory = SchemaServiceFixtureFactory.create();
        return buildDependencies(schemaServiceFactory);
    }

    private static Dependencies buildDependencies(final SchemaServiceFixtureFactory schemaServiceFactory) {

        PipelineStepRepositoryFixture pipelineStepRepository = new PipelineStepRepositoryFixture();
        PipelineRepositoryFixture pipelineRepository = new PipelineRepositoryFixture(pipelineStepRepository);

        PipelineStepTestRowRepositoryFixture pipelineStepTestRowRepository =
                new PipelineStepTestRowRepositoryFixture();
        PipelineStepTestRepository pipelineStepTestRepository =
                new PipelineStepTestRepositoryFixture(pipelineStepRepository, pipelineStepTestRowRepository);

        return Dependencies.builder()
                .pipelineRepository(pipelineRepository)
                .pipelineStepRepository(pipelineStepRepository)
                .pipelineStepTestRepository(pipelineStepTestRepository)
                .pipelineStepTestRowRepository(pipelineStepTestRowRepository)
                .schemaService(schemaServiceFactory.getSchemaService())
                .fieldService(schemaServiceFactory.getFieldService())
                .build();
    }

    @Data
    @Builder
    public static class Dependencies {

        private final PipelineStepRepositoryFixture pipelineStepRepository;
        private final PipelineRepositoryFixture pipelineRepository;
        private final PipelineStepTestRowRepositoryFixture pipelineStepTestRowRepository;
        private final PipelineStepTestRepository pipelineStepTestRepository;
        private final SchemaService schemaService;
        private final FieldService fieldService;
    }

    @Data
    @Builder
    public static class Exports {

        private final PipelineService pipelineService;
        private final PipelineStepService pipelineStepService;
        private final PipelineRepositoryFixture pipelineRepository;
        private final PipelineStepTestService pipelineStepTestService;
        private final PipelineStepTestRowService pipelineStepTestRowService;
        private final PipelineStepSelectsValidator pipelineStepSelectsValidator;
        private final PipelinePlanGenerator<Long, PipelineStep> pipelinePlanGenerator;
        private final PipelineConstraintsValidator pipelineConstraintsValidator;
    }

    @Data
    @Builder
    public static class TestHelpers {

        private final PipelineStepExecutor pipelineStepExecutor;
        private final ExamplePipelineHelper examplePipelineHelper;
    }
}
