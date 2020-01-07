package com.lombardrisk.ignis.design.field.model;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.assertThat;

public class DecimalFieldTest {

    @Rule
    public JUnitSoftAssertions soft = new JUnitSoftAssertions();

    @Test
    public void copy_CopiesAllFields() {
        DecimalField existing = DecimalField.builder()
                .name("decimal")
                .nullable(true)
                .scale(6)
                .precision(16)
                .build();

        existing.setId(129L);

        DecimalField newField = existing.copy();

        soft.assertThat(newField)
                .isNotSameAs(existing);

        soft.assertThat(newField.getId())
                .isNull();
        soft.assertThat(newField.getName())
                .isEqualTo("decimal");
        soft.assertThat(newField.isNullable())
                .isTrue();

        soft.assertThat(newField.getScale())
                .isEqualTo(6);
        soft.assertThat(newField.getPrecision())
                .isEqualTo(16);
    }

    @Test
    public void generateData_Precision1Scale1_ReturnsData() {
        DecimalField decimalField = DecimalField.builder()
                .scale(1)
                .precision(1)
                .build();

        BigDecimal randomDecimalValue = decimalField.generateData();
        assertThat(randomDecimalValue.setScale(1, RoundingMode.HALF_UP))
                .isEqualByComparingTo(randomDecimalValue);
    }

    @Test
    public void generateData_Precision1Scale10_ReturnsData() {
        DecimalField decimalField = DecimalField.builder()
                .scale(10)
                .precision(1)
                .build();

        BigDecimal randomDecimalValue = decimalField.generateData();
        assertThat(randomDecimalValue.setScale(1, RoundingMode.HALF_UP))
                .isEqualByComparingTo(randomDecimalValue);
    }

    @Test
    public void generateData_Precision2Scale10_ReturnsData() {
        DecimalField decimalField = DecimalField.builder()
                .scale(10)
                .precision(2)
                .build();

        BigDecimal randomDecimalValue = decimalField.generateData();

        assertThat(randomDecimalValue.setScale(2, RoundingMode.HALF_UP))
                .isEqualByComparingTo(randomDecimalValue);
    }

    @Test
    public void generateData_Precision9Scale10_ReturnsData() {
        DecimalField decimalField = DecimalField.builder()
                .scale(10)
                .precision(9)
                .build();

        BigDecimal randomDecimalValue = decimalField.generateData();

        assertThat(randomDecimalValue.setScale(9, RoundingMode.HALF_UP))
                .isEqualByComparingTo(randomDecimalValue);
    }

    @Test
    public void generateData_Precision10Scale10_ReturnsData() {
        DecimalField decimalField = DecimalField.builder()
                .scale(10)
                .precision(9)
                .build();

        BigDecimal randomDecimalValue = decimalField.generateData();

        assertThat(randomDecimalValue.setScale(10, RoundingMode.HALF_UP))
                .isEqualByComparingTo(randomDecimalValue);
    }
}
