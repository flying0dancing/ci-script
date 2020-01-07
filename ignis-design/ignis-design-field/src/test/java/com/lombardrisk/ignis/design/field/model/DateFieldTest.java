package com.lombardrisk.ignis.design.field.model;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;

public class DateFieldTest {

    @Rule
    public JUnitSoftAssertions soft = new JUnitSoftAssertions();

    @Test
    public void copy_CopiesAllFields() {
        DateField existing = DateField.builder()
                .name("date")
                .nullable(true)
                .format("dd/MM/yyyy")
                .build();

        existing.setId(129L);

        DateField newField = existing.copy();

        soft.assertThat(newField)
                .isNotSameAs(existing);

        soft.assertThat(newField.getId())
                .isNull();
        soft.assertThat(newField.getName())
                .isEqualTo("date");
        soft.assertThat(newField.isNullable())
                .isTrue();
        soft.assertThat(newField.getFormat())
                .isEqualTo("dd/MM/yyyy");
    }
}
