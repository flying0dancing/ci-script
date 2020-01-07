package com.lombardrisk.ignis.server.product.table.model;

import com.lombardrisk.ignis.server.product.table.model.DateField;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class DateFieldTest {

    @Test
    public void testDoValidate() {
        assertThat(new DateField().doValidate("xx").isInvalid()).isTrue();

        DateField dateField = new DateField();
        dateField.setName("date");
        dateField.setFormat("yyyy-MM-dd");
        assertThat(dateField.doValidate("1982-03-02").isValid()).isTrue();
        assertThat(dateField.doValidate("xx-03-02").isInvalid()).isTrue();
    }

    @Test
    public void testDoParse() {
        DateField dateField = new DateField();
        dateField.setName("date");
        dateField.setFormat("yyyy-MM-dd");
        assertThat(dateField.doParse("1982-02-02")).isNotNull();
    }

    @Test
    public void testParseFailed() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> {
                    DateField dateField = new DateField();
                    dateField.setName("date");
                    dateField.setFormat("yyyy-MM-dd");
                    dateField.doParse("1982-02-02-09");
                })
                .withMessage("Failed to parse date: 1982-02-02-09 using format: yyyy-MM-dd");
    }

    @Test
    public void toColumnDef_ReturnsDateColumnDef() {
        DateField dateField = new DateField();
        dateField.setName("DT");

        assertThat(dateField.toColumnDef())
                .isEqualTo("\"DT\" DATE");
    }
}
