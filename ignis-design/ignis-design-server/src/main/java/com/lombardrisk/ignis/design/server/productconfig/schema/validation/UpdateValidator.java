package com.lombardrisk.ignis.design.server.productconfig.schema.validation;

import com.lombardrisk.ignis.common.reservedWord.SchemaValidator;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.design.server.productconfig.api.SchemaRepository;
import com.lombardrisk.ignis.design.server.productconfig.schema.Schema;
import io.vavr.control.Validation;

import java.util.List;
import java.util.Optional;

import static org.springframework.util.CollectionUtils.isEmpty;

public class UpdateValidator {

    private final SchemaRepository schemaRepository;
    private final SchemaValidator schemaValidator = new SchemaValidator();

    public UpdateValidator(final SchemaRepository schemaRepository) {
        this.schemaRepository = schemaRepository;
    }

    public Validation<CRUDFailure, Schema> validateUpdateRequest(final Schema existingSchema, final Schema update) {
        List<String> capturedReservedWords = schemaValidator.catchReservedWords(update.getPhysicalTableName());
        if(!isEmpty(capturedReservedWords)) {
            return Validation.invalid(CRUDFailure.constraintFailure(
                    capturedReservedWords.get(0).toUpperCase() + " is a reserved word not allowed in a physical table name"));
        }

        if (update.getEndDate() != null && !update.getStartDate().isBefore(update.getEndDate())) {
            return Validation.invalid(CRUDFailure.invalidRequestParameter(
                    "startDate",
                    "Start date must be before end date"));
        }

        Optional<Schema> previousVersionResult = schemaRepository
                .findPreviousVersion(existingSchema.getDisplayName(), existingSchema.getMajorVersion());

        if (previousVersionResult
                .filter(prev -> prev.endsAfterOtherStarts(update))
                .isPresent()) {

            Schema previousSchema = previousVersionResult.get();
            return Validation.invalid(CRUDFailure.constraintFailure(
                    "Start date " + update.getStartDate() + " is before end date "
                            + previousSchema.getEndDate()
                            + " of previous version "
                            + previousSchema.getDisplayName()
                            + " v"
                            + previousSchema.getMajorVersion()));
        }

        Optional<Schema> nextVersionResult = schemaRepository
                .findNextVersion(existingSchema.getDisplayName(), existingSchema.getMajorVersion());

        //does updated end date
        if (nextVersionResult
                .filter(update::endsAfterOtherStarts)
                .isPresent()) {

            Schema next = nextVersionResult.get();
            return Validation.invalid(CRUDFailure.constraintFailure(
                    "End date "
                            + update.getEndDate()
                            + " is after start date of next schema version "
                            + next.getDisplayName()
                            + " v"
                            + next.getMajorVersion()
                            + " start date " + next.getStartDate()));
        }

        return Validation.valid(existingSchema);
    }
}
