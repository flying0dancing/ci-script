package com.lombardrisk.ignis.design.server.productconfig.fixture;

import com.lombardrisk.ignis.design.field.FieldService;
import com.lombardrisk.ignis.design.field.api.DatasetSchemaFieldRepository;
import com.lombardrisk.ignis.design.field.fixtures.FieldServiceFactory;
import com.lombardrisk.ignis.design.server.productconfig.schema.RuleService;
import com.lombardrisk.ignis.design.server.productconfig.schema.SchemaService;
import com.lombardrisk.ignis.design.server.productconfig.schema.validation.SchemaConstraintsValidator;
import com.lombardrisk.ignis.design.server.productconfig.schema.validation.UpdateValidator;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SchemaServiceFixtureFactory {

    private final SchemaService schemaService;
    private final SchemaRepositoryFixture schemaRepository;
    private final SchemaConstraintsValidator schemaConstraintsValidator;
    private final UpdateValidator updateValidator;
    private final FieldService fieldService;
    private final DatasetSchemaFieldRepository updateFieldRepository;
    private final RuleService ruleService;

    public static SchemaServiceFixtureFactory create() {
        FieldServiceFactory fieldServiceFactory = FieldServiceFactory.create(x -> true);
        SchemaRepositoryFixture schemaRepository = new SchemaRepositoryFixture(fieldServiceFactory.getFieldService());

        UpdateValidator updateValidator = new UpdateValidator(schemaRepository);
        SchemaConstraintsValidator schemaConstraintsValidator = new SchemaConstraintsValidator(schemaRepository);

        FieldService fieldService = fieldServiceFactory.getFieldService();
        DatasetSchemaFieldRepository updateFieldRepository = fieldServiceFactory.getDatasetSchemaFieldRepository();

        RuleService ruleService = RuleServiceFactory.create(schemaRepository);

        SchemaService schemaService = new SchemaService(
                schemaRepository,
                fieldService,
                updateValidator,
                schemaConstraintsValidator
        );

        return new SchemaServiceFixtureFactory(
                schemaService,
                schemaRepository,
                schemaConstraintsValidator,
                updateValidator,
                fieldService,
                updateFieldRepository,
                ruleService);
    }
}
