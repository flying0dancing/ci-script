package com.lombardrisk.ignis.design.server.productconfig.schema.validation;

import com.lombardrisk.ignis.common.reservedWord.SchemaValidator;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.data.common.failure.ConstraintFailure;
import com.lombardrisk.ignis.design.server.productconfig.api.SchemaRepository;
import com.lombardrisk.ignis.design.server.productconfig.schema.Schema;
import com.lombardrisk.ignis.design.server.productconfig.schema.model.SchemaConstraints;
import io.vavr.control.Option;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.springframework.util.CollectionUtils.isEmpty;

public class SchemaConstraintsValidator {

    private final SchemaRepository schemaRepository;
    private final SchemaValidator schemaValidator = new SchemaValidator();

    public SchemaConstraintsValidator(
            final SchemaRepository schemaRepository) {
        this.schemaRepository = schemaRepository;
    }

    public List<CRUDFailure> validateCreate(final SchemaConstraints schemaConstraints) {
        String physicalTableName = schemaConstraints.getPhysicalTableName();
        Integer majorVersion = schemaConstraints.getMajorVersion();

        List<CRUDFailure> failures = new ArrayList<>();

        Option<CRUDFailure> checkPhysicalNameSyntax = checkPhysicalNameSyntax(physicalTableName);
        if (checkPhysicalNameSyntax.isDefined()) {
            failures.add(checkPhysicalNameSyntax.get());
        }

        Option<CRUDFailure> checkPhysicalName = checkPhysicalNameAndVersion(physicalTableName, majorVersion);
        if (checkPhysicalName.isDefined()) {
            failures.add(checkPhysicalName.get());
        }

        Option<CRUDFailure> checkDisplayName = checkDisplayName(schemaConstraints.getDisplayName(), majorVersion);
        if (checkDisplayName.isDefined()) {
            failures.add(checkDisplayName.get());
        }

        Option<CRUDFailure> checkStartEndDate =
                checkStartEndDate(schemaConstraints.getStartDate(), schemaConstraints.getEndDate());
        if (checkStartEndDate.isDefined()) {
            failures.add(checkStartEndDate.get());
        }

        return failures;
    }

    public Option<CRUDFailure> checkPhysicalNameSyntax(
            final String physicalTableName) {

        List<String> capturedReservedWords = schemaValidator.catchReservedWords(physicalTableName);

        if(!isEmpty(capturedReservedWords)) {
            ConstraintFailure constraintFailure = CRUDFailure.constraintFailure(
                    capturedReservedWords.get(0).toUpperCase() + " is a reserved word not allowed in a physical table name");
            return Option.of(constraintFailure);
        }
        return Option.none();
    }

    public Option<CRUDFailure> checkPhysicalNameAndVersion(
            final String physicalTableName,
            final Integer majorVersion) {

        Optional<Schema> byPhysicalTableName =
                schemaRepository.findByPhysicalTableNameAndVersion(physicalTableName, majorVersion);

        if (byPhysicalTableName.isPresent()) {
            ConstraintFailure constraintFailure = CRUDFailure.constraintFailure(
                    "Schema(s) exist for physical table name, [" + physicalTableName
                            + "] and version [" + majorVersion + "]");
            return Option.of(constraintFailure);
        }
        return Option.none();
    }

    public Option<CRUDFailure> checkDisplayName(final String displayName, final Integer majorVersion) {
        Optional<Schema> byPhysicalTableName = schemaRepository.findByDisplayNameAndVersion(displayName, majorVersion);

        if (byPhysicalTableName.isPresent()) {
            ConstraintFailure constraintFailure = CRUDFailure.constraintFailure(
                    "Schema(s) exist for display name, [" + displayName + "] and version [" + majorVersion + "]");

            return Option.of(constraintFailure);
        }
        return Option.none();
    }

    public Option<CRUDFailure> checkStartEndDate(final LocalDate startDate, final LocalDate endDate) {

        if (endDate != null && !startDate.isBefore(endDate)) {
            ConstraintFailure constraintFailure = CRUDFailure.constraintFailure(
                    "Schema end date [" + endDate + "] must be at least one day after start date, [" + startDate + "]");

            return Option.of(constraintFailure);
        }
        return Option.none();
    }
}
