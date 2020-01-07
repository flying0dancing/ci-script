package com.lombardrisk.ignis.server.product.table.model;

import com.lombardrisk.ignis.server.product.table.model.StringField;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StringFieldTest {

    @Test
    public void doValidate() {
        StringField stringField = new StringField();
        stringField.setName("string");
        stringField.setMaxLength(9);
        stringField.setMinLength(5);
        stringField.setRegularExpression("abc");

        assertThat(stringField.doValidate("abc").isValid()).isFalse();
        assertThat(stringField.doValidate("1234567890").isValid()).isFalse();
        assertThat(stringField.doValidate("abcdef").isValid()).isFalse();
    }

    @Test
    public void doParse() {
        StringField stringField = new StringField();
        assertThat(stringField.doParse("sss")).isEqualTo("sss");
    }

    @Test
    public void toColumnDef_WithoutMaxLength_ReturnsStringColumnDef() {
        StringField stringField = new StringField();
        stringField.setName("TEXT_FIELD");

        assertThat(stringField.toColumnDef())
                .isEqualTo("\"TEXT_FIELD\" VARCHAR");
    }

    @Test
    public void toColumnDef_WithMaxLength_ReturnsStringColumnDefWithPrecision() {
        StringField stringField = new StringField();
        stringField.setName("TEXT_COL");
        stringField.setMaxLength(6789);

        assertThat(stringField.toColumnDef())
                .isEqualTo("\"TEXT_COL\" VARCHAR(6789)");
    }
}
