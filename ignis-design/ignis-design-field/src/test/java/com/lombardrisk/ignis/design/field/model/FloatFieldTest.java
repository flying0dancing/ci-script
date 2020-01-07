package com.lombardrisk.ignis.design.field.model;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;

public class FloatFieldTest {

    @Rule
    public JUnitSoftAssertions soft = new JUnitSoftAssertions();

    @Test
    public void copy_CopiesAllFields() {
        FloatField existing = FloatField.builder()
                .name("float")
                .nullable(true)
                .build();

        existing.setId(129L);

        FloatField newField = existing.copy();

        soft.assertThat(newField)
                .isNotSameAs(existing);
        soft.assertThat(newField.getId())
                .isNull();
        soft.assertThat(newField.getName())
                .isEqualTo("float");
        soft.assertThat(newField.isNullable())
                .isTrue();
    }
}
