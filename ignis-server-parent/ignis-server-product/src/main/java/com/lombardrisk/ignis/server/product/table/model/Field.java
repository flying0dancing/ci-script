package com.lombardrisk.ignis.server.product.table.model;

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
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;

import static org.apache.commons.lang3.StringUtils.SPACE;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = BooleanField.class, name = FieldTypes.BOOLEAN),
        @JsonSubTypes.Type(value = DateField.class, name = FieldTypes.DATE),
        @JsonSubTypes.Type(value = DoubleField.class, name = FieldTypes.DOUBLE),
        @JsonSubTypes.Type(value = FloatField.class, name = FieldTypes.FLOAT),
        @JsonSubTypes.Type(value = IntField.class, name = FieldTypes.INT),
        @JsonSubTypes.Type(value = LongField.class, name = FieldTypes.LONG),
        @JsonSubTypes.Type(value = DecimalField.class, name = FieldTypes.DECIMAL),
        @JsonSubTypes.Type(value = StringField.class, name = FieldTypes.STRING),
        @JsonSubTypes.Type(value = TimestampField.class, name = FieldTypes.TIMESTAMP)
})
@JsonInclude(JsonInclude.Include.NON_NULL)
@Entity
@javax.persistence.Table(name = "DATASET_SCHEMA_FIELD")
@DiscriminatorColumn(name = "FIELD_TYPE")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class Field<T extends Comparable> implements Serializable, Identifiable, Nameable, ValidatableField<T> {

    private static final long serialVersionUID = -4262889209132356990L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "fieldIdGenerator")
    @SequenceGenerator(name = "fieldIdGenerator", sequenceName = "FIELD_ID_SEQUENCE")
    @Column(name = "ID", nullable = false, updatable = false)
    private Long id;

    @Column(name = "NAME")
    @NotNull
    @Size(min = 1, max = 50)
    @Pattern(
            regexp = "^[a-zA-Z0-9_]*$",
            message = "Name must not contain any special characters or spaces")
    private String name;

    @Column(name = "FIELD_TYPE", insertable = false, updatable = false)
    private String fieldType;

    @Column(name = "NULLABLE")
    private boolean nullable = true;

    public String toColumnDef() {
        return "\"" + getName() + "\"" + SPACE + toColumnType();
    }

    public abstract String toColumnType();
}
