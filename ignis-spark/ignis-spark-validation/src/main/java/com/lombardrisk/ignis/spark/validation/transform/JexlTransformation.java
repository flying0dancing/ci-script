package com.lombardrisk.ignis.spark.validation.transform;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.vavr.Function1;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@NoArgsConstructor
public final class JexlTransformation<T> extends Field {

    private static final long serialVersionUID = -6180641234164298285L;

    private FieldType fieldType;
    private Function1<Object, T> javaParseResultFunction;

    @Builder
    private JexlTransformation(
            final @NonNull String name,
            final String as,
            final @NonNull FieldType fieldType,
            final @NonNull Function1<Object, T> javaParseResultFunction) {
        super(name, as);
        this.fieldType = fieldType;
        this.javaParseResultFunction = javaParseResultFunction;
    }

    public T parse(final Object object) {
        return javaParseResultFunction.apply(object);
    }
}