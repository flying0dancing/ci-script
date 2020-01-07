package com.lombardrisk.ignis.design.field;

import com.lombardrisk.ignis.design.field.model.BooleanField;
import com.lombardrisk.ignis.design.field.model.DateField;
import com.lombardrisk.ignis.design.field.model.DecimalField;
import com.lombardrisk.ignis.design.field.model.DoubleField;
import com.lombardrisk.ignis.design.field.model.FloatField;
import com.lombardrisk.ignis.design.field.model.IntField;
import com.lombardrisk.ignis.design.field.model.LongField;
import com.lombardrisk.ignis.design.field.model.StringField;
import com.lombardrisk.ignis.design.field.model.TimestampField;
import com.lombardrisk.ignis.design.field.request.FieldRequest;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DesignField {

    public static class Populated {

        public static final String DECIMAL_FIELD = "DECIMAL";
        public static final String DECIMAL_FIELD_EXPRESSION = DECIMAL_FIELD + " > 0";

        public static DecimalField.DecimalFieldBuilder decimalField(final String name) {
            return decimalField().name(name);
        }

        public static DecimalField.DecimalFieldBuilder decimalField() {
            return DecimalField.builder()
                    .name(DECIMAL_FIELD)
                    .nullable(false)
                    .precision(38)
                    .scale(2);
        }

        public static StringField.StringFieldBuilder stringField(final String name) {
            return stringField().name(name);
        }

        public static StringField.StringFieldBuilder stringField() {
            return StringField.builder()
                    .name("STRING")
                    .nullable(false)
                    .maxLength(38)
                    .minLength(2)
                    .regularExpression("[a-zA-Z0-9]");
        }

        public static BooleanField.BooleanFieldBuilder booleanField(final String name) {
            return booleanField().name(name);
        }

        public static BooleanField.BooleanFieldBuilder booleanField() {
            return BooleanField.builder()
                    .name("BOOLEAN")
                    .nullable(false);
        }

        public static LongField.LongFieldBuilder longField(final String name) {
            return longField().name(name);
        }

        public static LongField.LongFieldBuilder longField() {
            return LongField.builder()
                    .name("LONG")
                    .nullable(false);
        }

        public static IntField.IntFieldBuilder intField(final String name) {
            return intField().name(name);
        }

        public static IntField.IntFieldBuilder intField() {
            return IntField.builder()
                    .name("INT")
                    .nullable(false);
        }

        public static DoubleField.DoubleFieldBuilder doubleField(final String name) {
            return doubleField().name(name);
        }

        public static DoubleField.DoubleFieldBuilder doubleField() {
            return DoubleField.builder()
                    .name("DOUBLE")
                    .nullable(false);
        }

        public static FloatField.FloatFieldBuilder floatField(final String name) {
            return floatField().name(name);
        }

        public static FloatField.FloatFieldBuilder floatField() {
            return FloatField.builder()
                    .name("FLOAT")
                    .nullable(false);
        }

        public static DateField.DateFieldBuilder dateField(final String name) {
            return dateField().name(name);
        }

        public static DateField.DateFieldBuilder dateField() {
            return DateField.builder()
                    .name("DATE")
                    .nullable(false)
                    .format("dd/MM/yyyy");
        }

        public static TimestampField.TimestampFieldBuilder timestampField(final String name) {
            return timestampField().name(name);
        }

        public static TimestampField.TimestampFieldBuilder timestampField() {
            return TimestampField.builder()
                    .name("TIMESTAMP")
                    .nullable(false)
                    .format("dd/MM/yyyy");
        }

        public static FieldRequest.DecimalFieldRequest.DecimalFieldRequestBuilder decimalFieldRequest(final String name) {
            return decimalFieldRequest().name(name);
        }

        public static FieldRequest.DecimalFieldRequest.DecimalFieldRequestBuilder decimalFieldRequest() {
            return FieldRequest.DecimalFieldRequest.builder()
                    .name(DECIMAL_FIELD)
                    .nullable(false)
                    .precision(38)
                    .scale(2);
        }

        public static FieldRequest.StringFieldRequest.StringFieldRequestBuilder stringFieldRequest(final String name) {
            return stringFieldRequest().name(name);
        }

        public static FieldRequest.StringFieldRequest.StringFieldRequestBuilder stringFieldRequest() {
            return FieldRequest.StringFieldRequest.builder()
                    .name("STRING")
                    .nullable(false)
                    .maxLength(38)
                    .minLength(2)
                    .regularExpression("[a-zA-Z0-9]");
        }

        public static FieldRequest.BooleanFieldRequest.BooleanFieldRequestBuilder booleanFieldRequest(final String name) {
            return booleanFieldRequest().name(name);
        }

        public static FieldRequest.BooleanFieldRequest.BooleanFieldRequestBuilder booleanFieldRequest() {
            return FieldRequest.BooleanFieldRequest.builder()
                    .name("BOOLEAN")
                    .nullable(false);
        }

        public static FieldRequest.LongFieldRequest.LongFieldRequestBuilder longFieldRequest(final String name) {
            return longFieldRequest().name(name);
        }

        public static FieldRequest.LongFieldRequest.LongFieldRequestBuilder longFieldRequest() {
            return FieldRequest.LongFieldRequest.builder()
                    .name("LONG")
                    .nullable(false);
        }

        public static FieldRequest.IntegerFieldRequest.IntegerFieldRequestBuilder intFieldRequest(final String name) {
            return intFieldRequest().name(name);
        }

        public static FieldRequest.IntegerFieldRequest.IntegerFieldRequestBuilder intFieldRequest() {
            return FieldRequest.IntegerFieldRequest.builder()
                    .name("INT")
                    .nullable(false);
        }

        public static FieldRequest.DoubleFieldRequest.DoubleFieldRequestBuilder doubleFieldRequest(final String name) {
            return doubleFieldRequest().name(name);
        }

        public static FieldRequest.DoubleFieldRequest.DoubleFieldRequestBuilder doubleFieldRequest() {
            return FieldRequest.DoubleFieldRequest.builder()
                    .name("DOUBLE")
                    .nullable(false);
        }

        public static FieldRequest.FloatFieldRequest.FloatFieldRequestBuilder floatFieldRequest(final String name) {
            return floatFieldRequest().name(name);
        }

        public static FieldRequest.FloatFieldRequest.FloatFieldRequestBuilder floatFieldRequest() {
            return FieldRequest.FloatFieldRequest.builder()
                    .name("FLOAT")
                    .nullable(false);
        }

        public static FieldRequest.DateFieldRequest.DateFieldRequestBuilder dateFieldRequest(final String name) {
            return dateFieldRequest().name(name);
        }

        public static FieldRequest.DateFieldRequest.DateFieldRequestBuilder dateFieldRequest() {
            return FieldRequest.DateFieldRequest.builder()
                    .name("DATE")
                    .nullable(false)
                    .format("dd/MM/yyyy");
        }

        public static FieldRequest.TimestampFieldRequest.TimestampFieldRequestBuilder timestampFieldRequest(final String name) {
            return timestampFieldRequest().name(name);
        }

        public static FieldRequest.TimestampFieldRequest.TimestampFieldRequestBuilder timestampFieldRequest() {
            return FieldRequest.TimestampFieldRequest.builder()
                    .name("TIMESTAMP")
                    .nullable(false)
                    .format("dd/MM/yyyy");
        }
    }


}
