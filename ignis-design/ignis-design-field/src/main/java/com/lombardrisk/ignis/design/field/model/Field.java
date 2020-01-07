package com.lombardrisk.ignis.design.field.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.lombardrisk.ignis.api.table.validation.ValidatableField;
import com.lombardrisk.ignis.data.common.Identifiable;
import com.lombardrisk.ignis.data.common.Nameable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = BooleanField.class, name = "boolean"),
        @JsonSubTypes.Type(value = DateField.class, name = "date"),
        @JsonSubTypes.Type(value = DoubleField.class, name = "double"),
        @JsonSubTypes.Type(value = FloatField.class, name = "float"),
        @JsonSubTypes.Type(value = IntField.class, name = "int"),
        @JsonSubTypes.Type(value = LongField.class, name = "long"),
        @JsonSubTypes.Type(value = DecimalField.class, name = "decimal"),
        @JsonSubTypes.Type(value = StringField.class, name = "string"),
        @JsonSubTypes.Type(value = TimestampField.class, name = "timestamp")
})
@JsonInclude(JsonInclude.Include.NON_NULL)
@javax.persistence.Table(name = "DATASET_SCHEMA_FIELD")
@DiscriminatorColumn(name = "FIELD_TYPE")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class Field<T extends Comparable> implements Serializable, Identifiable, Nameable, ValidatableField<T> {

    private static final long serialVersionUID = -4262889209132356990L;
    public static final String FIELD_NAME = "Field";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID", nullable = false, updatable = false)
    private Long id;

    @Column(name = "DATASET_SCHEMA_ID")
    private Long schemaId;

    @Column(name = "NAME")
    @NotNull
    @Size(min = 1, max = 50)
    @Pattern(
            regexp = "^[a-zA-Z0-9_]*$",
            message = "Name must not contain any special characters or spaces")
    private String name;

    @Column(name = "NULLABLE")
    private boolean nullable = true;

    public abstract Field<T> copy();

    public abstract T generateData();
}
