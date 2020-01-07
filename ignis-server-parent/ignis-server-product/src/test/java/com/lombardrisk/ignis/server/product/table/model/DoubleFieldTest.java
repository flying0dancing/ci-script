package com.lombardrisk.ignis.server.product.table.model;

import com.lombardrisk.ignis.server.product.table.model.DoubleField;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DoubleFieldTest {

    @Test
    public void doParse() {
        assertThat(new DoubleField().doParse("10")).isEqualTo(new Double(10));
    }


    @Test
    public void checkRange() {
        DoubleField doubleField = new DoubleField();
        doubleField.setName("double");
        assertThat(doubleField.checkRange("2d").isValid()).isTrue();
    }

    @Test
    public void toColumnDef_ReturnsDoubleColumnDef() {
        DoubleField doubleField = new DoubleField();
        doubleField.setName("DF");

        assertThat(doubleField.toColumnDef())
                .isEqualTo("\"DF\" DOUBLE");
    }

}
