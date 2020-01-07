package com.lombardrisk.ignis.server.controller.external.converter.request;

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
import com.lombardrisk.ignis.server.product.table.view.FieldExportConverter;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class FieldExportConverterTest {

    private FieldExportConverter fieldExportConverter = new FieldExportConverter();

    @Test
    public void apply_IntegerFieldView_ConvertsFieldViewToField() {
        FieldExport.IntegerFieldExport view = defaultFieldWithId(new FieldExport.IntegerFieldExport());

        Field field = fieldExportConverter.apply(view);

        assertThat(field).isEqualTo(IntField.builder()
                .name("my IntegerFieldExport")
                .nullable(true)
                .build());
    }

    @Test
    public void apply_LongFieldView_ConvertsFieldViewToField() {
        FieldExport.LongFieldExport view = defaultFieldWithId(new FieldExport.LongFieldExport());

        Field field = fieldExportConverter.apply(view);

        assertThat(field).isEqualTo(LongField.builder()
                .name("my LongFieldExport")
                .nullable(true)
                .build());
    }

    @Test
    public void apply_DoubleFieldView_ConvertsFieldViewToField() {
        FieldExport.DoubleFieldExport fieldView = defaultFieldWithId(new FieldExport.DoubleFieldExport());

        Field field = fieldExportConverter.apply(fieldView);

        assertThat(field).isEqualTo(DoubleField.builder()
                .name("my DoubleFieldExport")
                .nullable(true)
                .build());
    }

    @Test
    public void apply_FloatFieldView_ConvertsFieldViewToField() {
        FieldExport.FloatFieldExport fieldView = defaultFieldWithId(new FieldExport.FloatFieldExport());

        Field field = fieldExportConverter.apply(fieldView);

        assertThat(field).isEqualTo(FloatField.builder()
                .name("my FloatFieldExport")
                .nullable(true)
                .build());
    }

    @Test
    public void apply_DecimalFieldView_ConvertsFieldViewToField() {
        FieldExport.DecimalFieldExport fieldView = defaultFieldWithId(FieldExport.DecimalFieldExport.builder()
                .scale(5)
                .precision(38)
                .build());

        Field field = fieldExportConverter.apply(fieldView);

        assertThat(field).isEqualTo(DecimalField.builder()
                .name("my DecimalFieldExport")
                .nullable(true)
                .scale(5)
                .precision(38)
                .build());
    }

    @Test
    public void apply_StringFieldView_ConvertsFieldViewToField() {
        FieldExport.StringFieldExport fieldView = defaultFieldWithId(FieldExport.StringFieldExport.builder()
                .maxLength(10)
                .minLength(5)
                .regularExpression("[a-zA-Z]+")
                .build());

        Field field = fieldExportConverter.apply(fieldView);

        assertThat(field).isEqualTo(StringField.builder()
                .name("my StringFieldExport")
                .nullable(true)
                .maxLength(10)
                .minLength(5)
                .regularExpression("[a-zA-Z]+")
                .build());
    }

    @Test
    public void apply_DateFieldView_ConvertsFieldViewToField() {
        FieldExport.DateFieldExport fieldView = defaultFieldWithId(FieldExport.DateFieldExport.builder()
                .format("dd/MM/yyyy")
                .build());

        Field field = fieldExportConverter.apply(fieldView);

        assertThat(field).isEqualTo(DateField.builder()
                .name("my DateFieldExport")
                .nullable(true)
                .format("dd/MM/yyyy")
                .build());
    }

    @Test
    public void apply_TimestampFieldView_ConvertsFieldViewToField() {
        FieldExport.TimestampFieldExport fieldView = defaultFieldWithId(FieldExport.TimestampFieldExport.builder()
                .format("dd/MM/yyyy")
                .build());

        Field field = fieldExportConverter.apply(fieldView);

        assertThat(field).isEqualTo(TimestampField.builder()
                .name("my TimestampFieldExport")
                .nullable(true)
                .format("dd/MM/yyyy")
                .build());
    }

    @Test
    public void apply_BooleanFieldView_ConvertsFieldViewToField() {
        FieldExport.BooleanFieldExport fieldView = defaultFieldWithId(new FieldExport.BooleanFieldExport());

        Field field = fieldExportConverter.apply(fieldView);

        assertThat(field).isEqualTo(BooleanField.builder()
                .name("my BooleanFieldExport")
                .nullable(true)
                .build());
    }

    @Test
    public void apply_DummyFieldView_ThrowsIllegalArgumentException() {
        DummyFieldView fieldView = new DummyFieldView();

        assertThatThrownBy(() -> fieldExportConverter.apply(fieldView))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Field [DummyFieldView] is not of a recognised type");
    }

    private <T extends FieldExport> T defaultFieldWithId(final T fieldView) {
        fieldView.setId(12345L);
        fieldView.setName("my " + fieldView.getClass().getSimpleName());
        fieldView.setNullable(true);
        return fieldView;
    }

    private static final class DummyFieldView extends FieldExport {

    }
}
