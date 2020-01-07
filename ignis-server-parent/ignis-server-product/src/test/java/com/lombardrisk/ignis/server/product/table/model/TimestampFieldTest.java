package com.lombardrisk.ignis.server.product.table.model;

import com.lombardrisk.ignis.server.product.table.model.TimestampField;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TimestampFieldTest {

    @Test
    public void testDoParse() {
        TimestampField dateField = new TimestampField();
        dateField.setFormat("hh:mm:ss");
        assertThat(dateField.doParse("12:20:22")).isNotNull();
    }

    @Test
    public void toColumnDef_ReturnsTimestampColumnDef() {
        TimestampField timestampField = new TimestampField();
        timestampField.setName("G1");

        assertThat(timestampField.toColumnDef())
                .isEqualTo("\"G1\" TIMESTAMP");
    }
}
