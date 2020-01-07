package com.lombardrisk.ignis.design.field.fixtures;

import com.lombardrisk.ignis.design.field.FieldService;
import com.lombardrisk.ignis.design.field.api.DatasetSchemaFieldRepository;
import com.lombardrisk.ignis.design.field.api.FieldDependencyRepository;
import com.lombardrisk.ignis.design.field.api.FieldRepository;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.function.Predicate;

import static org.mockito.Mockito.mock;

@AllArgsConstructor
@Data
public class FieldServiceFactory {

    private final FieldService fieldService;
    private final FieldRepository fieldRepository;
    private final DatasetSchemaFieldRepository datasetSchemaFieldRepository;
    private final FieldDependencyRepository mockFieldDependencyRepository;

    public static FieldServiceFactory create(final Predicate<Long> schemaExists) {
        return create(schemaExists, mock(FieldDependencyRepository.class));
    }

    public static FieldServiceFactory create(
            final Predicate<Long> schemaExists,
            final FieldDependencyRepository fieldDependencyRepository) {
        FieldRepositoryFixture fieldRepository = new FieldRepositoryFixture(schemaExists);
        DatasetSchemaFieldRepositoryFixture updateFieldRepository =
                new DatasetSchemaFieldRepositoryFixture(fieldRepository);

        FieldService fieldService = new FieldService(fieldRepository, updateFieldRepository, fieldDependencyRepository);
        return new FieldServiceFactory(
                fieldService,
                fieldRepository,
                updateFieldRepository,
                fieldDependencyRepository);
    }
}
