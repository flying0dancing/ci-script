package com.lombardrisk.ignis.spark.api.staging.field;

import com.lombardrisk.ignis.api.table.validation.ValidatableTimestampField;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.sql.Timestamp;

@ToString(callSuper = true)
@NoArgsConstructor
@Getter
@EqualsAndHashCode(callSuper = true)
@Data
public class TimestampFieldValidation extends FieldValidation<Timestamp> implements ValidatableTimestampField {

    private static final long serialVersionUID = 4045803133539131029L;
    private String format;

    @Builder
    public TimestampFieldValidation(final String name, final boolean nullable, final String format) {
        super(name, nullable);
        this.format = format;
    }
}
