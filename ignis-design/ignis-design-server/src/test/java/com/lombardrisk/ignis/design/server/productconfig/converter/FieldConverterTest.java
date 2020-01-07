package com.lombardrisk.ignis.design.server.productconfig.converter;

import com.lombardrisk.ignis.api.table.validation.ValidatableBooleanField;
import com.lombardrisk.ignis.client.design.schema.field.FieldDto;
import com.lombardrisk.ignis.design.field.FieldConverter;
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

public class FieldConverterTest {

    private FieldConverter fieldConverter = new FieldConverter();

    @Test
    public void apply_IntField_ConvertsFieldToFieldDto() {
        IntField field = defaultField(new IntField());

        FieldDto view = fieldConverter.apply(field);

        assertThat(view).isEqualTo(FieldDto.IntegerFieldDto.builder()
                .id(12345L)
                .name("my IntField")
                .nullable(true)
                .build());
    }

    @Test
    public void apply_LongField_ConvertsFieldToFieldDto() {
        LongField field = defaultField(new LongField());

        FieldDto view = fieldConverter.apply(field);

        assertThat(view).isEqualTo(FieldDto.LongFieldDto.builder()
                .id(12345L)
                .name("my LongField")
                .nullable(true)
                .build());
    }

    @Test
    public void apply_DoubleField_ConvertsFieldToFieldDto() {
        DoubleField field = defaultField(new DoubleField());

        FieldDto view = fieldConverter.apply(field);

        assertThat(view).isEqualTo(FieldDto.DoubleFieldDto.builder()
                .id(12345L)
                .name("my DoubleField")
                .nullable(true)
                .build());
    }

    @Test
    public void apply_FloatField_ConvertsFieldToFieldDto() {
        FloatField field = defaultField(new FloatField());

        FieldDto view = fieldConverter.apply(field);

        assertThat(view).isEqualTo(FieldDto.FloatFieldDto.builder()
                .id(12345L)
                .name("my FloatField")
                .nullable(true)
                .build());
    }

    @Test
    public void apply_DecimalField_ConvertsFieldToFieldDto() {
        DecimalField field = defaultField(new DecimalField());
        field.setScale(5);
        field.setPrecision(38);

        FieldDto view = fieldConverter.apply(field);

        assertThat(view).isEqualTo(FieldDto.DecimalFieldDto.builder()
                .id(12345L)
                .name("my DecimalField")
                .nullable(true)
                .scale(5)
                .precision(38)
                .build());
    }

    @Test
    public void apply_StringField_ConvertsFieldToFieldDto() {
        StringField field = defaultField(new StringField());
        field.setMaxLength(10);
        field.setMinLength(5);
        field.setRegularExpression("[a-zA-Z]+");

        FieldDto view = fieldConverter.apply(field);

        assertThat(view).isEqualTo(FieldDto.StringFieldDto.builder()
                .id(12345L)
                .name("my StringField")
                .nullable(true)
                .maxLength(10)
                .minLength(5)
                .regularExpression("[a-zA-Z]+")
                .build());
    }

    @Test
    public void apply_DateField_ConvertsFieldToFieldDto() {
        DateField field = defaultField(new DateField());
        field.setFormat("dd/MM/yyyy");

        FieldDto view = fieldConverter.apply(field);

        assertThat(view).isEqualTo(FieldDto.DateFieldDto.builder()
                .id(12345L)
                .name("my DateField")
                .nullable(true)
                .format("dd/MM/yyyy")
                .build());
    }

    @Test
    public void apply_TimestampField_ConvertsFieldToFieldDto() {
        TimestampField field = defaultField(new TimestampField());
        field.setFormat("dd/MM/yyyy");

        FieldDto view = fieldConverter.apply(field);

        assertThat(view).isEqualTo(FieldDto.TimestampFieldDto.builder()
                .id(12345L)
                .name("my TimestampField")
                .nullable(true)
                .format("dd/MM/yyyy")
                .build());
    }

    @Test
    public void apply_BooleanField_ConvertsFieldToFieldDto() {
        BooleanField field = defaultField(new BooleanField());

        FieldDto view = fieldConverter.apply(field);

        assertThat(view).isEqualTo(FieldDto.BooleanFieldDto.builder()
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
}
