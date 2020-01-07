package com.lombardrisk.ignis.design.server.productconfig.schema.validation;

import com.lombardrisk.ignis.client.design.schema.NewSchemaVersionRequest;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.design.server.productconfig.schema.Schema;
import io.vavr.control.Option;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class CreateNewVersionValidator {

    @SuppressWarnings("ConstantConditions") //In older versions of design studio existing start date can be null
    public Option<CRUDFailure> validateNewVersion(
            final Schema existingSchema, final NewSchemaVersionRequest newSchemaVersionRequest) {
        LocalDate existingStartDate = existingSchema.getStartDate();

        String newStartDate = newSchemaVersionRequest.getStartDate()
                .format(DateTimeFormatter.ISO_LOCAL_DATE);

        if (existingStartDate != null) {

            if (existingStartDate.equals(newSchemaVersionRequest.getStartDate()) || existingStartDate.isAfter(
                    newSchemaVersionRequest.getStartDate())) {
                String previousStartDate = existingStartDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
                String failureMessage = "New start date '" + newStartDate + "' "
                        + "must be after latest versions's start date '" + previousStartDate + "'";

                return Option.of(
                        CRUDFailure.invalidParameters()
                                .paramError("start date", failureMessage)
                                .asFailure());
            }

            if (newSchemaVersionRequest.getStartDate().minusDays(1)
                    .isEqual(existingStartDate)) {

                return Option.of(CRUDFailure.invalidRequestParameter(
                        "startDate",
                        "Start date results in previous schema version have same start and end date"));
            }
        }

        return Option.none();
    }
}
