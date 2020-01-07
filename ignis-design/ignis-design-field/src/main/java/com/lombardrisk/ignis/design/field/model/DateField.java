package com.lombardrisk.ignis.design.field.model;

import com.lombardrisk.ignis.api.table.validation.ValidatableDateField;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.RandomUtils;

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
    public DateField(final Long id, final Long schemaId, final String name, final boolean nullable, final String format) {
        super(id, schemaId, name, nullable);
        this.format = format;
    }

    @Override
    public DateField copy() {
        return DateField.builder()
                .name(getName())
                .nullable(isNullable())
                .format(format)
                .build();
    }

    @Override
    public Date generateData() {
        return new Date(RandomUtils.nextLong());
    }
}
