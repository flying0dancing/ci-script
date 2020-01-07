package com.lombardrisk.ignis.design.field.model;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;

public class StringFieldTest {

    @Rule
    public JUnitSoftAssertions soft = new JUnitSoftAssertions();

    @Test
    public void copy_CopiesAllFields() {
        StringField existing = StringField.builder()
                .name("string")
                .nullable(true)
                .maxLength(123)
                .minLength(99)
                .regularExpression("abc|cdf")
                .build();

        existing.setId(129L);

        StringField newField = existing.copy();

        soft.assertThat(newField)
                .isNotSameAs(existing);
        soft.assertThat(newField.getId())
                .isNull();
        soft.assertThat(newField.getName())
                .isEqualTo("string");
        soft.assertThat(newField.isNullable())
                .isTrue();
        soft.assertThat(newField.getMaxLength())
                .isEqualTo(123);
        soft.assertThat(newField.getMinLength())
                .isEqualTo(99);
        soft.assertThat(newField.getRegularExpression())
                .isEqualTo("abc|cdf");
    }
}
