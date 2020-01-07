package com.lombardrisk.ignis.spark.validation.transform;

import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.ClassUtils;

import java.io.Serializable;

@Getter
@ToString
public class FieldType implements Serializable {

    private static final long serialVersionUID = -6603737155987672290L;
    public static final FieldType STRING = FieldType.of(Type.STRING);
    public static final FieldType INT = FieldType.of(Type.INT);
    public static final FieldType LONG = FieldType.of(Type.LONG);
    public static final FieldType FLOAT = FieldType.of(Type.FLOAT);
    public static final FieldType DOUBLE = FieldType.of(Type.DOUBLE);
    public static final FieldType TIMESTAMP = FieldType.of(Type.TIMESTAMP);
    public static final FieldType BOOLEAN = FieldType.of(Type.BOOLEAN);
    public static final FieldType NULL = FieldType.of(Type.NULL);

    public static FieldType date(final String format) {
        return FieldType.of(Type.DATE, format);
    }

    private Type type;
    private String format;

    public static FieldType of(final Type datasetFieldType) {
        FieldType type = new FieldType();
        type.type = datasetFieldType;
        return type;
    }

    public static FieldType of(final Type datasetFieldType, final String format) {
        FieldType type = new FieldType();
        type.type = datasetFieldType;
        type.format = format;
        return type;
    }

    public Object parse(final Object value) {
        if (value == null) {
            return null;
        }

        if (ClassUtils.isPrimitiveOrWrapper(value.getClass())) {
            return value;
        }

        return type.parse(value.toString(), this);
    }
}