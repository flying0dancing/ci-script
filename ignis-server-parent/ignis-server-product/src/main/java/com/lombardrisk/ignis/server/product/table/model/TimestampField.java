package com.lombardrisk.ignis.server.product.table.model;

import com.lombardrisk.ignis.api.table.validation.ValidatableTimestampField;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@DiscriminatorValue(value = "timestamp")
public class TimestampField extends Field<Timestamp> implements ValidatableTimestampField {

    private static final long serialVersionUID = 2697388469931481199L;

    @Column(name = "DATE_FORMAT")
    private String format;

    @Builder
    public TimestampField(final Long id, final String name, final boolean nullable, final String format) {
        super(id, name, FieldTypes.TIMESTAMP, nullable);
        this.format = format;
    }

    @Override
    public String toColumnType() {
        return "TIMESTAMP";
    }
}
