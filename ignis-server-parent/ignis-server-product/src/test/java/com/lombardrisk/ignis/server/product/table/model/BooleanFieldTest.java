package com.lombardrisk.ignis.server.product.table.model;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BooleanFieldTest {

    @Test
    public void testDoValidate() {
        BooleanField booleanField = new BooleanField();
        booleanField.setName("boolean");

        assertThat(booleanField.doValidate("true").isValid()).isTrue();
        assertThat(booleanField.doValidate("false").isValid()).isTrue();
        assertThat(booleanField.doValidate("xx").isInvalid()).isTrue();
    }

    @Test
    public void testDoParse() {
        assertThat(new BooleanField().doParse("true")).isTrue();
        assertThat(new BooleanField().doParse("false")).isFalse();
    }

    @Test
    public void toColumnDef_ReturnsBooleanColumnDef() {
        BooleanField booleanField = new BooleanField();
        booleanField.setName("IS_ABC");

        assertThat(booleanField.toColumnDef())
                .isEqualTo("\"IS_ABC\" BOOLEAN");
    }
}
