package com.lombardrisk.ignis.design.server.productconfig.export.converter;

import com.lombardrisk.ignis.api.table.validation.ValidatableBooleanField;
import com.lombardrisk.ignis.client.external.productconfig.export.FieldExport;
import com.lombardrisk.ignis.design.field.DesignField;
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
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class FieldExportConverterTest {

    private FieldExportConverter fieldConverter = new FieldExportConverter();

    @Test
    public void apply_IntField_ConvertsFieldToFieldView() {
        IntField field = defaultField(new IntField());

        FieldExport view = fieldConverter.apply(field);

        assertThat(view).isEqualTo(FieldExport.IntegerFieldExport.builder()
                .id(12345L)
                .name("my IntField")
                .nullable(true)
                .build());
    }

    @Test
    public void apply_LongField_ConvertsFieldToFieldView() {
        LongField field = defaultField(new LongField());

        FieldExport view = fieldConverter.apply(field);

        assertThat(view).isEqualTo(FieldExport.LongFieldExport.builder()
                .id(12345L)
                .name("my LongField")
                .nullable(true)
                .build());
    }

    @Test
    public void apply_DoubleField_ConvertsFieldToFieldView() {
        DoubleField field = defaultField(new DoubleField());

        FieldExport view = fieldConverter.apply(field);

        assertThat(view).isEqualTo(FieldExport.DoubleFieldExport.builder()
                .id(12345L)
                .name("my DoubleField")
                .nullable(true)
                .build());
    }

    @Test
    public void apply_FloatField_ConvertsFieldToFieldView() {
        FloatField field = defaultField(new FloatField());

        FieldExport view = fieldConverter.apply(field);

        assertThat(view).isEqualTo(FieldExport.FloatFieldExport.builder()
                .id(12345L)
                .name("my FloatField")
                .nullable(true)
                .build());
    }

    @Test
    public void apply_DecimalField_ConvertsFieldToFieldView() {
        DecimalField field = defaultField(new DecimalField());
        field.setScale(5);
        field.setPrecision(38);

        FieldExport view = fieldConverter.apply(field);

        assertThat(view).isEqualTo(FieldExport.DecimalFieldExport.builder()
                .id(12345L)
                .name("my DecimalField")
                .nullable(true)
                .scale(5)
                .precision(38)
                .build());
    }

    @Test
    public void apply_StringField_ConvertsFieldToFieldView() {
        StringField field = defaultField(new StringField());
        field.setMaxLength(10);
        field.setMinLength(5);
        field.setRegularExpression("[a-zA-Z]+");

        FieldExport view = fieldConverter.apply(field);

        assertThat(view).isEqualTo(FieldExport.StringFieldExport.builder()
                .id(12345L)
                .name("my StringField")
                .nullable(true)
                .maxLength(10)
                .minLength(5)
                .regularExpression("[a-zA-Z]+")
                .build());
    }

    @Test
    public void apply_DateField_ConvertsFieldToFieldView() {
        DateField field = defaultField(new DateField());
        field.setFormat("dd/MM/yyyy");

        FieldExport view = fieldConverter.apply(field);

        assertThat(view).isEqualTo(FieldExport.DateFieldExport.builder()
                .id(12345L)
                .name("my DateField")
                .nullable(true)
                .format("dd/MM/yyyy")
                .build());
    }

    @Test
    public void apply_TimestampField_ConvertsFieldToFieldView() {
        TimestampField field = defaultField(new TimestampField());
        field.setFormat("dd/MM/yyyy");

        FieldExport view = fieldConverter.apply(field);

        assertThat(view).isEqualTo(FieldExport.TimestampFieldExport.builder()
                .id(12345L)
                .name("my TimestampField")
                .nullable(true)
                .format("dd/MM/yyyy")
                .build());
    }

    @Test
    public void apply_BooleanField_ConvertsFieldToFieldView() {
        BooleanField field = defaultField(new BooleanField());

        FieldExport view = fieldConverter.apply(field);

        assertThat(view).isEqualTo(FieldExport.BooleanFieldExport.builder()
                .id(12345L)
                .name("my BooleanField")
                .nullable(true)
                .build());
    }

    @Test
    public void apply_DummyField_ThrowsIllegalArgumentException() {
        DummyField field = new DummyField();

        assertThatThrownBy(() -> fieldConverter.apply(field))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Field [DummyField] is not of a recognised type");
    }

    @Test
    public void inverse_IntField_ConvertsBackToOriginal() {
        IntField original = DesignField.Populated.intField().build();

        FieldExport export = fieldConverter.apply(original);
        Field reconverted = fieldConverter.inverse().apply(export);

        assertThat(reconverted).isEqualTo(original);
    }

    @Test
    public void inverse_LongField_ConvertsBackToOriginal() {
        LongField original = DesignField.Populated.longField().build();

        FieldExport export = fieldConverter.apply(original);
        Field reconverted = fieldConverter.inverse().apply(export);

        assertThat(reconverted).isEqualTo(original);
    }

    @Test
    public void inverse_DoubleField_ConvertsBackToOriginal() {
        DoubleField original = DesignField.Populated.doubleField().build();

        FieldExport export = fieldConverter.apply(original);
        Field reconverted = fieldConverter.inverse().apply(export);

        assertThat(reconverted).isEqualTo(original);
    }

    @Test
    public void inverse_FloatField_ConvertsBackToOriginal() {
        FloatField original = DesignField.Populated.floatField().build();

        FieldExport export = fieldConverter.apply(original);
        Field reconverted = fieldConverter.inverse().apply(export);

        assertThat(reconverted).isEqualTo(original);
    }

    @Test
    public void inverse_DecimalField_ConvertsBackToOriginal() {
        DecimalField original = DesignField.Populated.decimalField().build();

        FieldExport export = fieldConverter.apply(original);
        Field reconverted = fieldConverter.inverse().apply(export);

        assertThat(reconverted).isEqualTo(original);
    }

    @Test
    public void inverse_StringField_ConvertsBackToOriginal() {
        StringField original = DesignField.Populated.stringField().build();

        FieldExport export = fieldConverter.apply(original);
        Field reconverted = fieldConverter.inverse().apply(export);

        assertThat(reconverted).isEqualTo(original);

    }

    @Test
    public void inverse_DateField_ConvertsBackToOriginal() {
        DateField original = DesignField.Populated.dateField().build();

        FieldExport export = fieldConverter.apply(original);
        Field reconverted = fieldConverter.inverse().apply(export);

        assertThat(reconverted).isEqualTo(original);
    }

    @Test
    public void inverse_TimestampField_ConvertsBackToOriginal() {
        TimestampField original = DesignField.Populated.timestampField().build();

        FieldExport export = fieldConverter.apply(original);
        Field reconverted = fieldConverter.inverse().apply(export);

        assertThat(reconverted).isEqualTo(original);
    }

    @Test
    public void inverse_BooleanField_ConvertsBackToOriginal() {
        BooleanField original = DesignField.Populated.booleanField().build();

        FieldExport export = fieldConverter.apply(original);
        Field reconverted = fieldConverter.inverse().apply(export);

        assertThat(reconverted).isEqualTo(original);
    }

    @Test
    public void inverse_DummyField_ThrowsIllegalArgumentException() {
        DummyFieldExport field = new DummyFieldExport();

        assertThatThrownBy(() -> fieldConverter.inverse().apply(field))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Field [DummyFieldExport] is not of a recognised type");
    }

    private <T extends Field> T defaultField(final T field) {
        field.setId(12345L);
        field.setName("my " + field.getClass().getSimpleName());
        field.setNullable(true);
        return field;
    }

    private static final class DummyField extends Field<Boolean> implements ValidatableBooleanField {

        private static final long serialVersionUID = 7652513023831415878L;

        @Override
        public Field<Boolean> copy() {
            return null;
        }

        @Override
        public Boolean generateData() {
            return null;
        }
    }

    private static final class DummyFieldExport extends FieldExport {

    }
}
