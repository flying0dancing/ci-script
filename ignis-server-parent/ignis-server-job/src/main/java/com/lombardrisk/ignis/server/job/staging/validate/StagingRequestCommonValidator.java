package com.lombardrisk.ignis.server.job.staging.validate;

import com.lombardrisk.ignis.common.stream.CollectorUtils;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.server.product.table.TableService;
import com.lombardrisk.ignis.server.product.table.model.Table;
import io.vavr.Function1;
import io.vavr.control.Option;
import io.vavr.control.Validation;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import static com.lombardrisk.ignis.data.common.failure.CRUDFailure.cannotFind;
import static java.util.stream.Collectors.toSet;

@AllArgsConstructor
public class StagingRequestCommonValidator {

    private final TableService tableService;

    <T> Validation<CRUDFailure, Set<String>> validateDuplicateStagingSchemas(
            final Collection<T> items, final Function1<T, String> toDisplayName) {

        Set<String> duplicateSchemas = items.stream()
                .map(toDisplayName)
                .collect(CollectorUtils.collectDuplicates());

        if (!duplicateSchemas.isEmpty()) {
            return Validation.invalid(
                    CRUDFailure.constraintFailure("Cannot stage duplicate schemas in the same request, "
                            + duplicateSchemas));
        }

        return Validation.valid(items.stream().map(toDisplayName).collect(toSet()));
    }

    Validation<CRUDFailure, Table> validateSchemaDisplayName(
            final String schemaDisplayName, final LocalDate referenceDate) {

        Optional<Table> optionalVersionedSchema = tableService.findTable(schemaDisplayName, referenceDate);

        return Option.ofOptional(optionalVersionedSchema)
                .toValid(cannotFind("any schema")
                        .with("display name", schemaDisplayName)
                        .with("period containing reference date", referenceDate)
                        .asFailure());
    }
}
