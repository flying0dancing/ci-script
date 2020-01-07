package com.lombardrisk.ignis.design.field.model;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;

public class DoubleFieldTest {

    @Rule
    public JUnitSoftAssertions soft = new JUnitSoftAssertions();

    @Test
    public void copy_CopiesAllFields() {
        DoubleField existing = DoubleField.builder()
                .name("double")
                .nullable(true)
                .build();

        existing.setId(129L);

        DoubleField newField = existing.copy();

        soft.assertThat(newField)
                .isNotSameAs(existing);

        soft.assertThat(newField.getId())
                .isNull();
        soft.assertThat(newField.getName())
                .isEqualTo("double");
        soft.assertThat(newField.isNullable())
                .isTrue();
    }
}
