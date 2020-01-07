package com.lombardrisk.ignis.spark.validation.transform;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FieldTest {

    @Test
    public void basic_field() {
        Field field1 = Field.of("age", "ageField");
        assertThat(field1.getAlias()).isEqualTo("ageField");

        Field field2 = Field.of("age");
        assertThat(field2.getAlias()).isEqualTo("age");

        Field field3 = Field.all();
        assertThat(field3.getName()).isEqualTo("*");
        assertThat(field3.isAll()).isTrue();
    }
}
