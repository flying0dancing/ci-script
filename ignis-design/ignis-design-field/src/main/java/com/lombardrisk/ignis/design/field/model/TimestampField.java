package com.lombardrisk.ignis.design.field.model;

import com.lombardrisk.ignis.api.table.validation.ValidatableTimestampField;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.RandomUtils;

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
    public TimestampField(
            final Long id,
            final Long schemaId,
            final String name,
            final boolean nullable,
            final String format) {
        super(id, schemaId, name, nullable);
        this.format = format;
    }

    @Override
    public TimestampField copy() {
        return TimestampField.builder()
                .name(getName())
                .nullable(isNullable())
                .format(this.format)
                .build();
    }

    @Override
    public Timestamp generateData() {
        return new Timestamp(RandomUtils.nextLong());
    }
}
