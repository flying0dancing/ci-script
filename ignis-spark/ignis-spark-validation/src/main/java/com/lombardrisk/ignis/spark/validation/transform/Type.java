package com.lombardrisk.ignis.spark.validation.transform;

import lombok.Getter;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;

import java.text.ParseException;
import java.util.function.BiFunction;
import java.util.function.Function;

@Getter
public enum Type {
    INT(uncurried(Integer::parseInt), DataTypes.IntegerType),
    LONG(uncurried(Long::parseLong), DataTypes.LongType),
    FLOAT(uncurried(Float::valueOf), DataTypes.FloatType),
    DOUBLE(uncurried(Double::valueOf), DataTypes.DoubleType),
    DATE(Type::parseDate, DataTypes.DateType),
    TIMESTAMP(Type::parseDate, DataTypes.TimestampType),
    STRING(uncurried(String::toString), DataTypes.StringType),
    BOOLEAN(uncurried(Boolean::valueOf), DataTypes.BooleanType),
    NULL((string, type) -> null, DataTypes.NullType);

    private final BiFunction<String, FieldType, Object> parseFunction;
    private final DataType dataType;

    Type(final BiFunction<String, FieldType, Object> parseFunction, final DataType dataType) {
        this.parseFunction = parseFunction;
        this.dataType = dataType;
    }

    public Object parse(final String value, final FieldType fieldType) {
        return this.parseFunction.apply(value, fieldType);
    }

    private static Object parseDate(final String value, final FieldType fieldType) {
        try {
            return DateUtils.parseDate(value, fieldType.getFormat());
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static BiFunction<String, FieldType, Object> uncurried(final Function<String, Object> function) {
        return (string, fieldType) -> function.apply(string);
    }

}
