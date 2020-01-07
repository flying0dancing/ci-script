package com.lombardrisk.ignis.server.product.table.model;

import com.lombardrisk.ignis.api.table.validation.ValidatableDateField;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.sql.Date;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@DiscriminatorValue(value = "date")
public class DateField extends Field<Date> implements ValidatableDateField {

    private static final long serialVersionUID = 6023779142767106643L;
    @Column(name = "DATE_FORMAT")
    private String format;

    @Builder
    public DateField(final Long id, final String name, final boolean nullable, final String format) {
        super(id, name, FieldTypes.DATE, nullable);
        this.format = format;
    }

    @Override
    public String toColumnType() {
        return "DATE";
    }
}
