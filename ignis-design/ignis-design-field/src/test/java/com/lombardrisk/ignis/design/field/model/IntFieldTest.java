package com.lombardrisk.ignis.design.field.model;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;

public class IntFieldTest {

    @Rule
    public JUnitSoftAssertions soft = new JUnitSoftAssertions();

    @Test
    public void copy_CopiesAllFields() {
        IntField existing = IntField.builder()
                .name("int")
                .nullable(true)
                .build();

        existing.setId(129L);

        IntField newField = existing.copy();

        soft.assertThat(newField)
                .isNotSameAs(existing);
        soft.assertThat(newField.getId())
                .isNull();
        soft.assertThat(newField.getName())
                .isEqualTo("int");
        soft.assertThat(newField.isNullable())
                .isTrue();
    }
}
