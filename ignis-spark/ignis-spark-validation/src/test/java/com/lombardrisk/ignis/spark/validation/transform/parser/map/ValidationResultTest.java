package com.lombardrisk.ignis.spark.validation.transform.parser.map;

import com.lombardrisk.ignis.spark.validation.function.ValidationResult;
import org.apache.spark.sql.Encoder;
import org.apache.spark.sql.Encoders;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ValidationResultTest {

    @Test
    public void isBeanSerializable() {
        Encoder<ValidationResult> bean = Encoders.bean(ValidationResult.class);
        assertThat(bean).isNotNull();
    }
}
