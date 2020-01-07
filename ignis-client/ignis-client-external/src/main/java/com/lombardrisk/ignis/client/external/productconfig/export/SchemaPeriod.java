package com.lombardrisk.ignis.client.external.productconfig.export;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import com.lombardrisk.ignis.common.json.LocalDateDeserializer;
import com.lombardrisk.ignis.common.json.LocalDateSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@Data
@NoArgsConstructor
public final class SchemaPeriod {

    private static final LocalDate EPOCH_MIN = LocalDate.of(1970, 1, 1);

    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate startDate;

    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate endDate;

    private SchemaPeriod(final LocalDate startDate, final LocalDate endDate) {
        this.startDate = requireNonNull(startDate);
        this.endDate = endDate;

        if (endDate != null) {
            Preconditions.checkArgument(
                    startDate.isBefore(endDate),
                    "Invalid schema period: start date %s must be before end date %s", startDate, endDate);
        }
    }

    public static SchemaPeriod between(@NotNull final LocalDate startDate, final LocalDate endDate) {
        return new SchemaPeriod(startDate, endDate);
    }

    public static SchemaPeriod max() {
        return SchemaPeriod.between(EPOCH_MIN, null);
    }

    public boolean isDifferent(final SchemaPeriod other) {
        boolean differentStartDate = startDate.compareTo(requireNonNull(other).startDate) != 0;

        boolean differentEndDate;
        if (endDate != null) {
            if (other.endDate != null) {
                differentEndDate = endDate.compareTo(other.endDate) != 0;
            } else {
                differentEndDate = true;
            }
        } else {
            differentEndDate = other.endDate != null;
        }
        return differentStartDate || differentEndDate;
    }

    @Override
    public String toString() {
        String endDateString = endDate == null ? EMPTY : endDate.format(ISO_LOCAL_DATE);
        return startDate.format(ISO_LOCAL_DATE) + ":" + endDateString;
    }
}
