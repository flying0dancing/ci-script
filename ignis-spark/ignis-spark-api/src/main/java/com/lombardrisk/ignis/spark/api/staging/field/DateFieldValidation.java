package com.lombardrisk.ignis.spark.api.staging.field;

import com.lombardrisk.ignis.api.table.validation.ValidatableDateField;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.sql.Date;

@ToString(callSuper = true)
@NoArgsConstructor
@Getter
@EqualsAndHashCode(callSuper = true)
@Data
public class DateFieldValidation extends FieldValidation<Date> implements ValidatableDateField {

    private static final long serialVersionUID = -4879656234432828053L;
    private String format;

    @Builder
    public DateFieldValidation(final String name, final boolean nullable, final String format) {
        super(name, nullable);
        this.format = format;
    }
}
