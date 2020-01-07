package com.lombardrisk.ignis.server.product.table.model;

import com.lombardrisk.ignis.server.product.table.model.FloatField;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FloatFieldTest {

    @Test
    public void doParse() {
        assertThat(new FloatField().doParse("10.1")).isEqualTo(new Float(10.1f));
    }

    @Test
    public void checkRange() {
        FloatField floatField = new FloatField();
        floatField.setName("float");
        assertThat(floatField.checkRange("ss").isInvalid()).isTrue();
        assertThat(floatField.checkRange("22.22").isValid()).isTrue();
    }

    @Test
    public void toColumnDef_ReturnsFloatColumnDef() {
        FloatField floatField = new FloatField();
        floatField.setName("FLO_AT");

        assertThat(floatField.toColumnDef())
                .isEqualTo("\"FLO_AT\" FLOAT");
    }
}
