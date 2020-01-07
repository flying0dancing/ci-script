package com.lombardrisk.ignis.spark.api.staging.field;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.lombardrisk.ignis.api.table.validation.ValidatableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = StringFieldValidation.class, name = "string"),
        @JsonSubTypes.Type(value = DateFieldValidation.class, name = "date"),
        @JsonSubTypes.Type(value = TimestampFieldValidation.class, name = "timestamp"),
        @JsonSubTypes.Type(value = DecimalFieldValidation.class, name = "decimal"),
        @JsonSubTypes.Type(value = BooleanFieldValidation.class, name = "boolean"),
        @JsonSubTypes.Type(value = DoubleFieldValidation.class, name = "double"),
        @JsonSubTypes.Type(value = FloatFieldValidation.class, name = "float"),
        @JsonSubTypes.Type(value = IntegerFieldValidation.class, name = "int"),
        @JsonSubTypes.Type(value = LongFieldValidation.class, name = "long"),
})
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class FieldValidation<T extends Comparable> implements ValidatableField<T>, Serializable {

    private static final long serialVersionUID = 8296632932160390019L;
    private String name;
    private boolean nullable;
}
