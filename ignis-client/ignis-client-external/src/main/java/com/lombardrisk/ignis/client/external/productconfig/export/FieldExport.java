package com.lombardrisk.ignis.client.external.productconfig.export;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = FieldExport.StringFieldExport.class, name = FieldTypes.STRING),
        @JsonSubTypes.Type(value = FieldExport.DateFieldExport.class, name = FieldTypes.DATE),
        @JsonSubTypes.Type(value = FieldExport.TimestampFieldExport.class, name = FieldTypes.TIMESTAMP),
        @JsonSubTypes.Type(value = FieldExport.DecimalFieldExport.class, name = FieldTypes.DECIMAL),
        @JsonSubTypes.Type(value = FieldExport.BooleanFieldExport.class, name = FieldTypes.BOOLEAN),
        @JsonSubTypes.Type(value = FieldExport.DoubleFieldExport.class, name = FieldTypes.DOUBLE),
        @JsonSubTypes.Type(value = FieldExport.FloatFieldExport.class, name = FieldTypes.FLOAT),
        @JsonSubTypes.Type(value = FieldExport.IntegerFieldExport.class, name = FieldTypes.INT),
        @JsonSubTypes.Type(value = FieldExport.LongFieldExport.class, name = FieldTypes.LONG),
})
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class FieldExport {

    private Long id;
    private String name;
    private boolean nullable;

    @NoArgsConstructor
    @ToString(callSuper = true)
    @Getter
    public static class StringFieldExport extends FieldExport {

        private Integer maxLength;
        private Integer minLength;
        private String regularExpression;

        @Builder
        public StringFieldExport(
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
    public static class DecimalFieldExport extends FieldExport {

        private Integer scale;
        private Integer precision;

        @Builder
        public DecimalFieldExport(
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
    public static class DateFieldExport extends FieldExport {

        private String format;

        @Builder
        public DateFieldExport(final Long id, final String name, final boolean nullable, final String format) {
            super(id, name, nullable);
            this.format = format;
        }
    }

    @ToString(callSuper = true)
    @NoArgsConstructor
    @Getter
    public static class TimestampFieldExport extends FieldExport {

        private String format;

        @Builder
        public TimestampFieldExport(final Long id, final String name, final boolean nullable, final String format) {
            super(id, name, nullable);
            this.format = format;
        }
    }

    @ToString(callSuper = true)
    @NoArgsConstructor
    public static class BooleanFieldExport extends FieldExport {

        @Builder
        public BooleanFieldExport(final Long id, final String name, final boolean nullable) {
            super(id, name, nullable);
        }
    }

    @ToString(callSuper = true)
    @NoArgsConstructor
    public static class DoubleFieldExport extends FieldExport {

        @Builder
        public DoubleFieldExport(final Long id, final String name, final boolean nullable) {
            super(id, name, nullable);
        }
    }

    @ToString(callSuper = true)
    @NoArgsConstructor
    public static class IntegerFieldExport extends FieldExport {

        @Builder
        public IntegerFieldExport(final Long id, final String name, final boolean nullable) {
            super(id, name, nullable);
        }
    }

    @ToString(callSuper = true)
    @NoArgsConstructor
    public static class LongFieldExport extends FieldExport {

        @Builder
        public LongFieldExport(final Long id, final String name, final boolean nullable) {
            super(id, name, nullable);
        }
    }

    @ToString(callSuper = true)
    @NoArgsConstructor
    public static class FloatFieldExport extends FieldExport {

        @Builder
        public FloatFieldExport(final Long id, final String name, final boolean nullable) {
            super(id, name, nullable);
        }
    }
}
