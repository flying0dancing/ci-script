package com.lombardrisk.ignis.design.field.model;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;

public class LongFieldTest {

    @Rule
    public JUnitSoftAssertions soft = new JUnitSoftAssertions();

    @Test
    public void copy_CopiesAllFields() {
        LongField existing = LongField.builder()
                .name("long")
                .nullable(true)
                .build();

        existing.setId(129L);

        LongField newField = existing.copy();

        soft.assertThat(newField)
                .isNotSameAs(existing);
        soft.assertThat(newField.getId())
                .isNull();
        soft.assertThat(newField.getName())
                .isEqualTo("long");
        soft.assertThat(newField.isNullable())
                .isTrue();
    }
}
