package com.lombardrisk.ignis.client.design.schema.field;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = FieldDto.StringFieldDto.class, name = "string"),
        @JsonSubTypes.Type(value = FieldDto.DateFieldDto.class, name = "date"),
        @JsonSubTypes.Type(value = FieldDto.TimestampFieldDto.class, name = "timestamp"),
        @JsonSubTypes.Type(value = FieldDto.DecimalFieldDto.class, name = "decimal"),
        @JsonSubTypes.Type(value = FieldDto.BooleanFieldDto.class, name = "boolean"),
        @JsonSubTypes.Type(value = FieldDto.DoubleFieldDto.class, name = "double"),
        @JsonSubTypes.Type(value = FieldDto.FloatFieldDto.class, name = "float"),
        @JsonSubTypes.Type(value = FieldDto.IntegerFieldDto.class, name = "int"),
        @JsonSubTypes.Type(value = FieldDto.LongFieldDto.class, name = "long"),
})
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FieldDto {

    private Long id;
    private String name;
    private boolean nullable;

    @NoArgsConstructor
    @ToString(callSuper = true)
    @Getter
    @EqualsAndHashCode(callSuper = true)
    public static class StringFieldDto extends FieldDto {

        private Integer maxLength;
        private Integer minLength;
        private String regularExpression;

        @Builder
        public StringFieldDto(
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
    }

    @ToString(callSuper = true)
    @NoArgsConstructor
    @Getter
    @EqualsAndHashCode(callSuper = true)
    public static class DecimalFieldDto extends FieldDto {

        private Integer scale;
        private Integer precision;

        @Builder
        public DecimalFieldDto(
                final Long id,
                final String name,
                final boolean nullable,
                final Integer scale,
                final Integer precision) {
            super(id, name, nullable);
            this.scale = scale;
            this.precision = precision;
        }
    }

    @ToString(callSuper = true)
    @NoArgsConstructor
    @Getter
    @EqualsAndHashCode(callSuper = true)
    public static class DateFieldDto extends FieldDto {

        private String format;

        @Builder
        public DateFieldDto(final Long id, final String name, final boolean nullable, final String format) {
            super(id, name, nullable);
            this.format = format;
        }
    }

    @ToString(callSuper = true)
    @NoArgsConstructor
    @Getter
    @EqualsAndHashCode(callSuper = true)
    public static class TimestampFieldDto extends FieldDto {

        private String format;

        @Builder
        public TimestampFieldDto(final Long id, final String name, final boolean nullable, final String format) {
            super(id, name, nullable);
            this.format = format;
        }
    }

    @ToString(callSuper = true)
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class BooleanFieldDto extends FieldDto {

        @Builder
        public BooleanFieldDto(final Long id, final String name, final boolean nullable) {
            super(id, name, nullable);
        }
    }

    @ToString(callSuper = true)
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class DoubleFieldDto extends FieldDto {

        @Builder
        public DoubleFieldDto(final Long id, final String name, final boolean nullable) {
            super(id, name, nullable);
        }
    }

    @ToString(callSuper = true)
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class IntegerFieldDto extends FieldDto {

        @Builder
        public IntegerFieldDto(final Long id, final String name, final boolean nullable) {
            super(id, name, nullable);
        }
    }

    @ToString(callSuper = true)
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class LongFieldDto extends FieldDto {

        @Builder
        public LongFieldDto(final Long id, final String name, final boolean nullable) {
            super(id, name, nullable);
        }
    }

    @ToString(callSuper = true)
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class FloatFieldDto extends FieldDto {

        @Builder
        public FloatFieldDto(final Long id, final String name, final boolean nullable) {
            super(id, name, nullable);
        }
    }
}
