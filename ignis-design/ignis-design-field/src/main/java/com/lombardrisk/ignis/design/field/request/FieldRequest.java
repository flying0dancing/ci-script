package com.lombardrisk.ignis.design.field.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.lombardrisk.ignis.design.field.model.BooleanField;
import com.lombardrisk.ignis.design.field.model.DateField;
import com.lombardrisk.ignis.design.field.model.DecimalField;
import com.lombardrisk.ignis.design.field.model.DoubleField;
import com.lombardrisk.ignis.design.field.model.Field;
import com.lombardrisk.ignis.design.field.model.FloatField;
import com.lombardrisk.ignis.design.field.model.IntField;
import com.lombardrisk.ignis.design.field.model.LongField;
import com.lombardrisk.ignis.design.field.model.StringField;
import com.lombardrisk.ignis.design.field.model.TimestampField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = FieldRequest.StringFieldRequest.class, name = "string"),
        @JsonSubTypes.Type(value = FieldRequest.DateFieldRequest.class, name = "date"),
        @JsonSubTypes.Type(value = FieldRequest.TimestampFieldRequest.class, name = "timestamp"),
        @JsonSubTypes.Type(value = FieldRequest.DecimalFieldRequest.class, name = "decimal"),
        @JsonSubTypes.Type(value = FieldRequest.BooleanFieldRequest.class, name = "boolean"),
        @JsonSubTypes.Type(value = FieldRequest.DoubleFieldRequest.class, name = "double"),
        @JsonSubTypes.Type(value = FieldRequest.FloatFieldRequest.class, name = "float"),
        @JsonSubTypes.Type(value = FieldRequest.IntegerFieldRequest.class, name = "int"),
        @JsonSubTypes.Type(value = FieldRequest.LongFieldRequest.class, name = "long"),
})
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class FieldRequest<T extends Comparable> {

    private Long id;
    @NotBlank
    private String name;
    private boolean nullable;

    public abstract Field<T> convert();

    @NoArgsConstructor
    @ToString(callSuper = true)
    @Getter
    @EqualsAndHashCode(callSuper = true)
    public static class StringFieldRequest extends FieldRequest<String> {

        private Integer maxLength;
        private Integer minLength;
        private String regularExpression;

        @Builder
        public StringFieldRequest(
                final Long id,
                final String name,
                final boolean nullable,
                final Integer maxLength,
                final Integer minLength,
                final String regularExpression) {
            super(id, name, nullable);
            this.maxLength = maxLength;
            this.minLength = minLength;
            this.regularExpression = regularExpression;
        }

        @Override
        public StringField convert() {
            return StringField.builder()
                    .id(getId())
                    .name(getName())
                    .nullable(isNullable())
                    .regularExpression(regularExpression)
                    .minLength(minLength)
                    .maxLength(maxLength)
                    .build();
        }
    }

    @ToString(callSuper = true)
    @NoArgsConstructor
    @Getter
    @EqualsAndHashCode(callSuper = true)
    public static class DecimalFieldRequest extends FieldRequest<BigDecimal> {

        private Integer scale;
        private Integer precision;

        @Builder
        public DecimalFieldRequest(
                final Long id,
                final String name,
                final boolean nullable,
                final Integer scale,
                final Integer precision) {
            super(id, name, nullable);
            this.scale = scale;
            this.precision = precision;
        }

        @Override
        public DecimalField convert() {
            return DecimalField.builder()
                    .id(getId())
                    .name(getName())
                    .nullable(isNullable())
                    .scale(scale)
                    .precision(precision)
                    .build();
        }
    }

    @ToString(callSuper = true)
    @NoArgsConstructor
    @Getter
    @EqualsAndHashCode(callSuper = true)
    public static class DateFieldRequest extends FieldRequest<Date> {

        private String format;

        @Builder
        public DateFieldRequest(
                final Long id,
                final String name,
                final boolean nullable,
                final String format) {
            super(id, name, nullable);
            this.format = format;
        }

        @Override
        public DateField convert() {
            return DateField.builder()
                    .id(getId())
                    .name(getName())
                    .nullable(isNullable())
                    .format(format)
                    .build();
        }
    }

    @ToString(callSuper = true)
    @NoArgsConstructor
    @Getter
    @EqualsAndHashCode(callSuper = true)
    public static class TimestampFieldRequest extends FieldRequest<Timestamp> {

        private String format;

        @Builder
        public TimestampFieldRequest(
                final Long id,
                final String name,
                final boolean nullable,
                final String format) {
            super(id, name, nullable);
            this.format = format;
        }

        @Override
        public TimestampField convert() {
            return TimestampField.builder()
                    .id(getId())
                    .name(getName())
                    .nullable(isNullable())
                    .format(format)
                    .build();
        }
    }

    @ToString(callSuper = true)
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class BooleanFieldRequest extends FieldRequest<Boolean> {

        @Builder
        public BooleanFieldRequest(
                final Long id,
                final String name,
                final boolean nullable) {
            super(id, name, nullable);
        }

        @Override
        public BooleanField convert() {
            return BooleanField.builder()
                    .id(getId())
                    .name(getName())
                    .nullable(isNullable())
                    .build();
        }
    }

    @ToString(callSuper = true)
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class DoubleFieldRequest extends FieldRequest<Double> {

        @Builder
        public DoubleFieldRequest(
                final Long id,
                final String name,
                final boolean nullable) {
            super(id, name, nullable);
        }

        @Override
        public DoubleField convert() {
            return DoubleField.builder()
                    .id(getId())
                    .name(getName())
                    .nullable(isNullable())
                    .build();
        }
    }

    @ToString(callSuper = true)
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class IntegerFieldRequest extends FieldRequest<Integer> {

        @Builder
        public IntegerFieldRequest(
                final Long id,
                final String name,
                final boolean nullable) {
            super(id, name, nullable);
        }

        @Override
        public IntField convert() {
            return IntField.builder()
                    .id(getId())
                    .name(getName())
                    .nullable(isNullable())
                    .build();
        }
    }

    @ToString(callSuper = true)
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class LongFieldRequest extends FieldRequest<Long> {

        @Builder
        public LongFieldRequest(
                final Long id,
                final String name,
                final boolean nullable) {
            super(id, name, nullable);
        }

        @Override
        public LongField convert() {
            return LongField.builder()
                    .id(getId())
                    .name(getName())
                    .nullable(isNullable())
                    .build();
        }
    }

    @ToString(callSuper = true)
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class FloatFieldRequest extends FieldRequest<Float> {

        @Builder
        public FloatFieldRequest(
                final Long id,
                final String name,
                final boolean nullable) {
            super(id, name, nullable);
        }

        @Override
        public FloatField convert() {
            return FloatField.builder()
                    .id(getId())
                    .name(getName())
                    .nullable(isNullable())
                    .build();
        }
    }
}
