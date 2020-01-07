package com.lombardrisk.ignis.design.field.model;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;

public class BooleanFieldTest {

    @Rule
    public JUnitSoftAssertions soft = new JUnitSoftAssertions();

    @Test
    public void copy_CopiesAllFields() {
        BooleanField existing = BooleanField.builder()
                .name("boolean")
                .nullable(true)
                .build();

        existing.setId(129L);

        BooleanField newField = existing.copy();

        soft.assertThat(newField)
                .isNotSameAs(existing);
        soft.assertThat(newField.getId())
                .isNull();
        soft.assertThat(newField.getName())
                .isEqualTo("boolean");
        soft.assertThat(newField.isNullable())
                .isTrue();
    }
}
