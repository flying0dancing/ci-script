package com.lombardrisk.ignis.server.controller.external.converter;

import com.lombardrisk.ignis.api.table.validation.ValidatableBooleanField;
import com.lombardrisk.ignis.client.external.productconfig.export.FieldExport;
import com.lombardrisk.ignis.server.product.table.model.BooleanField;
import com.lombardrisk.ignis.server.product.table.model.DateField;
import com.lombardrisk.ignis.server.product.table.model.DecimalField;
import com.lombardrisk.ignis.server.product.table.model.DoubleField;
import com.lombardrisk.ignis.server.product.table.model.Field;
import com.lombardrisk.ignis.server.product.table.model.FloatField;
import com.lombardrisk.ignis.server.product.table.model.IntField;
import com.lombardrisk.ignis.server.product.table.model.LongField;
import com.lombardrisk.ignis.server.product.table.model.StringField;
import com.lombardrisk.ignis.server.product.table.model.TimestampField;
import com.lombardrisk.ignis.server.product.table.view.FieldConverter;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class FieldConverterTest {

    private FieldConverter fieldConverter = new FieldConverter();

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

    private <T extends Field> T defaultField(final T field) {
        field.setId(12345L);
        field.setName("my " + field.getClass().getSimpleName());
        field.setNullable(true);
        return field;
    }

    private static final class DummyField extends Field<Boolean> implements ValidatableBooleanField {

        private static final long serialVersionUID = 9186109516983847421L;

        @Override
        public String toColumnType() {
            return null;
        }
    }
}
