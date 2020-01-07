package com.lombardrisk.ignis.server.product.table.model;

import com.lombardrisk.ignis.server.product.table.model.IntField;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class IntFieldTest {

    @Test
    public void doParse() {
        assertThat(new IntField().doParse("10")).isEqualTo(new Integer(10));

        assertThat(new IntField().parse("")).isNull();
        assertThat(new IntField().parse("10")).isEqualTo(new Integer(10));
    }

    @Test
    public void checkRange() {
        IntField intField = new IntField();
        intField.setName("int");
        assertThat(intField.validate("ss").isInvalid()).isTrue();
        assertThat(intField.validate(String.valueOf(Long.MAX_VALUE)).isInvalid()).isTrue();
        assertThat(intField.validate("22").isValid()).isTrue();
    }

    @Test
    public void toColumnDef_ReturnsIntColumnDef() {
        IntField intField = new IntField();
        intField.setName("INTEGER_F");

        assertThat(intField.toColumnDef())
                .isEqualTo("\"INTEGER_F\" INTEGER");
    }
}
