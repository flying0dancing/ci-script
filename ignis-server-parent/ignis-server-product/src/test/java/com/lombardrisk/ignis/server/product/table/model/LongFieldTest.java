package com.lombardrisk.ignis.server.product.table.model;

import com.lombardrisk.ignis.server.product.table.model.LongField;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LongFieldTest {

    @Test
    public void doParse() {
        assertThat(new LongField().doParse("10")).isEqualTo(new Long(10));
    }

    @Test
    public void checkRange() {
        LongField field = new LongField();
        field.setName("long");
        assertThat(field.checkRange("2").isValid()).isTrue();
    }

    @Test
    public void toColumnDef_ReturnsLongColumnDef() {
        LongField longField = new LongField();
        longField.setName("VERY_LONG_FIELD");

        assertThat(longField.toColumnDef())
                .isEqualTo("\"VERY_LONG_FIELD\" BIGINT");
    }
}
